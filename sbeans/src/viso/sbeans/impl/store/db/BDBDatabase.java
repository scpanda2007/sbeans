package viso.sbeans.impl.store.db;

import java.io.FileNotFoundException;

import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.Environment;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;

public class BDBDatabase {
	
	static DatabaseConfig createConfig = new DatabaseConfig();
	static{
		createConfig.setAllowCreate(true);
		createConfig.setType(DatabaseType.BTREE);
	}
	
	Database db;
	
	public BDBDatabase(Environment env, Transaction txn, 
			String fileName, boolean create){
		try {
			db = env.openDatabase(txn, fileName, null, create? createConfig:null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("File not found "+fileName);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed get databaes.",e);
		}
	}
	
	public DbCursor openCursor(DbTransaction txn){
		return new DbCursor(db, txn.getTransaction());
	}
	
	private final static byte[] EmptyBytes = {};
	
	//调用 update == true 的时候，似乎会锁住整个表
	public byte[] get(byte[] key,DbTransaction txn, boolean update){
		try {
			DatabaseEntry valueEntry = new DatabaseEntry();
			OperationStatus statu = db.get(txn.getTransaction(), new DatabaseEntry(key), valueEntry, update?LockMode.RMW:null);
			if(statu==OperationStatus.SUCCESS){
				return convertData(valueEntry.getData());
			}else if(statu==OperationStatus.NOTFOUND){
				return null;
			}
			throw new IllegalStateException("Faild get value.");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IllegalStateException("Faild get value.",e);
		}
	}
	
	//在transaction为 fullisolation 时 会与读产生冲突
	public void put(byte[] key, byte[] data, DbTransaction txn){
		try {
			OperationStatus statu = db.put(txn.getTransaction(), new DatabaseEntry(key), new DatabaseEntry(data));
			if(statu!=OperationStatus.SUCCESS){
				throw new IllegalStateException("Faild put.");
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IllegalStateException("Faild put.",e);
		}
	}
	
	/**
	 * @return 是否是第一个
	 */
	public boolean putNoOverWrite(byte[] key,byte[] data,DbTransaction txn){
		try {
			OperationStatus statu = db.putNoOverwrite(txn.getTransaction(), new DatabaseEntry(key), new DatabaseEntry(data));
			if(statu==OperationStatus.SUCCESS){
				return true;
			}else if(statu==OperationStatus.KEYEXIST){
				return false;
			}
			throw new IllegalStateException("Failed putNoOverWrite.");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed putNoOverWrite.",e);
		}
	}
	
	public byte[] testPartial(byte[] key,DbTransaction txn,int off,int len,boolean partial){
		DatabaseEntry valueEntry = new DatabaseEntry();
		valueEntry.setPartial(off, len, partial);
		try {
			OperationStatus statu = db.get(txn.getTransaction(), new DatabaseEntry(key),
					valueEntry, LockMode.RMW);
			if(statu==OperationStatus.SUCCESS){
				return convertData(valueEntry.getData());
			}else if(statu==OperationStatus.NOTFOUND){
				return null;
			}
			throw new IllegalStateException("Faild testPartial.");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Faild testPartial.",e);
		}		
	}
	
	public void setDirty(byte[] key,DbTransaction txn){
		DatabaseEntry valueEntry = new DatabaseEntry();
		valueEntry.setPartial(0, 0, true);
		try {
			OperationStatus statu = db.get(txn.getTransaction(), new DatabaseEntry(key),
					valueEntry, LockMode.RMW);
			if(statu!=OperationStatus.SUCCESS
					&& statu!=OperationStatus.NOTFOUND){
				throw new IllegalStateException("Faild setDirty.");
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Faild setDirty.",e);
		}
	}
	
	public boolean delete(byte[] key,DbTransaction txn){
		try {
			OperationStatus statu = db.delete(txn.getTransaction(), new DatabaseEntry(key));
			if(statu==OperationStatus.SUCCESS){
				return true;
			}else if(statu==OperationStatus.NOTFOUND){
				return false;
			}
			throw new IllegalStateException("Failed delete.");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed delete.",e);
		}
	}
	
	public static byte[] convertData(byte[] data){
		return data==null ? EmptyBytes : data;
	}
	
	public void close(){
		try {
			db.close();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed close.",e);
		}
	}
	
}
