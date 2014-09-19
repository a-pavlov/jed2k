package org.jed2k.protocol.tag;

import java.nio.charset.Charset;

import org.jed2k.protocol.Buffer;
import org.jed2k.protocol.ByteContainer;
import org.jed2k.protocol.Serializable;
import org.jed2k.protocol.UInt16;
import org.jed2k.protocol.tag.TagType;
import static org.jed2k.protocol.Unsigned.uint16;

public abstract class Tag implements Serializable{
    private byte type;
    private byte id;
    private boolean modern;
    String name;        
    
    /*
    public Tag(byte id, boolean modern){
        this.id = id;
        this.modern = modern;
    }
    
    public Tag(String name, boolean modern){
        this.name = name;
        this.modern = modern;
    }
    */
    @Override
    public Buffer put(Buffer dst){
        if (name == null){
            if (modern){
                dst.put((byte)(type | 0x80));
            } else{
                dst.put(type).put((short)1);
            }
            dst.put(id);
        } else {
            byte[] data = name.getBytes(Charset.forName("UTF-8"));
            dst.put(type).put((short)data.length).put(data);
        }
        
        return dst;
    }
    
    public final boolean isModern() {
        return modern;
    }
    
    public static Tag extractTag(Buffer src){
        byte type = src.getByte();
        byte id;
        String name;
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
        
        switch(type){
        case TAGTYPE_HASH16.value:
            break;     
            default:
          break;
        };        
        
        return null;
    }
}