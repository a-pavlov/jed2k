package org.jed2k.protocol.search;

import java.io.UnsupportedEncodingException;

import org.jed2k.protocol.ByteContainer;
import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.UInt16;

import static org.jed2k.protocol.Unsigned.uint16;

public class SearchRequest {
    static byte SEARCH_TYPE_BOOL       = 0x00;
    static byte SEARCH_TYPE_STR        = 0x01;
    static byte SEARCH_TYPE_STR_TAG    = 0x02;
    static byte SEARCH_TYPE_UINT32     = 0x03;
    static byte SEARCH_TYPE_UINT64     = 0x08;
    
    
    public static Serializable makeEntry(byte operator) {
        return new BooleanEntry(operator);
    }
    
    private static ByteContainer<UInt16> generateTag(String name, byte id) throws ProtocolException {
        ByteContainer<UInt16> tag;
        if (name != null) {
            try {
                byte[] content = name.getBytes("UTF-8");
                tag = new ByteContainer<UInt16>(uint16(content.length), content);
            } catch(UnsupportedEncodingException e) {
                throw new ProtocolException(e);
            }
                        
        } else {
            byte[] nm = {id};
            tag = new ByteContainer<UInt16>(uint16(1), nm);
        }
        
        return tag;
    }
    
    public static Serializable makeEntry(String name, byte id, String value) throws ProtocolException {                
        try {
            return new StringEntry(new ByteContainer<UInt16>(uint16(), value.getBytes("UTF-8")), generateTag(name, id));
        } catch(UnsupportedEncodingException e) {
            throw new ProtocolException(e);
        }
    }
    
    public static Serializable makeEntry(String name, byte id, byte operator, long value) throws ProtocolException {
        return new NumericEntry(value, operator, generateTag(name, id));
    }
}
