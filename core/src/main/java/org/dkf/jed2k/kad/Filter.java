package org.dkf.jed2k.kad;

/**
 * Created by inkpot on 19.12.2016.
 */
public interface Filter<T> {
    public boolean allow(final T t);
}
