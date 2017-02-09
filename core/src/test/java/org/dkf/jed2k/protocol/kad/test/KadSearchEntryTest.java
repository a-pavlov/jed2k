package org.dkf.jed2k.protocol.kad.test;

import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.kad.KadSearchEntry;
import org.dkf.jed2k.protocol.tag.Tag;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Created by inkpot on 05.01.2017.
 */
public class KadSearchEntryTest {

    @Test
    public void testEquals() {
        KadSearchEntry e1 = new KadSearchEntry();
        KadSearchEntry e2 = new KadSearchEntry();
        assertEquals(e1, e2);
        e2.getInfo().add(Tag.tag(Tag.FT_DL_PREVIEW, null, 0.1f));
        assertEquals(e1, e2);
        e2.getHash().assign(Hash.EMULE);
        assertFalse(e1.equals(e2));
    }
}
