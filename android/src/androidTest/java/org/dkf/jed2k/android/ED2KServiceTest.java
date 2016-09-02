package org.dkf.jed2k.android;


import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.util.Pair;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertThat;

import android.content.Intent;
import android.os.IBinder;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ServiceTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.MediumTest;

import java.util.concurrent.TimeoutException;

/**
 * Created by ap197_000 on 02.09.2016.
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ED2KServiceTest {

    @Rule
    public final ServiceTestRule mServiceRule = new ServiceTestRule();

    @Test
    public void testService() throws TimeoutException, InterruptedException {
        // Create the service Intent.
        Intent serviceIntent =
                new Intent(InstrumentationRegistry.getTargetContext(), ED2KService.class);

        IBinder binder = mServiceRule.bindService(serviceIntent);
        ED2KService service = ((ED2KService.ED2KServiceBinder)binder).getService();
        assertTrue(service != null);
        Thread.sleep(4000);
        Log.v("testService", "finished");
    }
}
