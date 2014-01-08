package viso.sbeans.impl.service.server;

import viso.sbeans.impl.util.MessageBuffer;
import viso.sbeans.service.net.ClientSession;
import viso.sbeans.session.ProtocolHandler;
import viso.sbeans.task.Task;
import viso.sbeans.task.TaskQueue;

public class ClientSessionImpl implements ClientSession{
	
	ProtocolHandler handler;
	TaskQueue actionQueue;
	long id;
	
	public ClientSessionImpl(long id,ProtocolHandler handler,TaskQueue actionQueue){
		this.id = id;
		this.handler = handler;
		this.actionQueue = actionQueue;
	}
	
	@Override
	public void sendSessionMessage(long id, final MessageBuffer message) {
		// TODO Auto-generated method stub
		actionQueue.addTask(new Task(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				handler.sendSessionMessage(message);
			}
		});
	}

}
