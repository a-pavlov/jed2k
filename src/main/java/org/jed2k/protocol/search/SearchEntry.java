package org.jed2k.protocol.search;

import org.jed2k.protocol.Serializable;

public abstract class SearchEntry implements Serializable {
    public enum Operator {
        OPER_AND(0),
        OPER_OR(1),
        OPER_NOT(2),
        OPER_OPEN_PAREN(3),
        OPER_CLOSE_PAREN(4),
        OPER_NONE(5);
        
        public final byte value;
        
        Operator(int value) {
            this.value = (byte)value;
        }
    }
        
    public abstract Operator getOperator();
    public abstract boolean isOperator();
}
