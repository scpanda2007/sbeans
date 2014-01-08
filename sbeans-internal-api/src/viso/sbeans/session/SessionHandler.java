package viso.sbeans.session;

import viso.sbeans.impl.util.MessageBuffer;

public interface SessionHandler {
	public void sessionMessage(MessageBuffer message, ProtocolRequestComplete<Void> complete);
	public void disconnect(ProtocolHandler handler, ProtocolRequestComplete<Void> complete);
}
