package viso.sbeans.impl.store.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

public interface ClassSerializer {
	/**
	 * 从ois中得到 ObjectStreamClass
	 * @throws IOException
	 * @throws IOException 
	 */
	public ObjectStreamClass readDescriptor(ObjectInputStream ois) throws IOException;
	/**
	 * 检查该osc是否可以序列化得到一个实体
	 * @throws DataObjectIOException 
	 * */
	public void checkInitializable(ObjectStreamClass osc) throws DataObjectIOException;
	/**
	 * 将一个ObjectStreamClass 写入到oos中
	 * @throws IOException
	 * */
	public void writeDescriptor(ObjectStreamClass osc,ObjectOutputStream oos) throws IOException;
}
