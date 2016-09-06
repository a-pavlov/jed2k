package org.dkf.jed2k.protocol.test;

import org.dkf.jed2k.ResourceFile;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.server.ServerMet;
import org.junit.Test;

import java.nio.ByteBuffer;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by ap197_000 on 06.09.2016.
 */
public class ServerMetTest {

    @Test
    public void testSerialization() throws JED2KException {
        ResourceFile rf = new ResourceFile();
        ByteBuffer buffer = rf.read("server.met", null);
        assertTrue(buffer.hasRemaining());
        ServerMet sm = new ServerMet();
        sm.get(buffer);
        assertFalse(sm.getServers().isEmpty());
    }
}