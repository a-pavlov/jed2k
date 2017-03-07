package org.dkf.jed2k.kad.server;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.ByteBufferInputStream;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.ReqDispatcher;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.kad.*;
import org.postgresql.ds.PGPoolingDataSource;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by apavlov on 06.03.17.
 */
@Slf4j
public class DhtRequestHandler implements Runnable, ReqDispatcher {
    private final Serializable packet;
    private final InetSocketAddress address;
    private final PGPoolingDataSource ds;

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
        if (packet instanceof KadDispatchable) {
            ((KadDispatchable)packet).dispatch(this, address);
        }
    }

    /**
     *
     * @param kadId
     * @param host
     * @param portTcp
     * @param portUdp
     * @param packet
     * @param sourceType
     * @throws JED2KException
     */
    private void insertSource(final String kadId
            , final String host
            , int portTcp
            , int portUdp
            , ByteBuffer packet
            , final String sourceType) throws JED2KException {
        try {
            Connection conn = getConnection();
            if (conn != null) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO test.sources(kad_id, host, port_tcp, port_udp, packet, source_type) " +
                        "VALUES (?, ?, ?, ?, ?, ?) ON CONFLICT ON CONSTRAINT sources_pk DO UPDATE SET last_update = current_timestamp, packet = ?");
                ps.setString(1, kadId);
                ps.setString(2, host);
                ps.setInt(3, portTcp);
                ps.setInt(4, portUdp);
                ps.setBinaryStream(5, new ByteBufferInputStream(packet));
                ps.setString(6, sourceType);
                ps.setBinaryStream(7, new ByteBufferInputStream(packet));
                ps.executeUpdate();
            }
        } catch(SQLException e) {
            log.error("SQL exception {}", e);
            throw new JED2KException(ErrorCode.SOURCE_INSERT_SQL_ERROR);
        }
    }

    private Connection getConnection() throws SQLException {
        Connection conn = null;
        try {
            conn = ds.getConnection();

        } catch(SQLException e) {
            log.error("connection error {}", e);
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch(SQLException e) {
                    log.warn("close connection error {}", e);
                }
            }
        }

        return conn;
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

    }

    @Override
    public void process(Kad2PublishSourcesReq p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2FirewalledReq p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2SearchKeysReq p, InetSocketAddress address) {

    }

    @Override
    public void process(Kad2SearchSourcesReq p, InetSocketAddress address) {

    }
}
