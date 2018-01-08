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

import java.io.*;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ServerValidator {

    private static final int OPER_TIMEOUT = 30;
    public static final SimpleDateFormat tsFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

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

        public long getTsOffset() {
            if (lastVerified != null) {
                return Long.parseLong(lastVerified) + (failures!=null?failures.intValue()*2*1000*3600:0);
            }

            return 0;
        }

        public void updateStatus(final ServerStatus status) {
            if (status.filesCount != -1 && status.usersCount != -1) {
                filesCount = new Integer(status.filesCount);
                usersCount = new Integer(status.usersCount);
                failures = new Integer(0);
            } else {
                failures = failures!=null?failures+1:new Integer(1);
            }

            lastVerified = Long.toString(new Date().getTime());
        }
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

            final String serversExtern = (args.length > 2) ? args[2] : "";
            if (!serversExtern.isEmpty()) {
                try {
                    byte[] data = IOUtils.toByteArray(new URI(serversExtern));
                    List<ServerEntry> svlistExtern = gson.fromJson(new String(data), SERVERS_LIST_TYPE);
                    if (svlistExtern != null) {
                        mergeServersLists(svlistExtern.stream().filter(x -> isValidEntry(x)).collect(Collectors.toList()), svlist);
                    }
                } catch(URISyntaxException e) {
                    log.warn("uri mailformed {}", e.getMessage());
                } catch (IOException e) {
                    log.warn("unable to load new servers list {}", e.getMessage());
                }
            }

            final Session session = new Session(settings);
            session.start();
            svlist.stream()
                    .filter(x -> isValidEntry(x))
                    .filter(x -> new Date().getTime() > x.getTsOffset())
                    .forEach(x -> x.updateStatus(validate(x.getHost(), x.getPort(), session)));

            session.abort();
            session.join();
            log.info("session finished");

            try (Writer writer = new FileWriter(serversLocal)) {
                gson.toJson(svlist, writer);
            }
        } catch (Exception e) {
            log.error("Error {}", e);
        }
    }

    /**
     * validate some host as emule server
     * start connection analyze alerts and return report
     * @param session
     */
    private static ServerStatus validate(final String host, final int port, final Session session) {
        ServerStatus ss = new ServerStatus(-1, -1);

        long startTime = System.nanoTime();
        session.connectoTo("Validation...", host, port);
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
                        //res.setFailures(res.getFailures() + 1);
                    }
                } else if (a instanceof ServerStatusAlert) {
                    ServerStatusAlert ssa = (ServerStatusAlert) a;
                    log.info("server files count: {} users count: {}", ssa.filesCount, ssa.usersCount);
                    ss.setFilesCount(ssa.filesCount);
                    ss.setUsersCount(ssa.usersCount);
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
        return ss;
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
