package viso.sbeans.impl.net.test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import viso.sbeans.impl.protocol.ProtocolAcceptorImpl;
import viso.sbeans.impl.util.LogWrapper;
import viso.sbeans.impl.util.MessageBuffer;
import viso.sbeans.session.ProtocolAcceptor;
import viso.sbeans.session.ProtocolHandler;
import viso.sbeans.session.ProtocolRequestComplete;
import viso.sbeans.session.SessionAcceptor;
import viso.sbeans.session.SessionHandler;

public class DummyServer {

	SessionAcceptor sessionAcceptor;
	ProtocolAcceptor acceptor;
	List<SessionListener> liseners = new ArrayList<SessionListener>();

	private static LogWrapper logger = new LogWrapper(Logger
			.getLogger("dummyServer"));

	public DummyServer(InetSocketAddress addr) {
		sessionAcceptor = new AppListener();
		acceptor = new ProtocolAcceptorImpl(new Properties(), sessionAcceptor);
	}

	public void shutdown() {
		acceptor.shutdown();
	}

	public void accept() {
		if (acceptor.isShutdown()) {
			throw new IllegalStateException("������ѹر�");
		}
		acceptor.accept();
	}

	class AppListener implements SessionAcceptor {

		@Override
		public void newLogin(ProtocolHandler handler,
				ProtocolRequestComplete<SessionHandler> loginComplete) {
			// TODO Auto-generated method stub
			SessionListener lisener = new SessionListener(handler);
			liseners.add(lisener);
			logger.log(Level.INFO, "���µ��û���¼������");
			loginComplete.complete(lisener);
		}

	}

	class SessionListener implements SessionHandler {

		ProtocolHandler handler;

		public SessionListener(ProtocolHandler handler) {
			this.handler = handler;
		}

		@Override
		public void disconnect(ProtocolHandler handler,
				ProtocolRequestComplete<Void> complete) {
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "���ӶϿ�");
			complete.complete(null);
		}

		@Override
		public void sessionMessage(MessageBuffer message,
				ProtocolRequestComplete<Void> complete) {
			// TODO Auto-generated method stub
			logger.log(Level.INFO, "�յ�һ����Ϣ");
			switch (message.getInt()) {
			case 1:
				System.out.println("" + message.getString());
			}
			complete.complete(null);
		}

	}

}
