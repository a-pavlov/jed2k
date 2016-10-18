package org.dkf.jed2k.alert;

import org.dkf.jed2k.exception.BaseErrorCode;

/**
 * Created by ap197_000 on 07.09.2016.
 */
public class ServerConectionClosed extends ServerAlert {
    public final BaseErrorCode code;

    public ServerConectionClosed(final String id, final BaseErrorCode ec) {
        super(id);
        code = ec;
    }
}
