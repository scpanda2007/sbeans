package viso.sbeans.impl.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NamedThreadFactory implements ThreadFactory{

	private final String prefix;
	private final AtomicInteger counter;

	private static final LogWrapper logger = new LogWrapper(Logger.getLogger("viso.sbeans.thread"));
	
	public NamedThreadFactory(String name){
		Objects.checkNull("name", name);
		this.prefix = name;
		counter = new AtomicInteger(0);
	}
	
	@Override
	public Thread newThread(Runnable arg0) {
		// TODO Auto-generated method stub
		String thread = prefix+"--"+counter.getAndIncrement();
		logger.log(Level.INFO, "创建线程:"+thread);
		return new Thread(arg0,thread);
	}

}
