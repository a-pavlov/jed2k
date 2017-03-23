package org.dkf.jed2k;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.apache.commons.daemon.Daemon;
import org.apache.commons.daemon.DaemonContext;
import org.apache.commons.daemon.DaemonInitException;
import org.dkf.jed2k.kad.server.SynDhtTracker;
import org.postgresql.ds.PGPoolingDataSource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by apavlov on 10.03.17.
 */
@Slf4j
public class KadDaemon implements Daemon {

    private static int SO_TIMEOUT = 10000;  // 10 sec
    private static int PARALLELISM = 5;

    private int port;
    private PGPoolingDataSource source = null;
    private ExecutorService service = null;
    private SynDhtTracker tracker = null;


    public static void main(String[] args) {
        System.exit(0);
    }

    @Override
    public void init(DaemonContext daemonContext) throws DaemonInitException, Exception {
        log.debug("[KD] init");
        Options options = new Options();

        options.addOption("h", "help", false, "display help information");
        options.addOption(OptionBuilder.withLongOpt("port")
                .withDescription("Port for incoming requests")
                .hasArg()
                .withArgName("PORT")
                .create());

        options.addOption(OptionBuilder.withLongOpt("database")
                .withDescription("Database alias")
                .hasArg()
                .withArgName("DB")
                .create());

        options.addOption(OptionBuilder.withLongOpt("host")
                .withDescription("Host")
                .hasArg()
                .withArgName("HOST")
                .create());

        options.addOption(OptionBuilder.withLongOpt("user")
                .withDescription("Database user name")
                .hasArg()
                .withArgName("USER")
                .create());

        options.addOption(OptionBuilder.withLongOpt("password")
                .withDescription("Database password")
                .hasArg()
                .withArgName("PASSWORD")
                .create());


        try {
            CommandLine line = new PosixParser().parse(options, daemonContext.getArguments());

            if (line.hasOption("h")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("stcpsrv", options);
                System.exit(0);
            }

            if (!line.hasOption("port")) throw new Exception("option \"port\" missed");
            if (!line.hasOption("host")) throw new Exception("option \"host\" missed");
            if (!line.hasOption("database")) throw new Exception("option \"database\" missed");
            if (!line.hasOption("user")) throw new Exception("option \"user\" missed");
            if (!line.hasOption("password")) throw new Exception("option \"password\" missed");
            port = Integer.parseInt(line.getOptionValue("port"));

            try {
                Class.forName("org.postgresql.Driver");
            } catch(ClassNotFoundException e) {
                log.error("unable to load database driver {}", e.getMessage());
                e.printStackTrace();
                System.exit(1);
            }


            source = new PGPoolingDataSource();
            source.setDataSourceName("Kad data source");
            source.setServerName(line.getOptionValue("host"));
            source.setDatabaseName(line.getOptionValue("database"));
            source.setUser(line.getOptionValue("user"));
            source.setPassword(line.getOptionValue("password"));
            source.setMaxConnections(PARALLELISM);
            service = Executors.newFixedThreadPool(PARALLELISM);

        } catch (ParseException | NumberFormatException e) {
            log.error("incorrect command line arguments {}", e.getMessage());
            System.exit(1);
        }
    }

    @Override
    public void start() throws Exception {
        log.debug("[KD] start on port {} with timeout {}", port, SO_TIMEOUT);
        tracker = new SynDhtTracker(port, SO_TIMEOUT, service, source);
        tracker.start();
    }

    @Override
    public void stop() throws Exception {
        log.debug("[KD] stop");
        if (tracker != null) {
            tracker.stopTracking();
            tracker.join();
        }
    }

    @Override
    public void destroy() {
        log.debug("[KD] destroy");
        if (service != null) service.shutdown();
        if (source != null) source.close();
    }
}
