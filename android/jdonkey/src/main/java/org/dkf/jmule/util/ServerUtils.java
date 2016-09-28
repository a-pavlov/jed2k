package org.dkf.jmule.util;

import org.dkf.jed2k.protocol.server.ServerMet;

/**
 * Created by ap197_000 on 14.09.2016.
 */
public class ServerUtils {

    public static String getIdentifier(ServerMet.ServerMetEntry entry) {
        return getIdentifier(entry.getName(), entry.getHost(), entry.getPort());
    }

    public static String getIdentifier(final String name, final String host, int port) {
        StringBuilder sb = new StringBuilder();
        return sb.append(name).append("_").append(host).append(port).toString();
    }
}
