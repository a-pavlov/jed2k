package org.dkf.jed2k.alert;

/**
 * Created by inkpot on 01.09.2016.
 */
public class ListenAlert extends Alert {
    public final String details;
    public int port;

    public ListenAlert(final String det, int port) {
        this.details = det;
        this.port = port;
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
