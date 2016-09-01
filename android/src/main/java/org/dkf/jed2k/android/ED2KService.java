package org.dkf.jed2k.android;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Binder;
import android.os.IBinder;
import org.dkf.jed2k.Session;
import org.dkf.jed2k.alert.Alert;
import org.dkf.jed2k.alert.SearchResultAlert;
import org.dkf.jed2k.alert.ServerMessageAlert;
import org.dkf.jed2k.alert.ServerStatusAlert;
import org.dkf.jed2k.protocol.server.search.SearchResult;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ED2KService extends Service {

    private Binder binder;
    private Session session;
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public ED2KService() {
        binder = new ED2KServiceBinder();
        // create session here
        // start alerts loop
    }

    public class ED2KServiceBinder extends Binder {
        public ED2KService getService() {
            return ED2KService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        scheduledExecutorService.shutdown();
    }

    private void alertsLoop() {
        assert(session != null);
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Alert a = session.popAlert();
                while(a != null) {
                    if (a instanceof SearchResultAlert) {
                        SearchResult sr = ((SearchResultAlert)a).results;
                    }
                    else if (a instanceof ServerMessageAlert) {
                        //System.out.println("Server message: " + ((ServerMessageAlert)a).msg);
                    }
                    else if (a instanceof ServerStatusAlert) {
                        ServerStatusAlert ssa = (ServerStatusAlert)a;
                        //System.out.println("Files count = " + ssa.filesCount + " users count = " + ssa.usersCount);
                    }
                    else {
                        //System.out.println("Unknown alert received: " + a.toString());
                    }

                    a = session.popAlert();
                }
            }
        },  100, 500, TimeUnit.MILLISECONDS);
    }
}
