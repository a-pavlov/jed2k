package org.jed2k.protocol.tag.test;

import java.nio.ByteBuffer;

import org.junit.Test;

import static junit.framework.Assert.assertTrue;

import org.jed2k.protocol.NetworkBuffer;
import org.jed2k.protocol.tag.Tag;
import org.jed2k.protocol.tag.TagType;

public class TagTest {
    
    @Test
    public void testTag(){
        byte source[] = {(byte)0x00};
        TagType tt;
        tt = TagType.TAGTYPE_HASH16;
        
        NetworkBuffer nb = new NetworkBuffer(ByteBuffer.wrap(source));
        Tag.extractTag(nb);
        assertTrue(true);
    }
}