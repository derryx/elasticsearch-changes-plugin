package org.elasticsearch.plugins.changes.tests;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.elasticsearch.plugins.changes.util.ConcurrentCircularBuffer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ConcurrentCircularBufferTest {
	private static final int THREAD_COUNT=4;
	ExecutorService threadpool;
	
	@Before
	public void setUp() {
		threadpool=Executors.newFixedThreadPool(THREAD_COUNT);
	}
	
	@Test
	public void doConcurrentTest() {
		ConcurrentCircularBuffer<Integer> buffer=new ConcurrentCircularBuffer<Integer>(Integer.class, 100);
		
		for (int i=0;i<THREAD_COUNT;++i) {
			threadpool.submit(new InsertCallable(buffer));
		}
		for (int i=0;i<30;++i) {
			List<Integer> snapshot=buffer.snapshot();
			System.out.println(snapshot);
			System.out.println(buffer.getModificationCount());
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	@After
	public void tearDown() {
		threadpool.shutdownNow();
	}
}

class InsertCallable implements Callable<Boolean>{
	ConcurrentCircularBuffer<Integer> buffer;
	Random random=new Random();
	
	public InsertCallable(ConcurrentCircularBuffer<Integer> buffer) {
		super();
		this.buffer = buffer;
	}
	
	@Override
	public Boolean call() throws Exception {
		while(true) {
			buffer.add(random.nextInt());
		}
	}
}