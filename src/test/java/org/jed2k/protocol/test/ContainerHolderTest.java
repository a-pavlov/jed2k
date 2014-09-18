package org.jed2k.protocol.test;


import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import static org.jed2k.protocol.Unsigned.uint8;

import org.jed2k.protocol.UInt8;
import org.jed2k.protocol.ContainerHolder;
import org.jed2k.protocol.NetworkIdentifier;

public class ContainerHolderTest{
    
    @Test
    public void testHoldersCreation(){
        ContainerHolder<UInt8, NetworkIdentifier> cni = new ContainerHolder<UInt8, NetworkIdentifier>(uint8(), new ArrayList<NetworkIdentifier>(), NetworkIdentifier.class);
        assertTrue(cni.isConsistent());
    }
}