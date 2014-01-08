package viso.sbeans.app;

import java.util.Properties;

import viso.sbeans.service.net.ClientSession;
import viso.sbeans.service.net.SessionListener;

public interface AppListener {
	public void initialize(Properties property);
	public SessionListener newClientSession(long id, ClientSession session);
}
