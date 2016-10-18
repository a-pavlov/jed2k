package org.dkf.jed2k.protocol.server;


public class LoginRequest extends UsualPacket {
    public static int SRV_TCPFLG_COMPRESSION     = 0x00000001;
    public static int SRV_TCPFLG_NEWTAGS         = 0x00000008;
    public static int SRV_TCPFLG_UNICODE         = 0x00000010;
    public static int SRV_TCPFLG_RELATEDSEARCH   = 0x00000040;
    public static int SRV_TCPFLG_TYPETAGINTEGER  = 0x00000080;
    public static int SRV_TCPFLG_LARGEFILES      = 0x00000100;
    public static int SRV_TCPFLG_TCPOBFUSCATION  = 0x00000400;

    public static int SRVCAP_ZLIB               = 0x0001;
    public static int SRVCAP_IP_IN_LOGIN        = 0x0002;
    public static int SRVCAP_AUXPORT            = 0x0004;
    public static int SRVCAP_NEWTAGS            = 0x0008;
    public static int SRVCAP_UNICODE            = 0x0010;
    public static int SRVCAP_LARGEFILES         = 0x0100;
    public static int SRVCAP_SUPPORTCRYPT       = 0x0200;
    public static int SRVCAP_REQUESTCRYPT       = 0x0400;
    public static int SRVCAP_REQUIRECRYPT       = 0x0800;

    public static int CAPABLE_ZLIB              = SRVCAP_ZLIB;
    public static int CAPABLE_IP_IN_LOGIN_FRAME = SRVCAP_IP_IN_LOGIN;
    public static int CAPABLE_AUXPORT           = SRVCAP_AUXPORT;
    public static int CAPABLE_NEWTAGS           = SRVCAP_NEWTAGS;
    public static int CAPABLE_UNICODE           = SRVCAP_UNICODE;
    public static int CAPABLE_LARGEFILES        = SRVCAP_LARGEFILES;

    public static int JED2K_VERSION_MAJOR    = 0;
    public static int JED2K_VERSION_MINOR    = 1;
    public static int JED2K_VERSION_TINY     = 0;
}