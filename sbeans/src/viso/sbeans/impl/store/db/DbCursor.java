package viso.sbeans.impl.store.db;

import com.sleepycat.db.Cursor;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;

public class DbCursor {
	DatabaseEntry keyEntry = new DatabaseEntry();
	DatabaseEntry valueEntry = new DatabaseEntry();
	boolean valid = false;
	Cursor cursor;
	
	public DbCursor(Database db, Transaction txn){
		try {
			cursor = db.openCursor(txn, null);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed to openCursor",e);
		}
	}
	
	public byte[] getKey(){
		return valid?BDBDatabase.convertData(keyEntry.getData()) : null;
	}
	
	public byte[] getValue(){
		return valid?BDBDatabase.convertData(valueEntry.getData()) : null;
	}
	
	public boolean findFirst(){
		try {
			OperationStatus statu = cursor.getFirst(keyEntry, valueEntry, null);
			if(statu==OperationStatus.SUCCESS){
				valid = true;
				return true;
			}else if(statu==OperationStatus.NOTFOUND){
				return false;
			}
			throw new IllegalStateException("Failed to findFirst");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed to findFirst",e);
		}
	}
	
	public boolean findNext(){
		try {
			OperationStatus statu = cursor.getNext(keyEntry, valueEntry, null);
			if(statu==OperationStatus.SUCCESS){
				valid = true;
				return true;
			}else if(statu==OperationStatus.NOTFOUND){
				return false;
			}
			throw new IllegalStateException("Failed to findNext0");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed to findNext0",e);
		}
	}
	
	public boolean findNext(byte[] key) {
		DatabaseEntry searchKey = new DatabaseEntry(key);
		try {
			OperationStatus statu = cursor.getSearchKeyRange(searchKey,
					valueEntry, null);
			if (statu == OperationStatus.SUCCESS) {
				keyEntry = searchKey;
				valid = true;
				return true;
			} else if (statu == OperationStatus.NOTFOUND) {
				return false;
			}
			throw new IllegalStateException("Failed to findNext1");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed to findNext1",e);
		}
	}
	
	public boolean findLast(){
		try {
			OperationStatus statu = cursor.getLast(keyEntry, valueEntry, null);
			if(statu==OperationStatus.SUCCESS){
				valid = true;
				return true;
			}else if(statu==OperationStatus.NOTFOUND){
				return false;
			}
			throw new IllegalStateException("Failed to findLast0");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed to findLast0",e);
		}
	}
	
	public boolean putNoOverWrite(byte[] key, byte[] value) {
		DatabaseEntry putKey = new DatabaseEntry(key);
		DatabaseEntry putValue = new DatabaseEntry(value);
		try {
			OperationStatus statu = cursor.putNoOverwrite(putKey, putValue);
			if (statu == OperationStatus.SUCCESS) {
				keyEntry = putKey;
				valueEntry = putValue;
				valid = true;
				return true;
			} else if (statu == OperationStatus.KEYEXIST) {
				return false;
			}
			throw new IllegalStateException("Failed to putNoOverWrite");
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed to putNoOverWrite", e);
		}
	}
	
	public void close(){
		try {
			cursor.close();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new IllegalStateException("Failed to close",e);
		}
	}
}
