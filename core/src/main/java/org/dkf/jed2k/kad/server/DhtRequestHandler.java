package org.dkf.jed2k.kad.server;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.pool.SynchronizedArrayPool;
import org.dkf.jed2k.protocol.PacketCombiner;
import org.dkf.jed2k.protocol.PacketHeader;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.KadDispatchable;
import org.dkf.jed2k.protocol.kad.KadPacketHeader;
import org.postgresql.ds.PGPoolingDataSource;

import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by apavlov on 06.03.17.
 */
@Slf4j
public class DhtRequestHandler implements Runnable {
    private final SynchronizedArrayPool pool;
    private final DatagramPacket packet;
    private final PGPoolingDataSource ds;
    private static PacketCombiner combiner = new org.dkf.jed2k.protocol.kad.PacketCombiner();

    public DhtRequestHandler(SynchronizedArrayPool pool
            , final DatagramPacket packet
            , final PGPoolingDataSource ds) {
        this.pool = pool;
        this.packet = packet;
        this.ds = ds;
    }

    @Override
    public void run() {
        assert pool != null;
        assert packet != null;

        try {
            ByteBuffer buffer = ByteBuffer.wrap(packet.getData(), 0, packet.getLength());
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            PacketHeader header = new KadPacketHeader();
            header.get(buffer);
            if (!header.isDefined()) throw new JED2KException(ErrorCode.PACKET_HEADER_UNDEFINED);
            Serializable s = combiner.unpack(header, buffer);
            log.trace("incoming packet {}", s);


            if (s instanceof KadDispatchable) {
                //((KadDispatchable)s).dispatch(node, address);
                Connection conn = null;
                try {
                    conn = ds.getConnection();
                } catch(SQLException e) {
                    log.error("connection error {}", e);
                } finally {
                    if (conn != null) {
                        try {
                            conn.close();
                        } catch(SQLException e) {
                            log.warn("close connection error {}", e);
                        }
                    }
                }
            } else {
                log.debug("incoming packet is not dispatchable");
            }

        } catch(JED2KException e) {

        } finally {
            pool.deallocate(packet.getData(), 0);
        }
    }
}
