package viso.sbeans.impl.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import viso.sbeans.impl.util.LogWrapper;
import viso.sbeans.impl.util.NamedThreadFactory;
import viso.sbeans.impl.util.PropertyWrapper;
import viso.sbeans.protocol.ProtocolAcceptor;

public class TcpTransport {
	
	private static final String kPckName = "viso.sbeans.net.transport.";
	
	private static LogWrapper logger = new LogWrapper(Logger.getLogger(kPckName+"logger"));
	
	public static final String kHostName = kPckName + "hostname";
	
	public static final String kPort = kPckName + "port";
	
	public static final String defaultHost = "127.0.0.1";
	
	public static final int defaultPort = 12345;
	
	InetSocketAddress listenAddress;
	
	AsynchronousServerSocketChannel channel;
	
	AsynchronousChannelGroup group;
	
	public TcpTransport(Properties property) {
		try {
			PropertyWrapper properties = new PropertyWrapper(property);
			String hostname = properties.getProerty(kHostName, defaultHost);
			int port = properties.getIntProperty(kPort, defaultPort);
			listenAddress = new InetSocketAddress(hostname, port);
			group = AsynchronousChannelProvider.provider().openAsynchronousChannelGroup(Executors.newCachedThreadPool(new NamedThreadFactory(kPckName+"thread")), 1);
			try{
				channel = AsynchronousChannelProvider.provider().openAsynchronousServerSocketChannel(group);
				channel.bind(listenAddress, 0);
			}catch(Exception e){
				try{
					channel.close();
				}catch(IOException ioe){
				}
				throw e;
			}
		} catch (Exception e) {
			logger.logThrow(Level.FINEST, e, "³õÊ¼»¯ÍøÂç´«Êä²ãÊ§°Ü");
		}
	}
	
	public void accept(ProtocolAcceptor acceptor){
		channel.accept(null, new ConnectionHandler(acceptor));
	}
	
	private class ConnectionHandler implements CompletionHandler<AsynchronousSocketChannel,Void>{
		
		private final ProtocolAcceptor acceptor;
		
		public ConnectionHandler(ProtocolAcceptor acceptor){
			this.acceptor = acceptor;
		}

		@Override
		public void completed(AsynchronousSocketChannel channel, Void arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void failed(Throwable arg0, Void arg1) {
			// TODO Auto-generated method stub
			
		}

	}
}
