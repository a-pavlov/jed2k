package org.dkf.jmule.transfers;

import lombok.extern.slf4j.Slf4j;
import org.dkf.jed2k.PeerInfo;
import org.dkf.jed2k.TransferHandle;
import org.dkf.jed2k.TransferStatus;
import org.dkf.jed2k.Utils;
import org.dkf.jmule.Engine;

import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * Created by ap197_000 on 12.09.2016.
 * this class is simple facade of transfer handle with cache transfer status feature to avoid
 * extra requests to session
 */
@Slf4j
public class ED2KTransfer implements Transfer {

    private final TransferHandle handle;
    private TransferStatus cachedStatus;
    private List<PeerInfo> cachedItems;

    /**
     * cache status in first call to avoid resources wasting
     * @return cached status
     */
    private final TransferStatus getStatus() {
        if (cachedStatus == null) cachedStatus = handle.getStatus();
        return cachedStatus;
    }

    public ED2KTransfer(final TransferHandle handle) {
        assert handle != null;
        this.handle = handle;
        cachedStatus = null;
        cachedItems = handle.getPeersInfo();
    }

    public String getHash() {
        return handle.getHash().toString();
    }

    public String getName() {
        return handle.getHash().toString();
    }

    public String getDisplayName() {
        File f = handle.getFile();
        if (f != null) return f.getName();
        return "";
    }

    public String getFilePath() {
        File f = handle.getFile();
        if (f != null) return f.getAbsolutePath();
        return "";
    }


    public long getSize() {
        return handle.getSize();
    }

    public Date getCreated() {
        return new Date(handle.getCreateTime());
    }

    public long getBytesReceived() {
        return getStatus().downloadPayload + getStatus().downloadProtocol;
    }

    public long getBytesSent() {
        return getStatus().upload;
    }

    public long getDownloadSpeed() {
        return getStatus().downloadRate;
    }

    public long getUploadSpeed() {
        return getStatus().uploadRate;
    }

    public boolean isDownloading() {
        return !isComplete();
    }

    public long getETA() {
        return getStatus().eta;
    }

    @Override
    public int getTotalPeers() {
        return getStatus().numPeers;
    }

    @Override
    public int getConnectedPeers() {
        return handle.getPeersInfo().size();
    }

    /**
     * [0..100]
     *
     * @return
     */
    public int getProgress() {
        return (int)(getStatus().progress*100);
    }

    public boolean isComplete() {
        return handle.isFinished();
    }

    @Override
    public void remove(boolean removeFile) {
        log.info("remove transfer {}", removeFile?"and file":"without file");
        Engine.instance().removeTransfer(handle.getHash(), true);
    }

    public List<PeerInfo> getItems() {
        return cachedItems;
    }

    @Override
    public boolean isPaused() {
        return handle.isPaused();
    }

    @Override
    public void pause() {
        handle.pause();
    }

    @Override
    public void resume() {
        handle.resume();
    }

    @Override
    public String toLink() {
        File f = handle.getFile();
        return Utils.formatLink(f!=null?f.getName():"", handle.getSize(), handle.getHash());
    }

    @Override
    public State getState() {
        if (isPaused()) return State.PAUSED;

        if (isDownloading()) {
            if (getItems().isEmpty()) return State.STALLED;
            return State.DOWNLOADING;
        }

        if (isComplete()) return State.COMPLETED;

        return State.NONE;
    }
}
