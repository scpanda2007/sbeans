package viso.sbeans.task;

import java.util.concurrent.Future;

public interface TaskScheduler {
	public TaskQueue createTaskQueue();
	public Future<?> scheduleTask(Task task);
}
