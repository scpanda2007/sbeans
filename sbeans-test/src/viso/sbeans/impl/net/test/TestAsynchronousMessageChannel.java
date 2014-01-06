package viso.sbeans.impl.net.test;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import viso.sbeans.impl.util.MessageBuffer;
import viso.sbeans.session.SessionHeader;

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
			Thread.sleep(1000L);
			byte sends[] = new byte[512];
			for(int i=0;i<sends.length;i++){
				sends[i] = (byte)sends[i];
			}
			MessageBuffer buffer = new MessageBuffer(1024);
			buffer.putByte(SessionHeader.kLoginRequest);
			getClient(0).write(buffer).get();
			Thread.sleep(1000L);
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
