package org.dkf.jed2k.kad;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.dkf.jed2k.protocol.kad.KadId;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by inkpot on 21.01.2017.
 */
public class Dictionary implements Indexed {

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
    public boolean addKeyword(KadId resourceId, KadId sourceId, int ip, int port, String name, long size, long lastActivityTime) {
        assert name != null && !name.isEmpty();
        assert size >= 0;

        Map<KadId, FileEntry> bucket = keywords.get(resourceId);

        if (bucket == null) {
            bucket = new HashMap<>();
            keywords.put(resourceId, bucket);
        }

        assert bucket != null;

        FileEntry entry = bucket.get(sourceId);

        if (entry == null) {
            entry = new FileEntry(name, size);
        }

        assert entry != null;

        entry.mergeSource(ip, port, lastActivityTime);
        return true;
    }

    @Override
    public boolean addSource(KadId resourceId, KadId sourceId, int ip, int port, int portTcp, long lastActivityTime) {
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
                return true;
            }
        }

        resource.add(newSrc);

        return true;

    }

    public int getKeywordsCount() {
        return keywords.size();
    }

    public int getSourcesCount() {
        return sources.size();
    }
}
