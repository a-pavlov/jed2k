package org.jed2k;

import jdk.nashorn.internal.runtime.UnwarrantedOptimismException;
import org.jed2k.exception.ErrorCode;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.Hash;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by inkpot on 21.08.2016.
 */
public class EMuleLink {
    public final Hash hash;
    public final long size;
    public final String filepath;

    public EMuleLink(final Hash h, final long s, final String f) {
        hash = h;
        size = s;
        filepath = f;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EMuleLink) {
            EMuleLink e = (EMuleLink)o;
            return hash.equals(e.hash) && size == e.size && filepath.equals(e.filepath);
        }

        return false;
    }

    public static EMuleLink fromString(final String link) throws JED2KException {
        // 12345678901234       56       7 + 32 + 89 = 19+32=51
        // ed2k://|file|fileName|fileSize|fileHash|/
        if (link.length() < 51 || (link.substring(0,13).compareTo("ed2k://|file|") != 0) || link.substring(link.length()-2).compareTo("|/") != 0) {
            throw new JED2KException(ErrorCode.LINK_MAILFORMED);
        }

        final String[] parts = link.split("\\|");

        if (parts.length < 6) {
            throw new JED2KException(ErrorCode.LINK_MAILFORMED);
        }

        try {
            return new EMuleLink(Hash.fromString(parts[4]), Long.parseLong(parts[3]), URLDecoder.decode(parts[2], "UTF-8"));
        } catch(UnsupportedEncodingException e) {
            throw new JED2KException(ErrorCode.UNSUPPORTED_ENCODING);
        }
    }
}
