package viso.sbeans.protocol;

import java.nio.channels.AsynchronousSocketChannel;

public interface ProtocolAcceptor {
	/**
	 * �յ�һ������
	 * */
	public void receConnection(AsynchronousSocketChannel channel);
}
