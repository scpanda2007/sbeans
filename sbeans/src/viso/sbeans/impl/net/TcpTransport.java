package viso.sbeans.impl.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AcceptPendingException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Properties;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import viso.sbeans.impl.protocol.ProtocolAcceptorImpl;
import viso.sbeans.impl.util.LogWrapper;
import viso.sbeans.impl.util.NamedThreadFactory;
import viso.sbeans.impl.util.PropertyWrapper;

public class TcpTransport {
	
	private static final String kPckName = "viso.sbeans.net.transport.";
	
	private static LogWrapper logger = new LogWrapper(Logger.getLogger(kPckName+"logger"));
	
	public static final String kHostName = kPckName + "hostname";
	
	public static final String kPort = kPckName + "port";
	
	public static final String defaultHost = "127.0.0.1";
	
	public static final int defaultPort = 12345;
	
	InetSocketAddress listenAddress;
	
	AsynchronousServerSocketChannel serverChannel;
	
	AsynchronousChannelGroup group;
	
	public TcpTransport(Properties property) {
		try {
			PropertyWrapper properties = new PropertyWrapper(property);
			String hostname = properties.getProerty(kHostName, defaultHost);
			int port = properties.getIntProperty(kPort, defaultPort);
			listenAddress = new InetSocketAddress(hostname, port);
			group = AsynchronousChannelProvider.provider().openAsynchronousChannelGroup(Executors.newCachedThreadPool(new NamedThreadFactory(kPckName+"thread")), 1);
			try{
				serverChannel = AsynchronousChannelProvider.provider().openAsynchronousServerSocketChannel(group);
				serverChannel.bind(listenAddress, 1);
			}catch(Exception e){
				try{
					serverChannel.close();
				}catch(IOException ioe){
				}
				throw e;
			}
			running = true;
			logger.log(Level.INFO, "初始化网络传输层成功");
		} catch (Exception e) {
			logger.logThrow(Level.INFO, e, "初始化网络传输层失败");
		}
	}
	
	ConnectionHandler connectionHandler;
	
	public void accept(ProtocolAcceptorImpl acceptor){
		if(group.isShutdown()){
			throw new IllegalStateException("通道组已经关闭");
		}
		if(connectionHandler!=null){
			throw new AcceptPendingException();
		}
		connectionHandler = new ConnectionHandler(acceptor);
		serverChannel.accept(null, connectionHandler);
	}
	
	private boolean running = false;
	
	public boolean isShutdown(){
		return running;
	}
	
	public void shutdown(){
		if(serverChannel!=null && serverChannel.isOpen()){
			try{
				serverChannel.close();
			}catch(IOException ioe){}
		}
		
		if (group != null && !group.isShutdown()) {
			boolean hasShutdown = false;
			try {
				hasShutdown = group.awaitTermination(1L, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(!hasShutdown){
				try {
					group.shutdownNow();
				} catch (IOException e) {}
			}
		}
		
		running = false;
	}
	
	private class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel,Void>{
		
		private final ProtocolAcceptorImpl acceptor;
		
		public ConnectionHandler(ProtocolAcceptorImpl acceptor){
			this.acceptor = acceptor;
		}

		@Override
		public void completed(AsynchronousSocketChannel channel, Void arg1) {
			// TODO Auto-generated method stub
			try{
				if(acceptor!=null){
					acceptor.receConnection(new AsynchronousMessageChannel(channel,10*1024));
				}
			}catch(Throwable t){
				logger.logThrow(Level.FINEST, t, "接收器接收连接失败");
			}
			serverChannel.accept(null, this);
		}

		@Override
		public void failed(Throwable arg0, Void arg1) {
			// TODO Auto-generated method stub
			if(arg0 instanceof CancellationException){
				return;
			}
			logger.logThrow(Level.FINEST, arg0, "服务器端通道监听异常,尝试重启");
			try{
				restart();
				serverChannel.accept(null, this);
			}catch(IOException ioe){
				logger.logThrow(Level.FINEST, ioe, "重新启动服务端通道失败");
			}
		}

	}
	
	public void restart() throws IOException {
		if (group.isShutdown()) {
			throw new IllegalStateException("通道组已经关闭");
		}
		try {
			serverChannel.close();
		} catch (IOException ioe) {
		}
		serverChannel = AsynchronousChannelProvider.provider()
				.openAsynchronousServerSocketChannel(group);
		serverChannel.bind(listenAddress, 1);
		running = true;
	}
	
}
