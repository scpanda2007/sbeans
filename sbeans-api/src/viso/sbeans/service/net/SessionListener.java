package viso.sbeans.service.net;

import viso.sbeans.impl.util.MessageBuffer;

public interface SessionListener {
	public void handleSessionMessage(long id, MessageBuffer message);//������Ϣ
	public void disconnect(long id);//�Ͽ�����
}
