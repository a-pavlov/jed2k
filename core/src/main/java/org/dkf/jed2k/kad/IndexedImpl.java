package org.dkf.jed2k.kad;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.Time;
import org.dkf.jed2k.kad.traversal.TimedLinkedHashMap;
import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadSearchEntry;

import java.util.Collection;
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

    private abstract class Decreaser {
        protected final int limit;
        public Decreaser(int limit) {
            this.limit = limit;
        }

        public abstract void decrease();
        public abstract boolean overfilled();
    }

    private class DecKeywords extends Decreaser {

        public DecKeywords() {
            super(KAD_MAX_KEYWORD_FILES);
        }

        @Override
        public boolean overfilled() { return totalKeywordFiles >= limit;  }

        @Override
        public void decrease() { totalKeywordFiles--;  }
    }

    private class DecSources extends Decreaser {

        public DecSources() {
            super(KAD_MAX_SOURCES);
        }

        @Override
        public boolean overfilled() { return totalSources >= limit; }

        @Override
        public void decrease() { totalSources--; }
    }

    private class IndexTimedLinkedHashMap<K, V extends Timed> extends TimedLinkedHashMap<K, V> {
        private final Decreaser dec;

        public IndexTimedLinkedHashMap(final Decreaser dec) {
            super(100, 10, KADEMLIAREPUBLISHTIMEN, 0);
            this.dec = dec;
            assert dec != null;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry<K,V> eldest) {
            boolean remove =  !isEmpty() && (super.removeEldestEntry(eldest) || dec.overfilled());
            if (remove) dec.decrease();
            return remove;
        }
    }

    @Data
    public static class Published implements Timed {
        private final KadSearchEntry entry;
        private long lastActiveTime;

        public Published(final KadSearchEntry entry, long lastActiveTime) {
            this.entry = entry;
            this.lastActiveTime = lastActiveTime;
        }
    }

    private Map<KadId, Map<KadId, Published>> keywords = new HashMap<>();
    private Map<KadId, Map<KadId, Published>> sources = new HashMap<>();

    @Override
    public int addKeyword(KadId resourceId, final KadSearchEntry entry, long lastActivityTime) {

        Map<KadId, Published> bucket = keywords.get(resourceId);

        if (bucket == null) {
            // we can't create new root
            if (totalKeywordFiles >= KAD_MAX_KEYWORD_FILES) {
                return 100;
            }

            bucket = new IndexTimedLinkedHashMap<>(new DecKeywords());
            keywords.put(resourceId, bucket);
        }

        assert bucket != null;

        Published fe = bucket.get(entry.getKid());

        if (fe != null) {
            fe.setLastActiveTime(lastActivityTime);
        }
        else {
            fe = new Published(entry, lastActivityTime);
            bucket.put(entry.getKid(), fe);
            totalKeywordFiles++;
        }

        assert entry != null;
        return totalKeywordFiles*100/KAD_MAX_KEYWORD_FILES; // zero?
    }

    @Override
    public int addSource(KadId resourceId, final KadSearchEntry entry, long lastActivityTime) {
        Map<KadId, Published> resource = sources.get(resourceId);

        if (resource == null) {
            // can't create new root
            if (totalSources >= KAD_MAX_SOURCES) {
                return 100;
            }

            resource = new IndexTimedLinkedHashMap<>(new DecSources());
            sources.put(resourceId, resource);
        }

        assert resource != null;

        Published src = resource.get(entry.getKid());

        if (src != null) {
            src.setLastActiveTime(lastActivityTime);
        } else {
            resource.put(entry.getKid(), new Published(entry, lastActivityTime));
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

    public Collection<Published> getFileByHash(final KadId id) {
        Map<KadId, Published> res = keywords.get(id);
        if (res != null) return res.values();
        return null;
    }

    public Collection<Published> getSourceByHash(final KadId id) {
        Map<KadId, Published> res = sources.get(id);
        if (res != null) return res.values();
        return null;
    }
}
