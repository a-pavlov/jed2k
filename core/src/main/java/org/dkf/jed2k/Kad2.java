package org.dkf.jed2k;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.apache.commons.io.IOUtils;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.DhtTracker;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadNodesDat;
import org.dkf.jed2k.util.FUtils;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Created by apavlov on 18.03.17.
 */
@Slf4j
public class Kad2 implements Daemon {
    DhtTracker tracker = null;
    int port = 0;
    InetSocketAddress sp = null;
    Path ws;
    DhtInitialData idata;
    boolean needBootstrap = false;
    private static final String STATUS_FILENAME = "dht.dat";

    @Override
    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
        log.debug("[Kad2] init");
        Options options = new Options();

        options.addOption("h", "help", false, "display help information");
        options.addOption(OptionBuilder.withLongOpt("port")
                .withDescription("Port for incoming requests")
                .hasArg()
                .withArgName("PORT")
                .create());

        options.addOption(OptionBuilder.withLongOpt("sport")
                .withDescription("Port for storage point")
                .hasArg()
                .withArgName("SPORT")
                .create());

        options.addOption(OptionBuilder.withLongOpt("workspace")
                .withDescription("Workspace directory - have to exist")
                .hasArg()
                .withArgName("WS")
                .create());

        try {
            CommandLine line = new PosixParser().parse(options, daemonContext.getArguments());

            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("stcpsrv", options);
                System.exit(0);
            }

            if (!line.hasOption("port")) throw new Exception("option \"port\" missed");
            if (!line.hasOption("sport")) throw new Exception("option \"sport\" missed");
            if (!line.hasOption("workspace")) throw new Exception("option \"workspace\" missed");

            port = Integer.parseInt(line.getOptionValue("port"));
            sp = new InetSocketAddress("localhost", Integer.parseInt(line.getOptionValue("sport")));
            ws = FileSystems.getDefault().getPath(line.getOptionValue("workspace"));

            idata = new DhtInitialData();

            if (Files.notExists(ws)) {
                throw new Exception("Workspace path not exist");
            }

            try {
                FUtils.read(idata, new File(ws.resolve(STATUS_FILENAME).toString()));
            } catch(JED2KException e) {
                log.error("[Kad2] unable to load initial data {}, skip it", e.getMessage());
                needBootstrap = true;
            }

            if (idata.getTarget().isAllZeros()) {
                idata.setTarget(new KadId(KadId.random(false)));
            }

        } catch (Exception e) {
            log.error("incorrect command line arguments {}", e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void start() throws Exception {
        assert port != 0;
        assert idata != null;
        assert sp != null;
        tracker = new DhtTracker(port, idata.getTarget(), sp);
        tracker.start();
        if (idata.getEntries().getList() != null) {
            tracker.addEntries(idata.getEntries().getList());
        } else {
            log.info("[Kad2] downloading nodes.dat from inet and bootstrap dht");
            try {
                byte[] data = IOUtils.toByteArray(new URI("http://server-met.emulefuture.de/download.php?file=nodes.dat"));
                ByteBuffer buffer = ByteBuffer.wrap(data);
                log.debug("[Kad2] downloaded nodes.dat size {}", buffer.remaining());
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                KadNodesDat nodes = new KadNodesDat();
                nodes.get(buffer);
                tracker.addKadEntries(nodes.getContacts());
                tracker.addKadEntries(nodes.getBootstrapEntries().getList());
            } catch (Exception e) {
                log.error("[KAD] unable to initialize DHT from inet: {}", e);
            }
        }
    }

    @Override
    public void stop() throws Exception {
        tracker.abort();
        try {
            tracker.join();
        } catch(InterruptedException e) {
            log.error("wait tracker raised interrupted exception {}", e);
        }

        idata.setEntries(tracker.getTrackerState());
        try {
            FUtils.write(idata, new File(ws.resolve(STATUS_FILENAME).toString()));
        } catch(JED2KException e) {
            log.error("unable to save status {}", e);
        }

        tracker = null;

    }

    @Override
    public void destroy() {

    }
}
