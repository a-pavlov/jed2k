package org.dkf.jed2k;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.DhtTracker;
import org.dkf.jed2k.kad.Listener;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadNodesDat;
import org.dkf.jed2k.protocol.kad.KadSearchEntry;
import org.dkf.jed2k.util.FUtils;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by inkpot on 22.11.2016.
 */
@Slf4j
public class Kad {

    private static GatewayDevice device = null;
    private final static int port = 9999;

    private static void startUpnp() {
        try {
            GatewayDiscover discover = new GatewayDiscover();
            discover.discover();
            device = discover.getValidGateway();

            if (device != null) {
                PortMappingEntry portMapping = new PortMappingEntry();
                if (!device.getSpecificPortMappingEntry(port, "UDP",portMapping)) {
                    InetAddress localAddress = device.getLocalAddress();
                    if (device.addPortMapping(port, port, localAddress.getHostAddress(),"UDP","JED2K")) {
                        // ok, mapping added
                        log.info("[KAD] port mapped {}", port);
                    } else {
                        log.info("[KAD] mapping error for port {}", port);
                    }
                } else {
                    log.debug("[KAD] port {} already mapped", port);
                }
            } else {
                log.debug("[KAD] can not find gateway device");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private static void stopUpnp() {

        if (device != null) try {
            device.deletePortMapping(port, "UDP");
            device = null;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

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

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

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
        DhtTracker tracker = new DhtTracker(port, idata.getTarget());
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
            else if (parts[0].compareTo("inet") == 0) {
                if (parts.length >= 2) {
                    try {
                        log.info("[KAD] try to use file {}", parts[1]);
                        KadNodesDat nodes = new KadNodesDat();
                        FUtils.read(nodes, new File(parts[1]));
                        tracker.addKadEntries(nodes.getContacts());
                    } catch(JED2KException e) {
                        log.error("[KAD] unable to load file {}: {}", parts[1], e);
                    }
                    //try (FileInputStream s = new FileInputStream(new File(parts[1])); FileChannel c = s.getChannel()){
                    //
                    //} catch(Exception e) {
                    //    log.error("[KAD] unable to read file {}: {}", parts[1], e);
                    //}
                } else {
                    log.info("[KAD] downloading nodes.dat from inet and bootstrap dht");
                    try {
                        byte[] data = IOUtils.toByteArray(new URI("http://server-met.emulefuture.de/download.php?file=nodes.dat"));
                        ByteBuffer buffer = ByteBuffer.wrap(data);
                        log.debug("[KAD] downloaded nodes.dat size {}", buffer.remaining());
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
            else if (parts[0].compareTo("startupnp") == 0) {
                startUpnp();
            }
        }

        stopUpnp();
        idata.setEntries(tracker.getTrackerState());
        FUtils.write(idata, new File(dir.resolve("dht_status.dat").toString()));
        log.info("[KAD] finished");
    }
}
