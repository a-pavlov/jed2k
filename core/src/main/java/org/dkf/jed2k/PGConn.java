package org.dkf.jed2k;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.server.DhtRequestHandler;
import org.dkf.jed2k.kad.server.SynDhtTracker;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.*;
import org.dkf.jed2k.protocol.tag.Tag;
import org.postgresql.ds.PGPoolingDataSource;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by apavlov on 03.03.17.
 */
@Slf4j
public class PGConn {

    private static Kad2SearchRes file2SearchRes(final String filepath) throws JED2KException {
        try {
            RandomAccessFile reader = new RandomAccessFile(filepath, "r");
            FileChannel inChannel = reader.getChannel();
            long fileSize = inChannel.size();
            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
            inChannel.read(buffer);
            buffer.flip();
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            inChannel.close();
            reader.close();
            Kad2SearchRes res = new Kad2SearchRes();
            res.get(buffer);
            return res;
        } catch(FileNotFoundException e) {
            throw new JED2KException(ErrorCode.FILE_NOT_FOUND);
        } catch(IOException e) {
            throw new JED2KException(ErrorCode.IO_EXCEPTION);
        }
    }

    public static void main(String[] args) throws IOException, JED2KException {
        log.info("PG connect started");

        PGPoolingDataSource source = new PGPoolingDataSource();
        source.setDataSourceName("Kad data source");
        source.setServerName("localhost");
        source.setDatabaseName("kad");
        source.setUser("kad");
        source.setPassword("kad");
        source.setMaxConnections(4);
        ExecutorService service = Executors.newFixedThreadPool(4);
        SynDhtTracker tracker = new SynDhtTracker(20000, 10000, service, source);
        tracker.start();

        Random rnd = new Random();

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String command;

        while ((command = in.readLine()) != null) {
            String[] parts = command.split("\\s+");
            if ("quit".equals(parts[0]) || "exit".equals(parts[0])) {
                log.info("pg conn exit");
                break;
            }
            else if ("sources".equals(parts[0])) {
                for(int i = 1; i < parts.length; ++i) {
                    log.info("load sources from {}", parts[i]);
                    try {
                        Kad2SearchRes res = file2SearchRes(parts[i]);
                        Kad2PublishSourcesReq ps = new Kad2PublishSourcesReq();
                        ps.setFileId(new KadId(KadId.EMULE));
                        for (final KadSearchEntry se: res.getResults()) {
                            Endpoint ep = new Endpoint(1234567, 20000);
                            se.getInfo().add(Tag.tag(Tag.TAG_SOURCETYPE, null, ep.getIP()));
                            ps.setSource(se);
                            //Endpoint ep = new Endpoint(rnd.nextInt(), rnd.nextInt(65535));
                            DhtRequestHandler rh = new DhtRequestHandler(ps, ep.toInetSocketAddress(), source);
                            rh.run();
                        }
                    } catch(JED2KException e) {
                        log.error("unable to parse file content, expected Kad2SearchRes {}", e);
                    }
                }
            }
            else if ("keywords".equals(parts[0])) {
                for(int i = 1; i < parts.length; ++i) {
                    log.info("load keywords from {}", parts[i]);
                    try {
                        Endpoint ep = new Endpoint(rnd.nextInt(), rnd.nextInt(65535));
                        //Endpoint ep = new Endpoint(1234567, 20000);
                        Kad2SearchRes res = file2SearchRes(parts[i]);
                        Kad2PublishKeysReq pk = new Kad2PublishKeysReq();
                        for(final KadSearchEntry kse: res.getResults()) {
                            // inject endpoint into search result entry - it will be used as source host in KAD storage
                            kse.getInfo().add(Tag.tag(Tag.TAG_SOURCETYPE, null, ep.getIP()));
                        }

                        pk.setKeywordId(new KadId(KadId.EMULE));
                        pk.getSources().addAll(res.getResults());

                        DhtRequestHandler rh = new DhtRequestHandler(pk, ep.toInetSocketAddress(), source);
                        rh.run();
                    } catch(JED2KException e) {
                        log.error("unable to parse file content, expected Kad2SearchRes {}", e);
                    }
                }
            }
        }

        if (tracker != null) {
            tracker.stopTracking();
            try {
                tracker.join();
            } catch(InterruptedException e) {
                log.error("interrupted exception on tracker wait {}", e);
            }
        }

        if (service != null) service.shutdown();
        if (source != null) source.close();
    }
}
