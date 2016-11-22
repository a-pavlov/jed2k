package org.dkf.jed2k;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.DhtTracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by inkpot on 22.11.2016.
 */
@Slf4j
public class Kad {

    public static void main(String[] args) throws IOException, JED2KException {
        log.info("Kad starting");
        String command;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        DhtTracker tracker = new DhtTracker(9999);

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
        }

        log.info("Kad finished");
    }
}
