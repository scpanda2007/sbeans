package viso.sbeans.protocol;

import viso.sbeans.impl.util.MessageBuffer;

public interface ProtocolHandler {
	public void receMessage(MessageBuffer message);
}
