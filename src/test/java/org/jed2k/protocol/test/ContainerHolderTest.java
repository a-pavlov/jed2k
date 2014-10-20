package org.jed2k.protocol.test;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertEquals;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;

import org.junit.Test;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.UInt8;
import org.jed2k.protocol.ContainerHolder;
import org.jed2k.protocol.NetworkIdentifier;

import static org.jed2k.protocol.Unsigned.uint8;

public class ContainerHolderTest{
    
    @Test
    public void testHolderSerialization() throws JED2KException{
      byte source[] = {(byte)0x02, // size
          (byte)0x01, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x05, (byte)0x00,   // net identifier 1
          (byte)0x02, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x06, (byte)0x00};  // net identifier 2
      
      LinkedList<NetworkIdentifier> nets = new LinkedList<NetworkIdentifier>();
      ContainerHolder<UInt8, NetworkIdentifier> cni = new ContainerHolder<UInt8, NetworkIdentifier>(uint8(), nets, NetworkIdentifier.class);
      assertTrue(cni != null);
      ByteBuffer nb = ByteBuffer.wrap(source);
      nb.order(ByteOrder.LITTLE_ENDIAN);
      cni.get(nb);
      assertEquals(2, cni.count());
      assertEquals(2, cni.sizeCollection());
      
      assertEquals(1, nets.get(0).ip);
      assertEquals(2, nets.get(1).ip);
      
      assertEquals(5, nets.get(0).port);
      assertEquals(6, nets.get(1).port);
    }
}