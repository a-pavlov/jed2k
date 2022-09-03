package org.dkf.jmule.adapters.menu;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.dkf.jmule.Platforms;
import org.dkf.jmule.R;
import org.dkf.jmule.transfers.Transfer;
import org.dkf.jmule.util.Ref;
import org.dkf.jmule.views.AbstractDialog;
import org.dkf.jmule.views.MenuAction;

import java.lang.ref.WeakReference;

public class ShowThePathMenuAction extends MenuAction {
    private final Transfer transfer;

    @SuppressWarnings("WeakerAccess")
    public final static class ShowThePathDialog extends AbstractDialog {
        Context context;
        String filename;

        public static ShowThePathDialog newInstance(Context context, String filename) {
            return new ShowThePathDialog(context, filename);
        }

        // Important to keep this guy 'public', even if IntelliJ thinks you shouldn't.
        // otherwise, the app crashes when you turn the screen and the dialog can't
        public ShowThePathDialog(Context contex, String filename) {
            super(R.layout.dialog_default_info);
            this.context = contex;
            this.filename = filename;
        }

        @Override
        protected void initComponents(Dialog dlg, Bundle savedInstanceState) {
            TextView title = findView(dlg, R.id.dialog_default_info_title);
            title.setText(R.string.transfers_context_menu_show_path_title);
            TextView text = findView(dlg, R.id.dialog_default_info_text);
            String pathsTemplate = context.getResources().getString(R.string.transfers_context_menu_show_path_body);
            String pathsStr = String.format(pathsTemplate, Platforms.data().getAbsolutePath(), filename);
            text.setText(pathsStr);
            Button okButton = findView(dlg, R.id.dialog_default_info_button_ok);
            okButton.setText(android.R.string.ok);
            okButton.setOnClickListener(new OkButtonOnClickListener(dlg));
        }
    }

    private final static class OkButtonOnClickListener implements View.OnClickListener {
        private final WeakReference<Dialog> dialogPtr;

        OkButtonOnClickListener(Dialog newNoWifiInformationDialog) {
            this.dialogPtr = Ref.weak(newNoWifiInformationDialog);
        }

        @Override
        public void onClick(View view) {
            if (Ref.alive(dialogPtr)) {
                dialogPtr.get().dismiss();
            }
        }
    }

    public ShowThePathMenuAction(Context context
            , Transfer transfer) {
        super(context, R.drawable.ic_search_black_24dp, R.string.transfers_context_menu_show_path_title);
        this.transfer = transfer;
    }

    @Override
    protected void onClick(Context context) {
        if (transfer != null) {
            ShowThePathDialog dialog = ShowThePathDialog.newInstance(context, transfer.getFilePath());
            dialog.show(((Activity)context).getFragmentManager());
        }
    }
}

