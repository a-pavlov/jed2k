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

package org.dkf.jmule.adapters.menu;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dkf.jmule.Engine;
import org.dkf.jmule.R;
import org.dkf.jmule.transfers.Transfer;
import org.dkf.jmule.views.AbstractDialog;
import org.dkf.jmule.views.MenuAction;

/**
 * @author gubatron
 * @author aldenml
 */
public class CancelMenuAction extends MenuAction {

    private final Transfer transfer;
    private final boolean deleteData;


    public CancelMenuAction(Context context, Transfer transfer, boolean removeFile) {
        super(context, R.drawable.ic_delete_forever_black_24dp, R.string.remove_torrent_and_data);
        this.transfer = transfer;
        this.deleteData = removeFile;
    }

    @Override
    protected void onClick(final Context context) {
        CancelMenuActionDialog.newInstance(
                transfer,
                deleteData, this).
                show(((Activity)getContext()).getFragmentManager());
    }

    @Getter
    public static class CancelMenuActionDialog extends AbstractDialog {
        private static Transfer transfer;
        private static boolean deleteData;
        private static CancelMenuAction cancelMenuAction;

        public static CancelMenuActionDialog newInstance(Transfer t,
                                                  boolean removeFile,
                                                  CancelMenuAction cancel_menu_action) {
            transfer = t;
            deleteData = removeFile;
            cancelMenuAction = cancel_menu_action;
            return new CancelMenuActionDialog();
        }

        public CancelMenuActionDialog() {
            super(R.layout.dialog_default_checkbox);
        }

        @Override
        protected void initComponents(Dialog dlg, Bundle savedInstanceState) {

            TextView dialogTitle = findView(dlg, R.id.dialog_default_checkbox_title);
            dialogTitle.setText(R.string.remove_torrent_and_data);

            TextView dialogText = findView(dlg, R.id.dialog_default_checkbox_text);
            dialogText.setText((deleteData) ? R.string.yes_no_cancel_delete_transfer_question : R.string.yes_no_cancel_transfer_question);
            CheckBox cbRemoveFile = findView(dlg, R.id.dialog_default_checkbox_show);
            cbRemoveFile.setChecked(deleteData);
            cbRemoveFile.setText(R.string.yes_no_dialog_remove_file);

            // Set the save button action
            Button noButton = findView(dlg, R.id.dialog_default_checkbox_button_no);
            noButton.setText(R.string.cancel);
            Button yesButton = findView(dlg, R.id.dialog_default_checkbox_button_yes);
            yesButton.setText(android.R.string.ok);
            boolean removeFile[] = {deleteData};

            noButton.setOnClickListener(new NegativeButtonOnClickListener(dlg));
            yesButton.setOnClickListener(new PositiveButtonOnClickListener(transfer
                    , removeFile
                    , cancelMenuAction
                    , dlg));

            cbRemoveFile.setOnCheckedChangeListener(new RemoveFileChecked(removeFile));
        }
    }

    private static class RemoveFileChecked implements CompoundButton.OnCheckedChangeListener {
        private final boolean removeFile[];

        public RemoveFileChecked(final boolean removeFile[]) {
            this.removeFile = removeFile;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            removeFile[0] = isChecked;
        }
    }

    private static class NegativeButtonOnClickListener implements View.OnClickListener {
        private final Dialog dlg;

        NegativeButtonOnClickListener(Dialog newCancelMenuActionDialog) {
            dlg = newCancelMenuActionDialog;
        }

        @Override
        public void onClick(View view) {
            dlg.cancel();
        }
    }

    @Slf4j
    private static class PositiveButtonOnClickListener implements View.OnClickListener {
        private final Transfer transfer;
        private final boolean removeFile[];
        private final Dialog dlg;

        @SuppressWarnings("unused")
        private final CancelMenuAction cancelMenuAction;

        PositiveButtonOnClickListener(Transfer transfer,
                                      final boolean removeFile[],
                                      CancelMenuAction cancelMenuAction,
                                      Dialog dialog) {
            this.transfer = transfer;
            this.dlg = dialog;
            this.removeFile = removeFile;
            this.cancelMenuAction = cancelMenuAction;
        }

        @Override
        public void onClick(View view) {
            Thread t = new Thread("Delete transfer - " + transfer.getDisplayName() + " file: " + (removeFile[0]?"remove":"save")) {
                @Override
                public void run() {
                    transfer.remove(removeFile[0]);
                }
            };

            Engine.instance().getThreadPool().execute(t);
            dlg.dismiss();
        }
    }
}