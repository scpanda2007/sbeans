package viso.sbeans.impl.service.data;

import java.util.concurrent.ConcurrentHashMap;

import viso.sbeans.impl.kernel.AccessCoordinator;
import viso.sbeans.impl.kernel.ThreadContext;
import viso.sbeans.impl.store.DataStore;
import viso.sbeans.impl.store.data.ClassTables;
import viso.sbeans.impl.store.data.DataObject;
import viso.sbeans.impl.store.data.DataObjectReference;
import viso.sbeans.impl.transaction.TransactionNotActiveException;
import viso.sbeans.impl.transaction.VTransaction;
import viso.sbeans.impl.util.Locker;

public class DataService {
	
	private ConcurrentHashMap<String,Object> services = new ConcurrentHashMap<String,Object>();
	
	private ThreadLocal<DataContext> context = new ThreadLocal<DataContext>();
	
	private static DataService instance;
	
	private DataStore store;
	
	private AccessCoordinator accessManager;
	
	private ClassTables classes;
	
	private DataService(DataStore store, AccessCoordinator accessManager){
		this.store = store;
		this.accessManager = accessManager;
		classes = new ClassTables(store);
	}
	
	public void shutdown(){
		this.store.shutdown();
	}
	
	public static DataService createDataService(DataStore store, AccessCoordinator accessManager){
		if(instance!=null){
			throw new IllegalStateException("Already has a dataService.");
		}
		instance = new DataService(store, accessManager);
		return instance;
	}
	
	public static DataService getInstance(){
		return instance;
	}
	
	public <T> T getService(String name,Class<T> klass){
		Object obj = services.get(name);
		if(obj==null)return null;
		return klass.cast(obj);
	}
	
	public Object registerService(String name,Object service){
		return services.putIfAbsent(name, service);
	}
	
	public DataContext getContextNoJoin(){
		return context.get();
	}
	
	public DataContext joinDataContext(){
		if(context.get()!=null) return context.get();
		VTransaction transaction = ThreadContext.getTransaction();
		if(transaction==null || !transaction.active()){
			throw new TransactionNotActiveException("Need an active transaction to create context");
		}
		DataContext ctxt = new DataContext(transaction, store, classes);
		context.set(ctxt);
		return ctxt;
	}
	
	public void clearContext(){
		this.context.set(null);
	}
	
	public DataObject getObject(String key,boolean lockWrite){
		DataObjectReference<?> ref = getReference(key,lockWrite);
		return ref==null? null : ref.get(lockWrite);
	}
	
	public void removeObject(DataObject object){
		this.joinDataContext().removeObject(object);
	}
	
	public void setDirty(DataObject object){
		this.joinDataContext().setDirty(object);
	}
	
	public DataObjectReference<?> addManaged(String key, DataObject object){
		return this.joinDataContext().addManaged(object, key);
	}
	
	public DataObjectReference<?> getReference(String key,boolean lockWrite){
		DataContext ctxt = this.joinDataContext();
		return ctxt.getReference(key,lockWrite);
	}
	
	public void locked(Locker locker) {
		// TODO Auto-generated method stub
		accessManager.locked(this.joinDataContext().transaction , locker);
	}
	
}
