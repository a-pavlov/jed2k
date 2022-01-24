package org.dkf.jed2k;


import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ServiceTestRule;

import org.dkf.jed2k.alert.ListenAlert;
import org.dkf.jed2k.alert.PortMapAlert;
import org.dkf.jed2k.alert.SearchResultAlert;
import org.dkf.jed2k.alert.ServerConectionClosed;
import org.dkf.jed2k.alert.ServerConnectionAlert;
import org.dkf.jed2k.alert.ServerIdAlert;
import org.dkf.jed2k.alert.ServerMessageAlert;
import org.dkf.jed2k.alert.ServerStatusAlert;
import org.dkf.jed2k.alert.TransferAddedAlert;
import org.dkf.jed2k.alert.TransferDiskIOErrorAlert;
import org.dkf.jed2k.alert.TransferPausedAlert;
import org.dkf.jed2k.alert.TransferRemovedAlert;
import org.dkf.jed2k.alert.TransferResumedAlert;
import org.dkf.jed2k.protocol.Hash;
import org.dkf.jmule.AlertListener;
import org.dkf.jmule.ED2KService;
import org.dkf.jmule.Engine;
import org.dkf.jmule.ResumeDataDbHelper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;

import java.io.File;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ap197_000 on 02.09.2016.
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ED2KServiceTest {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(ED2KServiceTest.class);

    private final AtomicBoolean listenAlertReceived = new AtomicBoolean(false);

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    private static final int MAX_ITERATION = 20;
    ED2KService service;

    @Before
    public void setUp() throws  TimeoutException {
        IBinder binder;
        int it = 0;

        while((binder = mServiceRule.bindService(
                new Intent(getApplicationContext(),
                        ED2KService.class))) == null && it < MAX_ITERATION){
            it++;
        }

        assertTrue(binder != null);

        service = ((ED2KService.ED2KServiceBinder) binder).getService();
        Engine.create(getApplicationContext());
    }

    @Test
    public void testService() throws TimeoutException, InterruptedException {
        assertTrue(service != null);
        Thread.sleep(1000);
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
    }

    @Test
    public void testDbHelper() throws Exception {
        final Hash TERMINAL   = Hash.fromString("31D6CFE0D16AE931B73C59D7E0C089C0");
        final Hash LIBED2K    = Hash.fromString("31D6CFE0D14CE931B73C59D7E0C04BC0");
        final Hash EMULE      = Hash.fromString("31D6CFE0D10EE931B73C59D7E0C06FC0");
        final Hash INVALID    = new Hash();

        ResumeDataDbHelper dbHelper = service.initializeDatabase();
        assertNotNull(dbHelper);
        // cleanup db
        dbHelper.getWritableDatabase().delete(ResumeDataDbHelper.TABLE_NAME, null, null);
        int count = 0;
        try (ResumeDataDbHelper.ATPIterator itrAtp = dbHelper.iterator()) {
            while (itrAtp.hasNext()) {
                AddTransferParams atp = itrAtp.next();
                ++count;
            }
        }

        assertEquals(0, count);
        // initial resume data created
        AddTransferParams atp_1 = new AddTransferParams(EMULE, Time.currentTimeMillis(), 100500L, new File("atp_file_1_filename"), false);
        AddTransferParams atp_2 = new AddTransferParams(LIBED2K, Time.currentTimeMillis(), 10050007L, new File("atp_file_2_filename"), true);
        dbHelper.saveResumeData(atp_1);
        dbHelper.saveResumeData(atp_2);

        count = 0;
        try (ResumeDataDbHelper.ATPIterator itrAtp = dbHelper.iterator()) {
            while (itrAtp.hasNext()) {
                AddTransferParams atp = itrAtp.next();
                ++count;
            }
        }

        assertEquals(2, count);

        // update resume data
        AddTransferParams atp_3 = new AddTransferParams(EMULE, Time.currentTimeMillis(), 200500L, new File("atp_file_1_filename_xxxx"), false);
        AddTransferParams atp_4 = new AddTransferParams(LIBED2K, Time.currentTimeMillis(), 20050007L, new File("atp_file_2_filename_YYYYY"), true);
        AddTransferParams atp_5 = new AddTransferParams(TERMINAL, Time.currentTimeMillis(), 1005007L, new File("atp_file_3_filename"), true);

        // update resume data
        dbHelper.saveResumeData(atp_3);
        dbHelper.saveResumeData(atp_4);
        dbHelper.saveResumeData(atp_5);

        count = 0;
        try (ResumeDataDbHelper.ATPIterator itrAtp = dbHelper.iterator()) {
            while (itrAtp.hasNext()) {
                AddTransferParams atp = itrAtp.next();
                ++count;
            }
        }

        assertEquals(3, count);
    }

    /*
    @Test
    public void testAsync() {
        async(this, ED2KServiceTest::testA);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {

        }
        log.info("async wait completed");
    }

    private void testA() {
        log.info("async test started");
        final Hash TERMINAL   = Hash.fromString("31D6CFE0D16AE931B73C59D7E0C089C0");
        final Hash LIBED2K    = Hash.fromString("31D6CFE0D14CE931B73C59D7E0C04BC0");
        final Hash EMULE      = Hash.fromString("31D6CFE0D10EE931B73C59D7E0C06FC0");
        final Hash INVALID    = new Hash();

        ResumeDataDbHelper dbHelper = service.initializeDatabase();
        try {
            AddTransferParams atp_1 = new AddTransferParams(EMULE, Time.currentTimeMillis(), 100500L, new File("atp_file_1_filename"), false);
            AddTransferParams atp_2 = new AddTransferParams(LIBED2K, Time.currentTimeMillis(), 10050007L, new File("atp_file_2_filename"), true);
            dbHelper.testData();
            dbHelper.saveResumeData(atp_1);
            dbHelper.saveResumeData(atp_1);
        } catch (Throwable e) {
            log.info("async error {}", e.getMessage());
        } finally {
            log.info("async finished");
        }

    }
    */
}
