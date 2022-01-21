package org.dkf.jmule.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.alert.*;
import org.dkf.jmule.AlertListener;
import org.dkf.jmule.ConfigurationManager;
import org.dkf.jmule.Constants;
import org.dkf.jed2k.protocol.server.ServerMet;
import org.dkf.jmule.Engine;
import org.dkf.jmule.R;
import org.dkf.jmule.adapters.menu.ServerConnectAction;
import org.dkf.jmule.adapters.menu.ServerDisconnectAction;
import org.dkf.jmule.adapters.menu.ServerRemoveAction;
import org.dkf.jmule.util.ServerUtils;
import org.dkf.jmule.util.UIUtils;
import org.dkf.jmule.views.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by ap197_000 on 07.09.2016.
 */
public class ServersFragment extends AbstractFragment implements MainFragment, AlertListener, View.OnClickListener {
    private final Logger log = LoggerFactory.getLogger(ServersFragment.class);
    private ListView list;
    private ServerAddView serverAddView;
    private ServersAdapter adapter;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    private RichNotification serviceStopped;
    ButtonServersParametersListener buttonServersParametersListener;

    public ServersFragment() {
        super(R.layout.fragment_servers);
        buttonServersParametersListener = new ButtonServersParametersListener(this);
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

    public void setupAdapter() {
        if (adapter == null) {
            adapter = new ServersAdapter(getActivity());
        }

        ServerMet sm = new ServerMet();
        ConfigurationManager.instance().getSerializable(Constants.PREF_KEY_SERVERS_LIST, sm);
        adapter.addServers(sm.getServers());
        if (list.getAdapter() == null) list.setAdapter(adapter);
    }

    @Override
    protected void initComponents(final View rootView) {
        list = (ListView)findView(rootView, R.id.servers_list);
        list.setVisibility(View.VISIBLE);
        serverAddView = (ServerAddView)findView(rootView, R.id.fragment_servers_add_server);
        serverAddView.setVisibility(View.GONE);
        serverAddView.setOnClickListener(this);
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
    public void onTransferAdded(TransferAddedAlert alert) {

    }

    @Override
    public void onTransferRemoved(TransferRemovedAlert alert) {

    }

    @Override
    public void onTransferPaused(TransferPausedAlert alert) {
        // do nothing not interested in transfer's state
    }

    @Override
    public void onTransferResumed(TransferResumedAlert alert) {
        // do nothing not interested in transfer's state
    }

    @Override
    public void onTransferIOError(TransferDiskIOErrorAlert alert) {
        // do nothing not interested in transfer's state
    }

    @Override
    public void onPortMapAlert(PortMapAlert alert) {

    }

    @Override
    public View getHeader(Activity activity) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        View header = inflater.inflate(R.layout.view_servers_header, null);
        TextView text = (TextView) header.findViewById(R.id.view_servers_header_text_title);
        text.setText(R.string.servers);
        ImageButton buttonMenu = (ImageButton) header.findViewById(R.id.view_servers_header_more_parameters);
        buttonMenu.setOnClickListener(buttonServersParametersListener);
        return header;
    }

    private static final class ButtonServersParametersListener extends ClickAdapter<ServersFragment> {

        ButtonServersParametersListener(ServersFragment f) {
            super(f);
        }

        @Override
        public void onClick(ServersFragment f, View v) {
            f.toggleServerAddView();
        }
    }

    private void toggleServerAddView() {
        if (serverAddView.getVisibility() == View.GONE) {
            serverAddView.setVisibility(View.VISIBLE);
        } else {
            serverAddView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onShow() {
        warnServiceStopped(getView());
    }

    @Override
    public void onClick(View v) {
        log.info("server added setup adapter");
        toggleServerAddView();
        setupAdapter();
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

        @Override
        public boolean equals(Object o) {
            return (o != null
                    && o instanceof ServerEntry
                    && ((ServerEntry)o).getIdentifier().compareTo(getIdentifier()) == 0);
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
        protected void populateView(View view, final ServerEntry item) {
            ImageView icon = findView(view, R.id.view_preference_servers_list_item_icon);
            TextView label = findView(view, R.id.view_preference_servers_list_item_label);
            TextView description = findView(view, R.id.view_preference_servers_list_item_description);
            TextView details = findView(view, R.id.view_server_details);
            TextView userId = findView(view, R.id.view_server_user_id);
            label.setText(item.name + " [" + item.ip + ":" + Integer.toString(item.port) + "]");
            description.setText(item.description);
            icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (Engine.instance().isStarted()) {
                        if (item.connStatus == ServerEntry.ConnectionStatus.DISCONNECTED) {
                            Engine.instance().connectTo(item.getIdentifier(), item.ip, item.port);
                        } else {
                            Engine.instance().disconnectFrom();
                        }
                    }
                }
            });

            switch(item.connStatus) {
                case CONNECTED:
                    icon.setImageResource(R.drawable.ic_flash_on_black_24dp);
                    icon.clearAnimation();
                    icon.setAlpha(1.0f);
                    break;
                case DISCONNECTED:
                    icon.setImageResource(R.drawable.ic_flash_off_black_24dp);
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
            ServerEntry se = getActiveItem();
            clear();

            for(final ServerMet.ServerMetEntry e: servers) {
                ServerEntry newSE = new ServerEntry(e);
                if (newSE.equals(se)) {
                    newSE = se;
                }

                list.add(newSE);
            }

            for(final ServerMet.ServerMetEntry e: servers) {
                ServerEntry newSE = new ServerEntry(e);
                if (newSE.equals(se)) {
                    newSE = se;
                }

                visualList.add(newSE);
            }
        }

        final ServerEntry getItem(final String id) {
            for(int i = 0; i < getCount(); ++i) {
                ServerEntry sr = getItem(i);
                if (sr.getIdentifier().compareTo(id) == 0) return sr;
            }

            return null;
        }

        final ServerEntry getActiveItem() {
            for(int i = 0; i < getCount(); ++i) {
                ServerEntry se = getItem(i);
                if (se.connStatus != ServerEntry.ConnectionStatus.DISCONNECTED) return se;
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
