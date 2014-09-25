package org.jed2k.protocol.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.jed2k.protocol.Unsigned.uint8;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.jed2k.hash.MD4;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.UInt8;
import org.junit.Test;
import org.jed2k.protocol.NetworkBuffer;

public class HashTest{
    private final byte[] terminal = {
            (byte)0x31, (byte)0xD6, (byte)0xCF, (byte)0xE0,
            (byte)0xD1, (byte)0x6A, (byte)0xE9, (byte)0x31, 
            (byte)0xB7, (byte)0x3C, (byte)0x59, (byte)0xD7, 
            (byte)0xE0, (byte)0xC0, (byte)0x89, (byte)0xC0};

    @Test
    public void testInitialization(){
        Hash h = new Hash();
        assertEquals(h, Hash.INVALID);
        assertEquals(h, Hash.fromString("00000000000000000000000000000000"));
        assertEquals(Hash.TERMINAL, Hash.fromBytes(terminal));
        assertEquals(new String("31D6CFE0D16AE931B73C59D7E0C089C0"), Hash.TERMINAL.toString());
    }
    
    @Test
    public void testElementarySerialization() throws ProtocolException {
        NetworkBuffer nb = new NetworkBuffer(ByteBuffer.wrap(terminal));
        Hash h = new Hash();
        h.get(nb);
        assertEquals(Hash.TERMINAL, h);
        
        ByteBuffer buff = ByteBuffer.allocate(MD4.HASH_SIZE);
        NetworkBuffer nbw = new NetworkBuffer(buff);
        Hash.LIBED2K.put(nbw);
        buff.flip();
        Hash h2 = new Hash();
        h2.get(nbw);
        assertEquals(Hash.LIBED2K, h2);
    }
}