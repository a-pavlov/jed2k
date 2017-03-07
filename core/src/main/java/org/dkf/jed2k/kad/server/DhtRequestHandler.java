package org.dkf.jed2k.kad.server;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.protocol.Serializable;
import org.postgresql.ds.PGPoolingDataSource;

import java.net.InetSocketAddress;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by apavlov on 06.03.17.
 */
@Slf4j
public class DhtRequestHandler implements Runnable {
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
    }
}
