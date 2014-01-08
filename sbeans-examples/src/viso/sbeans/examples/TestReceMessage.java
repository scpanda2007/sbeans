package viso.sbeans.examples;

import java.util.Properties;

import viso.sbeans.app.AppListener;
import viso.sbeans.impl.util.MessageBuffer;
import viso.sbeans.service.net.ClientSession;
import viso.sbeans.service.net.SessionListener;

public class TestReceMessage implements AppListener{

	@Override
	public void initialize(Properties property) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public SessionListener newClientSession(long id, ClientSession session) {
		// TODO Auto-generated method stub
		return null;
	}

	class SessionListenrImpl implements SessionListener{

		@Override
		public void disconnect(long id) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void handleSessionMessage(long id, MessageBuffer message){
			
		}
		
	}
}
