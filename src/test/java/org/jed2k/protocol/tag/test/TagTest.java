package org.jed2k.protocol.tag.test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

import org.jed2k.protocol.ContainerHolder;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.UInt16;
import org.jed2k.protocol.tag.Tag;

import static org.jed2k.protocol.tag.Tag.tag;
import static org.jed2k.protocol.Unsigned.uint16;

public class TagTest {
    
    @Test
    public void initialTest() throws JED2KException {
        Tag floatTag = Tag.tag(Tag.FT_DL_PREVIEW, null, 0.1f);
        assertEquals(0.1f, floatTag.floatValue());
        assertEquals(Tag.FT_DL_PREVIEW, floatTag.id());
        assertEquals(Tag.TAGTYPE_FLOAT32, floatTag.type());
    }
    
    @Test
    public void testTagReading() throws JED2KException {                
        byte[] source =
            {   /* 2 bytes list size*/      (byte)0x09, (byte)0x00,
                /*1 byte*/          (byte)(Tag.TAGTYPE_UINT8 | 0x80),   (byte)0x10, (byte)0xED,
                /*2 bytes*/         (byte)(Tag.TAGTYPE_UINT16 | 0x80),  (byte)0x11, (byte)0x0A, (byte)0x0D,
                /*8 bytes*/         (byte)(Tag.TAGTYPE_UINT64),         (byte)0x04, (byte)0x00, (byte)0x30, (byte)0x31, (byte)0x32, (byte)0x33, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08,
                /*variable string*/ (byte)(Tag.TAGTYPE_STRING),         (byte)0x04, (byte)0x00, (byte)'A',  (byte)'B',  (byte)'C',  (byte)'D',  (byte)0x06, (byte)0x00, 'S', 'T', 'R', 'I', 'N', 'G',
                /*defined string*/  (byte)(Tag.TAGTYPE_STR5),           (byte)0x04, (byte)0x00, (byte)'I',  (byte)'V',  (byte)'A',  (byte)'N',  (byte)'A',  (byte)'P',  (byte)'P', (byte)'L', (byte)'E',
                /*blob*/            (byte)(Tag.TAGTYPE_BLOB | 0x80),    (byte)0x0A, (byte)0x03, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x0D, (byte)0x0A, (byte)0x0B,
                /*float*/           (byte)(Tag.TAGTYPE_FLOAT32 | 0x80), (byte)0x15, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04,
                /*bool*/            (byte)(Tag.TAGTYPE_BOOL | 0x80),    (byte)0x15, (byte)0x01,
                /*hash*/            (byte)(Tag.TAGTYPE_HASH16 | 0x80),  (byte)0x20, (byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03, (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07, (byte)0x08, (byte)0x09, (byte)0x0A, (byte)0x0B, (byte)0x0C, (byte)0x0D, (byte)0x0E, (byte)0x0F };        
        ArrayList<Tag> atags = new ArrayList<Tag>();
        ContainerHolder<UInt16, Tag> tags = new ContainerHolder<UInt16, Tag>(uint16(), atags, Tag.class);
        ByteBuffer nb = ByteBuffer.wrap(source);  
        nb.order(ByteOrder.LITTLE_ENDIAN);
        tags.get(nb);
        assertEquals(9, tags.count());
        assertEquals(0, nb.remaining());
        assertEquals(0xED, atags.get(0).intValue());
        assertEquals(3, atags.get(0).size());
        assertEquals(0x0D0A, atags.get(1).intValue());
        assertEquals(4, atags.get(1).size());
        assertEquals(0x0807060504030201l, atags.get(2).longValue());
        assertEquals(1 + 2 + "0123".length() + 8, atags.get(2).size());
        assertEquals("ABCD", atags.get(3).name());
        assertEquals("STRING", atags.get(3).stringValue());
        assertEquals(1 + 2 + "abcd".length() + 2 + "string".length(), atags.get(3).size());
        assertEquals("IVAN", atags.get(4).name());
        assertEquals("APPLE", atags.get(4).stringValue());
    }
    
    @Test
    public void testTagWriting() throws JED2KException {
        ByteBuffer nb = ByteBuffer.allocate(100);
        nb.order(ByteOrder.LITTLE_ENDIAN);
        tag(Tag.FT_UNDEFINED, "Test name", "XXX data").put(tag(Tag.FT_FILEHASH, null, 100).put(nb));
        nb.flip();
        Tag itag = new Tag();
        itag.get(nb);
        assertEquals(Tag.FT_FILEHASH, itag.id());
        assertEquals(100, itag.intValue());    
        assertEquals(6, itag.size());
        Tag stag = new Tag();
        stag.get(nb);
        assertEquals("Test name", stag.name());
        assertEquals("XXX data", stag.stringValue());
        assertEquals(Tag.TAGTYPE_STR8, stag.type());
        assertEquals(20, stag.size());
        assertEquals(0, nb.remaining());
    }
}