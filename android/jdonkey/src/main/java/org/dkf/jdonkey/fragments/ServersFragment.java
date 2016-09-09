package org.dkf.jdonkey.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.*;
import org.dkf.jdonkey.Engine;
import org.dkf.jdonkey.R;
import org.dkf.jdonkey.core.ConfigurationManager;
import org.dkf.jdonkey.core.Constants;
import org.dkf.jdonkey.dialogs.ConnectServerDialog;
import org.dkf.jdonkey.dialogs.NewTransferDialog;
import org.dkf.jdonkey.views.AbstractAdapter;
import org.dkf.jdonkey.views.AbstractDialog;
import org.dkf.jdonkey.views.AbstractFragment;
import org.dkf.jed2k.Utils;
import org.dkf.jed2k.alert.*;
import org.dkf.jed2k.android.AlertListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ap197_000 on 07.09.2016.
 */
public class ServersFragment extends AbstractFragment implements MainFragment, AlertListener, AbstractDialog.OnDialogClickListener {
    private final Logger log = LoggerFactory.getLogger(ServersFragment.class);
    private ListView list;
    private ServersAdapter adapter;

    public ServersFragment() {
        super(R.layout.fragment_servers);
        //Engine.instance().setListener(this);
    }

    @Override
    protected void initComponents(final View rootView) {
        list = (ListView)findView(rootView, R.id.servers_list);

        adapter = new ServersAdapter(getActivity());
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AbstractAdapter.OnItemClickAdapter<ServerEntry>() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, AbstractAdapter<ServerEntry> adapter, int position, long id) {
                log.info("selected server {}", adapter.getItem(position).description);
                ServerEntry se = adapter.getItem(position);

                try {
                    ConnectServerDialog dlg = new ConnectServerDialog(se.getIdentifier());
                    dlg.initializeServerAttributes(se.getIdentifier(), se.ip, se.port);
                    dlg.show(getFragmentManager());
                } catch (IllegalStateException e) {
                    // android.app.FragmentManagerImpl.checkStateLoss:1323 -> java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
                    // just start the download then if the dialog crapped out.
                    onDialogClick(NewTransferDialog.TAG, Dialog.BUTTON_POSITIVE);
                }
            }

        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Engine.instance().setListener(this);
        invalidateServersState();
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

    private void invalidateServersState() {
        log.info("invalidate server parameters");
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
            Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
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
    public void onDialogClick(String tag, int which) {
        log.info("on dialog clicked {}", tag);
        if (which == Dialog.BUTTON_POSITIVE) {
            ServerEntry se = adapter.getItem(tag);
            if (se != null) {
                if (se.connStatus == ServerEntry.ConnectionStatus.DISCONNECTED) {
                    se.connStatus = ServerEntry.ConnectionStatus.CONNECTING;
                    adapter.notifyDataSetChanged();
                    Engine.instance().connectTo(se.getIdentifier(), se.ip, se.port);
                } else {
                    boolean needRefresh = ((ServersAdapter) adapter).process(new ServerEntryProcessor() {
                        @Override
                        public boolean process(final ServerEntry e) {
                            boolean res = (e.connStatus != ServerEntry.ConnectionStatus.DISCONNECTED);
                            e.connStatus = ServerEntry.ConnectionStatus.DISCONNECTED;
                            return res;
                        }
                    });

                    Engine.instance().disconnectFrom();
                    if (needRefresh) {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }


    @Override
    public View getHeader(Activity activity) {
        LayoutInflater inflater = LayoutInflater.from(activity);
        @SuppressLint("InflateParams") TextView header = (TextView) inflater.inflate(R.layout.view_main_fragment_simple_header, null);
        header.setText(R.string.search);
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

    }

    private static final class ServerEntry {
        public enum ConnectionStatus {
            CONNECTED,
            CONNECTING,
            DISCONNECTED;
        }

        public ServerEntry(final int rand, final String description, final String ip, final int port) {
            this.rand = rand;
            this.description = description;
            this.ip = ip;
            this.port = port;
        }

        public final int rand;
        public final String description;
        public final String ip;
        public final int port;
        public int usersCount = 0;
        public int filesCount = 0;
        public int userId = 0;
        public ConnectionStatus connStatus = ConnectionStatus.DISCONNECTED;


        public final String getIdentifier() {
            StringBuilder sb = new StringBuilder();
            sb.append(Integer.toString(rand)).append(ip).append(Integer.toString(port));
            return sb.toString();
        }

        @Override
        public String toString() {
            return "ID {" + getIdentifier() + "} " + ip + ":" + port;
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
    private interface ServerEntryProcessor {
        boolean process(final ServerEntry e);
    }

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
            TextView details = findView(view, R.id.view_server_details);
            TextView userId = findView(view, R.id.view_server_user_id);
            icon.setImageResource(R.drawable.internal_memory_notification_dark_bg/* : R.drawable.sd_card_notification_dark_bg*/);
            label.setText(item.description);
            description.setText(item.ip + "/" + Integer.toString(item.port));

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
                    log.info("connecting......");
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
                        Utils.isLowId(item.userId)?"Lo":"Hi"));
            }
        }

        private void addItems(Context context) {
            for (int i = 0; i < 3; ++i) {
                log.info("add server {}", i);
                add(new ServerEntry(i, "description " + i, "host", 56678));
            }

            add(new ServerEntry(100400, "Emule IS74 server", "emule.is74.ru", 4661));
            add(new ServerEntry(9955, "eMule security", "91.200.42.46", 1176));
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
    }
}
