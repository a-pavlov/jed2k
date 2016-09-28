package org.dkf.jed2k.protocol.tag;

import org.dkf.jed2k.Utils;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.*;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import static org.dkf.jed2k.Utils.sizeof;
import static org.dkf.jed2k.protocol.Unsigned.*;

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
    public static final byte TAGTYPE_STR10        = (byte)0x1A;
    public static final byte TAGTYPE_STR11        = (byte)0x1B;
    public static final byte TAGTYPE_STR12        = (byte)0x1C;
    public static final byte TAGTYPE_STR13        = (byte)0x1D;
    public static final byte TAGTYPE_STR14        = (byte)0x1E;
    public static final byte TAGTYPE_STR15        = (byte)0x1F;
    public static final byte TAGTYPE_STR16        = (byte)0x20;
    public static final byte TAGTYPE_STR17        = (byte)0x21;  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
                    // only because of a flaw, those tags are handled correctly,
                    // but should not be handled at all
    public static final byte TAGTYPE_STR18        = (byte)0x22;  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
            //  only because of a flaw, those tags are handled correctly,
            // but should not be handled at all
    public static final byte TAGTYPE_STR19        = (byte)0x23;  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
            // only because of a flaw, those tags are handled correctly,
            // but should not be handled at all
    public static final byte TAGTYPE_STR20        = (byte)0x24;  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
            // only because of a flaw, those tags are handled correctly,
            // but should not be handled at all
    public static final byte TAGTYPE_STR21        = (byte)0x25;  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
            // only because of a flaw, those tags are handled correctly,
            // but should not be handled at all
    public static final byte TAGTYPE_STR22        = (byte)0x26;


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

    public static final byte FT_PUBLISHINFO        = (byte)0x33;    // <uint32>
    public static final byte FT_ATTRANSFERRED      = (byte)0x50;    // <uint32>
    public static final byte FT_ATREQUESTED        = (byte)0x51;    // <uint32>
    public static final byte FT_ATACCEPTED         = (byte)0x52;    // <uint32>
    public static final byte FT_CATEGORY           = (byte)0x53;    // <uint32>
    public static final byte FT_ATTRANSFERREDHI    = (byte)0x54;    // <uint32>
    public static final byte FT_MEDIA_ARTIST       = (byte)0xD0;    // <string>
    public static final byte FT_MEDIA_ALBUM        = (byte)0xD1;    // <string>
    public static final byte FT_MEDIA_TITLE        = (byte)0xD2;    // <string>
    public static final byte FT_MEDIA_LENGTH       = (byte)0xD3;    // <uint32> !!!
    public static final byte FT_MEDIA_BITRATE      = (byte)0xD4;    // <uint32>
    public static final byte FT_MEDIA_CODEC        = (byte)0xD5;    // <string>
    public static final byte FT_FILERATING         = (byte)0xF7;    // <uint8>

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
    public static final byte CT_MOD_VERSION                  = (byte)0x55;


    public static final byte ET_COMPRESSION          = (byte)0x20;
    public static final byte ET_UDPPORT              = (byte)0x21;
    public static final byte ET_UDPVER               = (byte)0x22;
    public static final byte ET_SOURCEEXCHANGE       = (byte)0x23;
    public static final byte ET_COMMENTS             = (byte)0x24;
    public static final byte ET_EXTENDEDREQUEST      = (byte)0x25;
    public static final byte ET_COMPATIBLECLIENT     = (byte)0x26;
    public static final byte ET_FEATURES             = (byte)0x27;
    public static final byte ET_MOD_VERSION          = CT_MOD_VERSION;

    public static final byte ST_SERVERNAME         = (byte)0x01; // <string>
    // Unused (0x02-0x0A)
    public static final byte ST_DESCRIPTION        = (byte)0x0B; // <string>
    public static final byte ST_PING               = (byte)0x0C; // <uint32>
    public static final byte ST_FAIL               = (byte)0x0D; // <uint32>
    public static final byte ST_PREFERENCE         = (byte)0x0E; // <uint32>
    // Unused (0x0F-0x84)
    public static final byte ST_DYNIP              = (byte)0x85;
    public static final byte ST_LASTPING_DEPRECATED= (byte)0x86; // <uint32> // DEPRECATED, use 0x90
    public static final byte ST_MAXUSERS           = (byte)0x87;
    public static final byte ST_SOFTFILES          = (byte)0x88;
    public static final byte ST_HARDFILES          = (byte)0x89;
    // Unused (0x8A-0x8F)
    public static final byte ST_LASTPING           = (byte)0x90; // <uint32>
    public static final byte ST_VERSION            = (byte)0x91; // <string>
    public static final byte ST_UDPFLAGS           = (byte)0x92; // <uint32>
    public static final byte ST_AUXPORTSLIST       = (byte)0x93; // <string>
    public static final byte ST_LOWIDUSERS         = (byte)0x94; // <uint32>
    public static final byte ST_UDPKEY             = (byte)0x95; // <uint32>
    public static final byte ST_UDPKEYIP           = (byte)0x96; // <uint32>
    public static final byte ST_TCPPORTOBFUSCATION = (byte)0x97; // <uint16>
    public static final byte ST_UDPPORTOBFUSCATION = (byte)0x98; // <uint16>

    public static String type2String(byte id) {
        switch(id) {
            case TAGTYPE_UNDEFINED: return "TAGTYPE_UNDEFINED";
            case TAGTYPE_HASH16: return "TAGTYPE_HASH16";
            case TAGTYPE_STRING: return "TAGTYPE_STRING";
            case TAGTYPE_UINT32: return "TAGTYPE_UINT32";
            case TAGTYPE_FLOAT32: return "TAGTYPE_FLOAT32";
            case TAGTYPE_BOOL: return "TAGTYPE_BOOL";
            case TAGTYPE_BOOLARRAY: return "TAGTYPE_BOOLARRAY";
            case TAGTYPE_BLOB: return "TAGTYPE_BLOB";
            case TAGTYPE_UINT16: return "TAGTYPE_UINT16";
            case TAGTYPE_UINT8: return "TAGTYPE_UINT8";
            case TAGTYPE_BSOB: return "TAGTYPE_BSOB";
            case TAGTYPE_UINT64: return "TAGTYPE_UINT64";

            case TAGTYPE_STR1: return "TAGTYPE_STR1";
            case TAGTYPE_STR2: return "TAGTYPE_STR2";
            case TAGTYPE_STR3: return "TAGTYPE_STR3";
            case TAGTYPE_STR4: return "TAGTYPE_STR4";
            case TAGTYPE_STR5: return "TAGTYPE_STR5";
            case TAGTYPE_STR6: return "TAGTYPE_STR6";
            case TAGTYPE_STR7: return "TAGTYPE_STR7";
            case TAGTYPE_STR8: return "TAGTYPE_STR8";
            case TAGTYPE_STR9: return "TAGTYPE_STR9";
            case TAGTYPE_STR10: return "TAGTYPE_STR10";
            case TAGTYPE_STR11: return "TAGTYPE_STR11";
            case TAGTYPE_STR12: return "TAGTYPE_STR12";
            case TAGTYPE_STR13: return "TAGTYPE_STR13";
            case TAGTYPE_STR14: return "TAGTYPE_STR14";
            case TAGTYPE_STR15: return "TAGTYPE_STR15";
            case TAGTYPE_STR16: return "TAGTYPE_STR16";
            case TAGTYPE_STR17: return "TAGTYPE_STR17";
            case TAGTYPE_STR18: return "TAGTYPE_STR18";
            case TAGTYPE_STR19: return "TAGTYPE_STR19";
            case TAGTYPE_STR20: return "TAGTYPE_STR20";
            case TAGTYPE_STR21: return "TAGTYPE_STR21";
            case TAGTYPE_STR22: return "TAGTYPE_STR22";
            default: return "UNKNOWN";
        }
    }

    public static String id2String(byte id) {
        switch(id) {
            case FT_UNDEFINED: return "FT_UNDEFINED";
            case FT_FILENAME: return "FT_FILENAME/CT_NAME";
            case FT_FILESIZE: return "FT_FILESIZE";
            case FT_FILESIZE_HI: return "FT_FILESIZE_HI";
            case FT_FILETYPE: return "FT_FILETYPE";
            case FT_FILEFORMAT: return "FT_FILEFORMAT";
            case FT_LASTSEENCOMPLETE: return "FT_LASTSEENCOMPLETE";
            case FT_TRANSFERRED: return "FT_TRANSFERRED";
            case FT_GAPSTART: return "FT_GAPSTART";
            case FT_GAPEND: return "FT_GAPEND";
            case FT_PARTFILENAME: return "FT_PARTFILENAME";
            case FT_OLDDLPRIORITY: return "FT_OLDDLPRIORITY";
            case FT_STATUS: return "FT_STATUS";
            case FT_SOURCES: return "FT_SOURCES";
            case FT_PERMISSIONS: return "FT_PERMISSIONS";
            case FT_OLDULPRIORITY: return "FT_OLDULPRIORITY";
            case FT_DLPRIORITY: return "FT_DLPRIORITY";
            case FT_ULPRIORITY: return "FT_ULPRIORITY";
            case FT_KADLASTPUBLISHKEY: return "FT_KADLASTPUBLISHKEY/CT_SERVER_FLAGS";
            case FT_KADLASTPUBLISHSRC: return "FT_KADLASTPUBLISHSRC";
            case FT_FLAGS: return "FT_FLAGS";
            case FT_DL_ACTIVE_TIME: return "FT_DL_ACTIVE_TIME";
            case FT_CORRUPTEDPARTS: return "FT_CORRUPTEDPARTS";
            case FT_DL_PREVIEW: return "FT_DL_PREVIEW";
            case FT_KADLASTPUBLISHNOTES: return "FT_KADLASTPUBLISHNOTES";
            case FT_AICH_HASH: return "FT_AICH_HASH";
            case FT_FILEHASH: return "FT_FILEHASH";
            case FT_COMPLETE_SOURCES: return "FT_COMPLETE_SOURCES";
            case FT_FAST_RESUME_DATA: return "FT_FAST_RESUME_DATA";
            case CT_SERVER_UDPSEARCH_FLAGS: return "CT_SERVER_UDPSEARCH_FLAGS";
            case CT_PORT: return "CT_PORT";
            case CT_VERSION: return "CT_VERSION";
            case CT_EMULECOMPAT_OPTIONS: return "CT_EMULECOMPAT_OPTIONS";
            case CT_EMULE_RESERVED1: return "CT_EMULE_RESERVED1";
            case CT_EMULE_RESERVED2: return "CT_EMULE_RESERVED2";
            case CT_EMULE_RESERVED3: return "CT_EMULE_RESERVED3";
            case CT_EMULE_RESERVED4: return "CT_EMULE_RESERVED4";
            case CT_EMULE_RESERVED5: return "CT_EMULE_RESERVED5";
            case CT_EMULE_RESERVED6: return "CT_EMULE_RESERVED6";
            case CT_EMULE_RESERVED7: return "CT_EMULE_RESERVED7";
            case CT_EMULE_RESERVED8: return "CT_EMULE_RESERVED8";
            case CT_EMULE_RESERVED9: return "CT_EMULE_RESERVED9";
            case CT_EMULE_UDPPORTS: return "CT_EMULE_UDPPORTS";
            case CT_EMULE_MISCOPTIONS1: return "CT_EMULE_MISCOPTIONS1";
            case CT_EMULE_VERSION: return "CT_EMULE_VERSION";
            case CT_EMULE_BUDDYIP: return "CT_EMULE_BUDDYIP";
            case CT_EMULE_BUDDYUDP: return "CT_EMULE_BUDDYUDP";
            case CT_EMULE_MISCOPTIONS2: return "CT_EMULE_MISCOPTIONS2";
            case CT_EMULE_RESERVED13: return "CT_EMULE_RESERVED13";
            case CT_MOD_VERSION: return "CT_MOD_VERSION";
            default: return "UNKNOWN";
        }
    }

    private static class FloatSerial implements Serializable {
        public float value;

        FloatSerial(float value) {
            this.value = value;
        }

        @Override
        public ByteBuffer get(ByteBuffer src) throws JED2KException {
            value = src.getFloat();
            return src;
        }

        @Override
        public ByteBuffer put(ByteBuffer dst) throws JED2KException {
            return dst.putFloat(value);
        }

        @Override
        public int bytesCount() {
            return sizeof(value);
        }

        @Override
        public String toString() {
            return Float.toString(value);
        }
    }

    private static class BooleanSerial implements Serializable {
        public boolean value;

        BooleanSerial(boolean value) {
            this.value = value;
        }

        @Override
        public ByteBuffer get(ByteBuffer src) throws JED2KException {
            value = (src.get() == 0x00);
            return src;
        }

        @Override
        public ByteBuffer put(ByteBuffer dst) throws JED2KException {
            byte bval = (value)?(byte)0x01:(byte)0x00;
            return dst.put(bval);
        }

        @Override
        public int bytesCount() {
            return sizeof(value);
        }

        @Override
        public String toString() {
            return value?"true":"false";
        }
    }

    private static class StringSerial implements Serializable {
        public byte type;
        byte[] value = null;

        StringSerial(byte type, byte[] value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public ByteBuffer get(ByteBuffer src) throws JED2KException {
            short size = 0;
            if (type >= Tag.TAGTYPE_STR1 && type <= Tag.TAGTYPE_STR16) {
                size = (short)(type - Tag.TAGTYPE_STR1 + 1);
            } else {
                size = src.getShort();
            }

            value = new byte[size];
            return src.get(value);
        }

        @Override
        public ByteBuffer put(ByteBuffer dst) throws JED2KException {
            if (type == Tag.TAGTYPE_STRING) dst.putShort((short)value.length);
            return dst.put(value);
        }

        @Override
        public int bytesCount() {
            assert(value != null);
            // for modern tag we do not write 2 bytes for length - so first is 0, but for ordinary string +2 bytes for size
            return value.length + ((type >= Tag.TAGTYPE_STR1 && type <= Tag.TAGTYPE_STR16)?0:2);
        }

        public String stringValue() throws JED2KException {
            assert(value != null);
            try {
                return new String(value, "UTF-8");
            } catch(UnsupportedEncodingException e) {
                throw new JED2KException(ErrorCode.TAG_FROM_STRING_INVALID_CP);
            }
        }

        @Override
        public String toString() {
            try {
                return stringValue();
            } catch (JED2KException e){
                log.warning(e.getMessage());
            }

            return new String();
        }
    }

    public final class BoolArraySerial implements Serializable {
        private final UInt16 length = uint16();
        private byte value[] = null;

        @Override
        public ByteBuffer put(ByteBuffer dst) throws JED2KException {
            assert(false);
            return dst;
        }

        @Override
        public ByteBuffer get(ByteBuffer src) throws JED2KException {
            length.get(src);
            value = new byte[length.intValue() / 8];
            return src.get(value);
        }

        @Override
        public int bytesCount() {
            return length.bytesCount() + length.bytesCount()/8;
        }
    }

    private byte type;
    private byte id;
    String name = null;
    Serializable value = null;

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
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        this.type = src.get();

        if ((type & 0x80) != 0){
            type = (byte)(type & 0x7f);
            id = src.get();
        } else {
            ByteContainer<UInt16> bc = new ByteContainer<UInt16>(uint16());
            bc.get(src);
            if (bc.size.intValue() == 1) {
                id = bc.value[0];
            } else {
                name = bc.asString();   // use strong format here!
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
            throw new JED2KException(ErrorCode.TAG_TYPE_UNKNOWN);
        };

        assert(value != null);
        value.get(src);
        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        assert(initialized());
        assert(value != null);
        if (name == null){
            dst.put((byte)(type | 0x80));
            dst.put(id);
        } else {
            byte[] data = name.getBytes(Charset.forName("UTF-8"));
            dst.put(type).putShort((short)data.length).put(data);
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

    public final boolean isStringTag() {
        assert(value != null);
        return value instanceof StringSerial;
    }

    public final String stringValue() throws JED2KException {
        assert(initialized());

        if (value instanceof StringSerial) {
            return ((StringSerial) value).stringValue();
        }

        throw new JED2KException(ErrorCode.TAG_TO_STRING_INVALID);
    }

    public final String asStringValue() {
        try {
            return stringValue();
        } catch(JED2KException e) {
            return "";
        }
    }

    public final boolean isNumberTag() {
        assert(value != null);
        return value instanceof UNumber;
    }

    public final int intValue() throws JED2KException {
        assert(initialized());
        UNumber n = (UNumber)value;
        if (value == null)  throw new JED2KException(ErrorCode.TAG_TO_INT_INVALID);
        return n.intValue();
    }

    public final int asIntValue() {
        try {
            return intValue();
        } catch(JED2KException e) {
            return 0;
        }
    }

    public final long longValue() throws JED2KException {
        assert(initialized());
        UNumber n = (UNumber)value;
        if (n == null) throw new JED2KException(ErrorCode.TAG_TO_LONG_INVALID);
        return n.longValue();
    }

    public final long asLongValue() {
        try {
            return longValue();
        } catch(JED2KException e) {
            return 0;
        }
    }

    public final boolean isFloatTag() {
        assert(value != null);
        return value instanceof FloatSerial;
    }

    public final float floatValue() throws JED2KException {
        assert(initialized());
        FloatSerial fs = (FloatSerial)value;
        if (fs == null) throw new JED2KException(ErrorCode.TAG_TO_FLOAT_INVALID);
        return fs.value;
    }

    public final boolean isHashTag() {
        assert(value != null);
        return value instanceof Hash;
    }

    public final Hash hashValue() throws JED2KException {
        assert(initialized());
        Hash h = (Hash)value;
        if (h == null) throw new JED2KException(ErrorCode.TAG_TO_HASH_INVALID);
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

    public static Tag tag(byte id, String name, String value) throws JED2KException {
        byte type = Tag.TAGTYPE_STRING;
        byte[] bytes = null;

        try {
            bytes = value.getBytes("UTF-8");
            if (bytes.length <= 16) type = (byte)(Tag.TAGTYPE_STR1 + bytes.length - 1);
        } catch(UnsupportedEncodingException ex) {
            throw new JED2KException(ErrorCode.TAG_FROM_STRING_INVALID_CP);
        }

        assert(bytes != null);
        return new Tag(type, id, name, new StringSerial(type, bytes));
    }

    public static Tag tag(byte id, String name, Hash value) throws JED2KException {
        return new Tag(TAGTYPE_HASH16, id, name, value);
    }

    @Override
    public int bytesCount() {
        // TODO - fix twice conversion
        if (name == null) {
            return value.bytesCount() + 2;    // type + id
        } else {                  // type + len + name
            return value.bytesCount() + 1 + 2 + name.getBytes(Charset.forName("UTF-8")).length;
        }
    }

    @Override
    public String toString() {
        return type2String(type) + " " + ((name!=null)?name:id2String(id)) + " " + value.toString();
    }
}