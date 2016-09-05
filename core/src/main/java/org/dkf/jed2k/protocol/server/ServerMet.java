package org.dkf.jed2k.protocol.server;

import com.sun.corba.se.spi.activation.Server;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;
import org.dkf.jed2k.protocol.tag.Tag;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 06.09.2016.
 */
public class ServerMet implements Serializable {
    private static final byte  MET_HEADER  = 0x0E;
    private static final byte  MET_HEADER_WITH_LARGEFILES  = 0x0F;

    private byte header = MET_HEADER;
    Container<UInt32, ServerMetEntry>   servers = Container.makeInt(ServerMetEntry.class);

    private class ServerMetEntry implements Serializable {
        final NetworkIdentifier endpoint = new NetworkIdentifier();
        final Container<UInt32, Tag> tags = Container.makeInt(Tag.class);

        @Override
        public ByteBuffer get(ByteBuffer src) throws JED2KException {
            return tags.get(endpoint.get(src));
        }

        @Override
        public ByteBuffer put(ByteBuffer dst) throws JED2KException {
            return tags.put(endpoint.put(dst));
        }

        @Override
        public int bytesCount() {
            return endpoint.bytesCount() + tags.bytesCount();
        }
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        header = src.get();
        if (header != MET_HEADER && header != MET_HEADER_WITH_LARGEFILES) {
            throw new JED2KException(ErrorCode.SERVER_MET_HEADER_INCORRECT);
        }

        return servers.get(src);
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        assert(header == MET_HEADER_WITH_LARGEFILES || header == MET_HEADER);
        dst.put(header);
        return servers.put(dst);
    }

    @Override
    public int bytesCount() {
        return Utils.sizeof(header) + servers.bytesCount();
    }
}
