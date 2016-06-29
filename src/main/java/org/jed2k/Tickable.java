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
	 * @param tickIntervalMs - inteval since last tick was called in milliseconds
	 */
	public void secondTick(long tickIntervalMs);
}
