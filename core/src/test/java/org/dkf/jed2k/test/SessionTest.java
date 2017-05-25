package org.dkf.jed2k.test;

import org.dkf.jed2k.Session;
import org.dkf.jed2k.SessionTrial;
import org.dkf.jed2k.Settings;
import org.dkf.jed2k.TransferHandle;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by inkpot on 24.08.2016.
 */
public class SessionTest {
    private Settings settings;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setupSession() {
        settings = new Settings();
        settings.listenPort = -1;

    }

    @Test
    public void testTransferHandle() throws JED2KException, InterruptedException {
        Session session = new SessionTrial(settings, new LinkedList<Endpoint>());
        session.start();
        TransferHandle handle = session.addTransfer(Hash.EMULE, 1000L, new File("xxx"));
        assertTrue(handle.isValid());
        TransferHandle handle2 = session.addTransfer(Hash.EMULE, 1002L, new File("yyy"));
        assertEquals(1, session.getTransfers().size());
        assertEquals(handle, handle2);
        assertEquals(1000L, handle2.getSize());
        assertEquals("xxx", handle2.getFile().getName());
        assertTrue(handle2.getPeersInfo().isEmpty());
        session.removeTransfer(handle.getHash(), true);
        session.abort();
        session.join();
        assertEquals(0, session.getTransfers().size());
    }

    @Test
    public void testSessionFinish() throws IOException, JED2KException, InterruptedException {
        Session session = new SessionTrial(settings, new LinkedList<Endpoint>());
        session.start();

        TransferHandle h1 = session.addTransfer(Hash.EMULE, 1000L, folder.newFile("femule.dat"));
        assertTrue(h1.isValid());
        TransferHandle h2 = session.addTransfer(Hash.LIBED2K, 1000L, folder.newFile("flibed2k.dat"));
        assertTrue(h2.isValid());

        List<Hash> randomTransfers = new LinkedList<>();

        for(int i = 0; i < 10; i++) {
            TransferHandle h = session.addTransfer(Hash.random(false), 1000L, folder.newFile(String.format("f%d.dat", i)));
            assertTrue(h.isValid());
            randomTransfers.add(h.getHash());
        }

        for(final Hash h: randomTransfers) {
            session.removeTransfer(h, true);
        }

        session.abort();
        session.join();
        assertTrue(session.getTransfers().isEmpty());


    }
}
