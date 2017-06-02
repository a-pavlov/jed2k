package org.dkf.jed2k.test;

import org.dkf.jed2k.Statistics;
import org.junit.Test;

import java.util.Random;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by inkpot on 15.09.2016.
 */
public class StatisticsTest {
    public Random rnd = new Random();

    @Test
    public void testFading() {
        Statistics stat = new Statistics();
        assertEquals(0L, stat.totalPayloadDownload());
        assertEquals(0L, stat.totalProtocolDownload());
        stat.receiveBytes(100L, 20000L);
        assertEquals(100L, stat.totalProtocolDownload());
        assertEquals(20000L, stat.totalPayloadDownload());
        stat.secondTick(1000);

        for(int i = 0; i < 10; ++i) {
            stat.receiveBytes(rnd.nextInt(2000), rnd.nextInt(30000));
            stat.secondTick(1000 + rnd.nextInt(30));
        }

        assertTrue(stat.downloadRate() > 0);
        assertTrue(stat.downloadPayloadRate() > 0);

        for(int i = 0; i < 10; ++i) {
            stat.secondTick(1000 + rnd.nextInt(33));
        }

        assertEquals(0L, stat.downloadPayloadRate());
        assertEquals(0L, stat.downloadRate());
    }
}
