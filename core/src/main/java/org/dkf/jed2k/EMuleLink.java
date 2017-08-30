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
        NODES,
        FILE
    };

    public EMuleLink(final Hash hash, final long numberValue, final String stringValue, final LinkType type) {
        this.hash = hash;
        this.numberValue = numberValue;
        this.stringValue = stringValue;
        this.type = type;
    }

    public static EMuleLink fromString(final String uri) throws JED2KException {
        if (uri == null) throw new JED2KException(ErrorCode.LINK_MAILFORMED);

        String decUri;
        try {
            decUri = URLDecoder.decode(uri, "UTF-8");
        }
        catch(UnsupportedEncodingException e) {
            throw new JED2KException(ErrorCode.UNSUPPORTED_ENCODING);
        }

        assert decUri != null;

        String[] parts = decUri.split("\\|");

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
            return new EMuleLink(null, 0, parts[2], LinkType.SERVERS);
        }

        if ("nodeslist".equals(parts[1]) && parts.length == 4) {
            return new EMuleLink(null, 0, parts[2], LinkType.NODES);
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
            } catch (Exception e) {
                // here illegal argument exception most likely, but it doesn't matter
                throw new JED2KException(ErrorCode.INTERNAL_ERROR);
            }
        }

        throw new JED2KException(ErrorCode.UNKNOWN_LINK_TYPE);
    }
}
