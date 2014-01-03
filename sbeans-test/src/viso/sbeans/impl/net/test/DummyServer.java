package viso.sbeans.impl.net.test;

import java.net.InetSocketAddress;
import java.nio.channels.Channel;
import java.nio.channels.CompletionHandler;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import viso.sbeans.impl.net.AsynchronousMessageChannel;
import viso.sbeans.impl.net.TcpTransport;
import viso.sbeans.impl.util.LogWrapper;
import viso.sbeans.impl.util.MessageBuffer;
import viso.sbeans.protocol.ProtocolAcceptor;

public class DummyServer {

	TcpTransport transport;

	private static LogWrapper logger = new LogWrapper(Logger
			.getLogger("dummyServer"));

	public DummyServer(InetSocketAddress addr) {
		transport = new TcpTransport(new Properties());
	}

	public void shutdown(){
		transport.shutdown();
	}
	
	public void accept() {

		if (transport.isShutdown()) {
			throw new IllegalStateException("传输层已关闭");
		}
		
		transport.accept(new ProtocolAcceptorImpl());
	}

	class ProtocolAcceptorImpl implements ProtocolAcceptor {

		@Override
		public void receConnection(Channel channel) {
			// TODO Auto-generated method stub
			AsynchronousMessageChannel messageChannel = (AsynchronousMessageChannel)channel;
			messageChannel.read(new ReadHandler(messageChannel));
			logger.log(Level.FINEST, "收到一条连接");
		}

		@Override
		public void shutdown() {
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
