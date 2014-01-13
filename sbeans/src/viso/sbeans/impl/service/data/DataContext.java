package viso.sbeans.impl.service.data;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import viso.sbeans.impl.store.DataStore;
import viso.sbeans.impl.store.data.ClassSerializer;
import viso.sbeans.impl.store.data.ClassTables;
import viso.sbeans.impl.store.data.DataObject;
import viso.sbeans.impl.store.data.DataObjectReference;
import viso.sbeans.impl.store.data.FlushInfo;
import viso.sbeans.impl.transaction.TransactionListener;
import viso.sbeans.impl.transaction.VTransaction;

public class DataContext implements TransactionListener{
	
	public final DataStore store;
	ClassTables tables;
	public final ClassSerializer serializer;
	public final VTransaction transaction;
	
	SortedMap<String,DataObjectReference<?>> refs = new TreeMap<String,DataObjectReference<?>>();
	Map<DataObject,DataObjectReference<?>> objects = new IdentityHashMap<DataObject,DataObjectReference<?>>();
	
	public DataContext(VTransaction transaction,DataStore store,ClassTables tables){
		this.store = store;
		this.transaction = transaction;
		this.tables = tables;
		this.serializer = tables.createClassSerializer(transaction);
		this.transaction.registerListener(this);
	}
	
	public List<FlushInfo> flushAll(){
		List<FlushInfo> flushes = new ArrayList<FlushInfo>();
		Iterator<Map.Entry<String, DataObjectReference<?>>> iter = refs.entrySet().iterator();
		while(iter.hasNext()){
			Map.Entry<String, DataObjectReference<?>> entry = iter.next();
			FlushInfo flush = entry.getValue().flush();
			if(flush==null)continue;
			flushes.add(flush);
		}
		refs.clear();
		objects.clear();
		return flushes;
	}
	
	public void setDirty(DataObject object){
		objects.get(object).setDirty();
	}
	
	public void removeObject(DataObject object){
		objects.get(object).delete();
		objects.remove(object);
	}
	
	public void addObject(DataObject object, DataObjectReference<?> ref){
		if(objects.containsKey(object)){
			throw new IllegalStateException("object already put");
		}
		objects.put(object, ref);
	}
	
	public DataObjectReference<?> addManaged(DataObject object, String key){
		if(null!=objects.get(object)){
			throw new IllegalStateException("object already put");
		}
		DataObjectReference<?> ref = DataObjectReference.createReference(this, key, object);
		objects.put(object, ref);
		return ref;
	}
	
	public DataObjectReference<?> getReference(String key,boolean writeLock){
		return DataObjectReference.getReference(this, key, writeLock);
	}
	
	public DataObjectReference<?> find(String key){
		return refs.get(key);
	}
	
	public void unregister(String key){
		DataObjectReference<?> ref = refs.remove(key);
		if(ref!=null && ref.getObject()!=null){
			objects.remove(ref.getObject());
		}
	}
	
	public void register(String key,DataObjectReference<?> ref){
		refs.put(key, ref);
	}

	@Override
	public void beforeComplete(VTransaction transaction) {
		// TODO Auto-generated method stub
		DataObjectReference.flushAll(this, flushAll());
	}

	@Override
	public void afterComplete(VTransaction transaction, boolean commit) {
		// TODO Auto-generated method stub
		
	}
	
}
