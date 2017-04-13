package org.dkf.jed2k.kad.server;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.ByteBufferInputStream;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.ReqDispatcher;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.*;
import org.dkf.jed2k.protocol.tag.Tag;
import org.postgresql.ds.PGPoolingDataSource;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * Created by apavlov on 06.03.17.
 */
@Slf4j
public class DhtRequestHandler implements Runnable, ReqDispatcher {
    private final Serializable packet;
    private final InetSocketAddress address;
    private final PGPoolingDataSource ds;
    private ByteBuffer buffer;

    public DhtRequestHandler(final Serializable packet
            , final InetSocketAddress address
            , final PGPoolingDataSource ds) {
        this.packet = packet;
        this.address = address;
        this.ds = ds;
    }

    @Override
    public void run() {
        assert packet != null;
        buffer = ByteBuffer.allocate(packet.bytesCount());
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        if (packet instanceof KadDispatchable) {
            ((KadDispatchable)packet).dispatch(this, address);
        } else {
            if (packet instanceof Kad2SearchRes) {

            }
        }
    }

    private void insertSource(final KadId kadId
            , final KadSearchEntry entry
            , final InetSocketAddress address) throws JED2KException {

        Connection conn = null;

        try {

            conn = ds.getConnection();
            if (conn == null) throw new JED2KException(ErrorCode.NO_AVAILABLE_SQL_CONNECTIONS);
            Endpoint ep = Endpoint.fromInet(address);
            int portTcp = 0;
            int portUdp = ep.getPort();
            int srcType = 0;
            int ip = 0;

            for(final Tag t: entry.getInfo()) {
                if (t.getId() == Tag.TAG_SOURCETYPE) {
                    srcType = t.asIntValue();
                }
                else if (t.getId() == Tag.TAG_SOURCEIP) {
                    ip = Utils.ntohl(t.asIntValue());
                }
                else if (t.getId() == Tag.TAG_SOURCEPORT) {
                    portTcp = t.asIntValue();
                }
                else if (t.getId() == Tag.TAG_SOURCEUPORT) {
                    portUdp = t.asIntValue();
                }
            }

            entry.put(buffer);
            buffer.flip();

            ByteBuffer buffer2 = buffer.slice();

            if (ip != 0) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO kad.sources(kad_id, host, port_tcp, port_udp, packet, source_type) " +
                        "VALUES (?, ?::INET, ?, ?, ?, ?) ON CONFLICT ON CONSTRAINT sources_pk DO UPDATE SET last_update = current_timestamp, packet = ?, total_updates = kad.sources.total_updates + 1, source_type = ?");
                ps.setString(1, kadId.toString());
                ps.setString(2, Utils.ip2String(ip));
                ps.setInt(3, portTcp);
                ps.setInt(4, portUdp);
                ps.setBinaryStream(5, new ByteBufferInputStream(buffer));
                ps.setInt(6, srcType);
                ps.setBinaryStream(7, new ByteBufferInputStream(buffer2));
                ps.setInt(8, srcType);
                ps.executeUpdate();
                log.debug("store source from IP: {}", Utils.ip2String(ip));
            } else {
                log.warn("source packet doesn't contain source ip");
            }
        } catch(SQLException e) {
            log.error("SQL exception {}", e);
            throw new JED2KException(ErrorCode.SOURCE_INSERT_SQL_ERROR);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch(SQLException e) {
                    log.warn("close connection error {}", e);
                }
            }
        }
    }

    private void insertKeywords(final KadId kadId
            , final List<KadSearchEntry> keywords
            , final InetSocketAddress address) throws JED2KException {

        Connection conn = null;
        try {
            conn = ds.getConnection();
            if (conn == null) throw new JED2KException(ErrorCode.NO_AVAILABLE_SQL_CONNECTIONS);

            PreparedStatement ps = conn.prepareStatement("INSERT INTO kad.keywords(kad_id, file_id, host, packet) " +
                "VALUES(?, ?, ?::INET, ?) ON CONFLICT ON CONSTRAINT keywords_pk DO UPDATE SET last_update = current_timestamp, packet = ?, total_updates = kad.keywords.total_updates + 1");

            ps.setString(1, kadId.toString());

            for(final KadSearchEntry res: keywords) {
                int ip = 0;

                Iterator<Tag> itr = res.getInfo().iterator();
                while(itr.hasNext()) {
                    Tag t = itr.next();

                    /**
                     * extract ip address from additional(non standard) tag and remove that tag
                     * do not rotate bytes here since it is completely artificial tag in host byte order
                    **/
                    if (t.getId() == Tag.TAG_SOURCEIP) {
                        ip = t.asIntValue();
                        itr.remove();
                        break;
                    }
                }

                if (ip != 0) {
                    ps.setString(2, res.getKid().toString());
                    buffer.clear();
                    res.put(buffer);
                    buffer.flip();
                    ByteBuffer buffer2 = buffer.slice();
                    ps.setString(3, Utils.ip2String(ip));
                    ps.setBinaryStream(4, new ByteBufferInputStream(buffer));
                    ps.setBinaryStream(5, new ByteBufferInputStream(buffer2));
                    ps.executeUpdate();
                    log.debug("store keyword from {}", Utils.ip2String(ip));
                } else {
                    log.warn("ip is zero in kad publish keywords request, skip entry {}", res);
                }
            }

            //conn.commit();
        } catch(SQLException e) {
            log.error("SQL exception {}", e);
            throw new JED2KException(ErrorCode.SOURCE_INSERT_SQL_ERROR);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch(SQLException e) {
                    log.warn("close connection error {}", e);
                }
            }
        }
    }

    @Override
    public void process(Kad2Ping p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2HelloReq p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2SearchNotesReq p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2Req p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2BootstrapReq p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2PublishKeysReq p, InetSocketAddress address) {
        try {
            insertKeywords(p.getKeywordId(), p.getSources().getList(), address);
        } catch (JED2KException e) {
            log.error("process publish keywords request error {}", e.getMessage());
        }
    }

    @Override
    public void process(Kad2PublishSourcesReq p, InetSocketAddress address) {
        try {
            insertSource(p.getFileId(), p.getSource(), address);
        } catch(JED2KException e) {
            log.error("process publish sources request error {}", e.getMessage());
        }
    }

    @Override
    public void process(Kad2FirewalledReq p, InetSocketAddress address) {

    }

    private void processSearchRequest(Serializable p, InetSocketAddress address) {
        assert (p instanceof Kad2SearchSourcesReq) || (p instanceof Kad2SearchKeysReq);
        Connection conn = null;
        DatagramSocket socket = null;
        try {
            byte[] buf = new byte[4096];
            ByteBuffer buffer = ByteBuffer.wrap(buf);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            PacketCombiner pc = new PacketCombiner();

            KadPacketHeader kph = new KadPacketHeader();
            kph.reset(pc.classToKey(p.getClass()), 0);
            PacketRawPacker prp = new PacketRawPacker(buffer, kph);

            conn = ds.getConnection();
            if (conn == null) throw new JED2KException(ErrorCode.NO_AVAILABLE_SQL_CONNECTIONS);

            PreparedStatement ps = conn.prepareStatement(String.format("SELECT packet FROM kad.keywords WHERE kad_id = ?"
                    , (p instanceof Kad2SearchKeysReq)?"keywords":"sources"));

            if (p instanceof Kad2SearchKeysReq) {
                ps.setString(1, ((Kad2SearchKeysReq)(p)).getTarget().toString());
            } else {
                ps.setString(1, ((Kad2SearchSourcesReq)p).getTarget().toString());
            }

            int total = 0;
            ResultSet rs = ps.executeQuery();

            socket = null;

            if (rs != null) {
                while(rs.next()) {
                    byte[] data = rs.getBytes(1);
                    if (!prp.hasSpace(data.length)) {
                        if (socket == null) socket = new DatagramSocket();
                        ByteBuffer b = prp.releaseBuffer();
                        DatagramPacket dp = new DatagramPacket(buf, b.remaining(), address);
                        socket.send(dp);
                        buffer.clear();
                    }

                    prp.putBlock(data);
                    ++total;
                }

                rs.close();
            }

            ps.close();

            if (!prp.isEmpty()) {
                ByteBuffer b = prp.releaseBuffer();
                if (socket == null) socket = new DatagramSocket();
                DatagramPacket dp = new DatagramPacket(buf, b.remaining(), address);
                socket.send(dp);
                buffer.clear();
            }

            log.debug("processed search {} request with result count {}", (p instanceof Kad2SearchKeysReq)?"keywords":"sources", total);
        } catch(SQLException e) {
            log.error("[PS] SQL exception {}", e);
        } catch (JED2KException e) {
            log.error("[PS] JED2KException {}", e);
        } catch(IOException e) {
            log.error("[PS] I/O exception {}", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                }
                catch(Exception e) {
                    log.error("[PS] Connection close exception {}", e);
                }
            }

            if (socket != null) {
                socket.close();
            }
        }
    }

    @Override
    public void process(Kad2SearchKeysReq p, InetSocketAddress address) {
        processSearchRequest(p, address);
    }

    @Override
    public void process(Kad2SearchSourcesReq p, InetSocketAddress address) {
       processSearchRequest(p, address);
    }
}
