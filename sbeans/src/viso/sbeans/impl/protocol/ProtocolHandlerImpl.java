package viso.sbeans.impl.protocol;

import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadPendingException;
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

public class ProtocolHandlerImpl implements ProtocolHandler{

	ProtocolAcceptorImpl acceptor;
	SessionAcceptor sessionAcceptor;
	SessionHandler sessionHandler;
	AsynchronousMessageChannel channel;
	ReadHandler readHandler;
	LogWrapper logger;
	
	public ProtocolHandlerImpl(ProtocolAcceptorImpl acceptor, SessionAcceptor sessionAcceptor, AsynchronousMessageChannel channel, LogWrapper logger){
		this.acceptor = acceptor;
		this.sessionAcceptor = sessionAcceptor;
		this.channel = channel;
		this.readHandler = new ConnectedReadHandler();
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
			logger.logThrow(Level.FINEST, e, "��ȡ��Ϣʧ��");
		}
	}
	
	private void handleReceMessage(MessageBuffer message){
		switch(message.getByte()){
		case kLoginRequest:
		{
			logger.log(Level.INFO, "��½����");
			if(this.sessionAcceptor!=null){
				//TODO: ��֤Ψһ��
				this.sessionAcceptor.newLogin(this, new LoginRequestComplete());
			}
			readNow();
		}break;
		case kSessionMessage:
		{
			logger.log(Level.INFO, "�Ự��Ϣ");
			if(this.sessionHandler!=null){
				this.sessionHandler.sessionMessage(message, new SessionMessageComplete());
			}
		}break;
		case kLogoutRequest:
		{
			if(this.sessionHandler!=null){
				this.sessionHandler.disconnect(this, new LogoutComplete());
			}
			logger.log(Level.INFO, "��������");
		}break;
		default:
			throw new IllegalStateException("δ֪����Ϣ����");
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
			logger.log(Level.INFO, "�յ�һ����Ϣ");
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
		}

		@Override
		public void failed(Throwable t, SessionHandler arg) {
			// TODO Auto-generated method stub
			logger.logThrow(Level.FINEST, t, "��½������ʧ��");
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
			logger.logThrow(Level.FINEST, t, "�Ự��Ϣ����ʧ��");
		}
		
	}
	
	boolean closed = false;
	
	public synchronized void shutdown(){
		if(closed){
			return;
		}
		ProtocolHandlerImpl.this.readHandler = new ClosedReadHandler();
		ProtocolHandlerImpl.this.sessionHandler = null;
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
			logger.logThrow(Level.FINEST, t, "�ǳ�����ʧ��");
			shutdown();
		}
	}
}
