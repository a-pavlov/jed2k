package org.jed2k.protocol;

import java.nio.ByteBuffer;
import org.jed2k.hash.MD4;
import org.jed2k.exception.JED2KException;
import static org.jed2k.Utils.byte2String;

public final class Hash implements Serializable, Comparable<Hash> {
    
    private final byte[] value = { 
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, 
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};
    
    public static final Hash TERMINAL   = fromString("31D6CFE0D16AE931B73C59D7E0C089C0");
    public static final Hash LIBED2K    = fromString("31D6CFE0D14CE931B73C59D7E0C04BC0");
    public static final Hash EMULE      = fromString("31D6CFE0D10EE931B73C59D7E0C06FC0");
    public static final Hash INVALID    = new Hash();
    
    public Hash(){
        
    }
    
    public Hash(Hash h) {
        assign(h);
    }
    
    public Hash assign(Hash h) {
        System.arraycopy(h.value, 0, value, 0, MD4.HASH_SIZE);
        return this;
    }
    
    public static Hash fromString(String value) {
        assert(value.length() == MD4.HASH_SIZE*2);
        if (value.length() != MD4.HASH_SIZE*2) return INVALID;
        Hash res = new Hash();
        for (int i = 0; i < MD4.HASH_SIZE*2; i += 2) {
            res.value[i/2] = (byte) ((Character.digit(value.charAt(i), 16) << 4) + Character.digit(value.charAt(i+1), 16));
        }
        
        return res;
    }
    
    public static Hash fromBytes(byte[] value) {
        assert(value.length == MD4.HASH_SIZE);
        if (value.length != MD4.HASH_SIZE) return INVALID;
        Hash res = new Hash();
        for(int i = 0; i < MD4.HASH_SIZE; ++i) {
            res.value[i] = value[i];
        }
        
        return res;
    }
    
    public byte at(int index) {
        assert(index < MD4.HASH_SIZE);
        return value[index];
    }
    
    @Override
    public String toString() {
        return byte2String(value);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Hash) {
            return java.util.Arrays.equals(value, ((Hash)obj).value);
        }

        return false;
    }

    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        return src.get(value);
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        return dst.put(value);
    }

    @Override
    public int bytesCount() {
        return MD4.HASH_SIZE;
    }

    @Override
    public int compareTo(Hash h) {
        int diff = 0;
        for(int i = 0; i < value.length; ++ i) {
            diff = ((short)value[i] & 0xff) - ((short)h.value[i] & 0xff);
            if (diff != 0) break;            
        }
        
        if (diff < 0) return -1;
        if (diff > 0) return 1;        
        return 0;
    }
}