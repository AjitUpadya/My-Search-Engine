package com.ucr.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Frontier implements Queue<String> {

	public ConcurrentLinkedQueue<String> getQueue() {
		return queue;
	}

	public void setQueue(ConcurrentLinkedQueue<String> queue) {
		this.queue = queue;
	}
	String seedUrl;
	ConcurrentLinkedQueue<String> queue;
	
	public Frontier(String seedUrl) {
		queue = new ConcurrentLinkedQueue<String>();
		init(seedUrl);
	}
	
	private void init(String seedUrl) {
		queue.add(seedUrl);
	}
	
	public boolean addAll(Collection<? extends String> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public void clear() {
		// TODO Auto-generated method stub
		
	}

	public boolean contains(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean containsAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	public Iterator<String> iterator() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean remove(Object arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean removeAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean retainAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public int size() {
		// TODO Auto-generated method stub
		return queue.size();
	}

	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	public <T> T[] toArray(T[] arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean add(String e) {
		queue.add(e);
		return true;
	}

	public String element() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean offer(String e) {
		// TODO Auto-generated method stub
		return false;
	}

	public String peek() {
		// TODO Auto-generated method stub
		return this.queue.peek();
	}

	public String poll() {
		// TODO Auto-generated method stub
		return null;
	}

	public String remove() {
		String url;
		if(!queue.isEmpty()) {
			url = queue.remove();
			return url;
		}
		return null;
	}

}
