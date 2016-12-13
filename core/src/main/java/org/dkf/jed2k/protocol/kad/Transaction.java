package org.dkf.jed2k.protocol.kad;

import lombok.Data;
import org.dkf.jed2k.protocol.Serializable;

/**
 * Created by inkpot on 21.11.2016.
 */
@Data
// TODO - remove only
public abstract class Transaction implements Serializable {
    /**
     *
     * @return KAD id of requester - in other hands our KAD id
     */
    public KadId getSelfId() {
        return new KadId();
    }

    /**
     *
     * @return KAD id of searching resource
     */
    public KadId getTargetId() {
        return new KadId();
    }
}
