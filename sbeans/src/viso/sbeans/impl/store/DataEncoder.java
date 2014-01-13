package viso.sbeans.impl.store;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DataEncoder {
	public static short decodeShort(byte[] data,int off){
		int value = (data[off++] & 0xff) ^ 0x80;
		value <<= 8;
		value += (data[off] & 0xff);
		return (short)value;
	}
	
	public static byte[] encodeShort(short number,int off){
		byte bytes[] = new byte[off+number];
		bytes[off++]= (byte)(number>>>8);
		bytes[off] = (byte)number;
		return bytes;
	}
	
	public static int decodeInt(byte[] data,int off){
		int value = (data[off++] & 0xff) ^ 0x80;
		value <<= 8;
		value += (data[off++] & 0xff);
		value <<= 8;
		value += (data[off++] & 0xff);
		value <<= 8;
		value += (data[off] & 0xff);
		return value;
	}
	
	public static byte[] encodeInt(int number, int off){
		byte bytes[] = new byte[4+off];
		bytes[off++] = (byte)(((number>>>24)) ^ 0x80); 
		bytes[off++] = (byte)(number>>>16);
		bytes[off++] = (byte)(number>>>8);
		bytes[off++] = (byte)(number);
		return bytes;
	}
	
	public static void encodeVarInt(int number, OutputStream out)
			throws IOException {
		while(true) {
			byte n = (byte) (number | 0x80);
			number >>>= 7;
			if(number==0){
				n &= 0x7f;
				out.write(n);
				break;
			}
			out.write(n);
		} 
	}
	
	public static int decodeVarInt(InputStream in) throws IOException{
		int value = 0;
		int n = in.read();
		if(n==-1){
			throw new IOException("decodeVarInt at End of File.");
		}
		
		for(int i=0;i<5;i++){
			value |= ((n & 0x7f) << 7*i);
			if((n&0x80)==0){
				break;
			}
			n = in.read();
		}
		return value;
	}
	
	public static long decodeLong(byte[] bytes){
		if(bytes.length > 8){
			throw new IllegalArgumentException("Error param length.");
		}
		long n = (bytes[0] & 0xff) ^ 0x80;
		n <<= 8;
		n += (bytes[1] & 0xff);
		n <<= 8;
		n += (bytes[2] & 0xff);
		n <<= 8;
		n += (bytes[3] & 0xff);
		n <<= 8;
		n += (bytes[4] & 0xff);
		n <<= 8;
		n += (bytes[5] & 0xff);
		n <<= 8;
		n += (bytes[6] & 0xff);
		n <<= 8;
		n += (bytes[7] & 0xff);
		return n;
	}
	
	public static byte[] encodeLong(long number){
		byte bytes[] = new byte[8];
		bytes[0] = (byte)((number >>> 56) ^ 0x80);
		bytes[1] = (byte)(number >>> 48);
		bytes[2] = (byte)(number >>> 40);
		bytes[3] = (byte)(number >>> 32);
		bytes[4] = (byte)(number >>> 24);
		bytes[5] = (byte)(number >>> 16);
		bytes[6] = (byte)(number >>> 8);
		bytes[7] = (byte)(number);
		return bytes;
	}
}
