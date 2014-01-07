package viso.sbeans.session;

import viso.sbeans.impl.util.MessageBuffer;

public interface ProtocolHandler {
	public void sendSessionMessage(MessageBuffer message);
}
