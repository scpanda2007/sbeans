package viso.sbeans.util.test;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import viso.sbeans.impl.util.MessageBuffer;

public class TestMessageBuffer {
	
	@Test
	public void testByte(){
		MessageBuffer buffer = new MessageBuffer(1024);
		byte a = (byte)0xcd;
		buffer.putByte(a).rewind();
		byte b = buffer.getByte();
		System.out.println(a+">>>>>"+b);
		assertEquals(a,b);
	}
	
	@Test
	public void testInt(){
		MessageBuffer buffer = new MessageBuffer(1024);
		int a = 0xabcd;
		buffer.putInt(a).rewind();
		int b = buffer.getInt();
		System.out.println(">>>>>"+b);
		assertEquals(a,b);
	}
	
	@Test
	public void testShort(){
		MessageBuffer buffer = new MessageBuffer(1024);
		short a = (short)0xabcd;
		buffer.putShort(a).rewind();
		short b = buffer.getShort();
		assertEquals(a,b);
	}
	
	@Test
	public void testUnsignedShort(){
		MessageBuffer buffer = new MessageBuffer(1024);
		int a = 0xabcd;
		buffer.putShort(a).rewind();
		int b = buffer.getUnsignedShort();
		System.out.println(a+">>>>>"+b);
		assertEquals(a,b);
	}
	
	@Test
	public void testUTF(){
		MessageBuffer buffer = new MessageBuffer(1024);
		String testStr = "abcdefghidjklmn≤‚ ‘";
		buffer.putString(testStr);
		buffer.rewind();
		String str = buffer.getString();
		System.out.println(str);
		assertEquals(str,testStr);
	}
}
