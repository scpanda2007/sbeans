package viso.sbeans.impl.store.db;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.Transaction;
import com.sleepycat.db.TransactionConfig;

public class DbTransaction {
	private Transaction transaction;
	public DbTransaction(Environment env,long timeout,TransactionConfig config){
		try {
			transaction = env.beginTransaction(null, config);
			long timeoutMicroSeconds = (timeout < Long.MAX_VALUE/1000) ? timeout*1000 : 0;
			transaction.setTxnTimeout(timeoutMicroSeconds);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public Transaction getTransaction(){
		return transaction;
	}
	
	public void commit(){
		try {
			transaction.commit();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void abort(){
		try {
			transaction.abort();
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//分布式 和 嵌套中使用
	public void prepare(byte[] gid){
		try {
			transaction.prepare(gid);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
