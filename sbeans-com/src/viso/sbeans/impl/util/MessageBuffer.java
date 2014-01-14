package viso.sbeans.impl.util;

import java.io.UTFDataFormatException;
import java.nio.ByteBuffer;

public class MessageBuffer {

	byte[] message;
	int position;
	int capacity;
	int limit;

	public MessageBuffer(int capacity) {
		message = new byte[capacity];
		this.capacity = capacity;
		this.position = 0;
		this.limit = 1;
	}

	public MessageBuffer(byte[] payload) {
		message = payload;
		position = 0;
		this.capacity = payload.length;
		this.limit = payload.length;
	}

	public ByteBuffer buffer() {
		byte[] bytes = new byte[2 + position];
		bytes[0] = (byte) (position >>> 8);
		bytes[1] = (byte) position;
		System.arraycopy(message, 0, bytes, 2, position);
		return ByteBuffer.wrap(bytes);
	}

	public void rewind() {
		position = 0;
	}

	public int capacity() {
		return capacity;
	}

	public int position() {
		return position;
	}

	public int limit() {
		return limit;
	}

	public byte getByte() {
		if (position >= limit) {
			throw new IndexOutOfBoundsException();
		}
		return message[position++];
	}

	public byte[] getBytes() {
		int size = getShort();
		if (size < 0 || (size + position > limit)) {
			throw new IndexOutOfBoundsException();
		}
		byte[] bytes = new byte[size];
		System.arraycopy(message, position, bytes, 0, size);
		position += size;
		return bytes;
	}

	public MessageBuffer putByte(int value) {
		if (position == capacity) {
			throw new IndexOutOfBoundsException();
		}
		message[position++] = (byte) value;
		this.limit = position == capacity ? capacity : (position + 1);
		return this;
	}

	public MessageBuffer putBytes(byte[] bytes) {
		if (position + bytes.length + 2 >= capacity) {
			throw new IndexOutOfBoundsException();
		}
		putShort(bytes.length);
		System.arraycopy(bytes, 0, message, position, bytes.length);
		this.position += bytes.length;
		this.limit = position == capacity ? capacity : (position + 1);
		return this;
	}

	public short getShort() {
		if (position + 2 > limit) {
			throw new IndexOutOfBoundsException();
		}
		return  (short)(((message[position++]) << 8) + (message[position++] & 255));
	}

	public MessageBuffer putShort(int value) {
		if (position + 2 >= capacity) {
			throw new IndexOutOfBoundsException();
		}
		message[position++] = (byte) ((value >>> 8) & 255);
		message[position++] = (byte) ((value) & 255);
		this.limit = position == capacity ? capacity : (position + 1);
		return this;
	}

	public int getUnsignedShort() {
		if (position + 2 > limit) {
			throw new IndexOutOfBoundsException();
		}
		return (((message[position++] & 255) << 8) + (message[position++] & 255));
	}

	public MessageBuffer putUnsignedShort(int value) {
		return putShort(value);
	}
	
	public byte[] rawdata(){
		return message;
	}
	
	public int getInt() {
		if (position + 4 > limit) {
			throw new IndexOutOfBoundsException();
		}
		return (((message[position++] & 255) << 24)
				+((message[position++] & 255) << 16)
				+((message[position++] & 255) << 8)
				+(message[position++] & 255));
	}

	public MessageBuffer putInt(int value) {
		if (position + 4 >= capacity) {
			throw new IndexOutOfBoundsException();
		}
		message[position++] = (byte) (value >>> 24);
		message[position++] = (byte) (value >>> 16);
		message[position++] = (byte) (value >>> 8);
		message[position++] = (byte) (value);
		this.limit = position == capacity ? capacity : (position + 1);
		return this;
	}

	public long getLong() {
		if (position + 8 > limit) {
			throw new IndexOutOfBoundsException();
		}
		return ((long) ((message[position++] & 255) << 56) + 
				(long) ((message[position++] & 255) << 48) + 
				(long) ((message[position++] & 255) << 40) + 
				(long) ((message[position++] & 255) << 32) + 
				(long) ((message[position++] & 255) << 24) + 
				(long) ((message[position++] & 255) << 16) + 
				(long) ((message[position++] & 255) << 8) + 
				(long) ((message[position++] & 255)));
	}

	public MessageBuffer putLong(long value) {
		if (position + 8 >= capacity) {
			throw new IndexOutOfBoundsException();
		}
		message[position++] = (byte) (value >>> 56);
		message[position++] = (byte) (value >>> 48);
		message[position++] = (byte) (value >>> 40);
		message[position++] = (byte) (value >>> 32);
		message[position++] = (byte) (value >>> 24);
		message[position++] = (byte) (value >>> 16);
		message[position++] = (byte) (value >>> 8);
		message[position++] = (byte) (value);
		this.limit = position == capacity ? capacity : (position + 1);
		return this;
	}

	/**
	 * 返回一个utf-8编码的字符串的byte串大小+2(用于存放byte串大小)
	 */
	public static int getSize(String str) {

		// Note: code adapted from java.io.DataOutputStream.writeUTF
		int utfLen = 0;

		for (int i = 0; i < str.length(); i++) {
			int c = str.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				utfLen++;
			} else if (c > 0x07FF) {
				utfLen += 3;
			} else {
				utfLen += 2;
			}
		}

		return utfLen + 2;
	}

	public MessageBuffer putString(String str) {

		// Note: code adapted from java.io.DataOutputStream.writeUTF

		int size = getSize(str);
		if (position + size > capacity) {
			throw new IndexOutOfBoundsException();
		}

		/*
		 * Put length of modified UTF-8 encoded string, as two bytes.
		 */
		putShort(size - 2);

		/*
		 * Now, encode string, and put in buffer.
		 */
		int strlen = str.length();
		int i = 0;
		for (i = 0; i < strlen; i++) {
			char c = str.charAt(i);
			if (!((c >= 0x0001) && (c <= 0x007F))) {
				break;
			}
			message[position++] = (byte) c;
		}

		for (; i < strlen; i++) {
			char c = str.charAt(i);
			if ((c >= 0x0001) && (c <= 0x007F)) {
				message[position++] = (byte) c;

			} else if (c > 0x07FF) {
				message[position++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
				message[position++] = (byte) (0x80 | ((c >> 6) & 0x3F));
				message[position++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			} else {
				message[position++] = (byte) (0xC0 | ((c >> 6) & 0x1F));
				message[position++] = (byte) (0x80 | ((c >> 0) & 0x3F));
			}
		}

		/*
		 * Adjust limit, because we didn't use putByte.
		 */
		limit = (position == capacity ? position : position + 1);

		return this;
	}

	public String getString() {

		// Note: code adapted from java.io.DataInputStream.readUTF

		if (position + 2 > limit) {
			throw new IndexOutOfBoundsException();
		}

		int savePos = position;

		/*
		 * Get length of UTF encoded string.
		 */
		int utfLen = getUnsignedShort();
		int utfEnd = utfLen + position;
		if (utfEnd > limit) {
			position = savePos;
			throw new IndexOutOfBoundsException();
		}

		/*
		 * Decode string.
		 */
		char[] chars = new char[utfLen * 2];
		int c, char2, char3;
		int index = 0;

		while (position < utfEnd) {
			c = message[position] & 0xff;
			if (c > 127) {
				break;
			}
			position++;
			chars[index++] = (char) c;
		}

		try {
			while (position < utfEnd) {
				c = message[position] & 0xff;

				switch (c >> 4) {

				case 0:
				case 1:
				case 2:
				case 3:
				case 4:
				case 5:
				case 6:
				case 7:
					/* 0xxxxxxx */
					position++;
					chars[index++] = (char) c;
					break;

				case 12:
				case 13:
					/* 110x xxxx 10xx xxxx */
					position += 2;
					if (position > utfEnd) {
						throw new UTFDataFormatException(
								"malformed input: partial character at end");
					}
					char2 = message[position - 1];
					if ((char2 & 0xC0) != 0x80) {
						throw new UTFDataFormatException(
								"malformed input around byte " + position);
					}
					chars[index++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
					break;

				case 14:
					/* 1110 xxxx 10xx xxxx 10xx xxxx */
					position += 3;
					if (position > utfEnd) {
						throw new UTFDataFormatException(
								"malformed input: partial character at end");
					}
					char2 = message[position - 2];
					char3 = message[position - 1];
					if (((char2 & 0xC0) != 0x80) || ((char3 & 0xC0) != 0x80)) {
						throw new UTFDataFormatException(
								"malformed input around byte " + (position - 1));
					}
					chars[index++] = (char) (((c & 0x0F) << 12)
							| ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0));
					break;

				default:
					/* 10xx xxxx, 1111 xxxx */
					throw new UTFDataFormatException(
							"malformed input around byte " + position);
				}
			}

		} catch (UTFDataFormatException e) {
			// restore position
			position = savePos;
			throw (RuntimeException) (new RuntimeException()).initCause(e);
		}
		// The number of chars produced may be less than utfLen
		return new String(chars, 0, index);
	}
}
