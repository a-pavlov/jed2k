package org.dkf.jed2k.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.IOUtils;
import org.dkf.jed2k.GithubConfigurator;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

/**
 * Created by apavlov on 17.03.17.
 */
public class GithubConfiguratorTest {
    @Test
    public void testGithubDescriptionDownloader() throws URISyntaxException, IOException {
        byte[] data = IOUtils.toByteArray(new URI("https://raw.githubusercontent.com/a-pavlov/jed2k/config/config.json"));
        Gson gson = new GsonBuilder().create();
        String s = new String(data, StandardCharsets.UTF_8);
        GithubConfigurator gc = gson.fromJson(s, GithubConfigurator.class);
        assertTrue(gc.getKadStorageDescription() != null);
        assertEquals("192.168.0.45", gc.getKadStorageDescription().getIp());
        assertEquals(3, gc.getKadStorageDescription().getPorts().size());
        assertEquals(10000, gc.getKadStorageDescription().getPorts().get(0).intValue());
        assertEquals(20000, gc.getKadStorageDescription().getPorts().get(1).intValue());
        assertEquals(30000, gc.getKadStorageDescription().getPorts().get(2).intValue());
        assertTrue(gc.getKadStorageDescription().getDescription() == null);
        InetSocketAddress address = new InetSocketAddress(gc.getKadStorageDescription().getIp(), gc.getKadStorageDescription().getPorts().get(0));
    }
}
