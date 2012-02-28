package org.elasticsearch.plugins.changes.beans;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class IndexChangeWatcher {
	String indexName;
	Change change;
	Semaphore barrier;
	long timeout;
	
	public IndexChangeWatcher() {
		this.barrier=new Semaphore(0);
		this.timeout=15*60*1000;
	}
	
	public IndexChangeWatcher(long timeout) {
		this.barrier=new Semaphore(0);
		this.timeout=timeout;
	}
	
	public void permit() {
		barrier.release();
	}
	
	public boolean aquire() throws InterruptedException {
		return barrier.tryAcquire(timeout, TimeUnit.MILLISECONDS);
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	public Change getChange() {
		return change;
	}

	public void setChange(Change change) {
		this.change = change;
	}
}
