package viso.sbeans.impl.kernel;

import viso.sbeans.impl.transaction.VTransaction;

/***
 * 设置一个线程的副本变量
 * @author whq
 *
 */
public class ThreadContext {
	private static ThreadLocal<VTransaction> txn = new ThreadLocal<VTransaction>();
	public static void setTransaction(final VTransaction transaction){
		if(txn.get()!=null){
			throw new IllegalStateException("Already has an active transaction.");
		}
		txn.set(transaction);
	}
	public static VTransaction getTransaction(){
		return txn.get();
	}
	public static void clearTransaction(final VTransaction transaction){
		if(txn.get()!=transaction){
			throw new IllegalStateException("Transaction not equal the param.");
		}
		txn.set(null);
	}
}
