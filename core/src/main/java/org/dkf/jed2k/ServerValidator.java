package org.dkf.jed2k;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class ServerValidator {

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

        } catch (Exception e) {
            log.error("Error {}", e);
        }
    }

}
