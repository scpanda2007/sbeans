package viso.sbeans.impl.net.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AcceptPendingException;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.spi.AsynchronousChannelProvider;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import viso.sbeans.impl.net.AsynchronousMessageChannel;
import viso.sbeans.impl.util.LogWrapper;
import viso.sbeans.impl.util.MessageBuffer;
import viso.sbeans.impl.util.NamedThreadFactory;

public class DummyServer {

	AsynchronousServerSocketChannel channel;
	AsynchronousChannelGroup group;

	private static LogWrapper logger = new LogWrapper(Logger
			.getLogger("dummyServer"));

	public DummyServer(InetSocketAddress addr) {
		try {
			group = AsynchronousChannelProvider
					.provider()
					.openAsynchronousChannelGroup(
							Executors
									.newCachedThreadPool(new NamedThreadFactory(
											"dummyServer")), 1);
			channel = AsynchronousServerSocketChannel.open(group);
			channel.bind(addr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

	ConnectionHandler connHandler;

	public void shutdown(){
		if(channel!=null && channel.isOpen()){
			try {
				channel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(group!=null && !group.isShutdown()){
			boolean hasShutdown = false;
			try {
				hasShutdown = group.awaitTermination(1L, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				logger.logThrow(Level.INFO, e, "等待通道组结束出现异常");
			}
			if(!hasShutdown){
				try {
					group.shutdownNow();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.logThrow(Level.INFO, e, "关闭通道组出现异常");
				}
			}
		}
	}
	
	public void accept() {

		if (group.isShutdown()) {
			throw new IllegalStateException("通道已关闭");
		}

		if (connHandler != null) {
			throw new AcceptPendingException();
		}
		
		connHandler = new ConnectionHandler();
		channel.accept(null, connHandler);
	}

	class ConnectionHandler implements
			CompletionHandler<AsynchronousSocketChannel, Void> {

		@Override
		public void completed(AsynchronousSocketChannel arg0, Void arg1) {
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "收到一条连接");
			AsynchronousMessageChannel messageChannel = new AsynchronousMessageChannel(arg0,4096);
			messageChannel.read(new ReadHandler(messageChannel));
			channel.accept(null, this);
		}

		@Override
		public void failed(Throwable arg0, Void arg1) {
			// TODO Auto-generated method stub

		}
	}
	
	class ReadHandler implements CompletionHandler<MessageBuffer,Void>{
		
		AsynchronousMessageChannel messageChannel;
		
		public ReadHandler(AsynchronousMessageChannel messageChannel){
			this.messageChannel = messageChannel;
		}
		
		@Override
		public void completed(MessageBuffer arg0, Void arg1) {
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "收到一条消息");
			this.messageChannel.read(this);
		}

		@Override
		public void failed(Throwable arg0, Void arg1) {
			// TODO Auto-generated method stub
			logger.logThrow(Level.INFO, arg0, "读取消息失败");
		}
		
	}
}
