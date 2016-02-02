package org.jed2k;

/**
 * 
 * @author ap197_000
 * base interface for classes which implement second tick feature
 * 
 */
public interface Tickable {
	/**
	 * 
	 * @param tick_interval_ms - inteval since last tick was called in milliseconds
	 */
	public void secondTick(long tick_interval_ms);
}
