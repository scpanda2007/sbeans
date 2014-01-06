package viso.sbeans.session;

public interface ProtocolAcceptor {
	public void shutdown();
	public boolean isShutdown();
	public void accept();
}
