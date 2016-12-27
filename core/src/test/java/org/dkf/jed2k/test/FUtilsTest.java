package org.dkf.jed2k.test;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.UInt8;
import org.dkf.jed2k.util.FUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by inkpot on 18.12.2016.
 */
public class FUtilsTest {

    @Rule
    public TemporaryFolder folder= new TemporaryFolder();

    @Test
    public void testReadWrite() throws IOException, JED2KException {
        File temp = folder.newFile("temp.dat");
        Container<UInt8, Hash> d = Container.makeByte(Hash.class);
        d.add(Hash.EMULE);
        d.add(Hash.TERMINAL);
        d.add(Hash.LIBED2K);
        FUtils.write(d, temp);
        Container<UInt8, Hash> r = Container.makeByte(Hash.class);
        FUtils.read(r, temp);
        assertEquals(d.size(), r.size());
        for(int i = 0; i < d.size(); ++i) {
            assertEquals(d.get(i), r.get(i));
        }
    }

}
