package org.jed2k;

/**
 * Time provides feature of cached time(updates about one time in second) and hi resolution time
 * in milliseconds
 */
public class Time {
    /**
     * this value updates every second or more frequently
     */
    public static long currentCachedTime = currentTimeHiRes();

    /**
     *
     * @return milliseconds from java machine started
     * updated every second or more frequently
     */
    public static long currentTime() {
        return currentCachedTime;
    }

    /**
     *
     * @return actual time every call
     */
    public static long currentTimeHiRes() {
        return System.nanoTime() / 1000000;
    }
}
