package org.jed2k;

public class Time {
    /**
     * 
     * @return milliseconds from java machine started
     */
    public static long currentTime() {
        return System.nanoTime() / 1000000;
    }
}
