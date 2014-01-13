package viso.sbeans.impl.store.data;

import static viso.sbeans.impl.util.Objects.checkNull;

public class FlushInfo {
	public byte[] modified;
	public int type;//0�޸� 1ɾ��
	public String key;
	public FlushInfo(String key, int type, byte[] modified){
		checkNull("flush key",key);
		this.key = key;
		this.type = type;
		this.modified = modified;
	}
}
