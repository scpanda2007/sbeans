package viso.sbeans.impl.store.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import viso.sbeans.impl.store.DataEncoder;
import viso.sbeans.impl.store.DataStore;
import viso.sbeans.impl.transaction.VTransaction;

public class ClassTables {
	
	ReferenceQueue<ObjectStreamClass> referenceQueue = new ReferenceQueue<ObjectStreamClass>();
	Map<Integer,ClassDescInfo> classInfos = new HashMap<Integer,ClassDescInfo>();
	Map<ObjectStreamClass,ClassDescInfo> classDescs = new WeakHashMap<ObjectStreamClass,ClassDescInfo>();
	ReadWriteLock lock = new ReentrantReadWriteLock();
	
	DataStore store;
	
	private final short serialVersion = 0;
	
	public ClassTables(DataStore store){
		this.store = store;
	}
	
	public String dummy(){
		lock.readLock().lock();
		try{
			return classInfos.keySet().toString();
		}finally{
			lock.readLock().unlock();
		}
	}
	
	public ClassSerializer createClassSerializer(final VTransaction dbTxn){
		return new ClassSerializer(){

			@Override
			public void checkInitializable(ObjectStreamClass osc) throws DataObjectIOException {
				// TODO Auto-generated method stub
				getClassDescInfo(dbTxn, osc).checkInitializable();
			}

			@Override
			public ObjectStreamClass readDescriptor(ObjectInputStream ois) throws IOException {
				// TODO Auto-generated method stub
				ois.readShort();//保留字，打算将来用作版本
				return getClassDescInfo(dbTxn,DataEncoder.decodeVarInt(ois)).get();
			}

			@Override
			public void writeDescriptor(ObjectStreamClass osc,
					ObjectOutputStream oos) throws IOException {
				// TODO Auto-generated method stub
				oos.writeShort(serialVersion);
				DataEncoder.encodeVarInt(getClassDescInfo(dbTxn, osc).classId, oos);
			}
			
		};
	}
	
	private class UpdateMapResult{
		ClassDescInfo desc;
		UpdateMapResult(ClassDescInfo desc){
			this.desc = desc;
//			System.out.println(dummy());
		}
	}
	
	private ClassDescInfo getClassDescInfo(VTransaction dbTxn,int classId){
		ClassDescInfo info = null;
		lock.readLock().lock();
		try{
			info = this.classInfos.get(classId);
			if(info!=null)return info;
		}finally{
			lock.readLock().unlock();
		}
		byte[] classinfo = store.getClassInfo(dbTxn, classId);
		return updateMap(classId,BytesToObjectStreamClass(classinfo)).desc;
	}
	
	private ClassDescInfo getClassDescInfo(VTransaction dbTxn,ObjectStreamClass osc){
		ClassDescInfo info = null;
		lock.readLock().lock();
		try{
			info = this.classDescs.get(osc);
			if(info!=null)return info;
		}finally{
			lock.readLock().unlock();
		}
		int classId = store.getClassId(dbTxn,ObjectStreamClassToBytes(osc));
		return updateMap(classId, osc).desc;
	}
	
	public UpdateMapResult updateMap(int classId, ObjectStreamClass osc){
		lock.writeLock().lock();
		try{
			//清理一下不用的数据
			ClassDescInfo.processRefQueue(referenceQueue, classInfos);
			ClassDescInfo find = classInfos.get(classId);
			if(find==null || find.get()==null){
				find = new ClassDescInfo(osc, classId, referenceQueue);
				classInfos.put(classId, find);
			}
			if(!classDescs.containsKey(osc)){
				this.classDescs.put(osc, find);
			}
			return new UpdateMapResult(find);
		}finally{
			lock.writeLock().unlock();
		}
	}
	
	static ObjectStreamClass BytesToObjectStreamClass(byte[] bytes){
		ObjectInputStream ois = null;
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bais);
			return (ObjectStreamClass)ois.readObject();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new DataObjectIOException("Failed in BytesToObjectStreamClass",e);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			throw new DataObjectIOException("Failed in BytesToObjectStreamClass",e);
		} finally{
			if(ois!=null){
				try{
					ois.close();
				}catch(IOException e){
					
				}
			}
		}
	}
	
	static byte[] ObjectStreamClassToBytes(ObjectStreamClass osc){
		ObjectOutputStream oos = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(osc);
			oos.flush();
			return baos.toByteArray();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new DataObjectIOException("Failed in ObjectStreamClassToBytes",e);
		} finally{
			if(oos!=null){
				try {
					oos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
//					e.printStackTrace();
				}
			}
		}
	}
	
	private static class ClassDescInfo extends SoftReference<ObjectStreamClass>{
		
		private final int classId;
		
		boolean hasSerializeReadResolve = false;//是否自己实现了序列化读方法ReadResolve
		boolean hasSerializeWriteReplace = false;//是否自己实现了序列化写方法WriteReplace
		String missUnSerializeObjectCtorWithNoArg = null;//是否继承顺序中有一个非序列化类，且无法调用其无参构造函数进行初始化
		public ClassDescInfo(ObjectStreamClass ocs,int classId, ReferenceQueue<ObjectStreamClass> queue){
			super(ocs, queue);
			this.classId = classId;
			Class<?> klass = ocs.forClass();
			if(DataObject.class.isAssignableFrom(klass)){
				hasSerializeReadResolve = hasSerializeReadResolve(klass);
				hasSerializeWriteReplace = hasSerializeWriteReplace(klass);
				missUnSerializeObjectCtorWithNoArg = missUnSerializeObjectCtorWithNoArg(klass);
			}
		}
		
		public boolean checkInitializable() throws DataObjectIOException{
			if(missUnSerializeObjectCtorWithNoArg!=null){
				throw new DataObjectIOException("The Object Inheriate a non serializable" +
						" object whose non-arg constuctor is un-accessable");
			}
			if(hasSerializeReadResolve){
				throw new DataObjectIOException("The Object implement a ReadResolve method.");
			}
			if(hasSerializeWriteReplace){
				throw new DataObjectIOException("The Object implement a WriteReplace method.");
			}
			return true;
		}

		static void processRefQueue(ReferenceQueue<ObjectStreamClass> refQueue,Map<Integer,?> maps){
			ClassDescInfo ref;
			while((ref = (ClassDescInfo)refQueue.poll())!=null){
				maps.remove(ref.classId);
				System.out.println("clear some rubbish"+ref.classId);
			}
		}
	}
	
	static boolean hasSerializeReadResolve(Class<?> klass){
		return hasSerializeMethod(klass,"ReadResolve");
	}
	
	static boolean hasSerializeWriteReplace(Class<?> klass){
		return hasSerializeMethod(klass,"WriteReplace");
	}
	
	static boolean hasSerializeMethod(Class<?> klass,String methodName){
		Class<?> detect = klass.getSuperclass();
		Method method = null;
		while (detect != null) {
			try {
				method = detect.getDeclaredMethod(methodName);
				break;
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				detect = detect.getSuperclass();
			}
		}
		
		if(method == null || method.getReturnType()!=Object.class){
			return false;
		}
		
		int modifers = method.getModifiers();
		if(Modifier.isStatic(modifers) || Modifier.isAbstract(modifers)){
			return false;
		}
		
		if(Modifier.isPublic(modifers) || Modifier.isProtected(modifers)){
			return true;
		}
		
		if(Modifier.isPrivate(modifers)){
			return false;
		}
		
		return samePackage(klass,detect);
	}
	
	//不知道这个方法的原理 直接copy的
	static boolean samePackage(Class<?> klass,Class<?> another){
		return klass.getClassLoader()==another.getClassLoader() 
			&& getPackageName(klass).equals(getPackageName(another));
	}
	
	static String getPackageName(Class<?> klass){
		String name = klass.getName();
		int pos = name.lastIndexOf('[');
		if(pos>=0){
			name = name.substring(pos+2);
		}
		pos = name.lastIndexOf('.');
		return pos>=0? name.substring(0,pos) : name;
	}
	
	static String missUnSerializeObjectCtorWithNoArg(Class<?> klass){
		Class<?> detect = klass.getSuperclass();
		while(detect!=null && Serializable.class.isAssignableFrom(detect)){
			detect = detect.getSuperclass(); 
		}
		if(detect==null) return null;
		try {
			Constructor<?> ctor = detect.getDeclaredConstructor();
			int modifiers = ctor.getModifiers();
			if(Modifier.isPrivate(modifiers)){
				return detect.getName();
			}
			if(Modifier.isProtected(modifiers) || Modifier.isPublic(modifiers)){
				return null;
			}
			if(!samePackage(klass,detect)){
				return detect.getName();
			}
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			return detect.getName();
		}
		return null;
	}
}
