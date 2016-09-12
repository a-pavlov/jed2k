package org.dkf.jdonkey.transfers;

import org.dkf.jed2k.PeerInfo;
import org.dkf.jed2k.TransferHandle;
import org.dkf.jed2k.TransferStatus;

import java.util.Date;
import java.util.List;

/**
 * Created by ap197_000 on 12.09.2016.
 * this class is simple facade of transfer handle with cache transfer status feature to avoid
 * extra requests to session
 */
public class ED2KTransfer implements Transfer {

    private final TransferHandle handle;
    private TransferStatus cachedStatus;

    /**
     * cache status in first call to avoid resources wasting
     * @return cached status
     */
    private final TransferStatus getStatus() {
        if (cachedStatus == null) cachedStatus = handle.getStatus();
        return cachedStatus;
    }

    public ED2KTransfer(final TransferHandle handle) {
        this.handle = handle;
        cachedStatus = null;
    }

    public String getName() {
        return "";
    }

    public String getDisplayName() {
        return getName();
    }

    public String getFilePath() {
        return handle.getFilepath();
    }


    public long getSize() {
        return handle.getSize();
    }

    public Date getCreated() {
        return new Date(handle.getCreateTime());
    }

    //public TransferState getState();

    public long getBytesReceived() {
        return getStatus().downloadPayload + getStatus().downloadProtocol;
    }

    public long getBytesSent() {
        return getStatus().upload;
    }

    public long getDownloadSpeed() {
        return getStatus().downloadPayloadRate + getStatus().downloadRate;
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

    public void remove() {
        // dispatch call to handle here
    }

    public List<PeerInfo> getItems() {
        return handle.getPeersInfo();
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
}
