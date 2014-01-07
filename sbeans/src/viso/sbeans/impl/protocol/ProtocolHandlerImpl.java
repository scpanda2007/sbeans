package viso.sbeans.impl.protocol;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadPendingException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import viso.sbeans.impl.net.AsynchronousMessageChannel;
import viso.sbeans.impl.util.LogWrapper;
import viso.sbeans.impl.util.MessageBuffer;
import viso.sbeans.session.ProtocolHandler;
import viso.sbeans.session.ProtocolRequestComplete;
import viso.sbeans.session.SessionAcceptor;
import viso.sbeans.session.SessionHandler;
import viso.sbeans.task.Task;

import static viso.sbeans.session.SessionHeader.kLoginRequest;
import static viso.sbeans.session.SessionHeader.kSessionMessage;
import static viso.sbeans.session.SessionHeader.kLogoutRequest;
import static viso.sbeans.session.SessionHeader.kLoginSuccess;
import static viso.sbeans.session.SessionHeader.kLoginFailed;

public class ProtocolHandlerImpl implements ProtocolHandler{

	ProtocolAcceptorImpl acceptor;
	SessionAcceptor sessionAcceptor;
	SessionHandler sessionHandler;
	AsynchronousMessageChannel channel;
	ReadHandler readHandler;
	WriteHandler writeHandler;
	LogWrapper logger;
	
	public ProtocolHandlerImpl(ProtocolAcceptorImpl acceptor, SessionAcceptor sessionAcceptor, AsynchronousMessageChannel channel, LogWrapper logger){
		this.acceptor = acceptor;
		this.sessionAcceptor = sessionAcceptor;
		this.channel = channel;
		this.readHandler = new ConnectedReadHandler();
		this.writeHandler = new ConnectedWriteHandler();
		this.logger = logger;
		scheduleRead();
	}
	
	public void scheduleRead(){
		this.acceptor.scheduleTask(new Task(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				readNow();
			}
		});
	}
	
	public void readNow(){
		try {
			readHandler.read();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.logThrow(Level.FINEST, e, "读取消息失败");
		}
	}
	
	private void handleReceMessage(MessageBuffer message){
		switch(message.getByte()){
		case kLoginRequest:
		{
			logger.log(Level.INFO, "登陆请求");
			if(this.sessionAcceptor!=null){
				//TODO: 验证唯一性
				this.sessionAcceptor.newLogin(this, new LoginRequestComplete());
			}
			readNow();
		}break;
		case kSessionMessage:
		{
			logger.log(Level.INFO, "会话消息");
			if(this.sessionHandler!=null){
				this.sessionHandler.sessionMessage(message, new SessionMessageComplete());
			}
		}break;
		case kLogoutRequest:
		{
			if(this.sessionHandler!=null){
				this.sessionHandler.disconnect(this, new LogoutComplete());
			}
			logger.log(Level.INFO, "下线请求");
		}break;
		default:
			throw new IllegalStateException("未知的消息编码");
		}
	}
	
	private abstract class ReadHandler implements CompletionHandler<MessageBuffer,Void>{
		public abstract void read() throws Exception;
	}
	
	private class ClosedReadHandler extends ReadHandler {

		@Override
		public void read() throws ClosedChannelException {
			// TODO Auto-generated method stub
			throw new ClosedChannelException();
		}

		@Override
		public void completed(MessageBuffer arg0, Void arg1) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void failed(Throwable arg0, Void arg1) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	private class ConnectedReadHandler extends ReadHandler {

		private boolean isReading = false;
		private final Object lock = new Object();
		
		@Override
		public void completed(MessageBuffer arg0, Void arg1) {
			// TODO Auto-generated method stub
			synchronized(lock){
				isReading = false;
			}
			logger.log(Level.INFO, "收到一则消息");
			handleReceMessage(arg0);
		}

		@Override
		public void failed(Throwable arg0, Void arg1) {
			// TODO Auto-generated method stub
			synchronized(lock){
				isReading = false;
			}
		}

		@Override
		public void read() {
			// TODO Auto-generated method stub
			synchronized(lock){
				if(isReading){
					throw new ReadPendingException();
				}
				isReading = true;
			}
			ProtocolHandlerImpl.this.channel.read(this);
		}
		
	}

	private class LoginRequestComplete implements ProtocolRequestComplete<SessionHandler>{

		@Override
		public void complete(SessionHandler sessionHandler) {
			// TODO Auto-generated method stub
			ProtocolHandlerImpl.this.sessionHandler = sessionHandler;
			//TODO: write response.
			sendSessionMessage(new MessageBuffer(8).putByte(kLoginSuccess));
		}

		@Override
		public void failed(Throwable t, SessionHandler arg) {
			// TODO Auto-generated method stub
			logger.logThrow(Level.FINEST, t, "登陆请求处理失败");
			sendSessionMessage(new MessageBuffer(8).putByte(kLoginFailed));
		}
		
	}
	
	private class SessionMessageComplete implements ProtocolRequestComplete<Void>{

		@Override
		public void complete(Void arg) {
			// TODO Auto-generated method stub
			scheduleRead();
		}

		@Override
		public void failed(Throwable t, Void arg) {
			// TODO Auto-generated method stub
			logger.logThrow(Level.FINEST, t, "会话消息处理失败");
		}
		
	}
	
	boolean closed = false;
	
	public synchronized void shutdown(){
		if(closed){
			return;
		}
		if(channel.isOpen()){
			try{
				channel.close();
			}catch(IOException e){
			}
		}
		ProtocolHandlerImpl.this.writeHandler = new ClosedWriteHandler();
		ProtocolHandlerImpl.this.readHandler = new ClosedReadHandler();
		closed = true;
	}
	
	private class LogoutComplete implements ProtocolRequestComplete<Void>{

		@Override
		public void complete(Void arg) {
			// TODO Auto-generated method stub
			shutdown();
		}

		@Override
		public void failed(Throwable t, Void arg) {
			// TODO Auto-generated method stub
			logger.logThrow(Level.FINEST, t, "登出处理失败");
			shutdown();
		}
	}

	@Override
	public void sendSessionMessage(MessageBuffer message) {
		// TODO Auto-generated method stub
		try{
			this.writeHandler.write(message);
		}catch(Exception e){
			logger.logThrow(Level.FINEST, e, "发送消息失败");
		}
	}
	
	abstract class WriteHandler implements CompletionHandler<Void,Void>{
		abstract public void write(MessageBuffer message) throws Exception;
	}
	
	private class ConnectedWriteHandler extends WriteHandler{

		boolean isWriting = false;
		Object lock = new Object();
		List<MessageBuffer> messages = new ArrayList<MessageBuffer>();
		
		@Override
		public void write(MessageBuffer message) {
			// TODO Auto-generated method stub
			synchronized(lock){
				if(isWriting){
					messages.add(message);
					return;
				}
				isWriting = true;
			}
			ProtocolHandlerImpl.this.channel.write(message.buffer(), this);
		}

		@Override
		public void completed(Void arg0, Void arg1) {
			// TODO Auto-generated method stub
			MessageBuffer message = null;
			synchronized(lock){
				if(messages.isEmpty()){
					isWriting = false;
					return;
				}
				message = messages.remove(0);
			}
			ProtocolHandlerImpl.this.channel.write(message.buffer(), this);
		}

		@Override
		public void failed(Throwable arg0, Void arg1) {
			// TODO Auto-generated method stub
			logger.logThrow(Level.FINEST, arg0, "发送消息失败");
		}
		
	}
	
	private class ClosedWriteHandler extends WriteHandler{

		@Override
		public void write(MessageBuffer message) throws ClosedChannelException {
			// TODO Auto-generated method stub
			throw new ClosedChannelException();
		}

		@Override
		public void completed(Void arg0, Void arg1) {
			// TODO Auto-generated method stub
		}

		@Override
		public void failed(Throwable arg0, Void arg1) {
			// TODO Auto-generated method stub
		}
		
	}
}
