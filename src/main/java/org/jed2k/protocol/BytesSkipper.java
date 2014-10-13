package org.jed2k.protocol;

public class BytesSkipper implements Serializable {
    private final int skip_amount; 
    
    BytesSkipper(int skip_amount) {
        this.skip_amount = skip_amount;
    }
    
    @Override
    public Buffer get(Buffer src) throws ProtocolException {
        return src.position(skip_amount);
    }

    @Override
    public Buffer put(Buffer dst) throws ProtocolException {
        assert(false);
        return dst;
    }

    @Override
    public int size() {
        return skip_amount;
    }
    
    
}