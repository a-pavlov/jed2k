package org.jed2k.protocol.client;

import org.jed2k.protocol.Hash;

public class StartUpload extends Hash {

    public StartUpload(Hash h) {
        super(h);
    }

    @Override
    public String toString() {
        return String.format("StartUpload %s", super.toString());
    }
}
