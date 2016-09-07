package org.dkf.jed2k.alert;

/**
 * Created by inkpot on 24.07.2016.
 */
public class ServerMessageAlert extends ServerAlert {
    public final String msg;

    public ServerMessageAlert(final String id, final String s) {
        super(id);
        msg = s;
    }

    @Override
    public String toString() {
        return "server message: " + msg;
    }
}
