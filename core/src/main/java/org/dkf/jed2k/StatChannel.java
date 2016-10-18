package org.dkf.jed2k;

import java.util.Arrays;
import java.util.LinkedList;

public class StatChannel {
	private	long secondCounter = 0;
	private long totalCounter = 0;
	private long average5Sec = 0;
	private long average30Sec = 0;
	LinkedList<Long> samples = new LinkedList<Long>(Arrays.asList(0L,0L,0L,0L,0L));

	public void add(long count) {
		assert(count >= 0);
		secondCounter += count;
		totalCounter += count;
	}

	public long counter() {
		return secondCounter;
	}

	public long total() {
		return totalCounter;
	}

	public long rate() {
		return average5Sec;
	}

	public long lowPassRate() {
		return average30Sec;
	}

	public void clear() {
		secondCounter = 0;
		totalCounter = 0;
		average5Sec = 0;
		average30Sec = 0;
		samples  = new LinkedList<Long>(Arrays.asList(0L,0L,0L,0L,0L));
	}

	public StatChannel add(StatChannel s) {
		assert(secondCounter >= 0);
		assert(totalCounter >= 0);
		assert(s.secondCounter >= 0);
		secondCounter += s.secondCounter;
		totalCounter += s.totalCounter;
		assert(secondCounter >= 0);
		assert(totalCounter >= 0);
		return this;
	}

	void secondTick(long timeIntervalMS) {
		long sample = (secondCounter * 1000) / timeIntervalMS;
	    assert(sample >= 0);
	    samples.push(sample);
	    samples.removeLast();
	    long sum = 0;
	    for(long l: samples) {
	    	sum += l;
	    }
	    assert(samples.size() == 5);
	    average5Sec = sum / 5;
	    //m_5_sec_average = size_type(m_5_sec_average) * 4 / 5 + sample / 5;
	    average30Sec = average30Sec * 29 / 30 + sample / 30;
	    secondCounter = 0;
	}
}
