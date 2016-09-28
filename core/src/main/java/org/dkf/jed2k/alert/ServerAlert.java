package org.dkf.jed2k.alert;

/**
 * Created by ap197_000 on 07.09.2016.
 */
public class ServerAlert extends Alert {
    public final String identifier;

    public ServerAlert(final String id) {
        assert(id != null);
        identifier = id;
    }

    @Override
    public Severity severity() {
        return null;
    }

    @Override
    public int category() {
        return 0;
    }
}
