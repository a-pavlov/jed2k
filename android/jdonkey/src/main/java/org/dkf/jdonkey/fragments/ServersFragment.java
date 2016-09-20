package org.dkf.jdonkey.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.dkf.jdonkey.Engine;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.adapters.menu.ServerConnectAction;
import org.dkf.jdonkey.adapters.menu.ServerDisconnectAction;
import org.dkf.jdonkey.adapters.menu.ServerRemoveAction;
import org.dkf.jdonkey.core.ConfigurationManager;
import org.dkf.jdonkey.core.Constants;
import org.dkf.jdonkey.util.ServerUtils;
import org.dkf.jdonkey.util.UIUtils;
import org.dkf.jdonkey.views.*;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.android.AlertListener;
import org.dkf.jed2k.protocol.server.ServerMet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ap197_000 on 07.09.2016.
 */
public class ServersFragment extends AbstractFragment implements MainFragment, AlertListener {
    private final Logger log = LoggerFactory.getLogger(ServersFragment.class);
    private ListView list;
    private ServersAdapter adapter;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private RichNotification serviceStopped;

    public ServersFragment() {
        super(R.layout.fragment_servers);
        prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (key.equals(Constants.PREF_KEY_SERVERS_LIST)) {
                    setupAdapter();
                    invalidateServersState();
                }
            }
        };

        ConfigurationManager.instance().registerOnPreferenceChange(prefListener);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupAdapter();
        setRetainInstance(true);
    }

    private void setupAdapter() {
        if (adapter == null) {
            adapter = new ServersAdapter(getActivity());
        }

        ServerMet sm = new ServerMet();
        ConfigurationManager.instance().getSerializable(Constants.PREF_KEY_SERVERS_LIST, sm);
        adapter.clear();
        adapter.addServers(sm.getServers());
        list.setAdapter(adapter);
    }

    @Override
    protected void initComponents(final View rootView) {
        list = (ListView)findView(rootView, R.id.servers_list);
        list.setVisibility(View.VISIBLE);
        serviceStopped = (RichNotification)findView(rootView, R.id.fragment_servers_service_stopped_notification);
    }

    @Override
    public void onResume() {
        super.onResume();
        Engine.instance().setListener(this);
        invalidateServersState();
        warnServiceStopped(getView());
    }

    @Override
    public void onPause() {
        super.onPause();
        Engine.instance().removeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Engine.instance().removeListener(this);
    }

    private void warnServiceStopped(View v) {
        if (Engine.instance().isStopped()) {
            log.info("service is stopped");
            serviceStopped.setVisibility(View.VISIBLE);
        } else {
            log.info("service available");
            serviceStopped.setVisibility(View.GONE);
        }
    }

    private void invalidateServersState() {
        final String connectedServerId = Engine.instance().getCurrentServerId();
        boolean needRefresh = adapter.process(new ServerEntryProcessor() {
            @Override
            public boolean process(final ServerEntry e) {
                if (e.getIdentifier().compareTo(connectedServerId) == 0) {
                    e.connStatus = ServerEntry.ConnectionStatus.CONNECTED;
                } else {
                    e.connStatus = ServerEntry.ConnectionStatus.DISCONNECTED;
                }

                return true;
            }
        });

        if (needRefresh) adapter.notifyDataSetChanged();
    }

    private void handleServerIdChanged(final String id, int userId) {
        ServerEntry se = adapter.getItem(id);
        if (se != null) {
            se.userId = userId;
            se.connStatus = ServerEntry.ConnectionStatus.CONNECTED;
            adapter.notifyDataSetChanged();
        }
    }

    private void handleServerMessage(final String id, final String msg) {
        log.info("server {} message {}", id, msg);
        if (ConfigurationManager.instance().getBoolean(Constants.PREF_KEY_SHOW_SERVER_MSG)) {
            UIUtils.showShortMessage(getActivity(), msg);
        }
    }

    private void handleServerConnectionAlert(final String id) {
        ServerEntry se = adapter.getItem(id);
        if (se != null) {
            se.connStatus = ServerEntry.ConnectionStatus.CONNECTING;
            adapter.notifyDataSetChanged();
        }
    }

    private void handleServerConnectionClosed(final String id) {
        ServerEntry se = adapter.getItem(id);
        if (se != null) {
            se.userId = 0;
            se.filesCount = 0;
            se.usersCount = 0;
            se.connStatus = ServerEntry.ConnectionStatus.DISCONNECTED;
            adapter.notifyDataSetChanged();
        }
    }

    private void handleServerStatus(final String id, int uc, int fc) {
        ServerEntry se = adapter.getItem(id);
        if (se != null) {
            se.usersCount = uc;
            se.filesCount = fc;
            adapter.notifyDataSetChanged();
        }
    }

    private void listenAlert() { }

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
    public void onServerConnectionAlert(final ServerConnectionAlert alert) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleServerConnectionAlert(alert.identifier);
            }
        });
    }

    @Override
    public void onServerMessage(final ServerMessageAlert alert) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleServerMessage(alert.identifier, alert.msg);
            }
        });
    }

    @Override
    public void onServerStatus(final ServerStatusAlert alert) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleServerStatus(alert.identifier, alert.usersCount, alert.filesCount);
            }
        });
    }

    @Override
    public void onServerIdAlert(final ServerIdAlert alert) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleServerIdChanged(alert.identifier, alert.userId);
            }
        });
    }

    @Override
    public void onServerConnectionClosed(final ServerConectionClosed alert) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                handleServerConnectionClosed(alert.identifier);
            }
        });
    }


    @Override
    public View getHeader(Activity activity) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        @SuppressLint("InflateParams") TextView header = (TextView) inflater.inflate(R.layout.view_main_fragment_simple_header, null);
        header.setText(R.string.servers);
        header.setOnClickListener(new View.OnClickListener() {
            private int clickCount = 0;
            @Override
            public void onClick(View v) {
                clickCount++;
                log.info("header.onClick() - clickCount => " + clickCount);
                if (clickCount % 5 == 0) {
                    //Offers.showInterstitial(getActivity(), false, false);
                }
            }
        });
        return header;
    }

    @Override
    public void onShow() {
        warnServiceStopped(getView());
    }

    private static final class ServerEntry {
        public enum ConnectionStatus {
            CONNECTED,
            CONNECTING,
            DISCONNECTED;
        }

        public ServerEntry(final ServerMet.ServerMetEntry entry) {
            this.rand = 0;
            this.name = entry.getName();
            this.description = entry.getDescription();
            this.ip = entry.getHost();
            this.port = entry.getPort();
        }

        public ServerEntry(final int rand, final String name, final String description, final String ip, final int port) {
            this.rand = rand;
            this.name = name;
            this.description = description;
            this.ip = ip;
            this.port = port;
        }

        public final int rand;
        public final String name;
        public final String description;
        public final String ip;
        public final int port;
        public int usersCount = 0;
        public int filesCount = 0;
        public int userId = 0;
        public ConnectionStatus connStatus = ConnectionStatus.DISCONNECTED;


        // must be compatible with global server identifier notation!
        public final String getIdentifier() {
            return ServerUtils.getIdentifier(name, ip, port);
        }

        @Override
        public String toString() {
            return "ID {" + getIdentifier() + "} " + ip + ":" + port;
        }
    }

    private interface ServerEntryProcessor {
        boolean process(final ServerEntry e);
    }

    private final class ServersAdapter extends AbstractListAdapter<ServerEntry> {

        public ServersAdapter(Context context) {
            super(context, R.layout.view_servers_list_item);
        }

        @Override
        protected void populateView(View view, ServerEntry item) {
            ImageView icon = findView(view, R.id.view_preference_servers_list_item_icon);
            TextView label = findView(view, R.id.view_preference_servers_list_item_label);
            TextView description = findView(view, R.id.view_preference_servers_list_item_description);
            TextView details = findView(view, R.id.view_server_details);
            TextView userId = findView(view, R.id.view_server_user_id);
            icon.setImageResource(R.drawable.server/* : R.drawable.sd_card_notification_dark_bg*/);
            label.setText(item.name + " [" + item.ip + ":" + Integer.toString(item.port) + "]");
            description.setText(item.description);

            switch(item.connStatus) {
                case CONNECTED:
                    icon.clearAnimation();
                    icon.setAlpha(1.0f);
                    break;
                case DISCONNECTED:
                    icon.clearAnimation();
                    icon.setAlpha(0.4f);
                    break;
                case CONNECTING:
                    AlphaAnimation animation1 = new AlphaAnimation(0.5f, 1.0f);
                    animation1.setDuration(300);
                    animation1.setStartOffset(100);
                    animation1.setFillAfter(true);
                    animation1.setRepeatCount(Animation.INFINITE);
                    animation1.setRepeatMode(Animation.REVERSE);
                    icon.startAnimation(animation1);
                    break;
            }

            if (item.filesCount != 0 && item.usersCount != 0) {
                details.setText(String.format("%s: %d %s: %d",
                        getString(R.string.users_count),
                        item.usersCount,
                        getString(R.string.files_count),
                        item.filesCount));
            }
            else {
                details.setText(getString(R.string.NA));
            }

            if (item.userId != 0) {
                userId.setText(String.format("%s: %s",
                        getString(R.string.user_id),
                        Utils.isLowId(item.userId)?"LowID":"HiID"));
            }
        }

        public void addServers(final Collection<ServerMet.ServerMetEntry> servers) {
            for(final ServerMet.ServerMetEntry e: servers) {
                list.add(new ServerEntry(e));
            }

            for(final ServerMet.ServerMetEntry e: servers) {
                visualList.add(new ServerEntry(e));
            }
        }

        final ServerEntry getItem(final String id) {
            for(int i = 0; i < getCount(); ++i) {
                ServerEntry sr = getItem(i);
                if (sr.getIdentifier().compareTo(id) == 0) return sr;
            }

            return null;
        }

        public final boolean process(final ServerEntryProcessor p) {
            boolean affected = false;
            for(int i = 0; i < getCount(); ++i) {
                ServerEntry sr = getItem(i);
                if (p.process(getItem(i))) affected = true;
            }

            return affected;
        }

        @Override
        protected MenuAdapter getMenuAdapter(View view) {
            List<MenuAction> items = new ArrayList<>();
            if (Engine.instance().isStarted()) {
                ServerEntry entry = (ServerEntry) view.getTag();
                items.add(new ServerRemoveAction(view.getContext(), entry.ip, entry.getIdentifier()));
                if (entry.connStatus == ServerEntry.ConnectionStatus.DISCONNECTED) {
                    items.add(new ServerConnectAction(view.getContext(), entry.ip, entry.port, entry.getIdentifier()));
                } else {
                    items.add(new ServerDisconnectAction(view.getContext(), entry.ip, entry.port, entry.getIdentifier()));
                }
            }

            if (items.isEmpty()) return null;
            return new MenuAdapter(view.getContext(), R.string.sever_menu_title, items);
        }
    }
}
