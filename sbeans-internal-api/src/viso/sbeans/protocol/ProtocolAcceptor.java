package viso.sbeans.protocol;

import java.nio.channels.Channel;

public interface ProtocolAcceptor {
	/**
	 * �յ�һ������
	 * */
	public void receConnection(Channel channel);
	
	public void shutdown();
}
