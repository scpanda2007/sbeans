package viso.sbeans.impl.store.data;

public class DataObjectIOException extends RuntimeException{
	private static final long serialVersionUID = 1L;
	public DataObjectIOException(String message){
		super(message);
	}
	public DataObjectIOException(String message,Throwable t){
		super(message,t);
	}
}
