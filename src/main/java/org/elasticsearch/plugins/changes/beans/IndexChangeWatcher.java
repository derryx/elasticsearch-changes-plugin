package org.elasticsearch.plugins.changes.beans;

import java.util.concurrent.Semaphore;

public class IndexChangeWatcher {
	String indexName;
	Change change;
	Semaphore barrier;
	
	public IndexChangeWatcher() {
		barrier=new Semaphore(0);
	}
	
	public void permit() {
		barrier.release();
	}
	
	public void aquire() throws InterruptedException {
		barrier.acquire();
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
