package org.dkf.jed2k.protocol;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.hash.MD4;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;

import static org.dkf.jed2k.Utils.byte2String;

public class Hash implements Serializable, Comparable<Hash> {

    private final byte[] value = {
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
            (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00};

    public static final Hash TERMINAL   = fromString("31D6CFE0D16AE931B73C59D7E0C089C0");
    public static final Hash LIBED2K    = fromString("31D6CFE0D14CE931B73C59D7E0C04BC0");
    public static final Hash EMULE      = fromString("31D6CFE0D10EE931B73C59D7E0C06FC0");
    public static final Hash INVALID    = new Hash();

    public Hash() {

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

    public static Hash fromHashSet(Collection<Hash> hashes) {
        assert(!hashes.isEmpty());

        if (hashes.size() == 1) {
            assert(hashes.iterator().hasNext());
            return hashes.iterator().next();
        }

        ByteBuffer buffer = ByteBuffer.allocate(MD4.HASH_SIZE*hashes.size());
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        Iterator<Hash> itr = hashes.iterator();

        while(itr.hasNext()) {
            try {
                itr.next().put(buffer);
            } catch(JED2KException e) {
                return Hash.INVALID;    // impossible
            }
        }

        assert(buffer.remaining() == 0);
        buffer.flip();

        MD4 md4 = new MD4();
        return Hash.fromBytes(md4.digest(buffer.array()));
    }

    public static Hash random() {
        byte[] source = new byte[MD4.HASH_SIZE];
        Random rand = new Random();
        rand.nextBytes(source);
        return fromBytes(source);
    }

    public byte at(int index) {
        assert(index < MD4.HASH_SIZE);
        return value[index];
    }

    public void set(int index, byte value) {
        assert(index < MD4.HASH_SIZE);
        this.value[index] = value;
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
    public int hashCode() {
        int code = 0;
        for(int i = 0; i < 4; ++i) {
            code +=  (value[i*4 + 3] & 0xFF)
                    | ((value[i*4 + 2] & 0xFF) << 8)
                    | ((value[i*4 + 1] & 0xFF) << 16)
                    | ((value[i*4] & 0xFF) << 24);
        }

        return code;
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
