package org.dkf.jdonkey.views.preference;

import android.content.Context;
import android.os.Bundle;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.views.AbstractAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by inkpot on 05.09.2016.
 */
public class ServersPreference extends DialogPreference {
    private final Logger log = LoggerFactory.getLogger(ServersPreference.class);

    public ServersPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.dialog_preference_servers);
    }

    public ServersPreference(Context context) {
        this(context, null);
    }

    @Override
    public void showDialog(Bundle state) {
        super.showDialog(state);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        ListView list = (ListView) view.findViewById(R.id.preference_servers_list);

        list.setAdapter(new ServersAdapter(getContext()));
        list.setOnItemClickListener(new AbstractAdapter.OnItemClickAdapter<ServerEntry>() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, AbstractAdapter<ServerEntry> adapter, int position, long id) {
                log.info("selected server {}", adapter.getItem(position).description);
            }
        });
    }


    private static final class ServerEntry {
        public ServerEntry(final String description, final String ip, final String port) {
            this.description = description;
            this.ip = ip;
            this.port = port;
        }

        public final String description;
        public final String ip;
        public final String port;
    }


    private final class ServersAdapter extends AbstractAdapter<ServerEntry> {

        public ServersAdapter(Context context) {
            super(context, R.layout.view_preference_servers_list_item);

            addItems(context);
        }

        @Override
        protected void setupView(View view, ViewGroup parent, ServerEntry item) {
            ImageView icon = findView(view, R.id.view_preference_servers_list_item_icon);
            TextView label = findView(view, R.id.view_preference_servers_list_item_label);
            TextView description = findView(view, R.id.view_preference_servers_list_item_description);
            RadioButton radio = findView(view, R.id.view_preference_servers_list_item_radio);

            icon.setImageResource(R.drawable.internal_memory_notification_dark_bg/* : R.drawable.sd_card_notification_dark_bg*/);
            label.setText(item.description);
            description.setText(item.ip + "/" + item.port);
            radio.setChecked(false);
        }

        private void addItems(Context context) {
            for (int i = 0; i < 10; ++i) {
                add(new ServerEntry("description " + i, "host", "port"));
            }
        }
    }
}
