package viso.sbeans.task;

import java.util.concurrent.Future;

public interface TaskScheduler {
	public TaskQueue createTaskQueue();
	public Future<?> scheduleTask(Task task);
	public Future<?> scheduleTask(Task task, final long delay, final long period);
	public void shutdown();
}
