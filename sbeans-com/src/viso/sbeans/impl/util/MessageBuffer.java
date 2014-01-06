package viso.sbeans.impl.util;

import java.nio.ByteBuffer;

public class MessageBuffer {
	
	byte[] message;
	int position;
	int capacity;
	int limit;
	
	public MessageBuffer(int capacity){
		message = new byte[capacity];
		this.capacity = capacity;
		this.position = 0;
		this.limit = 1;
	}
	
	public MessageBuffer(byte[] payload){
		message = payload;
		position = 0;
		this.capacity = payload.length;
		this.limit = payload.length;
	}
	
	public ByteBuffer buffer(){
		byte[] bytes = new byte[2+position];
		bytes[0] = (byte)(position >>> 8);
		bytes[1] = (byte)position;
		System.arraycopy(message, 0, bytes, 2, position);
		return ByteBuffer.wrap(bytes);
	}
	
	public void rewind(){
		position = 0;
	}
	
	public int capacity(){
		return capacity;
	}
	
	public int position(){
		return position;
	}
	
	public int limit(){
		return limit;
	}
	
	public byte getByte(){
		if(position >= limit){
			throw new IndexOutOfBoundsException();
		}
		return message[position++];
	}
	
	public byte[] getBytes(){
		int size = getShort();
		if(size<0 || (size+position > limit)){
			throw new IndexOutOfBoundsException();
		}
		byte[] bytes = new byte[size];
		System.arraycopy(message, position, bytes, 0, size);
		position += size;
		return bytes;
	}
	
	public MessageBuffer putByte(int value){
		if(position == capacity){
			throw new IndexOutOfBoundsException();
		}
		message[position++] = (byte)value;
		this.limit = position==capacity? capacity : (position+1);
		return this;
	}
	
	public MessageBuffer putBytes(byte[] bytes){
		if(position+bytes.length+2 >= capacity){
			throw new IndexOutOfBoundsException();
		}
		putShort(bytes.length);
		System.arraycopy(bytes, 0, message, position, bytes.length);
		this.position += bytes.length;
		this.limit = position==capacity? capacity : (position+1);
		return this;
	}
	
	public short getShort(){
		if(position+2>limit){
			throw new IndexOutOfBoundsException();
		}
		return (short)(message[position++] << 8 + (message[position] & 255));
	}
	
	public MessageBuffer putShort(int value){
		if(position+2 >= capacity){
			throw new IndexOutOfBoundsException();
		}
		message[position++] = (byte)((value >>> 8) & 255);
		message[position++] = (byte)((value) & 255);
		this.limit = position==capacity? capacity : (position+1);
		return this;
	}
	
	public int getUnsignedShort(){
		if(position+2>limit){
			throw new IndexOutOfBoundsException();
		}
		return ((message[position++] & 255) << 8 + (message[position] & 255));
	}
	
	public MessageBuffer putUnsignedShort(int value){
		return putShort(value);
	}
	
	public int getInt(){
		if(position+4>limit){
			throw new IndexOutOfBoundsException();
		}
		return ((message[position++] & 255) << 24 +
				(message[position++] & 255) << 16 +
				(message[position++] & 255) << 8 +
				(message[position] & 255));
	}
	
	public MessageBuffer putInt(int value){
		if(position+4 >= capacity){
			throw new IndexOutOfBoundsException();
		}
		message[position++] = (byte)(value >>> 24);
		message[position++] = (byte)(value >>> 16);
		message[position++] = (byte)(value >>> 8 );
		message[position++] = (byte)(value);
		this.limit = position==capacity? capacity : (position+1);
		return this;
	}
	
	public long getLong(){
		if(position+8>limit){
			throw new IndexOutOfBoundsException();
		}
		return ((long)(message[position++] & 255) << 56 +
				(long)(message[position++] & 255) << 48 +
				(long)(message[position++] & 255) << 40 +
				(long)(message[position++] & 255) << 32 +
				(long)(message[position++] & 255) << 24 +
				(long)(message[position++] & 255) << 16 +
				(long)(message[position++] & 255) << 8 +
				(long)(message[position] & 255));
	}
	
	public MessageBuffer putLong(long value){
		if(position+8 >= capacity){
			throw new IndexOutOfBoundsException();
		}
		message[position++] = (byte)(value >>> 56);
		message[position++] = (byte)(value >>> 48);
		message[position++] = (byte)(value >>> 40);
		message[position++] = (byte)(value >>> 32);
		message[position++] = (byte)(value >>> 24);
		message[position++] = (byte)(value >>> 16);
		message[position++] = (byte)(value >>> 8 );
		message[position++] = (byte)(value);
		this.limit = position==capacity? capacity : (position+1);
		return this;
	}
}
