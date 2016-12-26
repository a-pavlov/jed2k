package org.dkf.jed2k.kad;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.dkf.jed2k.Session;
import org.dkf.jed2k.protocol.kad.KadNodesDat;

import java.lang.ref.WeakReference;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by inkpot on 26.12.2016.
 */
@Slf4j
public class Initiator implements Runnable {
    private static final String sourceUrl = "http://server-met.emulefuture.de/download.php?file=nodes.dat";
    private WeakReference<Session> session;

    public Initiator(final Session ses) {
        session = new WeakReference<Session>(ses);
    }

    @Override
    public void run() {
        Session s = session.get();
        if (s != null) {
            try {
                DhtTracker tracker = s.getDhtTracker();
                if (tracker != null && tracker.needBootstrap()) {
                    byte[] data = IOUtils.toByteArray(new URI(sourceUrl));
                    ByteBuffer buffer = ByteBuffer.wrap(data);
                    log.debug("[initiator] downloaded nodes.dat size {}", buffer.remaining());
                    buffer.order(ByteOrder.LITTLE_ENDIAN);
                    KadNodesDat nodes = new KadNodesDat();
                    nodes.get(buffer);
                    tracker.addKadEntries(nodes.getContacts());
                    tracker.addKadEntries(nodes.getBootstrapEntries().getList());
                }
            } catch(Exception e) {
                log.error("[initiator] unable to initiate DHT due to {}", e);
            }
        }
    }
}
