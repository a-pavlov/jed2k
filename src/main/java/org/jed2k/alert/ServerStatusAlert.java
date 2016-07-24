package org.jed2k.alert;

/**
 * Created by inkpot on 24.07.2016.
 */
public class ServerStatusAlert extends Alert {
    public int filesCount;
    public int usersCount;

    public ServerStatusAlert(int fc, int uc) {
        filesCount = fc;
        usersCount = uc;
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
