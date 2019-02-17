package org.dkf.jed2k;

import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.Hash;
import org.slf4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Created by inkpot on 21.08.2016.
 */
public class EMuleLink {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(EMuleLink.class);
    private final Hash hash;
    private final long numberValue;
    private final String stringValue;
    private final LinkType type;

    public Hash getHash() {
        return this.hash;
    }

    public long getNumberValue() {
        return this.numberValue;
    }

    public String getStringValue() {
        return this.stringValue;
    }

    public LinkType getType() {
        return this.type;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof EMuleLink)) return false;
        final EMuleLink other = (EMuleLink) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$hash = this.getHash();
        final Object other$hash = other.getHash();
        if (this$hash == null ? other$hash != null : !this$hash.equals(other$hash)) return false;
        if (this.getNumberValue() != other.getNumberValue()) return false;
        final Object this$stringValue = this.getStringValue();
        final Object other$stringValue = other.getStringValue();
        if (this$stringValue == null ? other$stringValue != null : !this$stringValue.equals(other$stringValue))
            return false;
        final Object this$type = this.getType();
        final Object other$type = other.getType();
        if (this$type == null ? other$type != null : !this$type.equals(other$type)) return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof EMuleLink;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $hash = this.getHash();
        result = result * PRIME + ($hash == null ? 43 : $hash.hashCode());
        final long $numberValue = this.getNumberValue();
        result = result * PRIME + (int) ($numberValue >>> 32 ^ $numberValue);
        final Object $stringValue = this.getStringValue();
        result = result * PRIME + ($stringValue == null ? 43 : $stringValue.hashCode());
        final Object $type = this.getType();
        result = result * PRIME + ($type == null ? 43 : $type.hashCode());
        return result;
    }

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
