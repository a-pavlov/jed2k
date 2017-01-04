package org.dkf.jed2k.test.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.kad.KadSearchEntryDistinct;
import org.dkf.jed2k.protocol.Container;
import org.dkf.jed2k.protocol.SearchEntry;
import org.dkf.jed2k.protocol.UInt16;
import org.dkf.jed2k.protocol.kad.KadSearchEntry;
import org.junit.Assume;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Random;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by inkpot on 05.01.2017.
 */
public class SearchKeywordTest {

    @Test
    public void convertKadSearchKeyword() throws IOException, JED2KException {
        Random rnd = new Random();
        Assume.assumeTrue(!System.getProperty("java.runtime.name").toLowerCase().startsWith("android"));
        String[] names = {"search_game.dat", "search_roxette.dat"};
        for (final String fileName : names) {
            ClassLoader classLoader = getClass().getClassLoader();
            File file = new File(classLoader.getResource(fileName).getFile());
            assertTrue(file != null);
            RandomAccessFile reader = new RandomAccessFile(file, "r");
            FileChannel inChannel = reader.getChannel();
            long fileSize = inChannel.size();
            ByteBuffer buffer = ByteBuffer.allocate((int) fileSize);
            inChannel.read(buffer);
            buffer.flip();
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            inChannel.close();
            reader.close();
            assertTrue(buffer.hasRemaining());
            Container<UInt16, KadSearchEntry> entries = Container.makeShort(KadSearchEntry.class);
            entries.get(buffer);
            assertFalse(buffer.hasRemaining());
            assertFalse(entries.isEmpty());
            Container<UInt16, KadSearchEntry> duplicated = Container.makeShort(KadSearchEntry.class);
            for(final KadSearchEntry e: entries.getList()) {
                int upper = rnd.nextInt(5) + 1;
                for(int i = 0; i < upper; ++i) {
                    duplicated.add(e);
                }
            }

            List<SearchEntry> result = KadSearchEntryDistinct.distinct(duplicated.getList());
            assertTrue(result.size() < duplicated.size());
            for(int i = 0; i < result.size(); ++i) {
                for(int j = i + 1; j < result.size(); ++j) {
                    assertFalse(result.get(i).equals(result.get(j)));
                }
            }
        }
    }
}
