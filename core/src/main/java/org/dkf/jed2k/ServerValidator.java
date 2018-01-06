package org.dkf.jed2k;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.sun.security.ntlm.Server;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.protocol.SearchEntry;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ServerValidator {

    private static final int OPER_TIMEOUT = 30;

    @EqualsAndHashCode
    public static class ServerEntry {

    }

    private static final Type SERVERS_LIST_TYPE = new TypeToken<List<ServerEntry>>() {
    }.getType();

    private static final Gson gson = new Gson();
    private static final Settings settings = new Settings();

    static {
        final Settings startSettings = new Settings();
        startSettings.maxConnectionsPerSecond = 10;
        startSettings.sessionConnectionsLimit = 100;
        startSettings.compressionVersion = 1;
        startSettings.serverPingTimeout = 0;
    }

    /**
     * 0 - local json file
     * 1 - listen port
     * 2 - url of external json file
     * @param args
     */
    public static void main(String[] args) {
        try {
            if (args.length < 2) {
                throw new RuntimeException("Local file missed or port missed");
            }

            final String serversLocal = args[0];
            settings.listenPort = Integer.parseInt(args[1]);

            if (settings.listenPort <= 0) {
                log.warn("incorrect port value {}", settings.listenPort);
                throw new RuntimeException("Incorrect port");
            }

            List<ServerEntry> servers;

            assert serversLocal != null;

            try {
                JsonReader reader = new JsonReader(new FileReader(serversLocal));
                servers = gson.fromJson(reader, SERVERS_LIST_TYPE); // contains the whole reviews list
            } catch(FileNotFoundException e) {
                log.debug("file not found {}", serversLocal);
                servers = new LinkedList<>();
            }

            assert servers != null;

            final String serversExtern = (args.length > 1) ? args[1] : "";

            final Session session = new Session(settings);
            session.start();
            validate("emule.is74.ru", 4661, session);
            session.abort();
            session.join();
            log.info("session finished");
        } catch (Exception e) {
            log.error("Error {}", e);
        }
    }

    /**
     * validate some host as emule server
     * start connection analyze alerts and return report
     * @param host
     * @param port
     * @param session
     * @throws InterruptedException
     */
    private static void validate(final String host, int port, final Session session) throws InterruptedException {
        assert host != null;
        assert port >= 0;
        long startTime = System.nanoTime();
        session.connectoTo("Validation...", host, port);

        while(TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime) < OPER_TIMEOUT) {
            Alert a = session.popAlert();

            while (a != null) {
                startTime = System.nanoTime();

                if (a instanceof ServerConnectionAlert) {
                    log.info("connected");
                } else if (a instanceof ServerConectionClosed) {
                    log.info("disconnected {}", ((ServerConectionClosed)a).code);
                } else if (a instanceof ServerStatusAlert) {
                    ServerStatusAlert ssa = (ServerStatusAlert) a;
                    log.info("server files count: {} users count: {}", ssa.filesCount, ssa.usersCount);
                } else if (a instanceof ServerInfoAlert) {
                    log.info("server info: {}", ((ServerInfoAlert) a).info);
                } else if (a instanceof ServerMessageAlert) {
                    log.info("server message {}", ((ServerMessageAlert)a).msg);
                } else if (a instanceof ServerAlert) {
                    log.info("server alert received {}", ((ServerAlert)a).identifier);
                } else {
                    log.info("[CONN] unknown alert received: {}", a.toString());
                }

                a = session.popAlert();
            }

            log.info("wait alerts");
            Thread.sleep(1000*10);
        }

        session.disconnectFrom();
    }

}
