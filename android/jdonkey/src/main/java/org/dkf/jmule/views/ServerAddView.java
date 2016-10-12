package org.dkf.jmule.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.server.ServerMet;
import org.dkf.jmule.R;
import org.dkf.jmule.core.ConfigurationManager;
import org.dkf.jmule.core.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ap197_000 on 12.10.2016.
 */
public class ServerAddView extends LinearLayout implements View.OnClickListener {
    private final Logger log = LoggerFactory.getLogger(ServerAddView.class);
    private EditText viewName;
    private EditText viewHost;
    private EditText viewPort;
    private ImageButton btnAdd;
    private View.OnClickListener listener;

    public ServerAddView(Context context, AttributeSet set) {
        super(context, set);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        View.inflate(getContext(), R.layout.view_add_server, this);
        viewName = (EditText)findViewById(R.id.view_server_add_name);
        viewHost = (EditText)findViewById(R.id.view_server_add_host);
        viewPort = (EditText)findViewById(R.id.view_server_add_port);
        btnAdd = (ImageButton) findViewById(R.id.view_add_server_btn);
        btnAdd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        log.info("add server {} on {}:{}"
                , viewName.getText().toString()
                , viewHost.getText().toString()
                , viewPort.getText().toString());

        if (viewName.getText().length() > 0
                && viewHost.getText().length() > 0
                && viewPort.getText().length() > 0) {
            ServerMet sm = new ServerMet();
            ConfigurationManager.instance().getSerializable(Constants.PREF_KEY_SERVERS_LIST, sm);
            try {

                sm.addServer(ServerMet.ServerMetEntry.create(viewHost.getText().toString()
                        , Integer.parseInt(viewPort.getText().toString())
                        , viewName.getText().toString()
                        , ""));
                ConfigurationManager.instance().setSerializable(Constants.PREF_KEY_SERVERS_LIST, sm);
                if (listener != null) listener.onClick(this);
            } catch(JED2KException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }
}
