package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.protocol.Hash;

/**
 * Created by inkpot on 05.11.2016.
 */
public class GetFileSourcesObfu extends GetFileSources {
    public GetFileSourcesObfu(Hash h, int hi, int lo) {
        super(h, hi, lo);
    }
}
