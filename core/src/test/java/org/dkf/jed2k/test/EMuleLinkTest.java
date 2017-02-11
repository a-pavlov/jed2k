package org.dkf.jed2k.test;

import org.dkf.jed2k.EMuleLink;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * Created by inkpot on 21.08.2016.
 */
public class EMuleLinkTest {

    EMuleLink[] ltemplate;

    @Before
    public void prepareTemplate() {
        ltemplate = new EMuleLink[4];
        ltemplate[0] = new EMuleLink(Hash.fromString("31D6CFE0D16AE931B73C59D7E0C089C0"), 100L, "some_file", EMuleLink.LinkType.FILE);
        ltemplate[1] = new EMuleLink(Hash.fromString("DB48A1C00CC972488C29D3FEC9F16A79"), 10L, "more2", EMuleLink.LinkType.FILE);
        ltemplate[2] = new EMuleLink(Hash.fromString("DB48A1C00CC972488C29D3FEC9F16A79"), 0L, "more1", EMuleLink.LinkType.FILE);
        ltemplate[3] = new EMuleLink(Hash.fromString("6462EAFF860B98A0592BB0284225F85B"), 1568L, "Code Geass.emulecollection", EMuleLink.LinkType.FILE);
    }

    @Test
    public void testNormalLinkParsing() throws JED2KException {
        for(int i = 0; i < ltemplate.length; ++i) {
            switch (i) {
                case 0:
                    assertEquals(ltemplate[i], EMuleLink.fromString("ed2k://|file|some%5Ffile|100|31D6CFE0D16AE931B73C59D7E0C089C0|/"));
                    break;
                case 1:
                    assertEquals(ltemplate[i], EMuleLink.fromString("ed2k://|file|more2|10|DB48A1C00CC972488C29D3FEC9F16A79|/"));
                    break;
                case 2:
                    assertEquals(ltemplate[i], EMuleLink.fromString("ed2k://|file|more1|0|DB48A1C00CC972488C29D3FEC9F16A79|/"));
                    break;
                case 3:
                    assertEquals(ltemplate[i], EMuleLink.fromString("ed2k://|file|Code Geass.emulecollection|1568|6462EAFF860B98A0592BB0284225F85B|h=52HRRJC7CCJBUZNP5JM6RQWYEDAM3YQM|/"));
                    break;
            }
        }
    }

    @Test(expected = JED2KException.class)
    public void testShortLink() throws JED2KException {
        EMuleLink.fromString("ed2k://|file|some%5Ffile|31D6CFE0D16AE931B73C59D7E0C089C0|/");
    }

    @Test(expected = JED2KException.class)
    public void testLinkMailformed() throws JED2KException {
        EMuleLink.fromString("et2k://|file|some%5Ffile|332355|31D6CFE0D16AE931B73C59D7E0C089C0|/");
    }

    @Test(expected = JED2KException.class)
    public void testLinkBadSize() throws JED2KException {
        EMuleLink.fromString("ed2k://|file|some%5Ffile|xc45545|31D6CFE0D16AE931B73C59D7E0C089C0|/");
    }

    @Test
    public void testRealLinks() throws JED2KException {
        EMuleLink eml1 = EMuleLink.fromString("ed2k://|file|Ps2%20Game%20Virtual%20Tennis%203.iso|734107648|66DEA14BB64F1FB690735F5322A46ADF|h=TAV3XL3F6MKDYULZG55JKMR6RMTOUVJ7|/");
        assertEquals("Ps2 Game Virtual Tennis 3.iso", eml1.getStringValue());
        EMuleLink eml2 = EMuleLink.fromString("ed2k://%7Cfile|SkypeSetupFull.exe|47026816|3636DF370FFDCC4783252183EC566A8C|h=O4F7O7NL7PWU4ATT3LRXWIG6CQBQXEVS|/");
        assertEquals("SkypeSetupFull.exe", eml2.getStringValue());
    }

    @Test
    public void testServerLink() throws JED2KException {
        EMuleLink server = EMuleLink.fromString("ed2k://|server|91.200.42.119|9939|/");
        assertEquals(EMuleLink.LinkType.SERVER, server.getType());
        assertEquals("91.200.42.119", server.getStringValue());
        assertEquals(9939, server.getNumberValue());
        assertEquals(null, server.getHash());
    }

    @Test
    public void testServersLink() throws JED2KException {
        EMuleLink server = EMuleLink.fromString("ed2k://|serverlist|http://upd.emule-security.org/server.met|/");
        assertEquals(EMuleLink.LinkType.SERVERS, server.getType());
        assertEquals("http://upd.emule-security.org/server.met", server.getStringValue());
        assertEquals(0, server.getNumberValue());
        assertEquals(null, server.getHash());
    }

    @Test(expected = JED2KException.class)
    public void testNumberFormatErrorInServerLink() throws JED2KException {
        EMuleLink.fromString("ed2k://|server|91.200.42.119|9939x|/");
    }

    @Test
    public void testNodesLink() throws JED2KException {
        EMuleLink nodes = EMuleLink.fromString("ed2k://|nodeslist|http://upd.emule-security.net/nodes.dat|/");
        assertEquals(EMuleLink.LinkType.NODES, nodes.getType());
        assertEquals("http://upd.emule-security.net/nodes.dat", nodes.getStringValue());
        assertEquals(0, nodes.getNumberValue());
        assertEquals(null, nodes.getHash());
    }
}
