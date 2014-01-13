package viso.sbeans.impl.store.data;

import java.io.Serializable;
import java.util.List;

import viso.sbeans.impl.service.data.DataContext;
import viso.sbeans.impl.service.data.DataService;
import viso.sbeans.impl.store.DataStore;
import viso.sbeans.impl.transaction.VTransaction;

public class DataObjectReference<T extends DataObject> implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private transient DataObject object;
	
	private transient DataContext context;
	
	private String key;
	
	private enum State{
		New,//创建一个对象
		Empty,//得到一个数据库对象，但尚取值
		UnModified,//得到一个数据库对象，尚未修改
		Dirty,//已经被修改的数据
		Removed,//删除该对象
		Flushed,//将修改写入数据库
		Untached//解除引用
	}
	
	private transient State state;
	
	private transient byte[] modified;//修改后的数据对象
	
	public DataObjectReference(DataObject object,String key, DataContext context){
		this.object = object;
		this.key = key;
		state = State.New;
		this.context = context;
	}
	
	public DataObjectReference(String key,DataContext context){
		this.key = key;
		state = State.Empty;
		this.context = context;
	}
	
	public DataObject getObject(){
		return this.object;
	}
	
	//我这里没有办法保证死锁
	public DataObject get(boolean lockWrite){
		switch(state){
		case Flushed:
			throw new IllegalStateException(" This ref is already flushed. ");
		case Untached:
			throw new IllegalStateException(" This ref is not used anymore. ");
		case Removed:
			return null;
		case New:
		case UnModified:
		case Dirty:
			return object;
		case Empty:
			break;
		default:
			throw new AssertionError("Unhandled State."+state);
		}
		state = State.UnModified;
		byte[] data = context.store.get(context.transaction, key, lockWrite);
		this.object = SerialUtil.read(data, context.serializer);
		this.context.addObject(object, this);
		return this.object;
	}
	
	public void unttach(){
		state = State.Untached;
		context.unregister(key);
	}
	
	public void setDirty(){
		state = State.Dirty;
		modified = SerialUtil.write(object,context.serializer);
	}
	
	public void delete(){
		switch (state) {
		case Flushed:
			throw new IllegalStateException(" This ref is already flushed. ");
		case Untached:
			throw new IllegalStateException(" This ref is not used anymore. ");
		case Removed: 
			throw new IllegalStateException(" This object already removed. ");
		case New: 
			this.state = State.Removed;
			break;
		case UnModified:
		case Empty:
		case Dirty: {
			this.object = null;
			this.state = State.Removed;
			modified = null;
			break;
		}
		default:
			throw new AssertionError("Unhandled State."+state);
		}
	}
	
	public FlushInfo flush() {
		switch (state) {
		case Flushed:
			throw new IllegalStateException(" This ref is already flushed. ");
		case Untached:
			throw new IllegalStateException(" This ref is not used anymore. ");
		case New:{
			this.state = State.Flushed;
			byte[] flush = SerialUtil.write(object,context.serializer);
			return new FlushInfo(key, 0, flush);		
		}
		case Dirty: {
			this.state = State.Flushed;
			byte[] flush = modified;
			modified = null;
			return new FlushInfo(key, 0, flush);
		}
		case Removed: {
			this.state = State.Flushed;
			return new FlushInfo(key, 1, null);
		}
		case UnModified:
		case Empty:
			break;
		default:
			throw new AssertionError("Unhandled State."+state);
		}
		return null;
	}
	
	static public void flushAll(DataContext context,List<FlushInfo> flushes){
		VTransaction txn = context.transaction;
		DataStore store = context.store;
		for(FlushInfo flush : flushes){
			String key = flush.key;
			if(flush.type==1){
				store.delete(txn, flush.key);
				continue;
			}
			store.put(txn, key, flush.modified);
		}
	}
	
	static public DataObjectReference<? extends DataObject> createReference(DataContext context,String key,DataObject object){
		if(getReference(context,key,false)!=null) throw new IllegalStateException("Key already exist. ["+key+"]");
		byte[] value = SerialUtil.write(object, context.serializer);
		assert key.indexOf('.')>0;
		context.store.put(context.transaction, key, value);
		DataObjectReference<?> ref = new DataObjectReference<DataObject>(object, key, context);
		context.register(key, ref);
		return ref;
	}
	
	static public DataObjectReference<? extends DataObject> findReference(DataContext context,String key){
		return context.find(key);
	}
	
	static public DataObjectReference<? extends DataObject> getReference(DataContext context,String key,boolean writeLock){
		DataObjectReference<?> ref = context.find(key);
		if(ref!=null){
//			System.out.println(" duplicate op...");
			return ref;
		}
		byte[] data = context.store.get(context.transaction, key, writeLock);
		if(data==null){
//			System.out.println(" data is null");
			return null;//没有相关的数据
		}
		ref = new DataObjectReference<DataObject>(key, context);
		context.register(key, ref);
		return ref;
	}
	
	private Object readResolve(){
		System.out.println("readResolve is called..");
		context = DataService.getInstance().getContextNoJoin();
		state = State.Empty;
		DataObjectReference<? extends DataObject> ref = context.find(key);
		if(ref!=null)return ref;
		context.register(key, this);
		return this;
	}

}
