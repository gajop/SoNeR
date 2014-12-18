package jp.ac.iwatepu.soner.crawler;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class PoolThread extends Thread {

	private ThreadPool threadPool = null;
	private boolean isStopped = false;
	static final Logger logger = LogManager.getLogger("ThreadPool");

	public PoolThread(ThreadPool threadPool){
		this.threadPool = threadPool;
	}

	public void run(){
		while (!isStopped()) {
			try{
				Runnable runnable = (Runnable) threadPool.take();
				runnable.run();				
			} catch(Exception e){
				logger.error(e);
			} finally {
				threadPool.done();
			}
		}
	}

	public synchronized void interruptThread() {
		isStopped = true;
		this.interrupt(); //break pool thread out of dequeue() call.
	}

	public synchronized boolean isStopped(){
		return isStopped;
	}

}