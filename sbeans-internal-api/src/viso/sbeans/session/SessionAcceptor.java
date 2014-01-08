package viso.sbeans.session;

public interface SessionAcceptor {
	public void newLogin(ProtocolHandler handler, ProtocolRequestComplete<SessionHandler> loginComplete);
}
