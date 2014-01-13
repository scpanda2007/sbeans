package viso.sbeans.impl.service.server;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

import viso.sbeans.app.AppListener;
import viso.sbeans.impl.kernel.TaskSchedulerImpl;
import viso.sbeans.impl.protocol.ProtocolAcceptorImpl;
import viso.sbeans.impl.util.LogWrapper;
import viso.sbeans.impl.util.MessageBuffer;
import viso.sbeans.kernel.CompomentRegister;
import viso.sbeans.service.net.ClientSession;
import viso.sbeans.service.net.SessionListener;
import viso.sbeans.session.ProtocolAcceptor;
import viso.sbeans.session.ProtocolHandler;
import viso.sbeans.session.ProtocolRequestComplete;
import viso.sbeans.session.SessionAcceptor;
import viso.sbeans.session.SessionHandler;
import viso.sbeans.task.Task;
import viso.sbeans.task.TaskQueue;
import viso.sbeans.task.TaskScheduler;

public class SessionService {
	
	private static final String PCK_NAME = "viso.sbeans.service.server.SessionService.";
	
	AppListener app;
	ProtocolAcceptor protocolAcceptor;
	TaskScheduler transactionScheduler;
	TaskScheduler taskScheduler;
	LogWrapper logger = new LogWrapper(Logger.getLogger(PCK_NAME));
	
	private AtomicLong counter = new AtomicLong(0);
	
	public ConcurrentHashMap<Long,ClientSession> clientsessions = new ConcurrentHashMap<Long,ClientSession>(); 
	
	public SessionService(Properties property,CompomentRegister register){
		app = register.getCompoment(AppListener.class);
		protocolAcceptor = new ProtocolAcceptorImpl(property,new SessionAcceptorImpl());
		transactionScheduler = new TaskSchedulerImpl(4,PCK_NAME+"txn.",logger);
		taskScheduler = new TaskSchedulerImpl(4,PCK_NAME+"tsk.",logger); 
	}
	
	public void start(){
		protocolAcceptor.accept();
	}
	
	public void shutdown(){
		protocolAcceptor.shutdown();
		transactionScheduler.shutdown();
		taskScheduler.shutdown();
	}
	
	class LoginTask implements Task{

		private ProtocolHandler handler;
		private ProtocolRequestComplete<SessionHandler> loginComplete;
		
		public LoginTask(final ProtocolHandler handler, 
				final ProtocolRequestComplete<SessionHandler> loginComplete){
			this.handler = handler;
			this.loginComplete = loginComplete;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			SessionHandlerImpl sessionHandler = null;
			long id;
			try{
				id = counter.getAndIncrement();
				ClientSessionImpl clientsession = 
					new ClientSessionImpl(id,handler,taskScheduler.createTaskQueue());
				sessionHandler = 
					new SessionHandlerImpl(id, 
							app.newClientSession(id,clientsession), 
							transactionScheduler.createTaskQueue());
				clientsessions.putIfAbsent(id, clientsession);
			}catch(Throwable t){
				try{
					loginComplete.failed(t,null);
				}catch(Exception e){
				}
				return;
			}
			try{
				loginComplete.complete(sessionHandler);
			}catch(Exception e){
			}
		}
		
	}
	
	class SessionAcceptorImpl implements SessionAcceptor{

		@Override
		public void newLogin(ProtocolHandler handler,
				ProtocolRequestComplete<SessionHandler> loginComplete) {
			// TODO Auto-generated method stub
			taskScheduler.scheduleTask(new LoginTask(handler,loginComplete));
		}
	}
	
	class SessionHandlerImpl implements SessionHandler{

		SessionListener listener;
		long id;
		TaskQueue queue;
		
		public SessionHandlerImpl(long id, SessionListener listener, TaskQueue queue){
			this.id = id;
			this.listener = listener;
			this.queue = queue;
		}
		
		@Override
		public void disconnect(ProtocolHandler handler,
				final ProtocolRequestComplete<Void> complete) {
			// TODO Auto-generated method stub
			this.queue.addTask(new Task(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try{
						listener.disconnect(id);
					}catch(Throwable t){
						try{
							complete.failed(t, null);
						}catch(Exception e){
						}
					}
					try{
						complete.complete(null);
					}catch(Exception e){
					}
				}
			});
		}

		@Override
		public void sessionMessage(final MessageBuffer message,
				final ProtocolRequestComplete<Void> complete) {
			// TODO Auto-generated method stub
			this.queue.addTask(new Task(){
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try{
						listener.handleSessionMessage(id, message);
					}catch(Throwable t){
						try{
							complete.failed(t, null);
						}catch(Exception e){
						}
					}
					try{
						complete.complete(null);
					}catch(Exception e){
					}
				}
			});
		}
		
	}
}

