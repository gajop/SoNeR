package jp.ac.iwatepu.soner.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ThreadPool {

	private ArrayBlockingQueue<Runnable> taskQueue = null;
	private List<PoolThread> threads = new ArrayList<PoolThread>();	
	private int busyCount = 0;
	private static final Logger logger = LogManager.getLogger("ThreadPool");
	
	public ThreadPool(int noOfThreads, int maxNoOfTasks){
		taskQueue = new ArrayBlockingQueue<Runnable>(maxNoOfTasks);

		for(int i=0; i<noOfThreads; i++){
			threads.add(new PoolThread(this));
		}
		for(PoolThread thread : threads){
			thread.start();
		}
	}

	public synchronized void execute(Runnable task) {
		try {
			this.taskQueue.put(task);
		} catch (InterruptedException e) {
			logger.error(e);
		}
	}

	public synchronized void stop() {
		for(PoolThread thread : threads){
			thread.interruptThread();
		}
	}

	public Runnable take() throws InterruptedException {
		Runnable runnable = taskQueue.take();
		busyCount++;
		return runnable;
	}

	public void done() {
		busyCount--;
	}
	
	public boolean isBusy() {
		return busyCount > 0 || !taskQueue.isEmpty();			
	}
}