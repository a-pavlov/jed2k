package org.jed2k.protocol.tag;

public enum TagType {
    TAGTYPE_UNDEFINED(0x00), // special tag definition for empty objects
    TAGTYPE_HASH16(0x01),
    TAGTYPE_STRING(0x02),
    TAGTYPE_UINT32(0x03),
    TAGTYPE_FLOAT32(0x04),
    TAGTYPE_BOOL(0x05),
    TAGTYPE_BOOLARRAY(0x06),
    TAGTYPE_BLOB(0x07),
    TAGTYPE_UINT16(0x08),
    TAGTYPE_UINT8(0x09),
    TAGTYPE_BSOB(0x0A),
    TAGTYPE_UINT64(0x0B),

    // Compressed string types
    TAGTYPE_STR1(0x11),
    TAGTYPE_STR2(0x12),
    TAGTYPE_STR3(0x13),
    TAGTYPE_STR4(0x14),
    TAGTYPE_STR5(0x15),
    TAGTYPE_STR6(0x16),
    TAGTYPE_STR7(0x17),
    TAGTYPE_STR8(0x18),
    TAGTYPE_STR9(0x19),
    TAGTYPE_STR10(0x20),
    TAGTYPE_STR11(0x21),
    TAGTYPE_STR12(0x22),
    TAGTYPE_STR13(0x23),
    TAGTYPE_STR14(0x24),
    TAGTYPE_STR15(0x25),
    TAGTYPE_STR16(0x26),
    TAGTYPE_STR17(0x27),  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
                    // only because of a flaw, those tags are handled correctly,
                    // but should not be handled at all
    TAGTYPE_STR18(0x28),  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
            //  only because of a flaw, those tags are handled correctly,
            // but should not be handled at all
    TAGTYPE_STR19(0x29),  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
            // only because of a flaw, those tags are handled correctly,
            // but should not be handled at all
    TAGTYPE_STR20(0x30),  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
            // only because of a flaw, those tags are handled correctly,
            // but should not be handled at all
    TAGTYPE_STR21(0x31),  // accepted by eMule 0.42f (02-Mai-2004) in receiving code
            // only because of a flaw, those tags are handled correctly,
            // but should not be handled at all
    TAGTYPE_STR22(0x32);   // accepted by eMule 0.42f (02-Mai-2004) in receiving code
            // only because of a flaw, those tags are handled correctly,
            // but should not be handled at all
    
    public byte value;
    
    TagType(int value){
        this.value = (byte)value;
    }
    
    TagType(TagType value){
        this.value = value.value;
    }
}