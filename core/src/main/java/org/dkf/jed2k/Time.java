package org.dkf.jed2k;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Time provides feature of cached time(updates about one time in second) and hi resolution time
 * in milliseconds
 */
public class Time {
    /**
     * this value updates every second or more frequently
     */
    public static AtomicLong currentCachedTime = new AtomicLong(currentTimeHiRes());

    /**
     *
     * @return milliseconds from java machine started
     * updated every second or more frequently
     */
    public static long currentTime() {
        return currentCachedTime.longValue();
    }

    /**
     * update global cached time
     */
    public static void updateCachedTime() {
        currentCachedTime.set(currentTimeHiRes());
    }

    /**
     *
     * @return actual time every call
     */
    public static long currentTimeHiRes() {
        return System.nanoTime() / 1000000;
    }

    /**
     *
     * @return current time offset convertable to Date object
     */
    public static long currentTimeMillis() { return System.currentTimeMillis(); }

    public static long minutes(int value) {
        return value*1000*60;
    }

    public static long hours(int value) { return value*1000*3600; }

    public static long seconds(int value) {
        return value*1000;
    }
}
