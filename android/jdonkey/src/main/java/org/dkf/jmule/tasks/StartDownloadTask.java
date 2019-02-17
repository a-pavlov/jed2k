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

package org.dkf.jmule.tasks;

import android.content.Context;
import org.dkf.jed2k.EMuleLink;
import org.dkf.jed2k.exception.ErrorCode;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.SearchEntry;
import org.dkf.jed2k.protocol.server.SharedFileEntry;
import org.dkf.jmule.R;
import org.dkf.jmule.transfers.Transfer;
import org.dkf.jmule.transfers.TransferManager;
import org.dkf.jmule.util.UIUtils;
import org.slf4j.Logger;


/**
 *
 * @author gubatron
 * @author aldenml
 *
 */
public class StartDownloadTask extends ContextTask<Transfer> {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(StartDownloadTask.class);
    private final String message;
    private SearchEntry entry;
    private String link = null;

    public StartDownloadTask(Context ctx, SearchEntry entry, String link, String message) {
        super(ctx);
        assert entry != null || link != null;
        this.entry = entry;
        this.link = link;
        this.message = message;
    }

    public StartDownloadTask(Context ctx, SharedFileEntry entry){
        this(ctx, entry, null, null);
    }

    @Override
    protected Transfer doInBackground() {
        Transfer transfer = null;
        try {
            UIUtils.showTransfersOnDownloadStart(getContext());
            if (entry != null) {
                transfer = TransferManager.instance().download(entry.getHash(), entry.getFileSize(), entry.getFileName());
            } else {
                EMuleLink el = EMuleLink.fromString(link);
                if (el.getType().equals(EMuleLink.LinkType.FILE)) {
                    transfer = TransferManager.instance().download(el.getHash(), el.getNumberValue(), el.getStringValue());
                } else {
                    // error message to user
                }
            }
        }
        catch(JED2KException e) {
            if (e.getErrorCode().equals(ErrorCode.IO_EXCEPTION)) {
            }
            else if (e.getErrorCode().equals(ErrorCode.INTERNAL_ERROR)) {
                //UIUtils.showShortMessage(getContext(), R.string.start_transfer_internal_error);
            }

            log.error("unable to start transfer {}", e.toString());
            e.printStackTrace();
        }
        catch (Exception e) {
            log.warn("Error adding new download from result {} {}", entry, e);
            e.printStackTrace();
        }

        return transfer;
    }

    @Override
    protected void onPostExecute(Context ctx, Transfer transfer) {
        if (transfer != null) {
            UIUtils.showShortMessage(getContext(), R.string.start_transfer_success);
        } else {
            UIUtils.showShortMessage(getContext(), R.string.start_transfer_file_error);
        }
    }
}
