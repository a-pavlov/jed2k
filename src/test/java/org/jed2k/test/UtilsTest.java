package org.jed2k.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
import static org.jed2k.Utils.int2Address;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;


public class UtilsTest {
    
    @Test
    public void testIpAddressConversion() throws UnknownHostException {        
        assertEquals(InetAddress.getByName("0.0.0.22"), int2Address(0x16));
        assertEquals(InetAddress.getByName("0.0.0.16"), int2Address(0x10));
        assertEquals(InetAddress.getByName("127.0.0.1"), int2Address(0x7f000001));
        assertEquals(InetAddress.getByName("196.127.10.1"), int2Address(0xc47f0a01));
    }
}
