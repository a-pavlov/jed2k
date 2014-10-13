package org.jed2k.protocol.tag;

import java.io.UnsupportedEncodingException;

import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.jed2k.Utils;
import org.jed2k.protocol.Buffer;
import org.jed2k.protocol.ByteContainer;
import org.jed2k.protocol.Hash;
import org.jed2k.protocol.ProtocolException;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.UInt16;
import org.jed2k.protocol.UInt32;
import org.jed2k.protocol.UNumber;

import static org.jed2k.protocol.Unsigned.uint8;
import static org.jed2k.protocol.Unsigned.uint16;
import static org.jed2k.protocol.Unsigned.uint32;
import static org.jed2k.protocol.Unsigned.uint64;
import static org.jed2k.Utils.sizeof;

public final class Tag implements Serializable {

    private static Logger log = Logger.getLogger(Tag.class.getName());
    
    public static final byte TAGTYPE_UNDEFINED    = (byte)0x00; // special tag definition for empty objects
    public static final byte TAGTYPE_HASH16       = (byte)0x01;
    public static final byte TAGTYPE_STRING       = (byte)0x02;
    public static final byte TAGTYPE_UINT32       = (byte)0x03;
    public static final byte TAGTYPE_FLOAT32      = (byte)0x04;
    public static final byte TAGTYPE_BOOL         = (byte)0x05;
    public static final byte TAGTYPE_BOOLARRAY    = (byte)0x06;
    public static final byte TAGTYPE_BLOB         = (byte)0x07;
    public static final byte TAGTYPE_UINT16       = (byte)0x08;
    public static final byte TAGTYPE_UINT8        = (byte)0x09;
    public static final byte TAGTYPE_BSOB         = (byte)0x0A;
    public static final byte TAGTYPE_UINT64       = (byte)0x0B;

    // Compressed string types
    public static final byte TAGTYPE_STR1         = (byte)0x11;
    public static final byte TAGTYPE_STR2         = (byte)0x12;
    public static final byte TAGTYPE_STR3         = (byte)0x13;
    public static final byte TAGTYPE_STR4         = (byte)0x14;
    public static final byte TAGTYPE_STR5         = (byte)0x15;
    public static final byte TAGTYPE_STR6         = (byte)0x16;
    public static final byte TAGTYPE_STR7         = (byte)0x17;
    public static final byte TAGTYPE_STR8         = (byte)0x18;
    public static final byte TAGTYPE_STR9         = (byte)0x19;
    public static final byte TAGTYPE_STR10        = (byte)0x20;
    public static final byte TAGTYPE_STR11        = (byte)0x21;
    public static final byte TAGTYPE_STR12        = (byte)0x22;
    public static final byte TAGTYPE_STR13        = (byte)0x23;
    public static final byte TAGTYPE_STR14        = (byte)0x24;
    public static final byte TAGTYPE_STR15        = (byte)0x25;
    public static final byte TAGTYPE_STR16        = (byte)0x26;
    public static final byte TAGTYPE_STR17        = (byte)0x27;  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
                    // only because of a flaw, those tags are handled correctly,
                    // but should not be handled at all
    public static final byte TAGTYPE_STR18        = (byte)0x28;  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
            //  only because of a flaw, those tags are handled correctly,
            // but should not be handled at all
    public static final byte TAGTYPE_STR19        = (byte)0x29;  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
            // only because of a flaw, those tags are handled correctly,
            // but should not be handled at all
    public static final byte TAGTYPE_STR20        = (byte)0x30;  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
            // only because of a flaw, those tags are handled correctly,
            // but should not be handled at all
    public static final byte TAGTYPE_STR21        = (byte)0x31;  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
            // only because of a flaw, those tags are handled correctly,
            // but should not be handled at all
    public static final byte TAGTYPE_STR22        = (byte)0x32;
    
    
    public static final byte FT_UNDEFINED          = (byte)0x00;    // undefined tag
    public static final byte FT_FILENAME           = (byte)0x01;    // <string>
    public static final byte FT_FILESIZE           = (byte)0x02;    // <uint32>
    public static final byte FT_FILESIZE_HI        = (byte)0x3A;    // <uint32>
    public static final byte FT_FILETYPE           = (byte)0x03;    // <string> or <uint32>
    public static final byte FT_FILEFORMAT         = (byte)0x04;    // <string>
    public static final byte FT_LASTSEENCOMPLETE   = (byte)0x05;    // <uint32>
    public static final byte FT_TRANSFERRED        = (byte)0x08;    // <uint32>
    public static final byte FT_GAPSTART           = (byte)0x09;    // <uint32>
    public static final byte FT_GAPEND             = (byte)0x0A;    // <uint32>
    public static final byte FT_PARTFILENAME       = (byte)0x12;    // <string>
    public static final byte FT_OLDDLPRIORITY      = (byte)0x13;    // Not used anymore
    public static final byte FT_STATUS             = (byte)0x14;    // <uint32>
    public static final byte FT_SOURCES            = (byte)0x15;    // <uint32>
    public static final byte FT_PERMISSIONS        = (byte)0x16;    // <uint32>
    public static final byte FT_OLDULPRIORITY      = (byte)0x17;    // Not used anymore
    public static final byte FT_DLPRIORITY         = (byte)0x18;    // Was 13
    public static final byte FT_ULPRIORITY         = (byte)0x19;    // Was 17
    public static final byte FT_KADLASTPUBLISHKEY  = (byte)0x20;    // <uint32>
    public static final byte FT_KADLASTPUBLISHSRC  = (byte)0x21;    // <uint32>
    public static final byte FT_FLAGS              = (byte)0x22;    // <uint32>
    public static final byte FT_DL_ACTIVE_TIME     = (byte)0x23;    // <uint32>
    public static final byte FT_CORRUPTEDPARTS     = (byte)0x24;    // <string>
    public static final byte FT_DL_PREVIEW         = (byte)0x25;
    public static final byte FT_KADLASTPUBLISHNOTES= (byte)0x26;    // <uint32>
    public static final byte FT_AICH_HASH          = (byte)0x27;
    public static final byte FT_FILEHASH           = (byte)0x28;
    public static final byte FT_COMPLETE_SOURCES   = (byte)0x30;    // nr. of sources which share a
    public static final byte FT_FAST_RESUME_DATA   = (byte)0x31;   // fast resume data array
    
    public static final byte CT_NAME                         = (byte)0x01;
    public static final byte CT_SERVER_UDPSEARCH_FLAGS       = (byte)0x0E;
    public static final byte CT_PORT                         = (byte)0x0F;
    public static final byte CT_VERSION                      = (byte)0x11;
    public static final byte CT_SERVER_FLAGS                 = (byte)0x20; // currently only used to inform a server about supported features
    public static final byte CT_EMULECOMPAT_OPTIONS          = (byte)0xEF;
    public static final byte CT_EMULE_RESERVED1              = (byte)0xF0;
    public static final byte CT_EMULE_RESERVED2              = (byte)0xF1;
    public static final byte CT_EMULE_RESERVED3              = (byte)0xF2;
    public static final byte CT_EMULE_RESERVED4              = (byte)0xF3;
    public static final byte CT_EMULE_RESERVED5              = (byte)0xF4;
    public static final byte CT_EMULE_RESERVED6              = (byte)0xF5;
    public static final byte CT_EMULE_RESERVED7              = (byte)0xF6;
    public static final byte CT_EMULE_RESERVED8              = (byte)0xF7;
    public static final byte CT_EMULE_RESERVED9              = (byte)0xF8;
    public static final byte CT_EMULE_UDPPORTS               = (byte)0xF9;
    public static final byte CT_EMULE_MISCOPTIONS1           = (byte)0xFA;
    public static final byte CT_EMULE_VERSION                = (byte)0xFB;
    public static final byte CT_EMULE_BUDDYIP                = (byte)0xFC;
    public static final byte CT_EMULE_BUDDYUDP               = (byte)0xFD;
    public static final byte CT_EMULE_MISCOPTIONS2           = (byte)0xFE;
    public static final byte CT_EMULE_RESERVED13             = (byte)0xFF;
    
    
    private static class FloatSerial implements Serializable {
        public float value;        
        
        FloatSerial(float value) {
            this.value = value;
        }
        
        @Override
        public Buffer get(Buffer src) throws ProtocolException {
            value = src.getFloat();
            return src;
        }

        @Override
        public Buffer put(Buffer dst) throws ProtocolException {  return dst.put(value); }

        @Override
        public int size() {
            // TODO Auto-generated method stub
            return 0;
        }
    }
    
    private static class BooleanSerial implements Serializable {
        public boolean value;
        
        BooleanSerial(boolean value) {
            this.value = value;
        }
        
        @Override
        public Buffer get(Buffer src) throws ProtocolException {
            value = (src.getByte() == 0x00);    
            return src;
        }

        @Override
        public Buffer put(Buffer dst) throws ProtocolException {
            byte bval = (value)?(byte)0x01:(byte)0x00;
            return dst.put(bval);
        }

        @Override
        public int size() {
            return sizeof(value);
        }        
    }
    
    private static class StringSerial implements Serializable {
        public byte type;
        public String value = null;
        
        StringSerial(byte type, String value) {
            this.type = type;
            this.value = value;
        }
        
        @Override
        public Buffer get(Buffer src) throws ProtocolException {
            short size = 0;
            if (type >= Tag.TAGTYPE_STR1 && type <= Tag.TAGTYPE_STR16){
                size = (short)(type - Tag.TAGTYPE_STR1 + 1);
            } else {
                size = src.getShort();
            }
            
            byte[] data = new byte[size];
            src.get(data);
            try {
                value = new String(data, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.severe(e.getMessage());
                throw new ProtocolException(e);
            }
            
            return src;
        }
        
        @Override
        public Buffer put(Buffer dst) throws ProtocolException {
            try {
                byte[] data = value.getBytes("UTF-8");
                if (type == Tag.TAGTYPE_STRING)
                    dst.put((short)data.length);            
                dst.put(data);
            } catch (UnsupportedEncodingException e) {
                log.severe(e.getMessage());
                throw new ProtocolException(e);
            }
            return dst;
        }

        @Override
        public int size() {
            // TODO Auto-generated method stub
            return 0;
        }
    }
    
    public final class BoolArraySerial implements Serializable {    
        private final UInt16 length = uint16();
        private byte value[] = null;               
        
        @Override
        public Buffer put(Buffer dst) throws ProtocolException {
            assert(false);
            return dst;
        }

        @Override
        public Buffer get(Buffer src) throws ProtocolException {
            length.get(src);
            value = new byte[length.intValue() / 8];
            return src.get(value);
        }

        @Override
        public int size() {
            return length.size() + length.size()/8;
        }
    }
    
    protected byte type;
    protected byte id;
    String name = null;
    Serializable value = null;
    float float_value;    
    
    private Tag(byte type, byte id, String name, Serializable value) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.value = value;
    }
    
    // uninitialized instance of Tag
    public Tag() {
        this.type = TAGTYPE_UNDEFINED;
        this.id = FT_UNDEFINED;
    }
    
    @Override 
    public Buffer get(Buffer src) throws ProtocolException {
        this.type = src.getByte();
        
        if ((type & 0x80) != 0){
            type = (byte)(type & 0x7f);
            id = src.getByte();
            log.info("process new type");
        } else {
            log.info("process old type");
            ByteContainer<UInt16> bc = new ByteContainer<UInt16>(uint16());
            bc.get(src);
            if (bc.size.intValue() == 1) {                
                id = bc.value[0];
            } else {
                name = bc.toString();
            }            
        }
        
        switch(type){
        case TAGTYPE_UINT8:
            value = uint8();
            break;
        case TAGTYPE_UINT16:
            value = uint16();
            break;
        case TAGTYPE_UINT32:
            value = uint32();
            break;
        case TAGTYPE_UINT64:
            value = uint64();
            break;
        case TAGTYPE_FLOAT32:
            value = new FloatSerial(0.0f);
            break;
        case TAGTYPE_BOOL:
            value = new BooleanSerial(false);
            break;
        case TAGTYPE_STR1:
        case TAGTYPE_STR2:
        case TAGTYPE_STR3:
        case TAGTYPE_STR4:
        case TAGTYPE_STR5:
        case TAGTYPE_STR6:
        case TAGTYPE_STR7:
        case TAGTYPE_STR8:
        case TAGTYPE_STR9:
        case TAGTYPE_STR10:
        case TAGTYPE_STR11:
        case TAGTYPE_STR12:
        case TAGTYPE_STR13:
        case TAGTYPE_STR14:
        case TAGTYPE_STR15:
        case TAGTYPE_STR16:
        case TAGTYPE_STRING:
            value = new StringSerial(type, null);
            break;
        case TAGTYPE_BLOB:
            value = new ByteContainer<UInt32>(uint32());
            break;
        case TAGTYPE_BOOLARRAY:
            value = new BoolArraySerial();
            break;
        case TAGTYPE_HASH16:
            value = new Hash();
            break;
        default:
            log.warning("Unknown tag type: " + Utils.byte2String(type));
            throw new ProtocolException("Unknown tag type " + Utils.byte2String(type));
        };
        
        assert(value != null);
        value.get(src);        
        return src;
    }
    
    @Override
    public Buffer put(Buffer dst) throws ProtocolException {
        assert(initialized());
        assert(value != null);
        if (name == null){
            dst.put((byte)(type | 0x80));
            dst.put(id);
        } else {
            byte[] data = name.getBytes(Charset.forName("UTF-8"));
            dst.put(type).put((short)data.length).put(data);
        }
       
        
        return value.put(dst);
    }
    
    public final boolean initialized() {
        return type != TAGTYPE_UNDEFINED && (id != FT_UNDEFINED || name != null);
    }
    
    public final byte type() {
        return type;
    }
    
    public final byte id() {
        return id;
    }
    
    public final String name() {
        return name;
    }
    
    public final String stringValue() throws ProtocolException {
        assert(initialized());
        StringSerial ss = (StringSerial)value;
        if (ss == null) throw new ProtocolException("Ivalid cast tag to string");
        return ss.value;
    }
    
    public final int intValue() throws ProtocolException {
        assert(initialized());
        UNumber n = (UNumber)value;
        if (value == null)  throw new ProtocolException("Invalid cast tag to int");        
        return n.intValue();
    }
    
    public final long longValue() throws ProtocolException {
        assert(initialized());
        UNumber n = (UNumber)value;
        if (n == null) throw new ProtocolException("Invalid cast tag to long");
        return n.longValue();
    }
    
    public final float floatValue() throws ProtocolException {
        assert(initialized());
        FloatSerial fs = (FloatSerial)value;
        if (fs == null) throw new ProtocolException("Invalid cast tag to float");
        return fs.value;
    }
    
    public final Hash hashValue() throws ProtocolException {
        assert(initialized());
        Hash h = (Hash)value;
        if (h == null) throw new ProtocolException("Invalid cast tag to hash");
        return h;
    }
    
    public static Tag tag(byte id, String name, int value) {        
        return new Tag(TAGTYPE_UINT32, id, name, uint32(value));
    }
    
    public static Tag tag(byte id, String name, short value) {
        return new Tag(TAGTYPE_UINT16, id, name, uint16(value));
    }
    
    public static Tag tag(byte id, String name, byte value) {
        return new Tag(TAGTYPE_UINT8, id, name, uint8(value));
    }
    
    public static Tag tag(byte id, String name, boolean value) {
        return new Tag(TAGTYPE_BOOL, id, name, new BooleanSerial(value));
    }
    
    public static Tag tag(byte id, String name, float value) {
        return new Tag(TAGTYPE_FLOAT32, id, name, new FloatSerial(value));
    }
    
    public static Tag tag(byte id, String name, String value) throws ProtocolException {
        byte type = Tag.TAGTYPE_STRING;
        
        try {
            int bytesCount = value.getBytes("UTF-8").length; 
            if (bytesCount <= 16) type = (byte)(Tag.TAGTYPE_STR1 + bytesCount - 1);
        } catch(UnsupportedEncodingException ex) {
            throw new ProtocolException(ex);
        }
        
        return new Tag(type, id, name, new StringSerial(type, value));
    }
    
    public static Tag tag(byte id, String name, Hash value) throws ProtocolException {
        return new Tag(TAGTYPE_HASH16, id, name, value);
    }

    @Override
    public int size() {
        // TODO Auto-generated method stub
        return 0;
    }
}