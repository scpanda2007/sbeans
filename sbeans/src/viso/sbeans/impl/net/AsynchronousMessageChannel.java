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
import java.util.concurrent.Future;
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
	
	public AsynchronousMessageChannel(AsynchronousByteChannel channel,int bufferSize){
		this.channel = channel;
		readBuffers = ByteBuffer.allocateDirect(bufferSize);
	}
	
	public void close() throws IOException{
		this.channel.close();
	}
	
	private AtomicBoolean readPending = new AtomicBoolean(false);
	
	public Future<MessageBuffer> read(CompletionHandler<MessageBuffer,Void> readHandler){
		if(!readPending.compareAndSet(false, true)){
			throw new ReadPendingException();
		}
		return new Reader(readHandler).start();
	}
	
	private int MessageLength(){
		if(readBuffers.position() >= kPrefixLength){
			return ((short)(readBuffers.get(1) & 0xff) + ((readBuffers.get(0)<<8))) + kPrefixLength;
		}
		return -1;
	}
	
	private class Reader extends FutureTask<MessageBuffer> implements CompletionHandler<Integer,Void> {
		
		private int messageLength = -1;
		
		CompletionHandler<MessageBuffer,Void> handler;
		
		public Reader(CompletionHandler<MessageBuffer,Void> handler){
			super(new FallCallable<MessageBuffer>());
			this.handler = handler;
		}
		
		private Object lock = new Object();
		
		public Future<MessageBuffer> start() {
			synchronized (lock) {
				if (isDone())
					return this;
				int length = MessageLength();
				int pos = readBuffers.position();
				if (pos > 0) {
					if (pos > length) {
						readBuffers.position(length);
						readBuffers.limit(pos);
						readBuffers.compact();
					} else {
						readBuffers.clear();
					}
				}
				processBuffer();
				return this;
			}
		}
		
		public void processBuffer(){
			if(messageLength==-1){
				//第一次读
				messageLength = MessageLength();
				if(messageLength > 0 && readBuffers.limit() < messageLength){
					setException(new BufferOverflowException());
					return;
				}
			}
			if (messageLength > 0 && readBuffers.position() >= messageLength) {
				ByteBuffer buffer = readBuffers.duplicate();
				buffer.position(kPrefixLength);
				byte[] payload = new byte[messageLength - kPrefixLength];
				buffer.get(payload);
				this.set(new MessageBuffer(payload));
				return;
			}
			channel.read(readBuffers, null, this);
		}
		
		@Override
		public void completed(Integer arg0, Void arg1) {
			synchronized (lock) {
				if(isDone())return;
				// TODO Auto-generated method stub
				if (arg0.intValue() == -1) {
					this.setException(new EOFException("读到 EOF."));
					return;
				}
				processBuffer();
			}
		}

		@Override
		public void failed(Throwable arg0, Void arg1) {
			// TODO Auto-generated method stub
			synchronized (lock) {
				this.setException(arg0);
			}
		}
		
		@Override
		public boolean cancel(boolean arg0){
			synchronized (lock) {
				// 当断开一个连接时
				return super.cancel(arg0);
			}
		}
		
		@Override
		protected void done() {
			synchronized (lock) {
				readPending.set(false);
				if(handler == null) return;
				MessageBuffer message = null;
				try {
					message = this.get();
				} catch (Throwable t) {
					try {
						handler.failed(t, null);
					} catch (Exception e) {
					}
					return;
				}
				try {
					handler.completed(message, null);
				} catch (Exception e) {
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
		
		private Object lock = new Object();
		
		public Writer(CompletionHandler<Void,Void> handler,ByteBuffer buffer){
			super(new FallCallable<Void>());
			this.handler = handler;
			short length = (short)buffer.remaining();
			byte[] send = new byte[kPrefixLength+length];
			send[0] = (byte)(length >>> 8);
			send[1] = (byte)(length);
			buffer.get(send, 2, length);
			sendBuffer = ByteBuffer.wrap(send);
		}
		
		public FutureTask<Void> startWrite() {
			synchronized (lock) {
				channel.write(sendBuffer, null, this);
				return this;
			}
		}
		
		@Override
		public void completed(Integer arg0, Void arg1) {
			synchronized (lock) {
				// TODO Auto-generated method stub
				if (sendBuffer.hasRemaining()) {
					channel.write(sendBuffer, null, this);
					return;
				}
				set(null);
			}
		}

		@Override
		public void failed(Throwable arg0, Void arg1) {
			// TODO Auto-generated method stub
			synchronized (lock) {
				setException(arg0);
			}
		}
		
		protected void done() {
			synchronized (lock) {
				writePending.set(false);
				if (handler == null)
					return;
				try {
					this.get();
				} catch (Throwable t) {
					try {
						handler.failed(t, null);
					} catch (Exception e) {
					}
					return;
				}
				try {
					handler.completed(null, null);
				} catch (Exception e) {
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
