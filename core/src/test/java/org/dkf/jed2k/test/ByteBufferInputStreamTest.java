package org.dkf.jed2k.test;

import org.dkf.jed2k.ByteBufferInputStream;
import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Scanner;

import static org.junit.Assert.*;

/**
 * Created by apavlov on 07.03.17.
 */
public class ByteBufferInputStreamTest {
    @Test
    public void testBBInputStream() {
        byte data[] = { 0x20, 0x20, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x20, 0x20 };
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        ByteBufferInputStream inputStream = new ByteBufferInputStream(bb);
        Scanner s = new Scanner(inputStream).skip("");

        assertTrue(s.hasNext());
        assertEquals("0123456", s.next());
        assertFalse(bb.hasRemaining());
    }
}
