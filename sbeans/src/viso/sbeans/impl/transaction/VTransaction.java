package viso.sbeans.impl.transaction;

import java.util.ArrayList;
import java.util.List;

public class VTransaction {
	Thread thread;
	List<TransactionListener> listeners;
	List<TransactionParticipant> participants;
	State state;
	
	private VTransaction(Thread thread){
		this.state = State.Active;
		this.thread = thread;
		listeners = new ArrayList<TransactionListener>();
		participants = new ArrayList<TransactionParticipant>();
	}
	
	enum State{
		Active,
		Commited,
		Aborted
	}
	
	public boolean aborted(){
		return state==State.Aborted;
	}
	
	public boolean commited(){
		return state==State.Commited;
	}
	
	public boolean active(){
		return state==State.Active;
	}
	
	public static VTransaction createTransaction(){
		return new VTransaction(Thread.currentThread());
	}
	
	public void checkThread(String method){
		if(Thread.currentThread()==thread) return;
		throw new IllegalStateException("Error , not in the right thread. -> "+method);
	}
	
	public void registerParticipant(TransactionParticipant participant){
		checkThread("registerParticipant");
		participants.add(participant);
	}
	
	public void registerListener(TransactionListener listener){
		checkThread("registerListener");
		listeners.add(listener);
	}
	
	public void commit() {
		checkThread("commit");
		if(State.Aborted == state){
			throw new TransactionNotActiveException("This transaction is aborted.");
		}
		if(State.Commited == state){
			throw new TransactionNotActiveException("This transaction is commited.");
		}
		Throwable e = null;
		try {
			for (TransactionListener listener : listeners) {
				listener.beforeComplete(this);
			}
			for (int i=0;i<participants.size();i++) {
				participants.get(i).commit(this);
				participants.remove(i);
				i-=1;
			}
			for(int i=0;i<listeners.size();i++){
				listeners.get(i).afterComplete(this,true);
				listeners.remove(i);
				i-=1;
			}
			state = State.Commited;
			return;
		} catch (Throwable t) {
			e = t;
		}
		abort(e);
	}
	
	Throwable lastThrow;
	
	public void abort(Throwable t) {
		checkThread("abort");
		if (State.Aborted == state) {
			throw new TransactionNotActiveException(
					"This transaction is aborted.");
		}
		if (State.Commited == state) {
			throw new TransactionNotActiveException(
					"This transaction is commited.");
		}

		for (TransactionParticipant participant : participants) {
			try {
				participant.abort(this);
			} catch (Throwable t1) {
				t1.printStackTrace();
			}
		}
		
		for(TransactionListener listener : listeners){
			try {
				listener.afterComplete(this,false);
			} catch (Throwable t1) {
				t1.printStackTrace();
			}
		}
		
		state = State.Aborted;
		lastThrow = t;
		t.printStackTrace();
		throw new IllegalStateException("Bad abort a transaction.. ",t);
	}
}
