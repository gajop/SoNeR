package jp.ac.iwatepu.soner.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class ThreadPool {

	private ArrayBlockingQueue<Runnable> taskQueue = null;
	private List<PoolThread> threads = new ArrayList<PoolThread>();	
	private boolean isBusy = false;
	int busyCount = 0;

	public ThreadPool(int noOfThreads, int maxNoOfTasks){
		taskQueue = new ArrayBlockingQueue<Runnable>(maxNoOfTasks);

		for(int i=0; i<noOfThreads; i++){
			threads.add(new PoolThread(this));
		}
		for(PoolThread thread : threads){
			thread.start();
		}
	}

	public synchronized void execute(Runnable task){
		try {
			isBusy = true;
			this.taskQueue.put(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
		if (busyCount == 0 && taskQueue.isEmpty()) {
			isBusy = false;
		}
	}
	
	public boolean isBusy() {
		return isBusy;
	}
}