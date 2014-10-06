package org.jed2k.protocol;

public class ProtocolException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -144L;

    public ProtocolException(Exception cause) {
        super(cause);
    }
    
    public ProtocolException(String message) {
        super(message);
    }
}