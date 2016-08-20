package org.jed2k.alert;

/**
 * Created by inkpot on 24.07.2016.
 */
public class ServerMessageAlert extends Alert {
    public String msg;

    public ServerMessageAlert(String s) {
        msg = s;
    }

    @Override
    public Severity severity() {
        return null;
    }

    @Override
    public int category() {
        return 0;
    }

    @Override
    public String toString() {
        return "server message: " + msg;
    }
}
