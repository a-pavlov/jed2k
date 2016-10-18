package org.dkf.jed2k.alert;

import org.dkf.jed2k.exception.BaseErrorCode;

/**
 * Created by inkpot on 10.10.2016.
 */
public class PortMapAlert extends Alert {
    public int port;
    public int externalPort;
    public BaseErrorCode ec;

    public PortMapAlert(int port, int externalPort, BaseErrorCode ec) {
        this.port = port;
        this.externalPort = externalPort;
        this.ec = ec;
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
