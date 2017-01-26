package org.dkf.jed2k.kad;

import org.dkf.jed2k.protocol.kad.KadId;
import org.dkf.jed2k.protocol.kad.KadSearchEntry;

/**
 * Created by inkpot on 19.01.2017.
 */
public interface Indexed {

    /**
     * index keyword and resource
     * @param resourceId keyword KAD id
     * @param entry published entry with keyword information
     * @param lastActivityTime current time from external system
     * @return percent of taken place in storage
     */
    int addKeyword(final KadId resourceId, final KadSearchEntry entry, long lastActivityTime);

    /**
     *
     * @param resourceId file KAD id
     * @param entry published entry with source information
     * @return true if source was indexed
     */
    int addSource(final KadId resourceId, final KadSearchEntry entry, long lastActivityTime);
}
