package org.jed2k;

public enum ClientSoftware {
    SO_EMULE(0),
    SO_CDONKEY(1),
    SO_LXMULE(2),
    SO_AMULE(3),
    SO_SHAREAZA(4),
    SO_EMULEPLUS(5),
    SO_HYDRANODE(6),
    SO_NEW2_MLDONKEY(0x0a),
    SO_LPHANT(0x14),
    SO_NEW2_SHAREAZA(0x28),
    SO_EDONKEYHYBRID(0x32),
    SO_EDONKEY(0x33),
    SO_MLDONKEY(0x34),
    SO_OLDEMULE(0x35),
    SO_UNKNOWN(0x36),
    SO_NEW_SHAREAZA(0x44),
    SO_NEW_MLDONKEY(0x98),
    SO_LIBED2K(0x99),
    SO_QMULE(0xA0),
    SO_COMPAT_UNK(0xFF);
    
    public final int value;
    
    private ClientSoftware(int value) {
        this.value = value;
    }       
}
