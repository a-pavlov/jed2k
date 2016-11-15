package org.dkf.jed2k.protocol.kad;

import lombok.Getter;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Serializable;
import org.dkf.jed2k.protocol.UInt32;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by inkpot on 15.11.2016.
 */
@Getter
public class KadNodesDat implements Serializable {
    private UInt32 numContacts = new UInt32();
    private UInt32 version = new UInt32();
    private UInt32 bootstrapEdition = new UInt32();
    Container<UInt32, KadEntry> bootstrapEntries = Container.makeInt(KadEntry.class);
    List<KadEntry> contacts = new LinkedList<>();
    List<KadExtEntry> extContacts = new LinkedList<>();

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        numContacts.get(src);
        if (numContacts.intValue() == 0) {
            version.get(src);
            if (version.intValue() == 3) {
                bootstrapEdition.get(src);
                if (bootstrapEdition.intValue() == 1) {
                    bootstrapEntries.get(src);
                    return src;
                }
            }

            if (version.intValue() >= 1 && version.intValue() <= 3) {
                numContacts.get(src);
            }
        }

        for (int i = 0; i != numContacts.intValue(); ++i) {
            KadEntry ke = new KadEntry();
            KadExtEntry kee = new KadExtEntry();
            ke.get(src);
            contacts.add(ke);
            if (version.intValue() >= 2) {
                kee.get(src);
                extContacts.add(kee);
            }
        }

        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        // back serialization hardcoded to version 1 to avoid useless emule features
        version.assign(1);
        numContacts.assign(0);
        version.put(numContacts.put(dst));
        numContacts.assign(contacts.size());
        numContacts.put(dst);
        for(final KadEntry e: contacts) {
            e.put(dst);
        }

        return dst;
    }

    @Override
    public int bytesCount() {
        return 0;
    }
}
