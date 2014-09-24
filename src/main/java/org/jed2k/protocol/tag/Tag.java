package org.jed2k.protocol.tag;

import java.nio.charset.Charset;

import org.jed2k.protocol.Buffer;
import org.jed2k.protocol.ByteContainer;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.UInt16;
import org.jed2k.protocol.UInt32;
import org.jed2k.protocol.UInt8;

import static org.jed2k.protocol.Unsigned.uint8;
import static org.jed2k.protocol.Unsigned.uint16;
import static org.jed2k.protocol.Unsigned.uint32;
import static org.jed2k.protocol.tag.TypedTag.valueOf;
import static org.jed2k.protocol.tag.FloatTag.valueOf;
import static org.jed2k.protocol.tag.BooleanTag.valueOf;
import static org.jed2k.protocol.tag.StringTag.valueOf;

public abstract class Tag implements Serializable{
       
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
    
    protected final byte type;
    protected final byte id;    
    String name;
    
    public Tag(byte type, byte id, String name){
        this.type = type;
        this.id = id;
        this.name = name;
        assert(name != null || this.id != FT_UNDEFINED);
    }
    
    @Override
    public Buffer put(Buffer dst){       
        if (name == null){
            dst.put((byte)(type | 0x80));
            dst.put(id);
        } else {
            byte[] data = name.getBytes(Charset.forName("UTF-8"));
            dst.put(type).put((short)data.length).put(data);
        }
        
        return dst;
    }
        
    public static Tag extractTag(Buffer src){
        byte type = src.getByte();
        byte id = FT_UNDEFINED;
        String name = null;
        if ((type & 0x80) != 0){
            type = (byte)(type & 0x7f);
            id = src.getByte();
        } else {
            
            ByteContainer<UInt16> bc = new ByteContainer<UInt16>(uint16());
            
            if (bc.size.intValue() == 1){
                id = bc.value[0];
            } else {
                name = bc.toString();
            }           
        }
        
        Tag result = null;
        
        switch(type){
        case TAGTYPE_UINT8:
            result = valueOf(id, name, uint8());
            break;
        case TAGTYPE_UINT16:
            result = valueOf(id, name, uint16());
            break;
        case TAGTYPE_UINT32:
            result = valueOf(id, name, uint32());
            break;
        case TAGTYPE_FLOAT32:
            result = valueOf(id, name, 0.0f);
            break;
        case TAGTYPE_BOOL:
            result = valueOf(id, name, false);
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
            result = valueOf(id, name, new String());
            break;
            
        default:
            break;
        };
        
        result.get(src);
        return result;
    }
    
    public static Tag tag(byte id, String name, int value) {
        return valueOf(id, name, uint32(value));
    }
    
    public static Tag tag(byte id, String name, short value) {
        return valueOf(id, name, uint16(value));
    }
    
    public static Tag tag(byte id, String name, byte value) {
        return valueOf(id, name, uint8(value));
    }
    
}