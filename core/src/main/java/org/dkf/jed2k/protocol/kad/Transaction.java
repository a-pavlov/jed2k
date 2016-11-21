package org.dkf.jed2k.protocol.kad;

import lombok.Data;
import org.dkf.jed2k.protocol.Serializable;

/**
 * Created by inkpot on 21.11.2016.
 */
@Data
public abstract class Transaction implements Serializable {
    public static final byte BOOTSTRAP = 0x01;
    public static final byte PING_PONG = 0x02;
    public static final byte HELLO = 0x03;
    public static final byte REQ_RES = 0x04;

    public abstract byte getTransactionId();
}
