/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2016, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dkf.jmule.transfers;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StatFs;
import org.dkf.jed2k.PeerInfo;
import org.dkf.jed2k.protocol.Endpoint;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jed2k.protocol.server.SharedFileEntry;
import org.dkf.jmule.Engine;
import org.dkf.jmule.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author gubatron
 * @author aldenml
 */
public final class TransferManager {

    private static final Logger LOG = LoggerFactory.getLogger(TransferManager.class);

    private int downloadsToReview;
    private int startedTransfers = 0;
    private final Object alreadyDownloadingMonitor = new Object();
    private volatile static TransferManager instance;
    private final ConfigurationManager CM;
    private List<Transfer> transfers = new ArrayList<>();
    private ParcelFileDescriptor fd;
    private FileOutputStream os;

    public static TransferManager instance() {
        if (instance == null) {
            instance = new TransferManager();
        }
        return instance;
    }

    private TransferManager() {
        registerPreferencesChangeListener();
        CM = ConfigurationManager.instance();
        this.downloadsToReview = 0;
    }

    /**
     * Is it using the SD Card's private (non-persistent after uninstall) app folder to save
     * downloaded files?
     */
    public static boolean isUsingSDCardPrivateStorage() {
        String primaryPath = Environment.getExternalStorageDirectory().getAbsolutePath();
        String currentPath = ConfigurationManager.instance().getStoragePath();

        return !primaryPath.equals(currentPath);
    }

    public List<Transfer> getTransfers() {
        return Engine.instance().getTransfers();
        /*List<Transfer> tran = new ArrayList<>();
        if (transfers.isEmpty()) {
            for (int i = 0; i < 10; ++i) {
                transfers.add(new MockTransfer());
            }
        } else {
            for(Transfer t: transfers) {
                ((MockTransfer)t).shuffle();
            }
        }

        return transfers;
        */

    }

    public Transfer download(final Hash hash, long size, final String fileName) {
        File f = new File(ConfigurationManager.instance().getStoragePath(), fileName);
        //return Engine.instance().startDownload(hash, size, f.getAbsolutePath(), null);

        os = null;
        FileChannel channel = null;
        try {
            LollipopFileSystem fs = (LollipopFileSystem) Platforms.fileSystem();
            fd = fs.openFD(f, "rw");
            if (fd != null) {
                os = new FileOutputStream(fd.getFileDescriptor());
                channel = os.getChannel();
                LOG.info("channel ready to start on file {}", f);
                //return Engine.instance().startDownload(hash, size, f.getAbsolutePath(), channel);
            } else {
                LOG.error("unable to get document for {}", f);
            }
        } catch(Exception e) {
            LOG.error("unable to fill file {} error {}", f, e);
            try {
                if (channel != null) channel.close();
                if (os != null) os.close();
            } catch(IOException ex) {
                LOG.error("unable to free resources {}", ex);
            }
        }

        return null;
    }

    public int getActiveDownloads() {
        int count = 0;

        List<Transfer> tr = Engine.instance().getTransfers();
        for (final Transfer t: tr) {
            if (!t.isComplete() && t.isDownloading()) {
                count++;
            }
        }

        return count;
    }

    public int getActiveUploads() {
        int count = 0;
        return count;
    }

    public long getDownloadsBandwidth() {
        long res = 0;

        for(final Transfer t: Engine.instance().getTransfers()) {
            res += t.getDownloadSpeed();
        }

        return res;
    }

    public double getUploadsBandwidth() {
        long res = 0;
        for(final Transfer t: transfers) {
            res += t.getUploadSpeed();
        }

        return res;
    }

    public int getDownloadsToReview() {
        return downloadsToReview;
    }

    public void incrementDownloadsToReview() {
        downloadsToReview++;
    }

    public void clearDownloadsToReview() {
        downloadsToReview = 0;
    }

    public boolean remove(Transfer transfer) {
        return false;
    }


    private Transfer newBittorrentDownload(SharedFileEntry sr) {
        /*try {
            BittorrentDownload bittorrentDownload = createBittorrentDownload(this, sr);
            if (bittorrentDownload != null) {
                bittorrentDownloads.add(bittorrentDownload);
            }
            return null;
        } catch (Throwable e) {
            LOG.warn("Error creating download from search result: " + sr);
            return new InvalidBittorrentDownload(R.string.empty_string);
        }
        */
        return null;
    }


    public boolean isMobileAndDataSavingsOn() {
        return NetworkManager.instance().isDataMobileUp() &&
                !ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_NETWORK_USE_MOBILE_DATA);
    }


    public void resumeResumableTransfers() {
/*
        List<Transfer> transfers = getTransfers();

        for (Transfer t : transfers) {
            if (t instanceof BittorrentDownload) {
                BittorrentDownload bt = (BittorrentDownload) t;
                if (bt.isPaused()) {
                    bt.resume();
                }
            } else if (t instanceof HttpDownload) {
            }
        }*/
    }

    public int getStartedTransfers() {
        return startedTransfers;
    }

    public int incrementStartedTransfers() {
        return ++startedTransfers;
    }

    public void resetStartedTransfers() {
        startedTransfers = 0;
    }

    /**
     * @return true if less than 10MB available
     */
    static boolean isCurrentMountAlmostFull() {
        return getCurrentMountAvailableBytes() < 10000000;
    }

    static long getCurrentMountAvailableBytes() {
        StatFs stat = new StatFs(ConfigurationManager.instance().getStoragePath());
        return ((long) stat.getBlockSize() * (long) stat.getAvailableBlocks());
    }

    private void registerPreferencesChangeListener() {
        OnSharedPreferenceChangeListener preferenceListener = new OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                //BTEngine e = BTEngine.getInstance();

                if (key.equals(Constants.PREF_KEY_TRANSFER_MAX_DOWNLOADS)) {
                    //e.setMaxActiveDownloads((int) ConfigurationManager.instance().getLong(key));
                } else if (key.equals(Constants.PREF_KEY_TRANSFER_MAX_TOTAL_CONNECTIONS)) {
                    //e.setMaxConnections((int) ConfigurationManager.instance().getLong(key));
                }
            }
        };

        ConfigurationManager.instance().registerOnPreferenceChange(preferenceListener);
    }

    public static class MockTransfer implements Transfer {

        private static Random rnd = new Random();
        private List<PeerInfo> info = new ArrayList<>();
        private String name = "Transfer name " + rnd.nextInt(20);
        private long size = rnd.nextInt(500000);
        private Date created = new Date(rnd.nextLong());
        private long bytesReceived = rnd.nextInt(3333333);
        private long bytesSent = rnd.nextInt(4000);
        private int dowloadSpeed = rnd.nextInt(400000);
        private int uploadSpeed = rnd.nextInt(400);
        private boolean downloading = rnd.nextBoolean();
        private int eta = rnd.nextInt(40000);
        private int progress = rnd.nextInt(100);
        private boolean paused = rnd.nextBoolean();
        private int totalPees = rnd.nextInt(20);

        PeerInfo generatePeer() {
            PeerInfo pi = new PeerInfo();
            pi.setEndpoint(new Endpoint(rnd.nextInt(), (short)rnd.nextInt(30000)));
            pi.setModName("mod");
            pi.setModVersion(rnd.nextInt(22));
            pi.setStrModVersion(Integer.toString(rnd.nextInt(33)));
            pi.setDownloadPayload(rnd.nextInt(555656));
            pi.setDownloadSpeed(rnd.nextInt(30000));
            return pi;
        }

        public MockTransfer() {
            int count = rnd.nextInt(20);
            for(int i = 0; i < count; ++i) {
                info.add(generatePeer());
            }
        }

        @Override
        public String getHash() {
            return "";
        }

        @Override
        public String getName() { return name; }

        @Override
        public String getDisplayName() { return getName(); }

        @Override
        public String getFilePath() { return "filepath/xx/ttt/" + getName(); }

        @Override
        public long getSize() { return size; }

        @Override
        public Date getCreated() { return created;  }

        @Override
        public long getBytesReceived() {
            return bytesReceived;
        }

        @Override
        public long getBytesSent() {
            return bytesSent;
        }

        @Override
        public long getDownloadSpeed() {
            return dowloadSpeed;
        }

        @Override
        public long getUploadSpeed() {
            return uploadSpeed;
        }

        @Override
        public boolean isDownloading() {
            return downloading;
        }

        @Override
        public long getETA() {
            return eta;
        }

        @Override
        public int getTotalPeers() {
            return totalPees;
        }

        @Override
        public int getConnectedPeers() {
            return info.size();
        }

        @Override
        public int getProgress() {
            return progress;
        }

        @Override
        public boolean isComplete() {
            return !isDownloading();
        }

        @Override
        public void remove() {

        }

        @Override
        public List<PeerInfo> getItems() {
            return info;
        }

        @Override
        public boolean isPaused() {
            return paused;
        }

        @Override
        public void pause() {
            paused = true;
        }

        @Override
        public void resume() {

        }

        @Override
        public String toLink() {
            return "";
        }

        @Override
        public State getState() {
            return State.NONE;
        }

        void shuffle() {
            boolean remove = rnd.nextBoolean();
            if (remove && !info.isEmpty()) {
                int item = rnd.nextInt(info.size());
                info.remove(item);
            } else {
                info.add(generatePeer());
            }
        }
    }
}
