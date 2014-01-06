package viso.sbeans.impl.net.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;

import viso.sbeans.impl.util.MessageBuffer;

public class DummyClient {
	AsynchronousSocketChannel channel;
	public Future<Void> connect(InetSocketAddress addr){
		try {
			channel = AsynchronousSocketChannel.open();
			return channel.connect(addr);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Future<Integer> write(MessageBuffer message){
		return channel.write(message.buffer());
	}
	
	public void shutdown(){
		if(channel!=null && channel.isOpen()){
			try {
				channel.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}	
