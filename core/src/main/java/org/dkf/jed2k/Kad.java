package org.dkf.jed2k;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.DhtTracker;
import org.dkf.jed2k.kad.Listener;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadSearchEntry;
import org.dkf.jed2k.util.FUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

/**
 * Created by inkpot on 22.11.2016.
 */
@Slf4j
public class Kad {

    private static class SearchReport implements Listener {
        private final KadId target;

        public SearchReport(final KadId id) {
            target = id;
        }

        @Override
        public void process(List<KadSearchEntry> data) {
            log.info("[KAD] search for {} finished, results {}", target, data.size());
            for(final KadSearchEntry e: data) {
                log.info("[KAD] {}", e);
            }
            log.info("[KAD] report done.");
        }
    }


    public static void main(String[] args) throws IOException, JED2KException {
        log.info("[KAD] starting");
        if (args.length < 1) {
            log.warn("[KAD] please provide working directory");
            return;
        }

        Path dir = FileSystems.getDefault().getPath(args[0]);

        DhtInitialData idata = new DhtInitialData();

        try {
            FUtils.read(idata, new File(dir.resolve("dht_status.dat").toString()));
        } catch(JED2KException e) {
            log.error("[KAD] unable to load initial data {}", e);
        }

        if (idata.getTarget().isAllZeros()) {
            idata.setTarget(new KadId(KadId.random(false)));
        }

        String command;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        DhtTracker tracker = new DhtTracker(9999, idata.getTarget());
        tracker.start();
        if (idata.getEntries().getList() != null) {
            tracker.addEntries(idata.getEntries().getList());
        } else {
            log.debug("[KAD] previous nodes list is empty");
        }

        while ((command = in.readLine()) != null) {
            String[] parts = command.split("\\s+");

            if (parts[0].compareTo("exit") == 0 || parts[0].compareTo("quit") == 0) {
                tracker.abort();
                try {
                    tracker.join();
                    log.info("[KAD] tracker aborted");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            else if (parts[0].compareTo("bootstrap") == 0 && parts.length == 3) {
                log.info("[KAD] bootstrap on {}:{}", parts[1], parts[2]);
                tracker.bootstrap(Collections.singletonList(Endpoint.fromString(parts[1], Integer.parseInt(parts[2]))));
            }
            else if (parts[0].compareTo("search") == 0 && parts.length > 1) {
                log.info("search {}", parts[1]);    // temporary search only first keyword
                tracker.searchKeywords(parts[1], new SearchReport(new KadId()));    // target id is not important here
            }
            else if (parts[0].compareTo("hello") == 0 && parts.length == 3) {
                log.info("[KAD] hello to {}:{}", parts[1], parts[2]);
                InetSocketAddress address = new InetSocketAddress(parts[1], Integer.parseInt(parts[2]));
                tracker.hello(address);
            }
            else if (parts[0].compareTo("status") == 0) {
                try (PrintWriter pw = new PrintWriter(dir.resolve("status.json").toString())) {
                    pw.write(tracker.getRoutingTableStatus());
                }
            }
        }

        idata.setEntries(tracker.getTrackerState());
        FUtils.write(idata, new File(dir.resolve("dht_status.dat").toString()));
        log.info("[KAD] finished");
    }
}
