package org.dkf.jed2k;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by inkpot on 21.08.2016.
 */
@EqualsAndHashCode
@Slf4j
@Getter
public class EMuleLink {
    private final Hash hash;
    private final long numberValue;
    private final String stringValue;
    private final LinkType type;

    public enum LinkType {
        SERVER,
        SERVERS,
        FILE
    };

    public EMuleLink(final Hash hash, final long numberValue, final String stringValue, final LinkType type) {
        this.hash = hash;
        this.numberValue = numberValue;
        this.stringValue = stringValue;
        this.type = type;
    }

    public static EMuleLink fromString(final String uri) throws JED2KException {
        String[] parts = uri.split("\\|");

        if (parts.length < 2 || !"ed2k://".equals(parts[0]) || !"/".equals(parts[parts.length - 1])) {
            throw new JED2KException(ErrorCode.LINK_MAILFORMED);
        }

        if ("server".equals(parts[1]) && parts.length == 5) {
            try {
                return new EMuleLink(null, Long.parseLong(parts[3]), parts[2], LinkType.SERVER);
            } catch(NumberFormatException e) {
                throw new JED2KException(ErrorCode.NUMBER_FORMAT_ERROR);
            }
        }

        if ("serverlist".equals(parts[1]) && parts.length == 4) {
            try {
                return new EMuleLink(null, 0, URLDecoder.decode(parts[2], "UTF-8"), LinkType.SERVERS);
            } catch(UnsupportedEncodingException e) {
                throw new JED2KException(ErrorCode.UNSUPPORTED_ENCODING);
            }
        }

        if ("file".equals(parts[1]) && parts.length >= 6) {
            try {
                return new EMuleLink(Hash.fromString(parts[4])
                        , Long.parseLong(parts[3])
                        , URLDecoder.decode(parts[2], "UTF-8")
                        , LinkType.FILE);
            } catch(NumberFormatException e) {
                throw new JED2KException(ErrorCode.NUMBER_FORMAT_ERROR);
            } catch(UnsupportedEncodingException e) {
                throw new JED2KException(ErrorCode.UNSUPPORTED_ENCODING);
            }
        }

        throw new JED2KException(ErrorCode.UNKNOWN_LINK_TYPE);
    }
}
