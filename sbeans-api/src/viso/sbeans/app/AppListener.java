package viso.sbeans.app;

import viso.sbeans.service.net.ClientSession;
import viso.sbeans.service.net.SessionListener;

public interface AppListener {
	public SessionListener newClientSession(long id, ClientSession session);
}
