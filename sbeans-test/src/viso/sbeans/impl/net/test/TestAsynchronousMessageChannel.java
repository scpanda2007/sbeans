package viso.sbeans.impl.net.test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestAsynchronousMessageChannel {
	InetSocketAddress serverAddress;
	DummyServer server;
	List<DummyClient> clients;
	
	@Before
	public void setUp(){
		serverAddress = new InetSocketAddress("127.0.0.1",12345);
		server = new DummyServer(serverAddress);
		clients = new ArrayList<DummyClient>();
		server.accept();
	}
	
	@After
	public void tearDown(){
		server.shutdown();
		for(DummyClient client : clients){
			client.shutdown();
		}
	}
	
	@Test
	public void testConnect(){
		createClients(1);
		try {
			getClient(0).connect(serverAddress).get();
			Thread.sleep(200L);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test 
	public void testWrite(){
		createClients(1);
		try {
			getClient(0).connect(serverAddress).get();
			Thread.sleep(200L);
			byte sends[] = new byte[512];
			for(int i=0;i<sends.length;i++){
				sends[i] = (byte)sends[i];
			}
			for(int i=0;i<10;i++){
				ByteBuffer buffer = ByteBuffer.allocate(1024);
				buffer.put((byte)2);
				buffer.put(sends);
				buffer.flip();
				getClient(0).write(buffer).get();
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void createClients(int number){
		for(int i=0;i<number;i++){
			clients.add(new DummyClient());
		}
	}
	
	private DummyClient getClient(int index){
		return clients.get(index);
	}
}
