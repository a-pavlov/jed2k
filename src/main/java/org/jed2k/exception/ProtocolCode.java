package org.jed2k.exception;

public enum ProtocolCode implements ErrorCode {
    NO_ERROR(0),
    SERVER_CONN_UNSUPPORTED_PACKET(1),
    PEER_CONN_UNSUPPORTED_PACKET(2),
    END_OF_STREAM(3),
    IO_EXCEPTION(4),
    NO_TRANSFER(5),
    FILE_NOT_FOUND(6),
    OUT_OF_PARTS(7),
    FAIL(7);

    private final int value;

    private ProtocolCode(int value) {
        this.value = value;
    }

    @Override
    public int intValue() {
        return value;
    }
}
