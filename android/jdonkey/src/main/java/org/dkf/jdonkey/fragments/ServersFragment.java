package org.dkf.jdonkey.fragments;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.dkf.jdonkey.Engine;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.views.AbstractAdapter;
import org.dkf.jdonkey.views.AbstractFragment;
import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.android.AlertListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ap197_000 on 07.09.2016.
 */
public class ServersFragment extends AbstractFragment implements AlertListener {
    private final Logger log = LoggerFactory.getLogger(ServersFragment.class);
    private ListView list;

    public ServersFragment() {
        super(R.layout.fragment_servers);
        //Engine.instance().setListener(this);
    }

    @Override
    protected void initComponents(final View rootView) {
        list = (ListView)findView(rootView, R.id.servers_list);
        list.setAdapter(new ServersAdapter(getActivity()));
        list.setOnItemClickListener(new AbstractAdapter.OnItemClickAdapter<ServerEntry>() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, AbstractAdapter<ServerEntry> adapter, int position, long id) {
                log.info("selected server {}", adapter.getItem(position).description);
                ServerEntry se = adapter.getItem(position);
                if (!Engine.instance().connectTo(se.id, se.ip, se.port)) {
                    log.error("Unable to start connection to ", se.ip);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        log.info("register servers listener");
        Engine.instance().setListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        log.info("remove servers listener");
        Engine.instance().removeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log.info("remove servers fragment listener");
        Engine.instance().removeListener(this);
    }

    private void setupItem(final String id, final String msg) {
        log.info("server {} message {}", id, msg);
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }

    private void serverConnClosed(final String str) {
        log.info("server conn closed {}", str);
        Toast.makeText(getActivity(), "conn closed " + str, Toast.LENGTH_LONG).show();
    }

    private void listenAlert() {
        log.info("listen received");
        Toast.makeText(getActivity(), "listen", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onListen(ListenAlert alert) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listenAlert();
            }
        });
    }

    @Override
    public void onSearchResult(SearchResultAlert alert) {

    }

    @Override
    public void onServerMessage(final ServerMessageAlert alert) {
        log.info("server conn closed {}", alert.msg);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setupItem(alert.identifier, alert.msg);
            }
        });
    }

    @Override
    public void onServerStatus(ServerStatusAlert alert) {

    }

    @Override
    public void onServerConnectionClosed(final ServerConectionClosed alert) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                serverConnClosed(alert.code.getDescription());
            }
        });
    }

    private static final class ServerEntry {
        public ServerEntry(final String id, final String description, final String ip, final int port) {
            this.id = id;
            this.description = description;
            this.ip = ip;
            this.port = port;
        }

        public final String id;
        public final String description;
        public final String ip;
        public final int port;

        @Override
        public String toString() {
            return "ID {" + id + "} " + ip + ":" + port;
        }
    }
/*
    private class ServersAdapter2 extends AbstractListAdapter<ServerEntry> {

        public ServersAdapter2(Context context) {
            super(context, R.layout.view_servers_list_item);
            addItems(context);
            addItem(new ServerEntry("is", "some description", "emule.is74.ru", (short)4661));
        }

        @Override
        protected void populateView(View view, ServerEntry data) {
            ImageView icon = findView(view, R.id.view_preference_servers_list_item_icon);
            TextView label = findView(view, R.id.view_preference_servers_list_item_label);
            TextView description = findView(view, R.id.view_preference_servers_list_item_description);
            icon.setImageResource(R.drawable.internal_memory_notification_dark_bg);
            label.setText(data.description);
            description.setText(data.ip + data.port);
        }

        private void addItems(Context context) {
            for (int i = 0; i < 10; ++i) {
                log.info("add server {}", i);
                addItem(new ServerEntry("id" + Integer.toString(i), "description " + i, "host", 5667), true);
            }
        }
    }
*/
    private final class ServersAdapter extends AbstractAdapter<ServerEntry> {

        public ServersAdapter(Context context) {
            super(context, R.layout.view_servers_list_item);
            addItems(context);
        }

        @Override
        protected void setupView(View view, ViewGroup parent, ServerEntry item) {
            ImageView icon = findView(view, R.id.view_preference_servers_list_item_icon);
            TextView label = findView(view, R.id.view_preference_servers_list_item_label);
            TextView description = findView(view, R.id.view_preference_servers_list_item_description);
            icon.setImageResource(R.drawable.internal_memory_notification_dark_bg/* : R.drawable.sd_card_notification_dark_bg*/);
            label.setText(item.description);
            description.setText(item.ip + "/" + item.port);
        }

        private void addItems(Context context) {
            for (int i = 0; i < 10; ++i) {
                log.info("add server {}", i);
                add(new ServerEntry("id" + Integer.toString(i), "description " + i, "host", 56678));
            }

            add(new ServerEntry("is", "IS Emule", "emule.is74.ru", 4661));
        }
    }
}
