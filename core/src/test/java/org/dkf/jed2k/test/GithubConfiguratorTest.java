package org.dkf.jed2k.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.io.IOUtils;
import org.dkf.jed2k.GithubConfigurator;
import org.dkf.jed2k.exception.JED2KException;
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
    Gson gson = new GsonBuilder().create();

    @Test
    public void testGithubDescriptionDownloader() throws URISyntaxException, IOException, JED2KException {
        byte[] data = IOUtils.toByteArray(new URI("https://raw.githubusercontent.com/a-pavlov/jed2k/config/config.json"));
        String s = new String(data);
        GithubConfigurator gc = gson.fromJson(s, GithubConfigurator.class);
        gc.validate();
        assertTrue(gc.getKadStorageDescription() != null);
        assertEquals("192.168.0.45", gc.getKadStorageDescription().getIp());
        assertEquals(3, gc.getKadStorageDescription().getPorts().size());
        assertEquals(10000, gc.getKadStorageDescription().getPorts().get(0).intValue());
        assertEquals(20000, gc.getKadStorageDescription().getPorts().get(1).intValue());
        assertEquals(30000, gc.getKadStorageDescription().getPorts().get(2).intValue());
        assertTrue(gc.getKadStorageDescription().getDescription() == null);
        InetSocketAddress address = new InetSocketAddress(gc.getKadStorageDescription().getIp(), gc.getKadStorageDescription().getPorts().get(0));
    }

    @Test(expected = JED2KException.class)
    public void testIncorrectConfigIp() throws JED2KException {
        GithubConfigurator gc = gson.fromJson("{\"kadStorageDescription\": {\n" +
                "    \"ports\": [\n" +
                "      20000,\n" +
                "      30000\n" +
                "    ]\n" +
                "  } }", GithubConfigurator.class);
        gc.validate();
    }

    @Test(expected = JED2KException.class)
    public void testIncorrectConfigPorts() throws JED2KException {
        GithubConfigurator gc = gson.fromJson("{\n" +
                "  \"kadStorageDescription\": {\n" +
                "    \"ip\": \"192.168.0.45\"\n" +
                "  }\n" +
                "}\n", GithubConfigurator.class);
        gc.validate();
    }

    @Test(expected = JED2KException.class)
    public void testIncorrectConfigPortsEmpty() throws JED2KException {
        GithubConfigurator gc = gson.fromJson("{\n" +
                "  \"kadStorageDescription\": {\n" +
                "    \"ip\": \"192.168.0.45\",\n" +
                "    \"ports\": []" +
                "  }\n" +
                "}\n", GithubConfigurator.class);
        gc.validate();
    }

    @Test
    public void testNoKadStorageConfig() throws JED2KException {
        GithubConfigurator gc = gson.fromJson("{}", GithubConfigurator.class);
        gc.validate();
    }

    @Test(expected = JsonSyntaxException.class)
    public void testIncorrectJson() {
        GithubConfigurator gc = gson.fromJson("{", GithubConfigurator.class);
    }
}
