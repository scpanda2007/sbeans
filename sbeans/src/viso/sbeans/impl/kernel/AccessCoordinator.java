package viso.sbeans.impl.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import viso.sbeans.impl.transaction.TransactionListener;
import viso.sbeans.impl.transaction.VTransaction;
import viso.sbeans.impl.util.Locker;

/**
 * ����bdb���е�ҳ������Ӧ�ò���Ҫ�ṩ������ƣ�����ֻ�ṩһ���򵥵�ʵ�֣���
 * ��֤�����Ȼ���
 * �ṩ��Ӧ�ò�����ڽ������������ķ���
 * */
public class AccessCoordinator {
	
	public AccessCoordinator(){
		
	}
	
	public void notifyNewTransactionInner(VTransaction txn){
		txn.registerListener(new TxnListener(txn));
	}
	
	public Map<VTransaction,List<Locker>> txnLockers = Collections.synchronizedMap(new IdentityHashMap<VTransaction,List<Locker>>());
	
	/**
	 * ע��һ���ӳ��ͷŵ���
	 * */
	public void locked(VTransaction txn,Locker locker){
		List<Locker> lockerList;
		if(!txnLockers.containsKey(txn)){
			lockerList = new ArrayList<Locker>();
			txnLockers.put(txn, lockerList);
			notifyNewTransactionInner(txn);
		}
		lockerList = txnLockers.get(txn);
		lockerList.add(locker);
	}
	
	class TxnListener implements TransactionListener{
		
		private final VTransaction txn;
		
		public TxnListener(VTransaction transaction){
			this.txn = transaction;
		}
		
		@Override
		public void beforeComplete(VTransaction transaction) {}

		@Override
		public void afterComplete(VTransaction transaction,boolean commit) {
			// TODO Auto-generated method stub
			if(txn!=transaction){
				throw new IllegalStateException("Not in the right transaction.");
			}
			List<Locker> lockerList = txnLockers.get(transaction);
			if(lockerList==null)return;
			txnLockers.remove(transaction);
			for(Locker locker: lockerList){
				locker.release();
			}
		}		
	}
}
