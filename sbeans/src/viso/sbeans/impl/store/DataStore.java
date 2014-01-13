package viso.sbeans.impl.store;

import java.io.ObjectStreamClass;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import viso.sbeans.impl.store.db.BDBDatabase;
import viso.sbeans.impl.store.db.DbEnvironment;
import viso.sbeans.impl.store.db.DbTransaction;
import viso.sbeans.impl.transaction.TransactionNotActiveException;
import viso.sbeans.impl.transaction.TransactionParticipant;
import viso.sbeans.impl.transaction.VTransaction;

import static viso.sbeans.impl.util.Objects.checkNull;

/**
 * 底层数据库数据存储实现，事务只负责数据的提交和撤销，不负责数据的同步和互斥
 * */
public class DataStore implements TransactionParticipant{

	Map<String, BDBDatabase> databases = new HashMap<String, BDBDatabase>();
	
	BDBDatabase classDb;
	BDBDatabase classIdxDb;
	
	Map<Integer, ObjectStreamClass> classDescs = new HashMap<Integer, ObjectStreamClass>();
	
	DbEnvironment env;

	Store store;
	
	ThreadLocal<TxnInfo> txnInfos = new ThreadLocal<TxnInfo>();
	
	class TxnInfo{
		VTransaction transaction;
		DbTransaction dbTxn;
		public TxnInfo(VTransaction txn,DbTransaction dbTxn){
			this.transaction = txn;
			this.dbTxn = dbTxn;
		}
	}
	
	/**
	 * 查看当前活动是否在当前 VTransaction 之中
	 * */
	public void checkHaveAnActiveTransaction(VTransaction transaction){
		if(transaction==null){
			throw new TransactionNotActiveException("Need an transaction on datastore op.");
		} 
		if(!transaction.active()){
			throw new TransactionNotActiveException("Need an active transaction on datastore op.");
		}
		checkNeedJoinTransaction(transaction);
	}
	
	/**
	 * 查看事务是否正确，如有必要加入一个数据库事务给当前事务
	 * */
	public void checkNeedJoinTransaction(VTransaction txn){
		if(txnInfos.get()==null){
			txnInfos.set(new TxnInfo(txn,env.beginTransaction(20000)));
			txn.registerParticipant(this);
			//20秒
			return;
		}
		if(txnInfos.get().transaction!=txn){
			throw new IllegalStateException("Already have a transaction.");
		}
	}
	
	/**
	 * 查看事务是否与当前事务挂钩
	 * */
	public DbTransaction checkDbTxn(VTransaction txn){
		if(txnInfos.get()==null){
			return null;
		}
		if(txnInfos.get().transaction!=txn){
			throw new IllegalStateException(" the txn is not equal.");
		}
		if(txnInfos.get().dbTxn==null){
			throw new IllegalStateException(" the dbTxn is lost.");
		}
		return txnInfos.get().dbTxn;
	}
	
	public DataStore(Properties property) {

	}

	public DataStore init(String base, String root,
			Set<String> fileNames){
		store = new Store(base,root,fileNames);
		return this;
	}
	
	public void shutdown(){
		store.close();
	}
	
	private class Store {

		/**
		 * 初始化数据库
		 * */
		public Store(String base, String root,
				Set<String> fileNames) {
			if (env != null) {
				throw new IllegalStateException("DataStore already setup.");
			}
			if(fileNames.contains("classes") || fileNames.contains("classidx")){
				throw new IllegalStateException("Illegal Param: classes.");
			}
			env = DbEnvironment.environment(base, root);
			DbTransaction txn = env.beginTransaction(100);
			try {
				for (String fileName : fileNames) {
					databases.put(fileName, env.open(txn, fileName, true));
				}
				classDb = env.open(txn, "classes", true);
				classIdxDb = env.open(txn, "classidx", true);
				txn.commit();
			} catch (Exception e) {
				txn.abort();
				env.close();
				throw new IllegalStateException("Failed open databases.", e);
			}
		}

		/**
		 * 获得某个表的某个数据
		 * */
		public byte[] get(DbTransaction txn, String table, byte[] key,
				boolean update) {
			checkNull("Table name:", table);
			if (!databases.containsKey(table)) {
				throw new IllegalArgumentException("The table:" + table
						+ " do not exist.");
			}
			return databases.get(table).get(key, txn, update);
		}

		/**
		 * 以不覆盖的方式尝试写入一个值，并返回该键值的值是否本不存在，如果存在是否写入成功取决于数据看的配置是否允许同一键值有 多个值
		 * */
		public boolean putNoOverWrite(DbTransaction txn, String table,
				byte[] key, byte[] data) {
			checkNull("Table name:", table);
			if (!databases.containsKey(table)) {
				throw new IllegalArgumentException("The table:" + table
						+ " do not exist.");
			}
			return databases.get(table).putNoOverWrite(key, data, txn);
		}

		/**
		 * 写入一个值
		 * */
		public void put(DbTransaction txn, String table, byte[] key, byte[] data) {
			checkNull("Table name:", table);
			if (!databases.containsKey(table)) {
				throw new IllegalArgumentException("The table:" + table
						+ " do not exist.");
			}
			databases.get(table).put(key, data, txn);
		}

		/**
		 * 删除一个值 并返回是否存在这样一个值
		 * */
		public boolean delete(DbTransaction txn, String table, byte[] key) {
			checkNull("Table name:", table);
			if (!databases.containsKey(table)) {
				throw new IllegalArgumentException("The table:" + table
						+ " do not exist.");
			}
			return databases.get(table).delete(key, txn);
		}
		
		/**
		 * 将一个数据升级写锁
		 * */
		public void setDirty(DbTransaction txn, String table, byte[] key) {
			checkNull("Table name:", table);
			if (!databases.containsKey(table)) {
				throw new IllegalArgumentException("The table:" + table
						+ " do not exist.");
			}
			databases.get(table).setDirty(key, txn);
		}

		public void close() {
			for (BDBDatabase database : databases.values()) {
				database.close();
			}
			classDb.close(); 
			classIdxDb.close();
		}
	}

	public void put(VTransaction txn, String key, byte[] value){
		checkHaveAnActiveTransaction(txn);
		String storeKey = key.substring(key.indexOf('.')+1);
		String table = key.substring(0,key.indexOf('.'));
		try {
			store.put(checkDbTxn(txn), table, storeKey.getBytes("UTF-8"), value);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed to put data",e);
		}
	}
	
	public boolean putNoOverWrite(VTransaction txn, String key, byte[] value){
		checkHaveAnActiveTransaction(txn);
		String storeKey = key.substring(key.indexOf('.')+1);
		String table = key.substring(0,key.indexOf('.'));
		try {
			return store.putNoOverWrite(checkDbTxn(txn), table, storeKey.getBytes("UTF-8"), value);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed to put data",e);
		}
	}
	
	public boolean delete(VTransaction txn, String key){
		checkHaveAnActiveTransaction(txn);
		String storeKey = key.substring(key.indexOf('.')+1);
		String table = key.substring(0,key.indexOf('.'));
		try {
			return store.delete(checkDbTxn(txn), table, storeKey.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed to put data",e);
		}
	}
	
	public byte[] get(VTransaction txn, String key,boolean writeLock){
		checkHaveAnActiveTransaction(txn);
		String storeKey = key.substring(key.indexOf('.')+1);
		String table = key.substring(0,key.indexOf('.'));
		try {
			return store.get(checkDbTxn(txn), table, storeKey.getBytes("UTF-8"), writeLock);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed to put data",e);
		}
	}
	
	public void setDirty(VTransaction txn, String key){
		checkHaveAnActiveTransaction(txn);
		String storeKey = key.substring(key.indexOf('.')+1);
		String table = key.substring(0,key.indexOf('.'));
		try {
			store.delete(checkDbTxn(txn), table, storeKey.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed to put data",e);
		}
	}
	
	public byte[] getClassInfo(VTransaction txn, int classId){
		return DataUtility.getClassInfo(classDb, classId, env, 20000);
	}
	
	public int getClassId(VTransaction txn, byte[] data) {
		// TODO Auto-generated method stub
		return DataUtility.getClassId(classDb, classIdxDb, data, env, 20000);
	}

	@Override
	public void abort(VTransaction transaction) {
		// TODO Auto-generated method stub
		DbTransaction dbTxn = checkDbTxn(transaction);
		if(dbTxn!=null){
			dbTxn.abort();
		}
		txnInfos.set(null);
	}

	@Override
	public void commit(VTransaction transaction) {
		// TODO Auto-generated method stub
		DbTransaction dbTxn = checkDbTxn(transaction);
		if(dbTxn!=null){
			dbTxn.commit();
		}
		txnInfos.set(null);
	}

}
