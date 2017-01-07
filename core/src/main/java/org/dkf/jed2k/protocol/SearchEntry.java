package org.dkf.jed2k.protocol;

/**
 * Created by apavlov on 09.12.16.
 */
public interface SearchEntry {
    public static final int SOURCE_SERVER = 1;
    public static final int SOURCE_KAD = 2;

    public Hash getHash();

    public int getSource();

    public String getFileName();

    public long getFileSize();

    public int getSources();

    public int getCompleteSources();

    public int getMediaBitrate();

    public int getMediaLength();

    public String getMediaCodec();

    public String getMediaAlbum();
}
