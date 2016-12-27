package org.dkf.jed2k.protocol.server;

import org.dkf.jed2k.protocol.SearchEntry;
import org.dkf.jed2k.protocol.tag.Tag;

public class SharedFileEntry extends UsualPacket implements SearchEntry {

    @Override
    public int getSource() {
        return SearchEntry.SOURCE_SERVER;
    }

    @Override
    public final String getFileName() {
        Tag t = Tag.getTagById(Tag.FT_FILENAME, properties);
        return (t != null)?t.asStringValue():"";
    }

    @Override
    public final long getFileSize() {
        Tag t = Tag.getTagById(Tag.FT_FILESIZE, properties);
        return (t != null)?t.asLongValue():0;
    }

    @Override
    public final int getSources() {
        Tag t = Tag.getTagById(Tag.FT_SOURCES, properties);
        return (t != null)?t.asIntValue():0;
    }

    @Override
    public final int getCompleteSources() {
        Tag t = Tag.getTagById(Tag.FT_COMPLETE_SOURCES, properties);
        return (t != null)?t.asIntValue():0;
    }

    @Override
    public int getMediaBitrate() {
        Tag t = Tag.getTagById(Tag.FT_MEDIA_BITRATE, properties);
        return (t != null)?t.asIntValue():0;
    }

    @Override
    public int getMediaLength() {
        Tag t = Tag.getTagById(Tag.FT_MEDIA_LENGTH, properties);
        return (t != null)?t.asIntValue():0;
    }

    @Override
    public String getMediaCodec() {
        Tag t = Tag.getTagById(Tag.FT_MEDIA_CODEC, properties);
        return (t != null)?t.asStringValue():"";
    }

    @Override
    public String getMediaAlbum() {
        return "";
    }
}
