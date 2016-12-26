package org.dkf.jed2k.kad;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
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
    private WeakReference<DhtTracker> dht;

    public Initiator(final DhtTracker tracker) {
        dht = new WeakReference<DhtTracker>(tracker);
    }

    @Override
    public void run() {
        DhtTracker tracker = dht.get();
        if (tracker != null) {
            try {
                byte[] data = IOUtils.toByteArray(new URI(sourceUrl));
                ByteBuffer buffer = ByteBuffer.wrap(data);
                log.debug("[initiator] downloaded nodes.dat size {}", buffer.remaining());
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                KadNodesDat nodes = new KadNodesDat();
                nodes.get(buffer);
                tracker.addKadEntries(nodes.getContacts());
                tracker.addKadEntries(nodes.getBootstrapEntries().getList());
            } catch(Exception e) {
                log.error("[initiator] unable to initiate DHT due to {}", e);
            }
        }
    }
}
