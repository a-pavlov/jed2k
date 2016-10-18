package org.dkf.jed2k;

/**
 * Created by inkpot on 11.09.2016.
 */
public class SpeedMonitor {
    public static long INVALID_SPEED = -1;
    public static long INVALID_ETA = -1;
    private long speedSamples[];
    private int roundRobin = 0;
    private int totalSamples = 0;

    public SpeedMonitor(int samplesLimit) {
        speedSamples = new long[samplesLimit];
    }

    public void addSample(long speedSample) {
        if (roundRobin == speedSamples.length) {
            roundRobin = 0;
            assert totalSamples == speedSamples.length;
        }

        if (roundRobin < speedSamples.length) {
            speedSamples[roundRobin++] = speedSample;
        }

        if (totalSamples != speedSamples.length) totalSamples++;
    }

    public long averageSpeed() {
        if (totalSamples == 0) return INVALID_SPEED;

        long sum = 0;
        for(int i = 0; i < totalSamples; ++i) {
            sum += speedSamples[i];
        }

        return sum/totalSamples;
    }

    public int getNumSamples() {
        return totalSamples;
    }

    public void clear() {
        roundRobin = 0;
        totalSamples = 0;
    }
}
