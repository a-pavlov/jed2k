package org.jed2k.exception;

public class JED2KException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -144L;

    private BaseErrorCode ec;

    public JED2KException(Exception cause, BaseErrorCode ec) {
        super(cause);
        this.ec = ec;
    }

    public JED2KException(BaseErrorCode code) {
        super("Encoded message");
        this.ec = code;
    }

    public BaseErrorCode getErrorCode() {
        return ec;
    }

    @Override
    public String toString() {
        return super.toString() + " " + ec.toString();
    }
}
