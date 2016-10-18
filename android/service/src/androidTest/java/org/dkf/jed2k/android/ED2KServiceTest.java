package org.dkf.jed2k.android;


import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;
import android.util.Log;
import org.dkf.jed2k.alert.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by ap197_000 on 02.09.2016.
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ED2KServiceTest {

    private final AtomicBoolean listenAlertReceived = new AtomicBoolean(false);

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    private static final int MAX_ITERATION = 20;
    ED2KService service;

    @Before
    public void setUp() throws  Exception {
        IBinder binder = null;
        int it = 0;

        while((binder = mServiceRule.bindService(
                new Intent(InstrumentationRegistry.getTargetContext(),
                        ED2KService.class))) == null && it < MAX_ITERATION){
            it++;
        }

        assertTrue(binder != null);

        service = ((ED2KService.ED2KServiceBinder) binder).getService();
    }

    @Test
    public void testService() throws TimeoutException, InterruptedException {
        assertTrue(service != null);
        Thread.sleep(4000);
        Log.v("testService", "finished");
    }

    @Test
    public void testListenAlert() throws TimeoutException, InterruptedException {
        assertTrue(service != null);
        service.addListener(new AlertListener() {
            @Override
            public void onListen(ListenAlert alert) {
                listenAlertReceived.set(true);
            }

            @Override
            public void onSearchResult(SearchResultAlert alert) {

            }

            @Override
            public void onServerConnectionAlert(ServerConnectionAlert alert) {

            }

            @Override
            public void onServerMessage(ServerMessageAlert alert) {

            }

            @Override
            public void onServerStatus(ServerStatusAlert alert) {

            }

            @Override
            public void onServerIdAlert(ServerIdAlert alert) {

            }

            @Override
            public void onServerConnectionClosed(ServerConectionClosed alert) {

            }

            @Override
            public void onTransferAdded(TransferAddedAlert alert) {

            }

            @Override
            public void onTransferRemoved(TransferRemovedAlert alert) {

            }

            @Override
            public void onTransferPaused(TransferPausedAlert alert) {

            }

            @Override
            public void onTransferResumed(TransferResumedAlert alert) {

            }

            @Override
            public void onTransferIOError(TransferDiskIOErrorAlert alert) {

            }

            @Override
            public void onPortMapAlert(PortMapAlert alert) {

            }
        });

        Thread.sleep(2000);
        assertTrue(listenAlertReceived.get());
    }
}
