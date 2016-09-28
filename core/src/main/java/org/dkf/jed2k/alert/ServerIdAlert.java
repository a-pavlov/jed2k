package org.dkf.jed2k.alert;

/**
 * Created by inkpot on 08.09.2016.
 */
public class ServerIdAlert extends ServerAlert {
    public final int userId;

    public ServerIdAlert(final String id, int userId) {
        super(id);
        this.userId = userId;
    }
}
