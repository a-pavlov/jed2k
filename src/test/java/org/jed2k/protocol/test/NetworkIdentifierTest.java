package org.jed2k.protocol.test;


import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.junit.Test;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.exception.JED2KException;

public class NetworkIdentifierTest{
  @Test
  public void testSerialize() throws JED2KException {
    byte source[] = {
      (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00,  (byte)0x01, (byte)0x00      
    };
    
    NetworkIdentifier ni = new NetworkIdentifier();
    ByteBuffer nb = ByteBuffer.wrap(source);
    nb.order(ByteOrder.LITTLE_ENDIAN);
    ni.get(nb);
    assertEquals(1, ni.ip);
    assertEquals(1, ni.port);    
  }
}