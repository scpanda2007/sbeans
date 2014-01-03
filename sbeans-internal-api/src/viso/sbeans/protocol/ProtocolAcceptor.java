package viso.sbeans.protocol;

import java.nio.channels.Channel;

public interface ProtocolAcceptor {
	/**
	 * 收到一则连接
	 * */
	public void receConnection(Channel channel);
	
	public void shutdown();
}
