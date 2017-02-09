package org.dkf.jed2k.kad;

/**
 * Created by inkpot on 24.01.2017.
 * designed be base for any element contains creation/update time
 */
public interface Timed {
    long getLastActiveTime();
}
