package org.dkf.jed2k.test;

import org.dkf.jed2k.PeerConnection;
import org.dkf.jed2k.Session;
import org.dkf.jed2k.Settings;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.client.HelloAnswer;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.mockito.Mockito.when;

/**
 * Created by inkpot on 26.08.2016.
 */
public class PeerConnectionTest {
    private final Logger log = LoggerFactory.getLogger(PeerConnectionTest.class);

    /**
     * trivial max packet size detector
     * currently max size in outgoing order is HelloAnswer packet, so check his size
     * set outgoing buffer size to twice of max packet size
     * @throws JED2KException
     * @throws IOException
     */
    @Test
    public void testMaxOutgoingPacketSize() throws JED2KException, IOException {
        Settings s = new Settings();
        Session session = Mockito.mock(Session.class);
        when(session.getUserAgent()).thenReturn(s.userAgent);
        when(session.getClientId()).thenReturn(100500);
        when(session.getListenPort()).thenReturn((short)4661);
        when(session.getClientName()).thenReturn("long client name here"); // TODO - add limits checking later
        when(session.getModName()).thenReturn("long mod name");
        when(session.getAppVersion()).thenReturn(s.version);
        when(session.getCompressionVersion()).thenReturn(1);
        when( session.getModMajorVersion()).thenReturn(1);
        when( session.getModMinorVersion()).thenReturn(2);
        when( session.getModBuildVersion()).thenReturn(3);

        //SocketChannel ss = Mockito.mock(SocketChannel.class);
        //doNothing().when(ss).register(any(Selector.class), SelectionKey.OP_CONNECT, any(Object.class));
        //doNothing().when(ss).configureBlocking(false);
        PeerConnection c = PeerConnection.make(null, session);

        HelloAnswer ha = new HelloAnswer();
        log.info("hello answer size {}", c.prepareHello(ha).bytesCount());
    }
}
