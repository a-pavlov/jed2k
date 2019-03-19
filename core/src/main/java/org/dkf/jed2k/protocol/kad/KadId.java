package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.hash.MD4;
import org.dkf.jed2k.protocol.Hash;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.zip.CRC32;

/**
 * Created by inkpot on 14.11.2016.
 */
public class KadId extends Hash {
    private static final Random rnd = new Random();

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
            try {
                byte b = src.get();
                value[(i / 4)*4 + 3 - (i % 4)] = b;
            } catch(BufferUnderflowException e) {
                throw new JED2KException(ErrorCode.BUFFER_UNDERFLOW_EXCEPTION);
            } catch(Exception e) {
                throw new JED2KException(ErrorCode.BUFFER_GET_EXCEPTION);
            }
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

    public KadId bitsOr(final KadId id) {
        for (int i = 0; i != value.length; ++i)
            value[i] |= id.at(i);
        return this;
    }

    public KadId bitsAnd(final KadId id) {
        for (int i = 0; i != value.length; ++i)
            value[i] &= id.at(i);
        return this;
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

    // returns -1 if: distance(n1, ref) < distance(n2, ref)
    // returns 1 if: distance(n1, ref) > distance(n2, ref)
    // returns 0 if: distance(n1, ref) == distance(n2, ref)
    public static int compareRef(final KadId n1, final KadId n2, final KadId ref) {
        for (int i = 0; i != MD4.HASH_SIZE; ++i) {
            int lhs = (n1.at(i) ^ ref.at(i)) & 0xFF;
            int rhs = (n2.at(i) ^ ref.at(i)) & 0xFF;
            if (lhs < rhs) return -1;
            if (lhs > rhs) return 1;
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
            assert t > 0;
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

    public static KadId generateId(int addr, int r) {
        byte[] mask = { 0x03, 0x0f, 0x3f, (byte)0xff };
        byte[] ip = {(byte)(addr & 0xff),
                (byte)((addr >> 8) & 0xff),
                (byte)((addr >> 16) & 0xff),
                (byte)((addr >> 24) & 0xff)};

        for(int i = 0; i < ip.length; ++i) {
            ip[i] &= mask[i];
        }

        ip[0] |= (r & 0x7) << 5;
        CRC32 crc = new CRC32();
        crc.update(ip);
        long c = crc.getValue();
        KadId id = new KadId();
        byte[] randBytes = new byte[MD4.HASH_SIZE];
        rnd.nextBytes(randBytes);

        id.set(0, (byte)((c >> 24) & 0xff));
        id.set(1, (byte)((c >> 16) & 0xff));
        id.set(2, (byte)(((c >> 8) & 0xf8) | (randBytes[2] & 0x7)));

        for (int i = 3; i < MD4.HASH_SIZE; ++i) id.set(i, randBytes[i]);
        return id;
    }

    // generate a random node_id within the given bucket
    public static KadId generateRandomWithinBucket(int bucketIndex, final KadId id) {
        assert bucketIndex >= 0;
        assert bucketIndex < KadId.TOTAL_BITS;

        KadId target = new KadId(KadId.random(false));
        int num_bits = bucketIndex + 1;  // std::distance(begin, itr) + 1 in C++
        KadId mask = new KadId();

        for (int j = 0; j < num_bits; ++j) {
            mask.set(j/8, (byte)(mask.at(j/8) | (byte)(0x80 >> (j&7))));
        }

        KadId root = new KadId(id);
        root.bitsAnd(mask);
        target.bitsAnd(mask.bitsInverse());
        target.bitsOr(root);

        // make sure this is in another subtree than m_id
        // clear the (num_bits - 1) bit and then set it to the
        // inverse of m_id's corresponding bit.
        int bitPos = (num_bits - 1) / 8;
        target.set(bitPos, (byte)(target.at(bitPos) & (byte)(~(0x80 >> ((num_bits - 1) % 8)))));
        target.set(bitPos, (byte)(target.at(bitPos) | (byte)((~(id.at(bitPos))) & (byte)(0x80 >> ((num_bits - 1) % 8)))));

        assert KadId.distanceExp(id, target) == KadId.TOTAL_BITS - num_bits;
        return target;
    }
}
