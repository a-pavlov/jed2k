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

import org.dkf.jed2k.PeerInfo;

import java.util.Date;
import java.util.List;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public interface Transfer {

    public enum State {
        PAUSED,
        COMPLETED,
        STALLED,
        DOWNLOADING,
        NONE
    }

    public String getHash();

    public String getName();

    public String getDisplayName();

    public String getFilePath();

    public long getSize();

    public Date getCreated();

    public long getBytesReceived();

    public long getBytesSent();

    public long getDownloadSpeed();

    public long getUploadSpeed();

    boolean isDownloading();

    public long getETA();

    public int getTotalPeers();

    public int getConnectedPeers();

    /**
     * [0..100]
     *
     * @return
     */
    public int getProgress();

    public boolean isComplete();

    public void remove();

    public List<PeerInfo> getItems();

    public boolean isPaused();

    public void pause();

    public void resume();

    public String toLink();

    public State getState();
}
