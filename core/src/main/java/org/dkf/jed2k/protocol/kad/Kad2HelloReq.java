package org.dkf.jed2k.protocol.kad;

import lombok.ToString;
import org.dkf.jed2k.kad.ReqDispatcher;

import java.net.InetSocketAddress;

/**
 * Created by inkpot on 15.11.2016.
 */
@ToString
public class Kad2HelloReq extends Kad2Hello implements KadDispatchable {
    @Override
    public void dispatch(ReqDispatcher dispatcher, final InetSocketAddress address) {
        dispatcher.process(this, address);
    }
}
