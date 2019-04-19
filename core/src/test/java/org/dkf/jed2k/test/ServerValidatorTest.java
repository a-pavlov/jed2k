package org.dkf.jed2k.test;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.dkf.jed2k.ServerValidator;
import org.junit.Test;

import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.dkf.jed2k.ServerValidator.SERVERS_LIST_TYPE;
import static org.dkf.jed2k.ServerValidator.isValidEntry;
import static org.junit.Assert.assertFalse;

public class ServerValidatorTest {
    private static final Gson gson = new Gson();
    private ServerValidator.ServerEntry se = new ServerValidator.ServerEntry("server1", "1.2.3.4", 1234, 3433, "", 45, 55);
    private ServerValidator.ServerEntry se2 = new ServerValidator.ServerEntry("server1", "1.2.3.5", 1234, 3433, "", 4, 5);

    @Test
    public void testEqualsOperator() {
        JsonReader reader = new JsonReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("svlist1.json")));
        List<ServerValidator.ServerEntry> svlist1 = gson.fromJson(reader, SERVERS_LIST_TYPE);
        assertEquals(3, svlist1.size());
        for(int i = 1; i < svlist1.size(); ++i) {
            assertFalse(svlist1.get(i-1).equals(svlist1.get(i)));
        }

        assertEquals(se, svlist1.get(2));
        assertFalse(se.equals(se2));
    }

    @Test
    public void testServersListsMergingSrcEmpty() {
        JsonReader reader = new JsonReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("svlist1.json")));
        List<ServerValidator.ServerEntry> svlist1 = gson.fromJson(reader, SERVERS_LIST_TYPE);
        assertEquals(3, svlist1.size());
        ServerValidator.mergeServersLists(Collections.EMPTY_LIST, svlist1);
        assertTrue(svlist1.isEmpty());
    }

    @Test
    public void testServersListsMergingDstEmpty() {
        JsonReader reader = new JsonReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("svlist1.json")));
        List<ServerValidator.ServerEntry> svlist1 = gson.fromJson(reader, SERVERS_LIST_TYPE);
        assertEquals(3, svlist1.size());
        List<ServerValidator.ServerEntry> dst = new LinkedList<>();
        ServerValidator.mergeServersLists(svlist1, dst);
        assertEquals(svlist1.size(), dst.size());
    }

    @Test
    public void testServersListsMergingSrcOneNewEntry() {
        JsonReader reader = new JsonReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("svlist1.json")));
        List<ServerValidator.ServerEntry> svlist1 = gson.fromJson(reader, SERVERS_LIST_TYPE);
        assertEquals(3, svlist1.size());
        ServerValidator.mergeServersLists(Collections.singletonList(se2), svlist1);
        assertEquals(1, svlist1.size());
        assertEquals(4, svlist1.get(0).filesCount.intValue());
        assertEquals(5, svlist1.get(0).usersCount.intValue());
    }

    @Test
    public void testServersListsMergingSrcOneOldEntry() {
        JsonReader reader = new JsonReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("svlist1.json")));
        List<ServerValidator.ServerEntry> svlist1 = gson.fromJson(reader, SERVERS_LIST_TYPE);
        assertEquals(3, svlist1.size());
        ServerValidator.mergeServersLists(Collections.singletonList(se), svlist1);
        assertEquals(1, svlist1.size());
        assertEquals(300, svlist1.get(0).filesCount.intValue());
        assertEquals(400, svlist1.get(0).usersCount.intValue());
    }

    @Test
    public void testServersListsMergingNoChanges() {
        JsonReader reader = new JsonReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("svlist1.json")));
        List<ServerValidator.ServerEntry> svlist1 = gson.fromJson(reader, SERVERS_LIST_TYPE);
        assertEquals(3, svlist1.size());

        JsonReader reader2 = new JsonReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("svlist2.json")));
        List<ServerValidator.ServerEntry> svlist2 = gson.fromJson(reader2, SERVERS_LIST_TYPE);
        for (int i = 0; i < svlist2.size(); ++i) {
            assertEquals(0, svlist2.get(i).filesCount.intValue());
            assertEquals(0, svlist2.get(i).usersCount.intValue());
        }

        ServerValidator.mergeServersLists(svlist2, svlist1);
        assertEquals(3, svlist1.size());
        for (int i = 0; i < svlist1.size(); ++i) {
            assertFalse(svlist1.get(i).filesCount.intValue() == 0);
            assertFalse(svlist1.get(i).usersCount.intValue() == 0);
        }

        assertEquals(300, svlist1.get(2).filesCount.intValue());
        assertEquals(400, svlist1.get(2).usersCount.intValue());
    }

    @Test
    public void testServersListPartialEntries() {
        JsonReader reader = new JsonReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("svlist3.json")));
        List<ServerValidator.ServerEntry> svlist = gson.fromJson(reader, SERVERS_LIST_TYPE);
        assertEquals(3, svlist.size());
        assertTrue(isValidEntry(svlist.get(0)));
        assertFalse(isValidEntry(svlist.get(1)));
        assertFalse(isValidEntry(svlist.get(2)));
    }

    @Test
    public void testServerEntryTimestamp() {
        ServerValidator.ServerEntry se = new ServerValidator.ServerEntry();
        se.setFailures(10);
        assertEquals(0, se.getTsOffset());
        Date dt = new Date();
        se.setLastVerified(Long.toString(dt.getTime()));
        se.setFailures(0);
        assertEquals(dt, new Date(se.getTsOffset()));
        se.setFailures(1);
        assertTrue(dt.compareTo(new Date(se.getTsOffset())) == -1);
    }
}
