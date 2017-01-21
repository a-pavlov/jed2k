package org.dkf.jed2k.kad;

import org.dkf.jed2k.protocol.kad.KadId;

/**
 * Created by inkpot on 19.01.2017.
 */
public interface Indexed {

    /**
     * index keyword and resource
     * @param resourceId keyword KAD id
     * @param sourceId resource KAD id
     * @param ip ?
     * @param port ?
     * @param name file name
     * @param size file size
     * @param lastActivityTime current time from external system
     * @return true if resource was indexed
     */
    boolean addKeyword(final KadId resourceId, final KadId sourceId, int ip, int port, final String name, long size, long lastActivityTime);

    /**
     *
     * @param resourceId file KAD id
     * @param sourceId source KAD id
     * @param ip address of endpoint
     * @param port UDP port of endpoint
     * @param portTcp TCP port of endpoint
     * @return true if source was indexed
     */
    boolean addSource(final KadId resourceId, final KadId sourceId, int ip, int port, int portTcp, long lastActivityTime);
}
