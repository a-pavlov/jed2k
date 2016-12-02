package org.dkf.jed2k;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.DhtTracker;
import org.dkf.jed2k.protocol.kad.KadId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

/**
 * Created by inkpot on 22.11.2016.
 */
@Slf4j
public class Kad {

    public static void main(String[] args) throws IOException, JED2KException {
        log.info("Kad starting");
        String command;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        KadId target = new KadId(KadId.random(false));
        DhtTracker tracker = new DhtTracker(9999, target);
        tracker.start();

        while ((command = in.readLine()) != null) {
            String[] parts = command.split("\\s+");

            if (parts[0].compareTo("exit") == 0 || parts[0].compareTo("quit") == 0) {
                tracker.abort();
                try {
                    tracker.join();
                    log.info("tracker aborted");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
            else if (parts[0].compareTo("bootstrap") == 0 && parts.length == 3) {
                log.info("bootstrap on {}:{}", parts[1], parts[2]);
                InetSocketAddress address = new InetSocketAddress(parts[1], Integer.parseInt(parts[2]));
                tracker.bootstrapTest(address);
            }
            else if (parts[0].compareTo("search") == 0 && parts.length == 4) {
                log.info("search on {}:{} for {}", parts[1], parts[2], parts[3]);
                InetSocketAddress address = new InetSocketAddress(parts[1], Integer.parseInt(parts[2]));
                tracker.searchKey(address, parts[3]);
            }
            else if (parts[0].compareTo("hello") == 0 && parts.length == 3) {
                log.info("hello to {}:{}", parts[1], parts[2]);
                InetSocketAddress address = new InetSocketAddress(parts[1], Integer.parseInt(parts[2]));
                tracker.hello(address);
            }
        }

        log.info("Kad finished");
    }
}
