package org.dkf.jed2k.test.kad;

import org.dkf.jed2k.kad.IndexedImpl;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.kad.KadId;
import org.junit.Test;

import java.util.Random;

import static junit.framework.Assert.assertTrue;

/**
 * Created by inkpot on 21.01.2017.
 */
public class IndexedTest {

    private final Random rnd = new Random();

    @Test
    public void trivialMassiveTest() {
        IndexedImpl dict = new IndexedImpl();

        for(int i = 0; i < 100; ++i) {
            int kp = dict.addKeyword(new KadId(Hash.random(false)), new KadId(Hash.random(false)), rnd.nextInt(), rnd.nextInt(10000), "some filename", rnd.nextInt(5678900), 0);
            assertTrue(kp >= 0);
            assertTrue(kp < 100);
            int sp = dict.addSource(new KadId(Hash.random(false)), new KadId(Hash.random(false)), rnd.nextInt(), rnd.nextInt(10000), rnd.nextInt(45678), 0);
            assertTrue(sp >= 0);
            assertTrue(sp < 100);
            assertTrue(dict.getKeywordsCount() > 0);
            assertTrue(dict.getSourcesCount() > 0);
        }
    }
}
