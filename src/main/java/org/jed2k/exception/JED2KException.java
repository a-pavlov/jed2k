package org.jed2k.exception;

public class JED2KException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -144L;

    public JED2KException(Exception cause) {
        super(cause);
    }
    
    public JED2KException(String message) {
        super(message);
    }
}