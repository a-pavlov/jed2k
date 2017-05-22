package org.dkf.jed2k.exception;

public enum ErrorCode implements BaseErrorCode {
    NO_ERROR(0, "No error"),
    SERVER_CONN_UNSUPPORTED_PACKET(1, "Server unsupported packet"),
    PEER_CONN_UNSUPPORTED_PACKET(2, "Peer connection unsupported packet"),
    PACKET_HEADER_UNDEFINED(3, "Packet header contains wrong bytes or undefined"),
    INFLATE_ERROR(4, "Inflate error"),
    PACKET_SIZE_INCORRECT(5, "Packet size less than zero"),
    PACKET_SIZE_OVERFLOW(6, "Packet size too big"),
    SERVER_MET_HEADER_INCORRECT(7, "Server met file contains incorrect header byte"),
    GENERIC_INSTANTIATION_ERROR(8, "Generic instantiation error"),
    GENERIC_ILLEGAL_ACCESS(9, "Generic illegal access"),


    END_OF_STREAM(10, "End of stream"),
    IO_EXCEPTION(11, "I/O exception"),
    NO_TRANSFER(12, "No transfer"),
    FILE_NOT_FOUND(13, "File not found"),
    OUT_OF_PARTS(14, "Out of parts"),
    CONNECTION_TIMEOUT(15, "Connection timeout"),
    CHANNEL_CLOSED(16, "Channel closed"),
    QUEUE_RANKING(17, "Queue ranking"),
    FILE_IO_ERROR(18, "File I/O error occured"),
    UNABLE_TO_DELETE_FILE(19, "Unable to delete file"),
    INTERNAL_ERROR(20, "Internal product error"),
    BUFFER_UNDERFLOW_EXCEPTION(21, "Buffer underflow exception"),
    BUFFER_GET_EXCEPTION(22, "Buffer get method raised common exception"),


    TAG_TYPE_UNKNOWN(30, "Tag type unknown"),
    TAG_TO_STRING_INVALID(31, "Tag to string convertion error"),
    TAG_TO_INT_INVALID(32, "Tag to int conversion error"),
    TAG_TO_LONG_INVALID(33, "Tag to long conversion error"),
    TAG_TO_FLOAT_INVALID(34, "Tag to float conversion error"),
    TAG_TO_HASH_INVALID(35, "Tag to hash conversion error"),
    TAG_FROM_STRING_INVALID_CP(36, "Tag from string creation error invalid code page"),
    TAG_TO_BLOB_INVALID(37, "Tag to blob conversion error"),
    TAG_TO_BSOB_INVALID(38, "Tag to bsob coversion error"),

    DUPLICATE_PEER(40, "Duplicate peer"),
    DUPLICATE_PEER_CONNECTION(41, "Duplicate peer connection"),
    PEER_LIMIT_EXEEDED(42, "Peer limit exeeded"),
    SECURITY_EXCEPTION(43, "Security exception"),
    UNSUPPORTED_ENCODING(44, "Unsupported encoding exception"),
    ILLEGAL_ARGUMENT(45, "Illegal argument"),

    TRANSFER_FINISHED(50, "Transfer finished"),
    TRANSFER_PAUSED(51, "Transfer paused"),
    TRANSFER_ABORTED(52, "Transfer aborted"),

    NO_MEMORY(60, "No memory available"),
    SESSION_STOPPING(61, "Session stopping"),
    INCOMING_DIR_INACCESSIBLE(62, "Incoming directory is inaccessible"),
    BUFFER_TOO_LARGE(63, "Buffer too large"),
    NOT_CONNECTED(64, "Not connected"),


    PORT_MAPPING_ALREADY_MAPPED(70, "Port already mapped"),
    PORT_MAPPING_NO_DEVICE(71, "No gateway device found"),
    PORT_MAPPING_ERROR(72, "Unable to map port"),
    PORT_MAPPING_IO_ERROR(73, "I/O exception on mapping port"),
    PORT_MAPPING_SAX_ERROR(74, "SAX parsing exception on port mapping"),
    PORT_MAPPING_CONFIG_ERROR(75, "Configuration exception on port mapping"),
    PORT_MAPPING_EXCEPTION(76, "Unknown exception on port mapping"),

    DHT_REQUEST_ALREADY_RUNNING(80, "DHT request with the same hash already in progress"),
    DHT_TRACKER_ABORTED(81, "DHT tracker was already aborted at the moment"),

    LINK_MAILFORMED(90, "Incorrect link format"),
    URI_SYNTAX_ERROR(91, "URI has incorrect syntax"),
    NUMBER_FORMAT_ERROR(92, "Parse number exception"),
    UNKNOWN_LINK_TYPE(93, "Emule link has unrecognized type"),
    GITHUB_CFG_IP_IS_NULL(94, "Ip is null in github kad config"),
    GITHUB_CFG_PORTS_ARE_NULL(95, "Ports are null in github kad config"),
    GITHUB_CFG_PORTS_ARE_EMPTY(96, "Ports are empty in github kad config"),

    FAIL(100, "Fail");

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
