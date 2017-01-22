package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt32;
import org.dkf.jed2k.protocol.tag.Tag;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 06.09.2016.
 */
public class ServerMet implements Serializable {
    private static final byte  MET_HEADER  = 0x0E;
    private static final byte  MET_HEADER_WITH_LARGEFILES  = 0x0F;

    private byte header = MET_HEADER;
    private Container<UInt32, ServerMet.ServerMetEntry>   servers = Container.makeInt(ServerMet.ServerMetEntry.class);

    public void addServer(final ServerMetEntry entry) {
        servers.add(entry);
    }

    public static class ServerMetEntry implements Serializable {
        final Endpoint endpoint = new Endpoint();
        final Container<UInt32, Tag> tags = Container.makeInt(Tag.class);

        public static ServerMetEntry create(int ip, int port, final String name, final String description) throws JED2KException {
            assert ip != 0;
            assert port != 0;
            assert name != null && !name.isEmpty();
            ServerMetEntry e = new ServerMetEntry();
            e.endpoint.assign(ip, port);
            e.tags.add(Tag.tag(Tag.FT_FILENAME, null, name));
            if (description != null && !description.isEmpty()) {
                e.tags.add(Tag.tag(Tag.ST_DESCRIPTION, null, description));
            }

            return e;
        }

        public static ServerMetEntry create(final String host, int port, final String name, final String description) throws JED2KException {
            assert host != null && !host.isEmpty();
            assert port != 0;
            assert name != null && !name.isEmpty();
            ServerMetEntry e = new ServerMetEntry();
            e.endpoint.assign(0, port);

            e.tags.add(Tag.tag(Tag.ST_PREFERENCE, null, host));
            e.tags.add(Tag.tag(Tag.FT_FILENAME, null, name));
            if (description != null && !description.isEmpty()) {
                e.tags.add(Tag.tag(Tag.ST_DESCRIPTION, null, description));
            }

            return e;
        }

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

        public String getName() {
            for(Tag t: tags) {
                if (t.getId() == Tag.FT_FILENAME) {
                    return t.asStringValue();
                }
            }

            return "";
        }

        public String getDescription() {
            for(Tag t: tags) {
                if (t.getId() == Tag.ST_DESCRIPTION) return t.asStringValue();
            }

            return "";
        }

        public String getHost() {
            String host = "";
            if (endpoint.getIP() == 0) {
                for(Tag t: tags) {
                    if (t.getId() == Tag.ST_PREFERENCE) {
                        host = t.asStringValue();
                        break;
                    }
                }
            } else {
                host = Utils.int2Address(endpoint.getIP()).getHostAddress();
            }

            return host;
        }

        public int getPort() {
            return endpoint.getPort();
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

    public final Container<UInt32, ServerMetEntry> getServers() {
        return servers;
    }
}
