package viso.sbeans.protocol;

import java.nio.channels.AsynchronousSocketChannel;

public interface ProtocolAcceptor {
	/**
	 * 收到一则连接
	 * */
	public void receConnection(AsynchronousSocketChannel channel);
}
