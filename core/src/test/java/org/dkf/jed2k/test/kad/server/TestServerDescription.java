package org.dkf.jed2k.test.kad.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.dkf.jed2k.kad.server.StorageDescription;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by apavlov on 13.03.17.
 */
public class TestServerDescription {
    @Test
    public void testGithubDescriptionDownloader() throws URISyntaxException, IOException {
        byte[] data = IOUtils.toByteArray(new URI("https://raw.githubusercontent.com/a-pavlov/jed2k/kadstore/core/daemon/storage_description_test.json"));
        Gson gson = new GsonBuilder().create();
        String s = new String(data, StandardCharsets.UTF_8);
        StorageDescription sd = gson.fromJson(s, StorageDescription.class);
        assertEquals("192.168.0.45", sd.getIp());
        assertEquals(3, sd.getPorts().size());
        assertEquals(10000, sd.getPorts().get(0).intValue());
        assertEquals(20000, sd.getPorts().get(1).intValue());
        assertEquals(30000, sd.getPorts().get(2).intValue());
        assertTrue(sd.getDescription() == null);
        InetSocketAddress address = new InetSocketAddress(sd.getIp(), sd.getPorts().get(0));
    }
}
