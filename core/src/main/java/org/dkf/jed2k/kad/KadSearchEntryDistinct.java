package org.dkf.jed2k.kad;

import org.dkf.jed2k.protocol.SearchEntry;
import org.dkf.jed2k.protocol.kad.KadSearchEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by inkpot on 04.01.2017.
 */
public class KadSearchEntryDistinct {

    /**
     * stupid distincter with O(n^2)
     * remove duplicates and prefer entries with max sources count
     * @param entries incoming DHT search entries
     * @return distinct list of search entries without duplicates
     */
    public static List<SearchEntry> distinct(final List<KadSearchEntry> entries) {
        List<SearchEntry> res = new ArrayList<>();

        for(final KadSearchEntry entry: entries) {
            int prevIndex = -1;
            for(int i = 0; i < res.size(); ++i) {
                if (res.get(i).getHash().equals(entry.getHash())) {
                    prevIndex = i;
                    break;
                }
            }

            if (prevIndex != -1) {
                if (res.get(prevIndex).getSources() < entry.getSources()) {
                    res.set(prevIndex, entry);
                }
            } else {
                res.add(entry);
            }
        }

        return res;
    }

}
