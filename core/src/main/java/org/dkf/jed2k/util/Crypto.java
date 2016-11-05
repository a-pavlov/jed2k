package org.dkf.jed2k.util;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.PacketHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by inkpot on 05.11.2016.
 */
public class Crypto {

    private static final byte MAGICVALUE_REQUESTER = 34;			// modification of the requester-send and server-receive key
    private static final byte MAGICVALUE_SERVER = (byte)0xCB;			// modification of the server-send and requester-send key
    private static final int MAGICVALUE_SYNC = 0x835E6FC4;	// value to check if we have a working encrypted stream
    private static final int ENM_OBFUSCATION = 0x00;

    private static Logger log = LoggerFactory.getLogger(Crypto.class);

    private static byte getSemiRandomNonProtocolByte() {
        byte res = 0;
        Random rnd = new Random();
        R: for(int i = 0; i < 10; ++i) {
            byte candidates[] = new byte[10];
            rnd.nextBytes(candidates);
            for (byte b: candidates) {
                if (b != PacketHeader.OP_EMULEPROT && b != PacketHeader.OP_EDONKEYPROT && b!= PacketHeader.OP_PACKEDPROT) {
                    res = b;
                    break R;
                }
            }
        }

        if (res == 0) res = 0x01;
        return res;
    }

    public static void main(String[] args) throws JED2KException, NoSuchAlgorithmException {
        if (args.length < 1) return;

        List<InetSocketAddress> endpoints = new LinkedList<>();

        for(int i = 0; i < args.length; ++i) {
            String[] endpoint = args[i].split(":");
            if (endpoint.length == 2) {
                endpoints.add(new InetSocketAddress(endpoint[0], Integer.parseInt(endpoint[1])));
            } else {
                log.warn("Incorrect endpoint format in {}", args[i]);
            }
        }

        Hash h = Hash.fromString("B54AE9F4A30E8AAD2BEB94EA3E776F4E");

        Random rnd = new Random();
        int randomKeyPart = rnd.nextInt();
        byte inputKeys[] = new byte[2];
        inputKeys[0] = MAGICVALUE_REQUESTER;
        inputKeys[1] = MAGICVALUE_SERVER;
        RC4 keys[] = new RC4[2];

        for(int i = 0; i < 2; ++i) {
            ByteBuffer initialBuffer = ByteBuffer.allocate(Hash.SIZE + 1 + 4);
            initialBuffer.order(ByteOrder.LITTLE_ENDIAN);
            h.put(initialBuffer).put(inputKeys[i]).putInt(randomKeyPart);
            assert initialBuffer.remaining() == 0;
            byte packet[] = new byte[1 + Hash.SIZE + 4];
            initialBuffer.flip();
            assert initialBuffer.remaining() == packet.length;
            initialBuffer.get(packet);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(packet);
            assert !initialBuffer.hasRemaining();
            keys[i] = new RC4(md5.digest(), false);
        }

        // TODO- remove it
        byte revMagic[] = new byte[4];
        ByteBuffer revers = ByteBuffer.allocate(4);
        revers.order(ByteOrder.LITTLE_ENDIAN);
        revers.putInt(MAGICVALUE_SYNC);
        revers.flip();
        revers.get(revMagic);

        byte tail[] = {0, 0, 0};
        ByteBuffer bb = ByteBuffer.allocate(48);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(getSemiRandomNonProtocolByte()).putInt(randomKeyPart);
        bb.put(keys[0].crypt(revMagic)).put(keys[0].crypt(tail));
        bb.flip();
        log.info("send {} bytes", bb.remaining());

        ByteBuffer receive = ByteBuffer.allocate(4);
        receive.order(ByteOrder.LITTLE_ENDIAN);

        for(final InetSocketAddress ia: endpoints) {
            try (SocketChannel sc = SocketChannel.open()) {
                sc.connect(ia);
                sc.write(bb);
                sc.read(receive);
                receive.flip();
                byte dw[] = new byte[4];
                receive.get(dw);
                receive.flip();
                receive.put(keys[1].crypt(dw));
                receive.flip();
                int magic = receive.getInt();
                log.info(" {} == {}", MAGICVALUE_SYNC, magic);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
