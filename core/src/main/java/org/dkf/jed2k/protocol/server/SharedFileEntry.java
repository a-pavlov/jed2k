package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.protocol.tag.Tag;

public class SharedFileEntry extends UsualPacket {

    private Tag getTagById(final byte id) {
        for(final Tag t: properties) {
            if (t.id() == id) return t;
        }

        return null;
    }

    public final String getFileName() {
        Tag t = getTagById(Tag.FT_FILENAME);
        if (t != null) return t.asStringValue();
        return "";
    }

    public final long getFileSize() {
        Tag t = getTagById(Tag.FT_FILESIZE);
        if (t != null) return t.asLongValue();
        return 0;
    }

    public final int getSources() {
        Tag t = getTagById(Tag.FT_SOURCES);
        if (t != null) return t.asIntValue();
        return 0;
    }

    public final int getCompleteSources() {
        Tag t = getTagById(Tag.FT_COMPLETE_SOURCES);
        if (t != null) return t.asIntValue();
        return 0;
    }
}