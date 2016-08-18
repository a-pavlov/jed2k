package org.jed2k.exception;

public enum ErrorCode implements BaseErrorCode {
    NO_ERROR(0, "No error"),
    SERVER_CONN_UNSUPPORTED_PACKET(1, "Server unsupported packet"),
    PEER_CONN_UNSUPPORTED_PACKET(2, "Peer connection unsupported packet"),
    END_OF_STREAM(3, "End of stream"),
    IO_EXCEPTION(4, "I/O exception"),
    NO_TRANSFER(5, "No transfer"),
    FILE_NOT_FOUND(6, "File not found"),
    OUT_OF_PARTS(7, "Out of parts"),
    INFLATE_ERROR(8, "Inflate error"),
    CONNECTION_TIMEOUT(9, "Connection timeout"),

    TAG_TYPE_UNKNOWN(10, "Tag type unknown"),
    TAG_TO_STRING_INVALID(11, "Tag to string convertion error"),
    TAG_TO_INT_INVALID(12, "Tag to int conversion error"),
    TAG_TO_LONG_INVALID(13, "Tag to long conversion error"),
    TAG_TO_FLOAT_INVALID(14, "Tag to float conversion error"),
    TAG_TO_HASH_INVALID(15, "Tag to hash conversion error"),
    TAG_FROM_STRING_INVALID_CP(16, "Tag from string creation error invalid code page"),
    GENERIC_INSTANTIATION_ERROR(17, "Generic instantiation error"),
    GENERIC_ILLEGAL_ACCESS(18, "Generic illegal access"),
    TRANSFER_ABORTED(19, "Transfer aborted"),
    CHANNEL_CLOSED(20, "Channel closed"),
    QUEUE_RANKING(21, "Queue ranking"),

    DUPLICATE_PEER(22, "Duplicate peer"),
    DUPLICATE_PEER_CONNECTION(23, "Duplicate peer connection"),
    PEER_LIMIT_EXEEDED(24, "Peer limit exeeded"),
    SECURITY_EXCEPTION(25, "Security exception"),
    UNSUPPORTED_ENCODING(26, "Unsupported encoding exception"),
    ILLEGAL_ARGUMENT(27, "Illegal argument"),
    PACKET_SIZE_INCORRECT(28, "Packet size less than zero"),
    PACKET_SIZE_OVERFLOW(29, "Packet size too big"),
    TRANSFER_FINISHED(30, "Transfer finished"),
    TRANSFER_PAUSED(31, "Transfer paused"),
    FAIL(32, "Fail");

    private final int code;
    private final String description;

    private ErrorCode(int c, String descr) {
        this.code = c;
        this.description = descr;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("%s {%d}", description, code);
    }
}
