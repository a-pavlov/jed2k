package org.dkf.jed2k.protocol.kad;

import org.dkf.jed2k.kad.ReqDispatcher;

import java.net.InetSocketAddress;

/**
 * Created by inkpot on 03.03.2017.
 */
public interface KadDispatchable {
    void dispatch(final ReqDispatcher dispatcher, final InetSocketAddress address);
}
