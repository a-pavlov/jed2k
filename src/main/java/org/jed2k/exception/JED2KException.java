package org.jed2k.exception;

public class JED2KException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -144L;

    private BaseErrorCode code;

    public JED2KException(Exception cause) {
        super(cause);
        code = ErrorCode.FAIL;
    }

    public JED2KException(String message) {
        super(message);
        code = ErrorCode.FAIL;
    }

    public JED2KException(BaseErrorCode code) {
        super("Encoded message");
        this.code = code;
    }

    public BaseErrorCode getErrorCode() {
        return code;
    }
}
