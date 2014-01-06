package viso.sbeans.session;

public interface ProtocolRequestComplete<Arg> {
	public void complete(Arg arg);
	public void failed(Throwable t, Arg arg);
}
