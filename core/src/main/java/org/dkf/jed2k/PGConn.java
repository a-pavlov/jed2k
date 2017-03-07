package org.dkf.jed2k;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.server.SynDhtTracker;
import org.postgresql.ds.PGPoolingDataSource;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by apavlov on 03.03.17.
 */
@Slf4j
public class PGConn {

    public static void main(String[] args) {
        log.info("PG connect started");

        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("A Data Source");
        source.setServerName("localhost");
        source.setDatabaseName("test");
        source.setUser("test");
        source.setPassword("test");
        source.setMaxConnections(10);

        java.sql.Connection conn = null;

        try {
            conn = source.getConnection();

            if (conn.isClosed()) {
                log.info("connection closed");
            }

            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT count(*) FROM test.sources");
            if (rs.next()) {
                log.info("records count {}", rs.getInt(1));
            } else {
                log.warn("no records");
            }

            rs.close();
        } catch(SQLException e) {
            log.error("sql exception {}", e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch(SQLException e) {

                }
            }
        }

        // create synchronized datagram socket server
        try {
            ExecutorService exec = Executors.newSingleThreadExecutor();
            SynDhtTracker dht = new SynDhtTracker(2000, 10000, exec, source);
            dht.processPackets();
            dht.close();
        } catch(JED2KException e) {
            log.error("sync dht tracker failed with {}", e);
        }
    }
}
