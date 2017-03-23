package org.dkf.jed2k;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.DhtTracker;
import org.dkf.jed2k.kad.Listener;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.UInt16;
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
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Random;
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
        private final File backup;

        public SearchReport(final KadId id, final File backup) {
            target = id;
            this.backup = backup;
        }

        @Override
        public void process(List<KadSearchEntry> data) {
            log.info("[KAD] search for {} finished, results {}", target, data.size());
            for(final KadSearchEntry e: data) {
                log.info("[KAD] {}", e);
            }
            log.info("[KAD] report done.");

            try(FileOutputStream stream = new FileOutputStream(backup, false);FileChannel channel = stream.getChannel()) {
                Container<UInt16, KadSearchEntry> entries = Container.makeShort(KadSearchEntry.class);
                for(final KadSearchEntry e: data) {
                    entries.add(e);
                }
                ByteBuffer bb = ByteBuffer.allocate(entries.bytesCount());
                bb.order(ByteOrder.LITTLE_ENDIAN);
                entries.put(bb);
                bb.flip();
                channel.write(bb);
                channel.close();
            } catch(IOException e) {
                log.error("[KAD] I/O exception on save DHT search results " + e);
            } catch(JED2KException e) {
                log.error("[KAD] unable to save search result: " + e);
            }
        }
    }

    private static class SourcesReport implements Listener {

        @Override
        public void process(List<KadSearchEntry> data) {
            log.info("[KAD] sources found {}", data.size());
        }
    }
    
    public static void main(String[] args) throws IOException, JED2KException {
        log.info("[KAD] starting");
        if (args.length < 1) {
            log.warn("[KAD] please provide working directory");
            return;
        }

        log.info("[KAD] local host {}", InetAddress.getLocalHost());

        String sp = System.getProperty("storage.point");
        InetSocketAddress spAddress = null;
        if (sp != null) {
            String[] parts = sp.split(":");
            if (parts.length == 2) {
                spAddress = new InetSocketAddress(parts[0], Integer.parseInt(parts[1]));
                log.info("storage point is {}", spAddress);
            } else {
                log.warn("storage point specification is invalid {}", sp);
            }
        } else {
            log.info("no storage point");
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
        DhtTracker tracker = new DhtTracker(port, idata.getTarget(), spAddress);
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
            else if (parts[0].compareTo("sources") == 0 && parts.length == 3) {
                log.info("search sources for {} size {}", parts[1], parts[2]);
                tracker.searchSources(Hash.fromString(parts[1])
                        , Long.parseLong(parts[2])
                        , new SourcesReport());
            }
            else if (parts[0].compareTo("search") == 0) {
                for(int i = 1; i < parts.length; ++i) {
                    log.info("search {}", parts[i]);    // temporary search only first keyword
                    tracker.searchKeywords(parts[i], new SearchReport(new KadId(), new File(dir.resolve("search_" + parts[i] + ".dat").toString())));    // target id is not important here
                }
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
            else if (parts[0].compareTo("firewalled") == 0) {
                tracker.firewalled();
            }
            else if (parts[0].compareTo("sp") == 0) {
                try {
                    Gson gson = new GsonBuilder().create();
                    byte[] data = IOUtils.toByteArray(new URI("https://raw.githubusercontent.com/a-pavlov/jed2k/config/config.json"));
                    String s = new String(data);
                    GithubConfigurator gc = gson.fromJson(s, GithubConfigurator.class);
                    gc.validate();
                    if (gc.getKadStorageDescription() != null) {
                        Random rnd = new Random();
                        int pos = rnd.nextInt(gc.getKadStorageDescription().getPorts().size());
                        InetSocketAddress spAddress2 = new InetSocketAddress(gc.getKadStorageDescription().getIp(), gc.getKadStorageDescription().getPorts().get(pos));
                        log.info("[KAD] storage point address {}", spAddress2);
                        tracker.setStoragePoint(spAddress2);
                    } else {
                        log.info("[KAD] storage point disabled in github");
                    }
                } catch(Exception e) {

                }
            }
        }

        stopUpnp();
        idata.setEntries(tracker.getTrackerState());
        FUtils.write(idata, new File(dir.resolve("dht_status.dat").toString()));
        log.info("[KAD] finished");
    }
}
