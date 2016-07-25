package org.jed2k.exception;

public enum ProtocolCode implements ErrorCode {
    NO_ERROR(0),
    SERVER_CONN_UNSUPPORTED_PACKET(1),
    PEER_CONN_UNSUPPORTED_PACKET(2),
    FAIL(3);

    private final int value;
    
    private ProtocolCode(int value) {
        this.value = value;
    }
    
    @Override
    public int intValue() {
        return value;
    }
}
