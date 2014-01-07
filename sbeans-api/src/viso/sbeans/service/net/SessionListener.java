package viso.sbeans.service.net;

import viso.sbeans.impl.util.MessageBuffer;

public interface SessionListener {
	public void handleSessionMessage(long id, MessageBuffer message);//处理消息
	public void disconnect(long id);//断开连接
}
