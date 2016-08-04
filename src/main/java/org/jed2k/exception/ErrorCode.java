package org.jed2k.exception;

public enum ErrorCode implements BaseErrorCode {
    NO_ERROR(0),
    SERVER_CONN_UNSUPPORTED_PACKET(1),
    PEER_CONN_UNSUPPORTED_PACKET(2),
    END_OF_STREAM(3),
    IO_EXCEPTION(4),
    NO_TRANSFER(5),
    FILE_NOT_FOUND(6),
    OUT_OF_PARTS(7),
    INFLATE_ERROR(8),
    CONNECTION_TIMEOUT(9),

    TAG_TYPE_UNKNOWN(10),
    TAG_TO_STRING_INVALID(11),
    TAG_TO_INT_INVALID(12),
    TAG_TO_LONG_INVALID(13),
    TAG_TO_FLOAT_INVALID(14),
    TAG_TO_HASH_INVALID(15),
    TAG_FROM_STRING_INVALID_CP(16),
    GENERIC_INSTANTIATION_ERROR(17),
    GENERIC_ILLEGAL_ACCESS(18),
    TRANSFER_ABORTED(19),
    CHANNEL_CLOSED(20),
    FAIL(21);

    private final int value;

    private ErrorCode(int value) {
        this.value = value;
    }

    @Override
    public int intValue() {
        return value;
    }
}
