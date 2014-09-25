package org.jed2k.protocol.test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.util.LinkedList;

import org.junit.Test;

import static org.jed2k.protocol.Unsigned.uint8;

import org.jed2k.protocol.NetworkBuffer;
import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.UInt8;
import org.jed2k.protocol.ContainerHolder;
import org.jed2k.protocol.NetworkIdentifier;

public class ContainerHolderTest{
    
    @Test
    public void testHolderSerialization() throws ProtocolException{
      byte source[] = {(byte)0x02, // size
          (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x05, (byte)0x00,   // net identifier 1
          (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x00};  // net identifier 2
      
      LinkedList<NetworkIdentifier> nets = new LinkedList<NetworkIdentifier>();
      ContainerHolder<UInt8, NetworkIdentifier> cni = new ContainerHolder<UInt8, NetworkIdentifier>(uint8(), nets, NetworkIdentifier.class);
      assertTrue(cni != null);
      NetworkBuffer nb = new NetworkBuffer(ByteBuffer.wrap(source));
      cni.get(nb);
      assertEquals(2, cni.size());
      assertEquals(2, cni.sizeCollection());
      
      assertEquals(1, nets.get(0).client_id.intValue());
      assertEquals(2, nets.get(1).client_id.intValue());
      
      assertEquals(5, nets.get(0).port.intValue());
      assertEquals(6, nets.get(1).port.intValue());
    }
}