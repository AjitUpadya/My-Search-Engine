package com.ucr.crawler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.LoggerFactory;

import com.ucr.service.Frontier;

/**
 * 
 * @author ajit
 * This class creates new threads of ParserThread
 *
 */
public class ThreadController {
	Frontier frontier = null;
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MainCrawler.class);
	
	public ThreadController() {}
	
	public synchronized void startThreads(int numOfPages, List<String> seedUrls, String outputDir, int depth) throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newFixedThreadPool(100);
		List<String> list = Collections.synchronizedList(new ArrayList<String>());
		for(String seedUrl : seedUrls) {
			if(frontier == null) {
				frontier = new Frontier(seedUrl);
			} else frontier.getQueue().add(seedUrl);
			
			Runnable thread = new RunnableThread(numOfPages, seedUrl, frontier.getQueue(), list, outputDir, depth);
			executor.execute(thread);
		}
		executor.shutdown();
		
		try {
			executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
		} catch(Exception e) {
			logger.error("Could not complete all threads successfully. Please try again");
		}
	}
}
