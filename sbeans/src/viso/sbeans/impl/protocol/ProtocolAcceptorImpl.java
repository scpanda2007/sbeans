package viso.sbeans.impl.protocol;

import java.nio.channels.Channel;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import viso.sbeans.impl.kernel.TaskSchedulerImpl;
import viso.sbeans.impl.net.AsynchronousMessageChannel;
import viso.sbeans.impl.net.TcpTransport;
import viso.sbeans.impl.util.LogWrapper;
import viso.sbeans.session.ProtocolAcceptor;
import viso.sbeans.session.SessionAcceptor;
import viso.sbeans.task.Task;
import viso.sbeans.task.TaskScheduler;

public class ProtocolAcceptorImpl implements ProtocolAcceptor{

	private static final String PCK_NAME = "viso.sbeans.protocol.ProtocolAcceptor";
	private static final LogWrapper logger = new LogWrapper(Logger.getLogger(PCK_NAME));
	
	private TaskScheduler scheduler;
	private TcpTransport transport;
	private SessionAcceptor acceptor;
	private boolean running = false;
	
	public ProtocolAcceptorImpl(Properties property, SessionAcceptor acceptor){
		transport = new TcpTransport(property);
		scheduler = new TaskSchedulerImpl(4,PCK_NAME,logger);
		this.acceptor = acceptor;
		running = true;
	}
	
	public void accept(){
		transport.accept(this);
	}
	
	public void scheduleTask(Task task){
		this.scheduler.scheduleTask(task);
	}
	
	public void receConnection(Channel channel) {
		// TODO Auto-generated method stub
		logger.log(Level.INFO, "收到一条连接{0}", channel);
		AsynchronousMessageChannel messageChannel = (AsynchronousMessageChannel)channel;
		new ProtocolHandlerImpl(this, this.acceptor, messageChannel, logger);
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		if(!running){
			throw new IllegalStateException("协议接收器已经停止");
		}
		scheduler.shutdown();
		transport.shutdown();
		running = false;
	}
	
	public boolean isShutdown(){
		return !running;
	}

}
