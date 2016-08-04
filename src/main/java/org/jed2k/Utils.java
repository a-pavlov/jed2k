package org.jed2k;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.jed2k.protocol.Hash;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.Serializable;

public final class Utils {
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static final String byte2String(byte[] value) {
        char[] hexChars = new char[value.length * 2];
        for ( int j = 0; j < value.length; j++ ) {
            int v = value[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static final String byte2String(byte value) {
        byte b[] = {value};
        return byte2String(b);
    }

    public static final int sizeof(byte value) {
        return 1;
    }

    public static final int sizeof(short value) {
        return 2;
    }

    public static final int sizeof(int value) {
        return 4;
    }

    public static final int sizeof(float value) {
        return 4;
    }

    public static final int sizeof(boolean value) {
        return 1;
    }

    public static final int sizeof(Serializable s) {
        return s.bytesCount();
    }

    public static InetAddress int2Address(int ip) {
        /*byte[] raw = { (byte)(ip >> 24),
                        (byte)((ip >> 16) & 0xff),
                        (byte)((ip >> 8) & 0xff),
                        (byte)(ip & 0xff)
                        };*/
        byte raw[] = { (byte)(ip & 0xff), (byte)((ip >> 8) & 0xff), (byte)((ip >> 16) & 0xff), (byte)((ip >> 24) & 0xff)};

        try {
            return InetAddress.getByAddress(raw);
        } catch (UnknownHostException e) {
            // this must't happens since raw data length always 4
            e.printStackTrace();
        }

        return null;
    }

    public static int networkByteOrderToIp(byte[] order) {
        assert(order.length == 4);
        int res =  ((int)order[0] << 24)
                | (((int)order[1] << 16) & 0x00FF0000)
                | (((int)order[2] << 8) & 0x0000FF00)
                | (((int)order[3]) & 0xFF);
        return res;
    }

    public static boolean isLocalAddress(NetworkIdentifier ep) {
        return ((ep.ip & 0xff000000) == 0x0a000000 // 10.x.x.x
                || (ep.ip & 0xfff00000) == 0xac100000 // 172.16.x.x
                || (ep.ip & 0xffff0000) == 0xc0a80000 // 192.168.x.x
                || (ep.ip & 0xffff0000) == 0xa9fe0000 // 169.254.x.x
                || (ep.ip & 0xff000000) == 0x7f000000); // 127.x.x.x
    }

    public static int lowPart(long value) {
        return (int)value;
    }

    public static int hiPart(long value) {
        return (int)(value >> 32);
    }

    public static long makeFullED2KVersion(int client_id, int a, int b, int c) {
        return ((((long)client_id) << 24) | (((long)a << 17)) | (((long)b << 10)) | (((long)c << 7)));
    }

    ClientSoftware uagent2csoft(final Hash hash)
    {
        if ( hash.at(5) == (byte)13 && hash.at(14) == (byte)110 ) {
            return ClientSoftware.SO_OLDEMULE;
        }

        if ( hash.at(5) == (byte)14 && hash.at(14) == (byte)111 ) {
            return ClientSoftware.SO_EMULE;
        }

        if ( hash.at(5) == 'M' && hash.at(14) == 'L' ) {
            return ClientSoftware.SO_MLDONKEY;
        }

        if ( hash.at(5) == 'L' && hash.at(14) == 'K') {
            return ClientSoftware.SO_LIBED2K;
        }

        if (hash.at(5) == 'Q' && hash.at(14) == 'M') {
            return ClientSoftware.SO_QMULE;
        }

        return ClientSoftware.SO_UNKNOWN;
    }


    public static int divCeil(int a, int b) {
        return (a + b - 1)/b;
    }

    public static <T extends Number> Long divCeil(T a, T b) {
        return (a.longValue() + b.longValue() - 1)/b.longValue();
    }

    public static Hash fingerprint(Hash hash, byte first, byte second) {
        hash.set(5, first);
        hash.set(15, second);
        return hash;
    }

    /**
     * compare two integers as unsigned integers
     * @param v ip address or client id
     * @return true if ip address/client id less than higest low id
     */
    public static boolean isLowId(int v) {
        assert(Constants.HIGHEST_LOWID_ED2K >= 0);
        long l = v;
        l &= 0xFFFFFFFFL;
        return l < (long)Constants.HIGHEST_LOWID_ED2K;
    }
}
