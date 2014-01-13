package viso.sbeans.impl.store.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

public interface ClassSerializer {
	/**
	 * ��ois�еõ� ObjectStreamClass
	 * @throws IOException
	 * @throws IOException 
	 */
	public ObjectStreamClass readDescriptor(ObjectInputStream ois) throws IOException;
	/**
	 * ����osc�Ƿ�������л��õ�һ��ʵ��
	 * @throws DataObjectIOException 
	 * */
	public void checkInitializable(ObjectStreamClass osc) throws DataObjectIOException;
	/**
	 * ��һ��ObjectStreamClass д�뵽oos��
	 * @throws IOException
	 * */
	public void writeDescriptor(ObjectStreamClass osc,ObjectOutputStream oos) throws IOException;
}
