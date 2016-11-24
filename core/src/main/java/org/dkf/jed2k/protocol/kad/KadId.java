package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.hash.MD4;
import org.dkf.jed2k.protocol.Hash;

import java.nio.ByteBuffer;

/**
 * Created by inkpot on 14.11.2016.
 */
public class KadId extends Hash {

    public static final int TOTAL_BITS = 128;

    public KadId() {
        super();
    }

    public KadId(final Hash h) {
        super(h);
    }

    public static KadId fromString(final String s) {
        return new KadId(Hash.fromString(s));
    }

    public static KadId fromBytes(byte[] data) {
        return new KadId(Hash.fromBytes(data));
    }

    /**
     * save/load as 4 32 bits digits in little endian save as 4 32 bits digits network byte order
     * rotate bytes in each 4 byte portion
     *
     */
    @Override
    public ByteBuffer get(ByteBuffer src) throws JED2KException {
        for (short i = 0; i < value.length; ++i) {
            byte b = src.get();
            value[(i / 4)*4 + 3 - (i % 4)] = b;
        }

        return src;
    }

    @Override
    public ByteBuffer put(ByteBuffer dst) throws JED2KException {
        for (short i = 0; i < value.length; ++i) {
            dst.put(value[(i / 4) * 4 + 3 - (i % 4)]);
        }

        return dst;
    }

    @Override
    public int bytesCount() {
        return value.length;
    }

    public boolean isAllZeros() {
        for(byte b: value) {
            if (b != 0) return false;
        }

        return true;
    }

    public KadId bitsInverse() {
        KadId res = new KadId(this);
        for (int i = 0; i != value.length; ++i)
            res.set(i, (byte) ~value[i]);

        return res;
    }

    public void bitsXor(final KadId id) {
        for (int i = 0; i != value.length; ++i)
            value[i] ^= id.at(i);
    }

    public void bitsOr(final KadId id) {
        for (int i = 0; i != value.length; ++i)
            value[i] |= id.at(i);
    }

    public void bitsAnd(final KadId id) {
        for (int i = 0; i != value.length; ++i)
            value[i] &= id.at(i);
    }

    // returns the distance between the two nodes
    // using the kademlia XOR-metric
    public static KadId distance(final KadId n1, final KadId n2) {
        assert n1 != null;
        assert n2 != null;

        KadId ret = new KadId();
        for(int i = 0; i < MD4.HASH_SIZE; ++i) {
            ret.set(i, (byte)(n1.at(i) ^ n2.at(i)));
        }
        return ret;
    }

    // returns true if: distance(n1, ref) < distance(n2, ref)
    public static int compareRef(final KadId n1, final KadId n2, final KadId ref) {
        for (int i = 0; i != MD4.HASH_SIZE; ++i) {
            int lhs = (n1.at(i) ^ ref.at(i)) & 0xFF;
            int rhs = (n2.at(i) ^ ref.at(i)) & 0xFF;
            if (lhs < rhs) return 1;
            if (lhs > rhs) return -1;
        }

        return 0;
    }

    // returns n in: 2^n <= distance(n1, n2) < 2^(n+1)
    // useful for finding out which bucket a node belongs to
    public static int distanceExp(final KadId n1, final KadId n2) {
        int bt = MD4.HASH_SIZE - 1;
        for (int i = 0; i != MD4.HASH_SIZE; ++i, --bt) {
            assert bt >= 0;
            int t = (n1.at(i) ^ n2.at(i)) & 0xFF;
            if (t == 0) continue;
            // we have found the first non-zero byte
            // return the bit-number of the first bit
            // that differs
            int bit = bt * 8;
            for (int b = 7; b >= 0; --b)
                if (t >= (1 << b)) return bit + b;
            return bit;
        }

        return 0;
    }
}
