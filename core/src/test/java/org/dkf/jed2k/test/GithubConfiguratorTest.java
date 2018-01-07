package org.dkf.jed2k.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.security.ntlm.Server;
import org.apache.commons.io.IOUtils;
import org.dkf.jed2k.GithubConfigurator;
import org.dkf.jed2k.ServerValidator;
import org.dkf.jed2k.exception.JED2KException;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Created by apavlov on 17.03.17.
 */
public class GithubConfiguratorTest {
    Gson gson = new GsonBuilder().create();

    @Test
    public void testGithubDescriptionDownloader() throws URISyntaxException, IOException, JED2KException {
        byte[] data = IOUtils.toByteArray(new URI("https://raw.githubusercontent.com/a-pavlov/jed2k/kad/core/src/test/resources/config.json"));
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

    public void testUsualConfig() throws JED2KException {
        GithubConfigurator gc = gson.fromJson("{\n" +
                "  \"kadStorageDescription\": {\n" +
                "    \"ip\": \"192.168.0.45\",\n" +
                "    \"ports\": [20000]" +
                "  }\n" +
                "}\n", GithubConfigurator.class);
        gc.validate();
        assertTrue(gc.getKadStorageDescription() != null);
        assertEquals("192.168.0.45", gc.getKadStorageDescription().getIp());
        assertEquals(1, gc.getKadStorageDescription().getPorts().size());
        assertEquals(20000, gc.getKadStorageDescription().getPorts().get(0).intValue());
    }

    // warning - actually is not unit test
    @Test
    public void testGithubServersVerifierList() throws URISyntaxException, IOException, JED2KException {
        byte[] data = IOUtils.toByteArray(new URI("https://raw.githubusercontent.com/a-pavlov/jed2k/config/core/src/test/resources/servers.json"));
        String s = new String(data);
        List<ServerValidator.ServerEntry> svlist = gson.fromJson(s, ServerValidator.SERVERS_LIST_TYPE);
        assertFalse(svlist.isEmpty());
    }

    @Test
    public void testGithubServersVerifierParsing() throws URISyntaxException, IOException, JED2KException {
        List<ServerValidator.ServerEntry> svlist = gson.fromJson("[{ \t\t\"name\": \"some name\", \t\t\"host\": \"102.44.556.7\", \t\t\"port\": 3945, \t\t\"version\": \"0\", \t\t\"failures\": 0, \t\t\"lastVerified\": \"10-10-2012 10:00:03\", \t\t\"description\": \"some text\", \t\t\"filesCount\": 100, \t\t\"usersCount\": 200 \t} ]"
                , ServerValidator.SERVERS_LIST_TYPE);
        assertFalse(svlist.isEmpty());
        assertEquals("some name", svlist.get(0).name);
        assertEquals("102.44.556.7", svlist.get(0).host);
        assertEquals(3945, svlist.get(0).port.intValue());
        assertEquals(100, svlist.get(0).filesCount.intValue());
        assertEquals(200, svlist.get(0).usersCount.intValue());
    }
}
