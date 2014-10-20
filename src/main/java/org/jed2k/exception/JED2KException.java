package org.jed2k.exception;

public class JED2KException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -144L;
    
    private ErrorCode code;

    public JED2KException(Exception cause) {
        super(cause);
        code = ProtocolCode.FAIL;
    }
    
    public JED2KException(String message) {
        super(message);
        code = ProtocolCode.FAIL;
    }
    
    public JED2KException(ErrorCode code) {
        super("Encoded message");
        this.code = code;
    }
    
    public ErrorCode getErrorCode() {
        return code;
    }
}
