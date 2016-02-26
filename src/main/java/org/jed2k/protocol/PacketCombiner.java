package org.jed2k.protocol;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.jed2k.exception.JED2KException;
import org.jed2k.hash.MD4;
import org.jed2k.protocol.server.search.SearchRequest;
import org.jed2k.protocol.server.CallbackRequestFailed;
import org.jed2k.protocol.server.CallbackRequestIncoming;
import org.jed2k.protocol.server.CallbackRequestOutgoing;
import org.jed2k.protocol.server.FoundFileSources;
import org.jed2k.protocol.server.GetFileSources;
import org.jed2k.protocol.server.GetList;
import org.jed2k.protocol.server.IdChange;
import org.jed2k.protocol.server.LoginRequest;
import org.jed2k.protocol.server.Message;
import org.jed2k.protocol.server.search.SearchMore;
import org.jed2k.protocol.server.search.SearchResult;
import org.jed2k.protocol.server.ServerInfo;
import org.jed2k.protocol.server.ServerList;
import org.jed2k.protocol.server.Status;

public abstract class PacketCombiner {
    
    private static Logger log = Logger.getLogger(PacketCombiner.class.getName());
    
    public enum ProtocolType {
        OP_EDONKEYHEADER(0xE3), 
        OP_EDONKEYPROT(0xE3), 
        OP_PACKEDPROT(0xD4), 
        OP_EMULEPROT(0xC5),
        OP_KAD_UDP(0xE4),
    	OP_KAD_COMPRESSED_UDP(0xE5);

        public final byte value;

        ProtocolType(int v) {
            this.value = (byte)v;
        }
    }

    private PacketHeader outgoingHeader = new PacketHeader();

    protected static void addHandler(Map<PacketKey, Class<? extends Serializable>> key2struct,
            Map<Class<? extends Serializable>, PacketKey> struct2key, 
            byte protocol, byte type, Class<? extends Serializable> clazz) {
        PacketKey pk = new PacketKey(protocol, type);
        assert(!key2struct.containsKey(pk));
        assert(clazz != null);
        key2struct.put(pk, clazz);
        struct2key.put(clazz, pk);
    }

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
                
        PacketKey key = header.key();
        Class<? extends Serializable> clazz = keyToPacket(key);
        Serializable ph = null;
        
        if (clazz != null) {
            try {
                ph = clazz.newInstance();
            } catch(InstantiationException e) {
                throw new JED2KException(e);
            } catch (IllegalAccessException e) {
                throw new JED2KException(e);
            }
        } else {
            log.warning("unable to find correspond packet for " + header);
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
        } catch(Exception e) {
            // catch any exception and convert it to our
            throw new JED2KException(e);
        }
        
        header.reset();
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
        //log.info("pack for class " + object.getClass().getName() + " bytes " + object.bytesCount());
        assert key != null;
        if ((outgoingHeader.bytesCount() + object.bytesCount()) < dst.remaining()) {
            outgoingHeader.reset(key, object.bytesCount() + 1);
            //log.info(outgoingHeader.toString());
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
}