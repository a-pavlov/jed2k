package org.jed2k.exception;

public enum ProtocolCode implements ErrorCode {
    NO_ERROR(0),
    FAIL(1);

    private final int value;
    
    private ProtocolCode(int value) {
        this.value = value;
    }
    
    @Override
    public int intValue() {
        return value;
    }
}
