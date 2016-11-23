package org.dkf.jed2k;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.NetworkIdentifier;
import org.dkf.jed2k.protocol.Serializable;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class Utils {
    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static final String byte2String(byte[] value) {
        if (value == null) return "";
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

    public static final int sizeof(long value) { return 8; }

    public static final int sizeof(boolean value) {
        return 1;
    }

    public static final int sizeof(Serializable s) {
        return s.bytesCount();
    }

    /**
     *
     * @param ip integer value from libed2k network LITTLE_ENDIAN int but interprets as network order(BIG_ENDIAN)
     *           so, most significant byte in position 0
     * @return java network endpoint
     */
    public static InetAddress int2Address(int ip) {
        byte raw[] = { (byte)(ip & 0xff), (byte)((ip >> 8) & 0xff), (byte)((ip >> 16) & 0xff), (byte)((ip >> 24) & 0xff)};

        try {
            return InetAddress.getByAddress(raw);
        } catch (UnknownHostException e) {
            // this must't happens since raw data length always 4
            e.printStackTrace();
        }

        return null;
    }

    public static String ip2String(int ip) {
        return String.format("%d.%d.%d.%d",
                ip & 0xff, (ip >> 8) & 0xff, (ip >> 16) & 0xff, (ip >> 24) & 0xff);
    }

    public static int string2Ip(final String s) throws JED2KException {
        String[] parts = s.split("[.]");
        if (parts.length != 4) throw new JED2KException(ErrorCode.ILLEGAL_ARGUMENT);
        try {
            int raw[] = {Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3])};

            for (int r : raw) {
                if (r < 0 || r > 255) throw new JED2KException(ErrorCode.ILLEGAL_ARGUMENT);
            }

            return raw[0]
                    | ((raw[1] << 8) & 0xff00)
                    | ((raw[2] << 16) & 0xff0000)
                    | ((raw[3] << 24) & 0xff000000);
        }
        catch(NumberFormatException e) {
            throw new JED2KException(ErrorCode.ILLEGAL_ARGUMENT);
        }
    }

    /**
     *
     * @param order sequence of four bytes
     * @return host byte order integer
     */
    public static int networkByteOrderToIp(byte[] order) {
        assert(order.length == 4);
        int res =  ((int)order[0] << 24)
                | (((int)order[1] << 16) & 0x00FF0000)
                | (((int)order[2] << 8) & 0x0000FF00)
                | (((int)order[3]) & 0xFF);
        return res;
    }

    /**
     *
     * @param order - bytes order of IP address in network byte order - most significant byte in position 0
     * @return integer with the same byte order since ed2k treats ip integer address as big endian integer
     */
    public static int packToNetworkByteOrder(byte[] order) {
        assert(order.length == 4);
        int res =  ((int)order[3] << 24)
                | (((int)order[2] << 16) & 0x00FF0000)
                | (((int)order[1] << 8) & 0x0000FF00)
                | (((int)order[0]) & 0xFF);
        return res;
    }

    /**
     *
     * @param ip arbitrary integer
     * @return integer with reversed bytes order
     */
    public static int ntohl(int ip) {
        byte raw[] = { (byte)(ip & 0xff), (byte)((ip >> 8) & 0xff), (byte)((ip >> 16) & 0xff), (byte)((ip >> 24) & 0xff)};
        return networkByteOrderToIp(raw);
    }

    /**
     *
     * @param ep ip address in network byte order
     * @return true if it is local address
     */
    public static boolean isLocalAddress(NetworkIdentifier ep) {
        int host = ntohl(ep.getIP());
        return ((host & 0xff000000) == 0x0a000000 // 10.x.x.x
                || (host & 0xfff00000) == 0xac100000 // 172.16.x.x
                || (host & 0xffff0000) == 0xc0a80000 // 192.168.x.x
                || (host & 0xffff0000) == 0xa9fe0000 // 169.254.x.x
                || (host & 0xff000000) == 0x7f000000); // 127.x.x.x
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
     * assuming host IP is X.Y.Z.W the ID will
     * be X +28  Y +216 Z +224 W (’big endian representation’). A low ID is always lower than
     * 16777216 (0x1000000)
     * @param v ip address or client id
     * @return true if ip address/client id less than higest low id
     */
    public static boolean isLowId(int v) {
        assert(Constants.HIGHEST_LOWID_ED2K >= 0);
        long l = v;
        l &= 0xFFFFFFFFL;
        return l < (long)Constants.HIGHEST_LOWID_ED2K;
    }

    public static String formatLink(final String fileName, long fileSize, final Hash hash) {
        StringBuilder sb = new StringBuilder();
        return sb.append("ed2k://|file|")
                .append(fileName).append("|")
                .append(fileSize).append("|")
                .append(hash.toString())
                .append("|/").toString();
    }
}
