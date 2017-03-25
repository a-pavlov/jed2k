package org.dkf.jmule.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import org.dkf.jed2k.android.ConfigurationManager;
import org.dkf.jed2k.android.Constants;
import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.server.ServerMet;
import org.dkf.jmule.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ap197_000 on 12.10.2016.
 */
public class ServerAddView extends LinearLayout implements View.OnKeyListener {
    private final Logger log = LoggerFactory.getLogger(ServerAddView.class);
    private EditText viewName;
    private EditText viewHost;
    private EditText viewPort;
    private EditText viewDescr;
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
        viewDescr = (EditText)findViewById(R.id.view_server_add_descr);
        viewName.setOnKeyListener(this);
        viewHost.setOnKeyListener(this);
        viewPort.setOnKeyListener(this);
        viewDescr.setOnKeyListener(this);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        log.info("key {}", keyEvent);
        if (i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction() == KeyEvent.ACTION_UP) {
            log.info("enter");
            if (viewName.getText().length() > 0
                    && viewHost.getText().length() > 0
                    && viewPort.getText().length() > 0) {
                ServerMet sm = new ServerMet();
                ConfigurationManager.instance().getSerializable(Constants.PREF_KEY_SERVERS_LIST, sm);
                try {

                    sm.addServer(ServerMet.ServerMetEntry.create(viewHost.getText().toString()
                            , Integer.parseInt(viewPort.getText().toString())
                            , viewName.getText().toString()
                            , viewDescr.getText().toString()));
                    ConfigurationManager.instance().setSerializable(Constants.PREF_KEY_SERVERS_LIST, sm);
                    if (listener != null) listener.onClick(this);
                    InputMethodManager manager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (manager != null) {
                        manager.hideSoftInputFromWindow(getWindowToken(), 0);
                    }
                } catch (JED2KException e) {
                    e.printStackTrace();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }

            return true;
        }

        return false;
    }
}
