package org.dkf.jed2k.test.kad;

import org.dkf.jed2k.Time;
import org.dkf.jed2k.kad.IndexedImpl;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadSearchEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Random;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by inkpot on 21.01.2017.
 */
public class IndexedTest {

    private final Random rnd = new Random();
    private IndexedImpl dictMany = new IndexedImpl();
    KadId keywords[] = {new KadId(Hash.LIBED2K), new KadId(Hash.EMULE), new KadId(Hash.TERMINAL)};

    @Before
    public void setUp() {
        Time.updateCachedTime();
    }

    @Test
    public void trivialMassiveTest() {
        IndexedImpl dict = new IndexedImpl();

        for(int i = 0; i < 100; ++i) {
            int kp = dict.addKeyword(new KadId(Hash.random(false)), new KadSearchEntry(new KadId(Hash.random(false))), 0);
            assertTrue(kp >= 0);
            assertTrue(kp < 100);
            int sp = dict.addSource(new KadId(Hash.random(false)), new KadSearchEntry(new KadId(Hash.random(false))), 0);
            assertTrue(sp >= 0);
            assertTrue(sp < 100);
            assertTrue(dict.getKeywordsCount() > 0);
            assertTrue(dict.getSourcesCount() > 0);
        }
    }

    @Test
    public void testAddingManySourcesToFewKeywords() {
        for(int i = 0; i < IndexedImpl.KAD_MAX_KEYWORD_FILES*2; i++) {
            dictMany.addKeyword(keywords[rnd.nextInt(3)], new KadSearchEntry(new KadId(Hash.random(false))), Time.currentTime());
        }

        assertEquals(IndexedImpl.KAD_MAX_KEYWORD_FILES, dictMany.getTotalFiles());
        assertEquals(keywords.length, dictMany.getKeywordsCount());

        int total = 0;
        for(final KadId id: keywords) {
            Collection<IndexedImpl.Published> files = dictMany.getFileByHash(id);
            assertTrue(files != null);
            total += files.size();
        }

        assertEquals(IndexedImpl.KAD_MAX_KEYWORD_FILES, total);
    }

    @Test
    public void testAddingManyFileSourcesToFewSrcs() {
        for(int i = 0; i < IndexedImpl.KAD_MAX_SOURCES*2; i++) {
            dictMany.addSource(keywords[rnd.nextInt(3)]
                    , new KadSearchEntry(new KadId(Hash.random(false)))
                    , Time.currentTime());
        }

        assertEquals(IndexedImpl.KAD_MAX_SOURCES, dictMany.getTotalSources());
        assertEquals(keywords.length, dictMany.getSourcesCount());

        int total = 0;
        for(final KadId id: keywords) {
            Collection<IndexedImpl.Published> files = dictMany.getSourceByHash(id);
            assertTrue(files != null);
            total += files.size();
        }

        assertEquals(IndexedImpl.KAD_MAX_SOURCES, total);
    }
}
