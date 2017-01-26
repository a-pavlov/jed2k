package org.dkf.jed2k.kad;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
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
    public static final int KAD_MAX_KEYWORD_FILES = 5000;
    public static final int KAD_MAX_SOURCES = 10000;

    private int totalKeywordFiles = 0;
    private int totalSources = 0;


    private class KeywordsTimedLinkedHashMap<K, V extends Timed> extends TimedLinkedHashMap<K, V> {
        public KeywordsTimedLinkedHashMap() { super(100, 10, KADEMLIAREPUBLISHTIMEN, 0); }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
            boolean remove = !isEmpty() && (super.removeEldestEntry(eldest) || totalKeywordFiles >= KAD_MAX_KEYWORD_FILES);
            if (remove) totalKeywordFiles--;
            return remove;
        }
    }

    private class SourcesTimedLinkedHashMap<K, V extends Timed> extends TimedLinkedHashMap<K, V> {

        public SourcesTimedLinkedHashMap() { super(100, 10, KADEMLIAREPUBLISHTIMEN, 0); }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
            boolean remove = !isEmpty() && (super.removeEldestEntry(eldest) || totalSources >= KAD_MAX_SOURCES);
            if (remove) totalSources--;
            return remove;
        }
    }


    @Data
    @EqualsAndHashCode(exclude = {"lastActivityTime"})
    public static class FileSource implements Timed {
        private int ip;
        private int port;
        private int tcpPort;
        private long lastActivityTime;

        public FileSource(int ip, int port, int tcpPort, long lastActivityTime) {
            this.ip = ip;
            this.port = port;
            this.tcpPort = tcpPort;
            this.lastActivityTime = lastActivityTime;
        }

        @Override
        public long getLastActiveTime() {
            return lastActivityTime;
        }
    }

    @Getter
    public static class FileEntry implements Timed {
        private int popularityIndex = 0;
        private String fileName;
        private long fileSize;
        private int lastPublisherIp = 0;
        private long lastActiveTime;

        public FileEntry(final String fileName, long fileSize, long lastActiveTime) {
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.lastActiveTime = lastActiveTime;
        }

        public void setLastPublisherIp(int ip) {
            if (lastPublisherIp != ip) {
                ++popularityIndex;
            }

            lastPublisherIp = ip;
        }

        @Override
        public long getLastActiveTime() {
            return lastActiveTime;
        }

        public void setLastActiveTime(long lastActiveTime) {
            this.lastActiveTime = lastActiveTime;
        }
    }

    private Map<KadId, Map<KadId, FileEntry>> keywords = new HashMap<>();
    private Map<KadId, Map<KadId, FileSource>> sources = new HashMap<>();

    @Override
    public int addKeyword(KadId resourceId, KadId sourceId, int ip, int port, String name, long size, long lastActivityTime) {
        assert name != null && !name.isEmpty();
        assert size >= 0;

        Map<KadId, FileEntry> bucket = keywords.get(resourceId);

        if (bucket == null) {
            // we can't create new root
            if (totalKeywordFiles >= KAD_MAX_KEYWORD_FILES) {
                return 100;
            }

            bucket = new KeywordsTimedLinkedHashMap<>();
            keywords.put(resourceId, bucket);
        }

        assert bucket != null;

        FileEntry entry = bucket.get(sourceId);

        if (entry != null) {
            entry.setLastActiveTime(lastActivityTime);
        }
        else {
            entry = new FileEntry(name, size, lastActivityTime);
            bucket.put(sourceId, entry);
            totalKeywordFiles++;
        }

        assert entry != null;

        entry.setLastPublisherIp(ip);

        return totalKeywordFiles*100/KAD_MAX_KEYWORD_FILES;
    }

    @Override
    public int addSource(KadId resourceId, KadId sourceId, int ip, int port, int portTcp, long lastActivityTime) {
        Map<KadId, FileSource> resource = sources.get(resourceId);

        if (resource == null) {
            // can't create new root
            if (totalSources >= KAD_MAX_SOURCES) {
                return 100;
            }

            resource = new SourcesTimedLinkedHashMap<>();
            sources.put(resourceId, resource);
        }

        assert resource != null;

        FileSource src = resource.get(sourceId);

        if (src != null) {
            src.setLastActivityTime(lastActivityTime);
        } else {
            resource.put(sourceId, new FileSource(ip, port, portTcp, lastActivityTime));
            totalSources++;
        }

        return totalSources/KAD_MAX_SOURCES;
    }

    public int getKeywordsCount() {
        return keywords.size();
    }

    public int getSourcesCount() {
        return sources.size();
    }

    public int getTotalFiles() {
        return totalKeywordFiles;
    }

    public int getTotalSources() {
        return totalSources;
    }

    public Map<KadId, FileEntry> getFileByHash(final KadId id) {
        return keywords.get(id);
    }

    public Map<KadId, FileSource> getSourceByHash(final KadId id) {
        return sources.get(id);
    }
}
