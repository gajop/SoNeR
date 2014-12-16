package jp.ac.iwatepu.soner.crawler.foaf;

public class PoolThread extends Thread {

	private ThreadPool threadPool = null;
	private boolean isStopped = false;

	public PoolThread(ThreadPool threadPool){
		this.threadPool = threadPool;
	}

	public void run(){
		while (!isStopped()) {
			try{
				Runnable runnable = (Runnable) threadPool.take();
				runnable.run();
				threadPool.done();
			} catch(Exception e){
				//log or otherwise report exception,
				//but keep pool thread alive.
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