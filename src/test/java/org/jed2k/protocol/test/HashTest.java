package org.jed2k.protocol.test;

import static junit.framework.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.jed2k.hash.MD4;
import org.jed2k.protocol.Hash;
import org.jed2k.exception.JED2KException;
import org.junit.Test;

public class HashTest{
    private final byte[] terminal = {
            (byte)0x31, (byte)0xD6, (byte)0xCF, (byte)0xE0,
            (byte)0xD1, (byte)0x6A, (byte)0xE9, (byte)0x31, 
            (byte)0xB7, (byte)0x3C, (byte)0x59, (byte)0xD7, 
            (byte)0xE0, (byte)0xC0, (byte)0x89, (byte)0xC0};

    @Test
    public void testInitialization() {
        Hash h = new Hash();
        assertEquals(h, Hash.INVALID);
        assertEquals(h, Hash.fromString("00000000000000000000000000000000"));
        assertEquals(Hash.TERMINAL, Hash.fromBytes(terminal));
        assertEquals(new String("31D6CFE0D16AE931B73C59D7E0C089C0"), Hash.TERMINAL.toString());
    }
    
    @Test
    public void testElementarySerialization() throws JED2KException {
        ByteBuffer nb = ByteBuffer.wrap(terminal);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        Hash h = new Hash();
        h.get(nb);
        assertEquals(Hash.TERMINAL, h);
        
        ByteBuffer buff = ByteBuffer.allocate(MD4.HASH_SIZE);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer nbw = buff;
        Hash.LIBED2K.put(nbw);
        buff.flip();
        Hash h2 = new Hash();
        h2.get(nbw);
        assertEquals(Hash.LIBED2K, h2);
    }
    
    @Test
    public void testCompare() {
        Hash h = new Hash();
        assertEquals(0, h.compareTo(h));
        assertEquals(-1, h.compareTo(Hash.TERMINAL));
        assertEquals(1, Hash.fromString("10000000000000000000000000000000").compareTo(Hash.fromString("0FFFFFFFF00000000000000000000CCC")));
    }
}