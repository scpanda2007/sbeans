package viso.sbeans.impl.transaction;

public interface TransactionListener {
	public void beforeComplete(VTransaction transaction);
	public void afterComplete(VTransaction transaction,boolean commited);
}
