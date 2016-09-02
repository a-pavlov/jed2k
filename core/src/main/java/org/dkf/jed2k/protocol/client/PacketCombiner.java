package org.dkf.jed2k.protocol.client;

import org.dkf.jed2k.hash.MD4;
import org.dkf.jed2k.protocol.*;

import java.util.HashMap;
import java.util.Map;

public class PacketCombiner extends org.dkf.jed2k.protocol.PacketCombiner {
	enum StandardClientTcp
    {
        OP_HELLO(0x01), // 0x10<HASH 16><ID 4><PORT 2><1 Tag_set>
        OP_SENDINGPART(0x46), // <HASH 16><von 4><bis 4><Daten len:(von-bis)>
        OP_REQUESTPARTS(0x47), // <HASH 16><von[3] 4*3><bis[3] 4*3>
        OP_FILEREQANSNOFIL(0x48), // <HASH 16>
        OP_END_OF_DOWNLOAD(0x49), // <HASH 16> // Unused for sending
        OP_ASKSHAREDFILES(0x4A), // (null)
        OP_ASKSHAREDFILESANSWER(0x4B), // <count 4>(<HASH 16><ID 4><PORT 2><1 Tag_set>)[count]
        OP_HELLOANSWER(0x4C), // <HASH 16><ID 4><PORT 2><1 Tag_set><SERVER_IP 4><SERVER_PORT 2>
        OP_CHANGE_CLIENT_ID(0x4D), // <ID_old 4><ID_new 4> // Unused for sending
        OP_MESSAGE(0x4E), // <len 2><Message len>
        OP_SETREQFILEID(0x4F), // <HASH 16>
        OP_FILESTATUS(0x50), // <HASH 16><count 2><status(bit array) len:((count+7)/8)>
        OP_HASHSETREQUEST(0x51), // <HASH 16>
        OP_HASHSETANSWER(0x52), // <count 2><HASH[count] 16*count>
        OP_STARTUPLOADREQ(0x54), // <HASH 16>
        OP_ACCEPTUPLOADREQ(0x55), // (null)
        OP_CANCELTRANSFER(0x56), // (null)
        OP_OUTOFPARTREQS(0x57), // (null)
        OP_REQUESTFILENAME(0x58), // <HASH 16>    (more correctly file_name_request)
        OP_REQFILENAMEANSWER(0x59), // <HASH 16><len 4><NAME len>
        OP_CHANGE_SLOT(0x5B), // <HASH 16> // Not used for sending
        OP_QUEUERANK(0x5C), // <wert  4> (slot index of the request) // Not used for sending
        OP_ASKSHAREDDIRS(0x5D), // (null)
        OP_ASKSHAREDFILESDIR(0x5E), // <len 2><Directory len>
        OP_ASKSHAREDDIRSANS(0x5F), // <count 4>(<len 2><Directory len>)[count]
        OP_ASKSHAREDFILESDIRANS(0x60), // <len 2><Directory len><count 4>(<HASH 16><ID 4><PORT 2><1 T
        OP_ASKSHAREDDENIEDANS(0x61);  // (null)

        public final byte value;

        private StandardClientTcp(int v) {
            value = (byte)v;
        }
    }

    enum ExtendedClientTcp {
        OP_EMULEINFO                (0x01), //
        OP_EMULEINFOANSWER          (0x02), //
        OP_COMPRESSEDPART           (0x40), //
        OP_QUEUERANKING             (0x60), // <RANG 2>
        OP_FILEDESC                 (0x61), // <len 2><NAME len>
        OP_VERIFYUPSREQ             (0x71), // (never used)
        OP_VERIFYUPSANSWER          (0x72), // (never used)
        OP_UDPVERIFYUPREQ           (0x73), // (never used)
        OP_UDPVERIFYUPA             (0x74), // (never used)
        OP_REQUESTSOURCES           (0x81), // <HASH 16>
        OP_ANSWERSOURCES            (0x82), //
        OP_REQUESTSOURCES2          (0x83), // <HASH 16>
        OP_ANSWERSOURCES2           (0x84), //
        OP_PUBLICKEY                (0x85), // <len 1><pubkey len>
        OP_SIGNATURE                (0x86), // v1: <len 1><signature len>
        // v2:<len 1><signature len><sigIPused 1>
        OP_SECIDENTSTATE            (0x87), // <state 1><rndchallenge 4>
        OP_REQUESTPREVIEW           (0x90), // <HASH 16> // Never used for sending on aMule
        OP_PREVIEWANSWER            (0x91), // <HASH 16><frames 1>{frames * <len 4><frame len>} // Never used for sending on aMule
        OP_MULTIPACKET              (0x92),
        OP_MULTIPACKETANSWER        (0x93),
        // OP_PEERCACHE_QUERY       (0x94), // Unused on aMule - no PeerCache
        // OP_PEERCACHE_ANSWER      (0x95), // Unused on aMule - no PeerCache
        // OP_PEERCACHE_ACK         (0x96), // Unused on aMule - no PeerCache
        OP_PUBLICIP_REQ             (0x97),
        OP_PUBLICIP_ANSWER          (0x98),
        OP_CALLBACK                 (0x99), // <HASH 16><HASH 16><uint 16>
        OP_REASKCALLBACKTCP         (0x9A),
        OP_AICHREQUEST              (0x9B), // <HASH 16><uint16><HASH aichhashlen>
        OP_AICHANSWER               (0x9C), // <HASH 16><uint16><HASH aichhashlen> <data>
        OP_AICHFILEHASHANS          (0x9D),
        OP_AICHFILEHASHREQ          (0x9E),
        OP_BUDDYPING                (0x9F),
        OP_BUDDYPONG                (0xA0),
        OP_COMPRESSEDPART_I64       (0xA1), // <HASH 16><von 8><size 4><Data len:size>
        OP_SENDINGPART_I64          (0xA2), // <HASH 16><start 8><end 8><Data len:(end-start)>
        OP_REQUESTPARTS_I64         (0xA3), // <HASH 16><start[3] 8*3><end[3] 8*3>
        OP_MULTIPACKET_EXT          (0xA4),
        OP_CHATCAPTCHAREQ           (0xA5),
        OP_CHATCAPTCHARES           (0xA6);

        public final byte value;

        private ExtendedClientTcp(int v) {
            value = (byte)v;
        }
    }

    protected static final Map<PacketKey, Class<? extends Serializable>> supportedPacketsClient;
    protected static final Map<Class<? extends Serializable>, PacketKey> struct2KeyClient;

    private static void addHandlerClient(byte protocol, byte type, Class<? extends Serializable> clazz) {
        PacketKey pk = new PacketKey(protocol, type);
        assert(!supportedPacketsClient.containsKey(pk));
        assert(clazz != null);
        supportedPacketsClient.put(pk, clazz);
        struct2KeyClient.put(clazz, pk);
    }

    /**
     * Most packets in ed2k don't have payload data except packets with files parts
     * serviceSize extracts always service size of packet - summary metadata fields size
     *
     * @param ph - packet header structure
     * @return service size of structure
     */
    public int serviceSize(PacketHeader ph) {
        int size = ph.sizePacket();
        if (ph.key().compareTo(pkClientSendingPart) == 0)
            size = SendingPart32.SIZE;
        else if (ph.key().compareTo(pkClientSendingPart64) == 0) // add protocol type check
            size = SendingPart64.SIZE;
        else if (ph.key().compareTo(pkClientSendingCompPart) == 0)
            size = MD4.HASH_SIZE + UInt32.SIZE*2;   // TODO - correct this temp code
        else if (ph.key().compareTo(pkClientSendingCompPart64) == 0)
            size = MD4.HASH_SIZE + UInt32.SIZE + UInt64.SIZE; // TODO - correct this temp code
        return size;
    }

    private static PacketKey pkClientSendingPart = PacketKey.pk(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_SENDINGPART.value);
    private static PacketKey pkClientSendingPart64 = PacketKey.pk(ProtocolType.OP_EMULEPROT.value, ExtendedClientTcp.OP_SENDINGPART_I64.value);
    private static PacketKey pkClientSendingCompPart = PacketKey.pk(ProtocolType.OP_EMULEPROT.value, ExtendedClientTcp.OP_COMPRESSEDPART.value);
    private static PacketKey pkClientSendingCompPart64 = PacketKey.pk(ProtocolType.OP_EMULEPROT.value, ExtendedClientTcp.OP_COMPRESSEDPART_I64.value);

    static {
        supportedPacketsClient = new HashMap<PacketKey, Class<? extends Serializable>>();
        struct2KeyClient = new HashMap<Class<? extends Serializable>, PacketKey>();

        // client <-> client tcp messages section
        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_HELLO.value, Hello.class);
        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_HELLOANSWER.value, HelloAnswer.class);
        addHandlerClient(ProtocolType.OP_EMULEPROT.value, ExtendedClientTcp.OP_EMULEINFO.value, ExtHello.class);
        addHandlerClient(ProtocolType.OP_EMULEPROT.value, ExtendedClientTcp.OP_EMULEINFOANSWER.value, ExtHelloAnswer.class);

        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_REQUESTFILENAME.value, FileRequest.class);
        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_REQFILENAMEANSWER.value, FileAnswer.class);

        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_CANCELTRANSFER.value, CancelTransfer.class);

        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_SETREQFILEID.value, FileStatusRequest.class);
        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_FILEREQANSNOFIL.value, NoFileStatus.class);
        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_FILESTATUS.value, FileStatusAnswer.class);

        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_HASHSETREQUEST.value, HashSetRequest.class);
        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_HASHSETANSWER.value, HashSetAnswer.class);

        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_STARTUPLOADREQ.value, StartUpload.class);
        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_ACCEPTUPLOADREQ.value, AcceptUpload.class);
        addHandlerClient(ProtocolType.OP_EMULEPROT.value, ExtendedClientTcp.OP_QUEUERANKING.value, QueueRanking.class);
        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_OUTOFPARTREQS.value, OutOfParts.class);

        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_REQUESTPARTS.value, RequestParts32.class);
        addHandlerClient(ProtocolType.OP_EMULEPROT.value, ExtendedClientTcp.OP_REQUESTPARTS_I64.value, RequestParts64.class);
        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_SENDINGPART.value, SendingPart32.class);
        addHandlerClient(ProtocolType.OP_EMULEPROT.value, ExtendedClientTcp.OP_SENDINGPART_I64.value, SendingPart64.class);

        addHandlerClient(ProtocolType.OP_EMULEPROT.value, ExtendedClientTcp.OP_COMPRESSEDPART.value, CompressedPart32.class);
        addHandlerClient(ProtocolType.OP_EMULEPROT.value, ExtendedClientTcp.OP_COMPRESSEDPART_I64.value, CompressedPart64.class);

        addHandlerClient(ProtocolType.OP_EDONKEYPROT.value, StandardClientTcp.OP_END_OF_DOWNLOAD.value, EndDownload.class);
    }

    @Override
    protected Class<? extends Serializable> keyToPacket(PacketKey key) {
        return supportedPacketsClient.get(key);
    }

    @Override
    protected PacketKey classToKey(Class<? extends Serializable> clazz) {
        return struct2KeyClient.get(clazz);
    }
}