package org.dkf.jed2k.protocol;

import org.dkf.jed2k.exception.JED2KException;

public interface Dispatchable {
    public void dispatch(Dispatcher dispatcher) throws JED2KException;
}
