package org.dkf.jed2k.protocol.kad;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.UInt8;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 15.11.2016.
 */
@Data
@EqualsAndHashCode(callSuper = false)
@ToString
public class Kad2BootstrapRes extends Transaction {
    private KadId kid = new KadId();
    private UInt16 portTcp = new UInt16();
    private UInt8 version = new UInt8();
    private Container<UInt16, KadEntry> contacts = Container.makeShort(KadEntry.class);

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return contacts.get(version.get(portTcp.get(kid.get(src))));
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return contacts.put(version.put(portTcp.put(kid.put(dst))));
    }

    @Override
    public int bytesCount() {
        return kid.bytesCount() + portTcp.bytesCount() + version.bytesCount() + contacts.bytesCount();
    }
}
