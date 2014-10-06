package org.jed2k;

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
}