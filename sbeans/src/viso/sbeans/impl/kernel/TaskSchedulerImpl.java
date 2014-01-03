package viso.sbeans.impl.kernel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

import viso.sbeans.impl.util.LogWrapper;
import viso.sbeans.impl.util.NamedThreadFactory;
import viso.sbeans.task.Task;
import viso.sbeans.task.TaskQueue;

public class TaskSchedulerImpl {
	
	ExecutorService executor;
	
	private final LogWrapper logger;
	
	public TaskSchedulerImpl(int requestThreads,String packName,final LogWrapper logger){
		executor = Executors.newScheduledThreadPool(requestThreads, new NamedThreadFactory(packName+".taskScheduler"));
		this.logger = logger;
	}
	
	public Future<?> scheduleTask(Task task){
		return executor.submit(new TaskImpl(task, null));
	}
	
	public TaskQueue createTaskQueue(){
		return new TaskQueueImpl();
	}
	
	private class TaskImpl implements Runnable{

		private final Task task;
		private final TaskQueueImpl queue;
		
		public TaskImpl(final Task task, TaskQueueImpl queue){
			this.task = task;
			this.queue = queue;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				task.run();
			}catch(Throwable t){
				logger.logThrow(Level.FINEST, t, "»ŒŒÒ ß∞‹");
			}
			if(queue!=null){
				queue.scheduleNextTask();
			}
		}
		
	}
	
	private class TaskQueueImpl implements TaskQueue{
		
		List<TaskImpl> taskQueue = new ArrayList<TaskImpl>();
		
		boolean running = false;
		
		private Object lock = new Object();
		
		@Override
		public void addTask(Task task) {
			// TODO Auto-generated method stub
			synchronized (lock) {
				if (!running) {
					executor.submit(new TaskImpl(task, this));
					return;
				}
				taskQueue.add(new TaskImpl(task, this));
			}
		}
		
		public void scheduleNextTask(){
			synchronized(lock){
				if(taskQueue.size()==0){
					running = false;
					return;
				}
				executor.submit(taskQueue.remove(0));
			}
		}
		
	}
}
