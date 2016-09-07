package org.dkf.jed2k.alert;

/**
 * Created by inkpot on 24.07.2016.
 */
public class ServerStatusAlert extends ServerAlert {
    public final int filesCount;
    public final int usersCount;

    public ServerStatusAlert(final String id, int fc, int uc) {
        super(id);
        filesCount = fc;
        usersCount = uc;
    }

    @Override
    public String toString() {
        return "server status files " + filesCount + " users " + usersCount;
    }
}
