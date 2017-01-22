package org.dkf.jed2k.kad;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Time;
import org.dkf.jed2k.protocol.kad.KadId;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by inkpot on 21.01.2017.
 */
@Slf4j
public class IndexedImpl implements Indexed {
    /**
     * just ported constants from aMule for KAD
     */
    public static final long KADEMLIAASKTIME = Time.seconds(1);
    public static final int  KADEMLIATOTALFILE = 5;		//Total files to search sources for.
    public static final long KADEMLIAREASKTIME = Time.hours(1);	//1 hour
    public static final long KADEMLIAPUBLISHTIME = Time.seconds(2);		//2 second
    public static final int  KADEMLIATOTALSTORENOTES	= 1;		//Total hashes to store.
    public static final int  KADEMLIATOTALSTORESRC = 3;	//Total hashes to store.
    public static final int  KADEMLIATOTALSTOREKEY =	2;	//Total hashes to store.
    public static final long KADEMLIAREPUBLISHTIMES = Time.hours(5);		//5 hours
    public static final long KADEMLIAREPUBLISHTIMEN	= Time.hours(24);   //24 hours
    public static final long KADEMLIAREPUBLISHTIMEK	= Time.hours(24);   //24 hours
    public static final long KADEMLIADISCONNECTDELAY = Time.minutes(20); //20 mins
    public static final int  KADEMLIAMAXINDEX = 50000;	//Total keyword indexes.
    public static final int  KADEMLIAHOTINDEX = KADEMLIAMAXINDEX - 5000;
    public static final int  KADEMLIAMAXENTRIES = 60000;	//Total keyword entries.
    public static final int  KADEMLIAMAXSOURCEPERFILE = 1000;    //Max number of sources per file in index.
    public static final int  KADEMLIAMAXNOTESPERFILE = 150;	//Max number of notes per entry in index.
    public static final int  KADEMLIAFIREWALLCHECKS = 4;	//Firewallcheck Request at a time

    @Data
    @EqualsAndHashCode(exclude = {"lastActivityTime"})
    private static class Source {
        private int ip;
        private int port;
        private long lastActivityTime;

        public Source(int ip, int port, long lastActivityTime) {
            this.ip = ip;
            this.port = port;
            this.lastActivityTime = lastActivityTime;
        }
    }

    @Data
    private static class NetworkSource extends Source {
        private final KadId id;
        private int portTcp;

        public NetworkSource(final KadId id, int ip, int port, int portTcp, long lastActivityTime) {
            super(ip, port, lastActivityTime);
            this.id = id;
            this.portTcp = portTcp;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;

            NetworkSource that = (NetworkSource) o;

            if (portTcp != that.portTcp) return false;
            return id != null ? id.equals(that.id) : that.id == null;

        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (id != null ? id.hashCode() : 0);
            result = 31 * result + portTcp;
            return result;
        }
    }


    @Data
    private static class FileEntry {
        private int popularityIndex = 0;
        private String fileName;
        private long fileSize;
        private List<Source> sources = new LinkedList<>();

        public FileEntry(final String fileName, long fileSize) {
            this.fileName = fileName;
            this.fileSize = fileSize;
        }

        public void mergeSource(int ip, int port, long lastActivityTime) {
            for(final Source s: sources) {
                if (s.getIp() == ip) {
                    s.setPort(port);
                    s.setLastActivityTime(lastActivityTime);
                    return;
                }
            }

            sources.add(new Source(ip, port, lastActivityTime));
        }
    }

    private Map<KadId, Map<KadId, FileEntry>> keywords = new HashMap<>();
    private Map<KadId, List<NetworkSource>> sources = new HashMap<>();

    @Override
    public int addKeyword(KadId resourceId, KadId sourceId, int ip, int port, String name, long size, long lastActivityTime) {
        assert name != null && !name.isEmpty();
        assert size >= 0;

        if (keywords.size() > KADEMLIAMAXENTRIES) {
            log.debug("[indexed] KADEMLIAMAXENTRIES exceeded");
            return 100;
        }

        Map<KadId, FileEntry> bucket = keywords.get(resourceId);

        if (bucket == null) {
            bucket = new HashMap<>();
            keywords.put(resourceId, bucket);
        } else if (bucket.size() > KADEMLIAMAXINDEX) {
            log.debug("[indexed] KADEMLIAMAXINDEX exceeded for {}", resourceId);
            return 100;
        }

        assert bucket != null;

        FileEntry entry = bucket.get(sourceId);

        if (entry == null) {
            entry = new FileEntry(name, size);
        } else if (entry.getSources().size() > KADEMLIAHOTINDEX) {
            log.debug("[indexed] KADEMLIAHOTINDEX detected");
            return 100;
        }

        assert entry != null;
        entry.mergeSource(ip, port, lastActivityTime);
        return entry.getSources().size()*100/KADEMLIAMAXINDEX;
    }

    @Override
    public int addSource(KadId resourceId, KadId sourceId, int ip, int port, int portTcp, long lastActivityTime) {
        if (sources.size() > KADEMLIAMAXINDEX) {
            return 100;
        }

        List<NetworkSource> resource = sources.get(resourceId);
        if (resource == null) {
            resource = new LinkedList<>();
            sources.put(resourceId, resource);
        }

        assert resource != null;
        NetworkSource newSrc = new NetworkSource(sourceId, ip, port, portTcp, lastActivityTime);

        for(final NetworkSource ns: resource) {
            if (newSrc.equals(ns)) {
                ns.setLastActivityTime(lastActivityTime);
                return resource.size()*100/KADEMLIAMAXSOURCEPERFILE;
            }
        }

        // TODO - add removing oldest entries to cleanup space in storage instead of skip new source
        if (resource.size() < KADEMLIAMAXSOURCEPERFILE) {
            resource.add(newSrc);
        }

        return resource.size()/KADEMLIAMAXSOURCEPERFILE;
    }

    public int getKeywordsCount() {
        return keywords.size();
    }

    public int getSourcesCount() {
        return sources.size();
    }
}
