package org.dkf.jed2k;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.DhtTracker;
import org.dkf.jed2k.kad.NodeEntry;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.UInt32;
import org.dkf.jed2k.protocol.kad.KadId;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;

/**
 * Created by inkpot on 22.11.2016.
 */
@Slf4j
public class Kad {

    public static void main(String[] args) throws IOException, JED2KException {
        log.info("[KAD] starting");
        if (args.length < 1) {
            log.warn("[KAD] please provide working directory");
            return;
        }

        Path dir = FileSystems.getDefault().getPath(args[0]);

        KadId target = new KadId();
        Container<UInt32, NodeEntry> entries = Container.makeInt(NodeEntry.class);

        // read state
        try (RandomAccessFile reader = new RandomAccessFile(dir.resolve("dht_status.dat").toString(), "r");
             FileChannel inChannel = reader.getChannel();) {
            long fileSize = inChannel.size();
            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
            inChannel.read(buffer);
            buffer.flip();
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            entries.get(target.get(buffer));
            log.info("[KAD] load {} nodes", entries.size());
        } catch(IOException e) {
            log.info("[KAD] unable to load dht_status.dat");
        } catch(JED2KException e) {
            log.error("[KAD] unable to load nodes {}", e);
        }

        if (target.isAllZeros()) {
            target = new KadId(KadId.random(false));
        }

        String command;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        DhtTracker tracker = new DhtTracker(9999, target);
        tracker.start();

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
                tracker.searchKeywords(parts[1], null);
            }
            else if (parts[0].compareTo("hello") == 0 && parts.length == 3) {
                log.info("[KAD] hello to {}:{}", parts[1], parts[2]);
                InetSocketAddress address = new InetSocketAddress(parts[1], Integer.parseInt(parts[2]));
                tracker.hello(address);
            }
            else if (parts[0].compareTo("status") == 0) {
                log.info(tracker.getRoutingTableStatus());
            }
        }

        // write state
        try (RandomAccessFile reader = new RandomAccessFile(dir.resolve("dht_status.dat").toString(), "rw");
             FileChannel inChannel = reader.getChannel();) {
            entries = tracker.getTrackerState();
            long fileSize = entries.bytesCount() + target.bytesCount();
            assert fileSize > 0;
            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            entries.put(target.put(buffer));
            buffer.flip();
            inChannel.write(buffer);
            log.info("[KAD] load {} nodes", entries.size());
        } catch(IOException e) {
            log.info("[KAD] unable to load dht_status.dat");
        } catch(JED2KException e) {
            log.error("[KAD] unable to write nodes to buffer {}", e);
        }


        log.info("[KAD] finished");
    }
}
