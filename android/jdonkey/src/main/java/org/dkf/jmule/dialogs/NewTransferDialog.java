/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml), Marcelina Knitter (@marcelinkaaa)
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

package org.dkf.jmule.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import org.dkf.jmule.ConfigurationManager;
import org.dkf.jmule.Constants;
import org.dkf.jed2k.protocol.SearchEntry;
import org.dkf.jmule.R;
import org.dkf.jmule.util.UIUtils;
import org.dkf.jmule.views.AbstractDialog;
import org.dkf.jmule.views.ClickAdapter;

//import com.frostwire.search.FileSearchResult;

/**
 * @author gubatron
 * @author aldenml
 * @author marcelinkaaa
 */
public class NewTransferDialog extends AbstractDialog {

    public static final String TAG = getSuggestedTAG(NewTransferDialog.class);

    private static final String SEARCH_RESULT_DATA_KEY = "search_result_data";
    private static final String HIDE_CHECK_SHOW_KEY = "hide_check_show";

    public static SearchEntry entry;
    private boolean hideCheckShow;

    public NewTransferDialog() {
        super(R.layout.dialog_default_checkbox);
    }

    void setArgument(final SearchEntry entry, boolean hideCheckShow) {
        this.entry = entry;
        this.hideCheckShow = hideCheckShow;
    }

    public static NewTransferDialog newInstance(SearchEntry entry, boolean hideCheckShow) {
        NewTransferDialog f = new NewTransferDialog();
        f.setArgument(entry, hideCheckShow);

        Bundle args = new Bundle();
        //srRef = Ref.weak(sr);
        //args.putSerializable(SEARCH_RESULT_DATA_KEY, new SearchResultData(sr));
        //args.putBoolean(HIDE_CHECK_SHOW_KEY, hideCheckShow);
        f.setArguments(args);

        return f;
    }

    @Override
    protected void initComponents(Dialog dlg, Bundle savedInstanceState) {

        Bundle args = getArguments();

        //boolean hideCheckShow = args.getBoolean(HIDE_CHECK_SHOW_KEY);

        dlg.setContentView(R.layout.dialog_default_checkbox);

        TextView dialogTitle = findView(dlg, R.id.dialog_default_checkbox_title);
        dialogTitle.setText(R.string.confirm_download);

        TextView dialogText = findView(dlg, R.id.dialog_default_checkbox_text);
        dialogText.setText(R.string.dialog_new_transfer_text_text);

        Context ctx = dlg.getContext();

        String sizeStr = entry.getFileSize() > 0 ? UIUtils.getBytesInHuman(entry.getFileSize()) : ctx.getString(R.string.size_unknown);

        TextView textQuestion = findView(dlg, R.id.dialog_default_checkbox_text);
        textQuestion.setText(dlg.getContext().getString(R.string.dialog_new_transfer_text_text, entry.getFileName(), sizeStr));

        DialogListener yes = new DialogListener(this, true);
        DialogListener no = new DialogListener(this, false);

        Button buttonYes = findView(dlg, R.id.dialog_default_checkbox_button_yes);
        buttonYes.setText(android.R.string.yes);
        buttonYes.setOnClickListener(yes);

        Button buttonNo = findView(dlg, R.id.dialog_default_checkbox_button_no);
        buttonNo.setText(android.R.string.no);
        buttonNo.setOnClickListener(no);

        CheckBox checkShow = findView(dlg, R.id.dialog_default_checkbox_show);
        checkShow.setChecked(ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_GUI_SHOW_NEW_TRANSFER_DIALOG));
        checkShow.setOnCheckedChangeListener(yes);

        if (hideCheckShow) {
            checkShow.setVisibility(View.GONE);
        }
    }

    private static final class DialogListener extends ClickAdapter<NewTransferDialog> {

        private final boolean positive;

        public DialogListener(NewTransferDialog owner, boolean positive) {
            super(owner);
            this.positive = positive;
        }

        @Override
        public void onClick(NewTransferDialog owner, View v) {
            if (positive) {
                // see SearchFragment::OnDialogClickListener::onDialogClick(tag,which)
                owner.performDialogClick(Dialog.BUTTON_POSITIVE);
            }
            owner.dismiss();
        }

        @Override
        public void onCheckedChanged(NewTransferDialog owner, CompoundButton buttonView, boolean isChecked) {
            ConfigurationManager.instance().setBoolean(Constants.PREF_KEY_GUI_SHOW_NEW_TRANSFER_DIALOG, isChecked);
        }
    }
}
