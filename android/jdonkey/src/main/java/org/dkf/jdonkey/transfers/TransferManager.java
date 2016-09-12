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

package org.dkf.jdonkey.transfers;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Environment;
import android.os.StatFs;
import org.dkf.jdonkey.core.ConfigurationManager;
import org.dkf.jdonkey.core.Constants;
import org.dkf.jdonkey.core.NetworkManager;
import org.dkf.jed2k.PeerInfo;
import org.dkf.jed2k.protocol.NetworkIdentifier;
import org.dkf.jed2k.protocol.server.SharedFileEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/*
import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.gui.NetworkManager;
import com.frostwire.android.gui.services.Engine;
import com.frostwire.bittorrent.BTDownload;
import com.frostwire.bittorrent.BTEngine;
import com.frostwire.bittorrent.BTEngineAdapter;
import com.frostwire.util.Logger;
import com.frostwire.search.HttpSearchResult;
import com.frostwire.search.ScrapedTorrentFileSearchResult;
import com.frostwire.search.SearchResult;
import com.frostwire.search.soundcloud.SoundcloudSearchResult;
import com.frostwire.search.torrent.TorrentCrawledSearchResult;
import com.frostwire.search.torrent.TorrentSearchResult;
import com.frostwire.search.youtube.YouTubeCrawledSearchResult;
import com.frostwire.transfers.*;
*/

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
        loadTorrents();
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
        if (transfers.isEmpty()) {
            for (int i = 0; i < 10; ++i) {
                transfers.add(new MockTransfer());
            }
        }

        return transfers;
    }

    public Transfer download(SharedFileEntry sr) {
        Transfer transfer = null;
/*
        if (sr instanceof TorrentSearchResult) {
            transfer = newBittorrentDownload((TorrentSearchResult) sr);
        } else if (sr instanceof HttpSlideSearchResult) {
            transfer = newHttpDownload((HttpSlideSearchResult) sr);
        } else if (sr instanceof YouTubeCrawledSearchResult) {
            transfer = newYouTubeDownload((YouTubeCrawledSearchResult) sr);
        } else if (sr instanceof SoundcloudSearchResult) {
            transfer = newSoundcloudDownload((SoundcloudSearchResult) sr);
        } else if (sr instanceof HttpSearchResult) {
            transfer = newHttpDownload((HttpSearchResult) sr);
        }
*/
        return transfer;
    }

    public void clearComplete() {
        /*
        List<Transfer> transfers = getTransfers();
        for (Transfer transfer : transfers) {
            if (transfer != null && transfer.isComplete()) {
                if (transfer instanceof BittorrentDownload) {
                    BittorrentDownload bd = (BittorrentDownload) transfer;
                    if (bd.isPaused()) {
                        bd.remove(false);
                    }
                } else {
                    transfer.remove(false);
                }
            }
        }
        */
    }

    public int getActiveDownloads() {
        int count = 0;

        for (final Transfer t: transfers) {
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

        for(final Transfer t: transfers) {
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

    public void loadTorrents() {
        /*
        bittorrentDownloads.clear();

        BTEngine engine = BTEngine.getInstance();

        engine.setListener(new BTEngineAdapter() {
            @Override
            public void downloadAdded(BTEngine engine, BTDownload dl) {
                String name = dl.getName();
                if (name != null && name.contains("fetch_magnet:")) {
                    return;
                }

                File savePath = dl.getSavePath();
                if (savePath != null && savePath.toString().contains("fetch_magnet/")) {
                    return;
                }

                bittorrentDownloads.add(new UIBittorrentDownload(TransferManager.this, dl));
            }

            @Override
            public void downloadUpdate(BTEngine engine, BTDownload dl) {
                try {
                    int count = bittorrentDownloads.size();
                    for (int i = 0; i < count; i++) {
                        if (i < bittorrentDownloads.size()) {
                            BittorrentDownload download = bittorrentDownloads.get(i);
                            if (download instanceof UIBittorrentDownload) {
                                UIBittorrentDownload bt = (UIBittorrentDownload) download;
                                if (bt.getInfoHash().equals(dl.getInfoHash())) {
                                    bt.updateUI(dl);
                                    break;
                                }
                            }
                        }
                    }
                } catch (Throwable e) {
                    LOG.error("Error updating bittorrent download", e);
                }
            }
        });

        engine.restoreDownloads();
        */
    }

    public boolean remove(Transfer transfer) {
        return false;
    }

    public void pauseTorrents() {
    }

    public Transfer downloadTorrent(String uri) {
        //return downloadTorrent(uri, null);
        return null;
    }

    /*
    public BittorrentDownload downloadTorrent(String uri, TorrentFetcherListener fetcherListener) {
        String url = uri.trim();
        try {
            if (url.contains("urn%3Abtih%3A")) {
                //fixes issue #129: over-encoded url coming from intent
                url = url.replace("urn%3Abtih%3A", "urn:btih:");
            }

            if (isAlreadyDownloadingTorrentByUri(url)) {
                return null;
            }

            Uri u = Uri.parse(url);

            if (!u.getScheme().equalsIgnoreCase("file") &&
                !u.getScheme().equalsIgnoreCase("http") &&
                !u.getScheme().equalsIgnoreCase("https") &&
                !u.getScheme().equalsIgnoreCase("magnet")) {
                LOG.warn("Invalid URI scheme: " + u.toString());
                return new InvalidBittorrentDownload(R.string.torrent_scheme_download_not_supported);
            }

            BittorrentDownload download = null;

            if (fetcherListener == null) {
                if (u.getScheme().equalsIgnoreCase("file")) {
                    BTEngine.getInstance().download(new File(u.getPath()), null);
                } else if (u.getScheme().equalsIgnoreCase("http") || u.getScheme().equalsIgnoreCase("https") || u.getScheme().equalsIgnoreCase("magnet")) {
                    download = new TorrentFetcherDownload(this, new TorrentUrlInfo(u.toString()));
                    bittorrentDownloads.add(download);
                }
            } else {
                if (u.getScheme().equalsIgnoreCase("file")) {
                    fetcherListener.onTorrentInfoFetched(FileUtils.readFileToByteArray(new File(u.getPath())), null);
                } else if (u.getScheme().equalsIgnoreCase("http") || u.getScheme().equalsIgnoreCase("https") || u.getScheme().equalsIgnoreCase("magnet")) {
                    // this executes the listener method when it fetches the bytes.
                    new TorrentFetcherDownload(this, new TorrentUrlInfo(u.toString()), fetcherListener);
                }
                return null;
            }

            return download;
        } catch (Throwable e) {
            LOG.warn("Error creating download from uri: " + url);
            return new InvalidBittorrentDownload(R.string.torrent_scheme_download_not_supported);
        }
    }
*/
    /*
    private static Transfer createBittorrentDownload(TransferManager manager, TorrentSearchResult sr) {
        if (sr instanceof TorrentCrawledSearchResult) {
            BTEngine.getInstance().download((TorrentCrawledSearchResult) sr, null);
        } else if (sr instanceof ScrapedTorrentFileSearchResult) {
            return new TorrentFetcherDownload(manager, new TorrentSearchResultInfo(sr, sr.getReferrerUrl()));
        } else if (sr.getTorrentUrl() != null) {
            return new TorrentFetcherDownload(manager, new TorrentSearchResultInfo(sr));
        }

        return null;
    }
    */

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

        public MockTransfer() {
            int count = rnd.nextInt(6);
            for(int i = 0; i < count; ++i) {
                PeerInfo pi = new PeerInfo();
                pi.endpoint = new NetworkIdentifier(rnd.nextInt(), (short)rnd.nextInt(30000));
                pi.modName = "mod";
                pi.modVersion = rnd.nextInt(22);
                pi.strModVersion = Integer.toString(rnd.nextInt(33));
                pi.downloadPayload = rnd.nextInt(555656);
                pi.downloadSpeed = rnd.nextInt(30000);
                info.add(pi);
            }
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
    }
}
