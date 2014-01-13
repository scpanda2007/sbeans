package viso.sbeans.impl.store.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class SerialUtil {
	
	public SerialUtil(){}
	
	@SuppressWarnings("unchecked")
	public static <T extends DataObject> T read(byte[] data,ClassSerializer csl){
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		DataObjectInputStream dois = null;
		try {
			dois = new DataObjectInputStream(bais,csl);
			return (T)dois.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if(dois!=null){
				try{
					dois.close();
				}catch(IOException e){
					
				}
			}
		}
		return null;
	}
	
	public static <T extends DataObject> byte[] write(T object,ClassSerializer csl){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataObjectOutputStream doos = null;
		try {
			doos = new DataObjectOutputStream(baos,csl,object);
			doos.writeObject(object);
			doos.flush();
			return baos.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if(doos!=null){
				try {
					doos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			}
		}
		return null;
	}
	
	static class DataObjectInputStream extends ObjectInputStream{

		private final ClassSerializer serializer;
		
		public DataObjectInputStream(InputStream arg0,ClassSerializer csl) throws IOException {
			super(arg0);
			// TODO Auto-generated constructor stub
			this.serializer = csl;
		}
		
		protected ObjectStreamClass readClassDescriptor() throws IOException{
			return this.serializer.readDescriptor(this);
		}
		
	}
	
	static class DataObjectOutputStream extends ObjectOutputStream{

		private final ClassSerializer serializer;
		
		private final DataObject topLevelObject;
		
		public DataObjectOutputStream(OutputStream arg0,ClassSerializer csl,DataObject orgObject) throws IOException {
			super(arg0);
			// TODO Auto-generated constructor stub
			AccessController.doPrivileged(new PrivilegedAction<Void>() {
				public Void run() {
					enableReplaceObject(true);
					return null;
				}
			});
			this.topLevelObject = orgObject;
			this.serializer = csl;
		}
		
		//�漰��������Ҫ���л��Ŀ����л����󶼻��������� (���л���ֵֻ��ȥ���л���Щû��ֱ�ӳ�ʼ��ֵ�ĵط�)
		protected void writeClassDescriptor(ObjectStreamClass osc) throws IOException {
//			System.out.println("writeClassDescriptor is called..."+osc.getName());
			this.serializer.writeDescriptor(osc, this);
		}
		
		//��ÿһ�������л������Ա���øú���
		protected Object replaceObject(Object obj){
			if(obj==null)return null;
			if(obj!=topLevelObject && (obj instanceof DataObject)){
				throw new IllegalStateException("you have a DataObeject nested:"+obj.getClass().getName());
			}else if(obj instanceof Serializable){
				serializer.checkInitializable(ObjectStreamClass.lookup(obj.getClass()));
			}
			Class<?> detect = obj.getClass();
			if(detect.isAnonymousClass()){
				System.out.println("����һ��������");
			}else if(detect.isLocalClass()){
				System.out.println("����һ���ڲ���");
			}
			return obj;
		}
		
	}
}
