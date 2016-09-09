package org.dkf.jdonkey.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import org.dkf.jdonkey.Engine;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.views.AbstractDialog;
import org.dkf.jdonkey.views.ClickAdapter;

/**
 * Created by ap197_000 on 09.09.2016.
 */
public class ConnectServerDialog extends AbstractDialog {
    private String serverId;
    private String serverHost;
    private int serverPort;

    public ConnectServerDialog() {
        super(R.layout.dialog_default);
    }

    public ConnectServerDialog(final String tag) {
        super(tag, R.layout.dialog_default);
    }

    public void initializeServerAttributes(final String serverId, final String serverHost, int serverPort) {
        this.serverId = serverId;
        this.serverHost = serverHost;
        this.serverPort = serverPort;
    }

    @Override
    protected void initComponents(Dialog dlg, Bundle savedInstanceState) {
        final String currentServerId = Engine.instance().getCurrentServerId();

        dlg.setContentView(R.layout.dialog_default);

        TextView dialogTitle = findView(dlg, R.id.dialog_default_title);
        dialogTitle.setText((currentServerId.compareTo(serverId) == 0)?R.string.confirm_server_disconnect:R.string.confirm_server_connect);

        TextView dialogText = findView(dlg, R.id.dialog_default_text);
        if (currentServerId.isEmpty()) {
            dialogText.setText(R.string.server_connect);
        } else if (currentServerId.compareTo(serverId) == 0) {
            dialogText.setText(R.string.server_disconnect);
        } else {
            dialogText.setText(R.string.server_disconnect_connect);
        }

        Context ctx = dlg.getContext();

        TextView textQuestion = findView(dlg, R.id.dialog_default_text);

        if (currentServerId.isEmpty()) {
            textQuestion.setText(dlg.getContext().getString(R.string.server_connect, serverHost));
        } else if (currentServerId.compareTo(serverId) == 0) {
            textQuestion.setText(dlg.getContext().getString(R.string.server_disconnect, serverHost));
        } else {
            textQuestion.setText(dlg.getContext().getString(R.string.server_disconnect_connect, serverHost));
        }

        DialogListener yes = new DialogListener(this, true);
        DialogListener no = new DialogListener(this, false);

        Button buttonYes = findView(dlg, R.id.dialog_default_button_yes);
        buttonYes.setText(android.R.string.yes);
        buttonYes.setOnClickListener(yes);

        Button buttonNo = findView(dlg, R.id.dialog_default_button_no);
        buttonNo.setText(android.R.string.no);
        buttonNo.setOnClickListener(no);
    }

    private static final class DialogListener extends ClickAdapter<ConnectServerDialog> {
        private final boolean positive;

        public DialogListener(ConnectServerDialog owner, boolean positive) {
            super(owner);
            this.positive = positive;
        }

        @Override
        public void onClick(ConnectServerDialog owner, View v) {
            if (positive) {
                // see SearchFragment::OnDialogClickListener::onDialogClick(tag,which)
                owner.performDialogClick(Dialog.BUTTON_POSITIVE);
            }

            owner.dismiss();
        }
    }
}
