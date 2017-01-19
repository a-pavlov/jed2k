package org.dkf.jed2k.protocol;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.util.HexDump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public abstract class PacketCombiner {

    private static Logger log = LoggerFactory.getLogger(PacketCombiner.class);

    public enum ProtocolType {
        OP_EDONKEYHEADER(0xE3),
        OP_EDONKEYPROT(0xE3),
        OP_PACKEDPROT(0xD4),
        OP_EMULEPROT(0xC5),
    	OP_KAD_COMPRESSED_UDP(0xE5),
        OP_KADEMLIAHEADER(0xE4);

        public final byte value;

        ProtocolType(int v) {
            this.value = (byte)v;
        }
    }

    private PacketHeader reusableHeader = new PacketHeader();

    /**
     *
     * @param header - packet header
     * @param src - byte buffer with raw data
     * @return serializable structure according with header
     * @throws JED2KException
     */
    public Serializable unpack(PacketHeader header, ByteBuffer src) throws JED2KException {
        assert(header.isDefined());
        assert(src.remaining() == serviceSize(header));

        // special case for packed protocol - both tcp and KAD udp
        if (header.key().protocol == ProtocolType.OP_PACKEDPROT.value || header.key().protocol == ProtocolType.OP_KAD_COMPRESSED_UDP.value) {
            byte[] compressedData = new byte[src.remaining()];
            byte[] plainData = new byte[src.remaining()*10];
            src.get(compressedData);
            Inflater decompresser = new Inflater();
            decompresser.setInput(compressedData, 0, compressedData.length);
            int resultLength = 0;
            try {
                resultLength = decompresser.inflate(plainData);
                log.trace("Compressed data size {} uncompressed data size {}", compressedData.length, resultLength);
            } catch(DataFormatException e) {
                throw new JED2KException(ErrorCode.INFLATE_ERROR);
            }

            decompresser.end();
            src.clear();

            // TODO fix this temp code
            if (src.capacity() < resultLength) {
                log.debug("re-create input buffer due to decompress size to {}", resultLength);
                src = ByteBuffer.allocate(resultLength);
                src.order(ByteOrder.LITTLE_ENDIAN);
            }

            src.put(plainData, 0, resultLength);
            src.flip();
            header.reset(header.key(), resultLength);   // use correct protocol value here to be compatible with HashMap
        }

        PacketKey key = header.key();
        Class<? extends Serializable> clazz = keyToPacket(key);
        Serializable ph = null;

        if (clazz != null) {
            try {
                ph = clazz.newInstance();
            } catch(InstantiationException e) {
                throw new JED2KException(e, ErrorCode.GENERIC_INSTANTIATION_ERROR);
            } catch (IllegalAccessException e) {
                throw new JED2KException(e, ErrorCode.GENERIC_ILLEGAL_ACCESS);
            }
        } else {
            log.error("[combiner] unable to find correspond packet for {}", header);
            log.trace("[combiner] packet dump \n{}", HexDump.dump(src.array()
                    , 0
                    , Math.min(src.remaining(), Math.min(Math.max(header.size, 0), 256))));
            ph = new BytesSkipper(serviceSize(header));
        }

        try {
            if (ph instanceof SoftSerializable) {
                SoftSerializable ssp = (SoftSerializable)ph;
                assert(ssp != null);
                ssp.get(src, serviceSize(header));
            } else {
                ph.get(src);
            }
        } catch(JED2KException e) {
            throw e;
        }

        return ph;
    }

    /**
     *
     * @param object - serializable structure
     * @param dst - destination byte buffer
     * @return true if buffer has enough space for packing
     * @throws JED2KException
     */
    public boolean pack(Serializable object, ByteBuffer dst) throws JED2KException {
        PacketKey key = classToKey(object.getClass());
        assert(key != null);
        // use appropriate header here
        PacketHeader outgoingHeader = getHeader();
        if ((outgoingHeader.bytesCount() + object.bytesCount()) < dst.remaining()) {
            outgoingHeader.reset(key, object.bytesCount() + 1);
            assert(outgoingHeader.isDefined());
            outgoingHeader.put(dst);
            object.put(dst);
            return true;
        }

        return false;
    }

    protected abstract Class<? extends Serializable> keyToPacket(PacketKey key);
    protected abstract PacketKey classToKey(Class<? extends Serializable> clazz);
    public abstract int serviceSize(PacketHeader ph);

    /**
     * by default returns standard tcp emule packet header
     * @return packet header for generation of new packet
     */
    protected PacketHeader getHeader() {
        return reusableHeader;
    }
}