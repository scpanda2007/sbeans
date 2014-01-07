package viso.sbeans.service.net;

import viso.sbeans.impl.util.MessageBuffer;

public interface ClientSession {
	public void sendSessionMessage(long id, MessageBuffer message);
}
