package org.dkf.jed2k;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.sun.security.ntlm.Server;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.SearchEntry;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ServerValidator {

    private static final int OPER_TIMEOUT = 30;

    @EqualsAndHashCode(exclude = {"failures", "lastVerified", "filesCount", "usersCount"})
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class ServerEntry {
        public String name;
        public String host;
        public Integer port;
        public Integer failures;
        public String lastVerified;
        public Integer filesCount;
        public Integer usersCount;
    }

    public static boolean isValidEntry(final ServerEntry se) {
        return se.name != null && !se.name.isEmpty() && se.host != null && !se.host.isEmpty() && se.port != null && se.port > 0;
    }

    @Data
    @AllArgsConstructor
    @ToString
    private static class ServerStatus {
        private int filesCount;
        private int usersCount;
    }

    public static final Type SERVERS_LIST_TYPE = new TypeToken<List<ServerEntry>>() {
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

            List<ServerEntry> svlist;

            assert serversLocal != null;

            try {
                JsonReader reader = new JsonReader(new FileReader(serversLocal));
                svlist = gson.fromJson(reader, SERVERS_LIST_TYPE); // contains the whole reviews list
            } catch(FileNotFoundException e) {
                log.debug("file not found {}", serversLocal);
                svlist = new LinkedList<>();
            }

            assert svlist != null;

            final String serversExtern = (args.length > 1) ? args[1] : "";
            if (!serversExtern.isEmpty()) {
                try {
                    byte[] data = IOUtils.toByteArray(new URI("https://raw.githubusercontent.com/a-pavlov/jed2k/config/servers.json"));
                    List<ServerEntry> svlistExtern = gson.fromJson(new String(data), SERVERS_LIST_TYPE);
                    if (svlistExtern != null) {
                        mergeServersLists(svlist.stream().filter(x -> isValidEntry(x)).collect(Collectors.toList()), svlist);
                    }
                } catch(URISyntaxException e) {
                    log.warn("uri mailformed {}", e.getMessage());
                } catch (IOException e) {
                    log.warn("unable to load new servers list {}", e.getMessage());
                }
            }

            final Session session = new Session(settings);
            session.start();
            svlist.stream().map(x -> validate(x, session)).collect(Collectors.toList());
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
     * @param session
     * @throws InterruptedException
     */
    private static ServerEntry validate(final ServerEntry se, final Session session) {
        ServerEntry res = new ServerEntry();
        res.name = se.name;
        res.host = se.host;
        res.port = se.port;
        res.failures = se.failures;
        ServerStatus ss = new ServerStatus(-1, -1);

        long startTime = System.nanoTime();
        session.connectoTo("Validation...", se.host, se.port);
        boolean exitNow = false;

        while(TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startTime) < OPER_TIMEOUT) {
            Alert a = session.popAlert();

            while (a != null) {
                startTime = System.nanoTime();

                if (a instanceof ServerConnectionAlert) {
                    log.info("connected");
                } else if (a instanceof ServerConectionClosed) {
                    log.info("disconnected {}", ((ServerConectionClosed)a).code);
                    exitNow = true;
                    if (!(((ServerConectionClosed)a).code.equals(ErrorCode.NO_ERROR))) {
                        res.setFailures(res.getFailures() + 1);
                    }
                } else if (a instanceof ServerStatusAlert) {
                    ServerStatusAlert ssa = (ServerStatusAlert) a;
                    log.info("server files count: {} users count: {}", ssa.filesCount, ssa.usersCount);
                    res.setFilesCount(ssa.filesCount);
                    res.setUsersCount(ssa.usersCount);
                    res.setFailures(0);
                    session.disconnectFrom();
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

            if (exitNow) break;

            log.info("wait alerts");
            try {
                Thread.sleep(1000 * 10);
            } catch(InterruptedException e) {
                log.error("interrupted {}", e);
                break;
            }
        }

        session.disconnectFrom();
        return res;
    }

    public static void mergeServersLists(final List<ServerEntry> src, final List<ServerEntry> dst) {
        Iterator<ServerEntry> itr = dst.iterator();
        while (itr.hasNext()) {
            ServerEntry se = itr.next();
            if (!src.contains(se)) itr.remove();
        }

        for(final ServerEntry se: src) {
            if (!dst.contains(se)) dst.add(se);
        }
    }
}
