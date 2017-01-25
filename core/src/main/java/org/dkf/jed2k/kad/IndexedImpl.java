package org.dkf.jed2k.kad;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Time;
import org.dkf.jed2k.kad.traversal.TimedLinkedHashMap;
import org.dkf.jed2k.protocol.kad.KadId;

import java.util.HashMap;
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

    /**
     * small storage size for mobile devices
     */
    public static final int KAD_MAX_KEYWORDS = 100;
    public static final int KAD_MAX_FILES_PER_KEYWORD = 1000;
    public static final int KAD_MAX_IP_PER_KEYWORD_FILE = 50;

    public static final int KAD_MAX_SOURCES = 1000;
    public static final int KAD_MAX_IP_PER_SOURCE = 100;

    @Data
    @EqualsAndHashCode(exclude = {"lastActivityTime", "port"})
    public static class Source implements Timed {
        private int ip;
        private int port;
        private long lastActivityTime;

        public Source(int ip, int port, long lastActivityTime) {
            this.ip = ip;
            this.port = port;
            this.lastActivityTime = lastActivityTime;
        }

        @Override
        public long getLastActiveTime() {
            return lastActivityTime;
        }
    }

    @Data
    private static class NetworkSource extends Source {
        private int portTcp;

        public NetworkSource(int ip, int port, int portTcp, long lastActivityTime) {
            super(ip, port, lastActivityTime);
            this.portTcp = portTcp;
        }
    }

    @Data
    private static class FileEntry {
        private int popularityIndex = 0;
        private String fileName;
        private long fileSize;
        private TimedLinkedHashMap<Integer, Source> sources = new TimedLinkedHashMap<>(100, 100, KADEMLIAREPUBLISHTIMEK, KAD_MAX_IP_PER_KEYWORD_FILE);

        public FileEntry(final String fileName, long fileSize) {
            this.fileName = fileName;
            this.fileSize = fileSize;
        }

        public void mergeSource(int ip, int port, long lastActivityTime) {
            Source src = sources.get(ip);
            if (src != null) {
                src.setLastActivityTime(lastActivityTime);
            } else {
                sources.put(ip, new Source(ip, port, lastActivityTime));
            }
        }
    }

    private Map<KadId, Map<KadId, FileEntry>> keywords = new HashMap<>();
    private Map<KadId, TimedLinkedHashMap<KadId, NetworkSource>> sources = new HashMap<>();

    @Override
    public int addKeyword(KadId resourceId, KadId sourceId, int ip, int port, String name, long size, long lastActivityTime) {
        assert name != null && !name.isEmpty();
        assert size >= 0;

        if (keywords.size() > KAD_MAX_KEYWORDS) {
            log.debug("[indexed] KAD_MAX_KEYWORDS exceeded");
            return 100;
        }

        Map<KadId, FileEntry> bucket = keywords.get(resourceId);

        if (bucket == null) {
            bucket = new HashMap<>();
            keywords.put(resourceId, bucket);
        } else if (bucket.size() > KAD_MAX_FILES_PER_KEYWORD) {
            log.debug("[indexed] KAD_MAX_FILES_PER_KEYWORD exceeded for {}", resourceId);
            return 100;
        }

        assert bucket != null;

        FileEntry entry = bucket.get(sourceId);

        if (entry == null) {
            entry = new FileEntry(name, size);
        }

        assert entry != null;
        entry.mergeSource(ip, port, lastActivityTime);
        return entry.getSources().size()*100/KAD_MAX_IP_PER_KEYWORD_FILE;
    }

    @Override
    public int addSource(KadId resourceId, KadId sourceId, int ip, int port, int portTcp, long lastActivityTime) {
        if (sources.size() > KAD_MAX_SOURCES) {
            return 100;
        }

        TimedLinkedHashMap<KadId, NetworkSource> resource = sources.get(resourceId);
        if (resource == null) {
            resource = new TimedLinkedHashMap<>(100, 100, KADEMLIAREPUBLISHTIMEN, KAD_MAX_IP_PER_SOURCE);
            sources.put(resourceId, resource);
        }

        assert resource != null;

        Source src = resource.get(sourceId);
        if (src != null) {
            src.setLastActivityTime(lastActivityTime);
        } else {
            resource.put(sourceId, new NetworkSource(ip, port, portTcp, lastActivityTime));
        }

        return resource.size()/KAD_MAX_IP_PER_SOURCE;
    }

    public int getKeywordsCount() {
        return keywords.size();
    }

    public int getSourcesCount() {
        return sources.size();
    }
}
