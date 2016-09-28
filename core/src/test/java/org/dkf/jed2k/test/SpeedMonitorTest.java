package org.dkf.jed2k.test;

import org.dkf.jed2k.SpeedMonitor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by inkpot on 11.09.2016.
 */
public class SpeedMonitorTest {

    @Test
    public void testSpeedMon() {
        SpeedMonitor sm = new SpeedMonitor(5);
        assertEquals(-1, sm.averageSpeed());
        sm.addSample(10);
        assertEquals(10, sm.averageSpeed());
        sm.addSample(20);
        assertEquals(15, sm.averageSpeed());
        sm.addSample(30);
        assertEquals(20, sm.averageSpeed());
        sm.addSample(20);
        assertEquals(20, sm.averageSpeed());
        sm.addSample(40);
        assertEquals(24, sm.averageSpeed());
        assertEquals(5, sm.getNumSamples());
        sm.addSample(40);
        assertEquals(30, sm.averageSpeed());
        assertEquals(5, sm.getNumSamples());
        sm.addSample(10);
        assertEquals(28, sm.averageSpeed());
        assertEquals(5, sm.getNumSamples());
        sm.clear();
        assertEquals(-1, sm.averageSpeed());
        assertEquals(0, sm.getNumSamples());
        sm.addSample(100);
        sm.addSample(200);
        sm.addSample(300);
        assertEquals(200, sm.averageSpeed());
    }

}
