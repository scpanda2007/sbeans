package viso.sbeans.impl.net;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.Channel;
import java.nio.channels.CompletionHandler;
import java.nio.channels.ReadPendingException;
import java.nio.channels.WritePendingException;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

import viso.sbeans.impl.util.MessageBuffer;

/**
 * 异步消息通道
 * */
public class AsynchronousMessageChannel implements Channel{
	
	private final AsynchronousByteChannel channel;
	
	private final ByteBuffer readBuffers;
	
	private final int kPrefixLength = 2;
	
	private final int bufferSize;
	
	public AsynchronousMessageChannel(AsynchronousByteChannel channel,int bufferSize){
		this.channel = channel;
		readBuffers = ByteBuffer.allocateDirect(bufferSize);
		this.bufferSize = bufferSize;
	}
	
	public void close() throws IOException{
		this.channel.close();
	}
	
	private AtomicBoolean readPending = new AtomicBoolean(false);
	
	public void read(CompletionHandler<MessageBuffer,Void> readHandler){
		if(!readPending.compareAndSet(false, true)){
			throw new ReadPendingException();
		}
		new Reader(readHandler).startRead();
	}
	
	private int MessageLength(){
		if(readBuffers.position() >= kPrefixLength){
			return (readBuffers.get(1) & 0x0f) | ((readBuffers.get(0)<<8) & 0xf0) + kPrefixLength;
		}
		return -1;
	}
	
	private class Reader extends FutureTask<MessageBuffer> implements CompletionHandler<Integer,Void> {
		
		int messageLength = -1;
		
		CompletionHandler<MessageBuffer,Void> handler;
		
		public Reader(CompletionHandler<MessageBuffer,Void> handler){
			super(new FallCallable<MessageBuffer>());
			this.handler = handler;
		}
		
		public void startRead(){
			int length = MessageLength();
			int pos = readBuffers.position();
			if(length>0){
				readBuffers.position(length);
				readBuffers.limit(pos);
				readBuffers.compact();
				messageLength = -1;
			}
			processBuffer();
		}
		
		public void processBuffer(){
			if(messageLength==-1){
				//第一次读
				messageLength = MessageLength();
				if(messageLength > bufferSize){
					throw new BufferOverflowException();
				}
			}
			if(messageLength >= 0){
				int pos = readBuffers.position();
				if(pos >= messageLength){
					ByteBuffer buffer = readBuffers.duplicate().asReadOnlyBuffer();
					buffer.position(kPrefixLength);
					byte[] payload = new byte[messageLength - kPrefixLength];
					buffer.get(payload);
					this.set(new MessageBuffer(payload));
					return;
				}
			}
			channel.read(readBuffers, null, this);
		}
		
		@Override
		public void completed(Integer arg0, Void arg1) {
			// TODO Auto-generated method stub
			if(arg0.intValue() == -1){
				this.setException(new EOFException("读到 EOF."));
				return;
			}
			processBuffer();
		}

		@Override
		public void failed(Throwable arg0, Void arg1) {
			// TODO Auto-generated method stub
			this.setException(arg0);
		}
		
		@Override
		protected void done(){
			
			readPending.set(false);
			MessageBuffer message = null;
			try{
				message = this.get();
			}catch(Throwable t){
				if(handler!=null){
					try{
						handler.failed(t, null);
					}catch(Exception e){
					}
				}
				return;
			}
			
			if(handler!=null){
				try{
					handler.completed(message,null);
				}catch(Exception e){
				}
			}
		}
		
	}
	
	private AtomicBoolean writePending = new AtomicBoolean(false);
	
	public void write(ByteBuffer buffer, CompletionHandler<Void,Void> writeHandler){
		if(!writePending.compareAndSet(false, true)){
			throw new WritePendingException();
		}
		new Writer(writeHandler, buffer).startWrite();
	}
	
	private class Writer extends FutureTask<Void> implements CompletionHandler<Integer,Void>{

		CompletionHandler<Void,Void> handler;
		
		ByteBuffer sendBuffer;
		
		public Writer(CompletionHandler<Void,Void> handler,ByteBuffer buffer){
			super(new FallCallable<Void>());
			this.handler = handler;
			int length = buffer.remaining();
			byte[] send = new byte[kPrefixLength+length];
			send[0] = (byte)(length >>> 8);
			send[1] = (byte)(length);
			buffer.get(send, 2, length);
			sendBuffer = ByteBuffer.wrap(send);
		}
		
		public void startWrite(){
			channel.write(sendBuffer, null, this);
		}
		
		@Override
		public void completed(Integer arg0, Void arg1) {
			// TODO Auto-generated method stub
			if(sendBuffer.hasRemaining()){
				channel.write(sendBuffer, null, this);
				return;
			}
			set(null);
		}

		@Override
		public void failed(Throwable arg0, Void arg1) {
			// TODO Auto-generated method stub
			setException(arg0);
		}
		
		protected void done(){
			writePending.set(false);
			try{
				this.get();
			}catch(Throwable t){
				if(handler!=null){
					try{
						handler.failed(t, null);
					}catch(Exception e){
					}
				}
				return;
			}
			if(handler!=null){
				try{
					handler.completed(null, null);
				}catch(Exception e){
				}
			}
		}
		
	}
	
	private class FallCallable<T> implements Callable<T>{

		@Override
		public T call() throws Exception {
			// TODO Auto-generated method stub
			return null;
		}
		
	}

	@Override
	public boolean isOpen() {
		// TODO Auto-generated method stub
		return channel.isOpen();
	}
}
