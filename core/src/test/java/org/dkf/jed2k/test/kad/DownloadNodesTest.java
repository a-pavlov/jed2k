package org.dkf.jed2k.test.kad;

import org.apache.commons.io.IOUtils;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.kad.KadNodesDat;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static junit.framework.Assert.assertTrue;

/**
 * Created by inkpot on 20.12.2016.
 */
public class DownloadNodesTest {

    private String[] links = {"http://www.nodes-dat.com/dl.php?load=nodes&trace=41173982.9444"};

    @Test
    public void downloadAndParseNodesIntegrationTest() throws JED2KException {
        for(final String link: links) {
            try {
                byte[] data = IOUtils.toByteArray(new URI(link));
                ByteBuffer buffer = ByteBuffer.wrap(data);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                KadNodesDat nodes = new KadNodesDat();
                nodes.get(buffer);
                assertTrue(!nodes.getContacts().isEmpty());
            } catch(URISyntaxException e) {
                throw new JED2KException(ErrorCode.URI_SYNTAX_ERROR);
            } catch(IOException e) {
                throw new JED2KException(ErrorCode.IO_EXCEPTION);
            }
        }
    }
}
