package viso.sbeans.impl.transaction;

public class TransactionNotActiveException extends IllegalStateException{
	private static final long serialVersionUID = 1L;
	public TransactionNotActiveException(String message){super(message);}
}
