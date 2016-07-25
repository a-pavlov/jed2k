package org.jed2k;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.net.InetSocketAddress;

import org.jed2k.alert.Alert;
import org.jed2k.alert.SearchResultAlert;
import org.jed2k.alert.ServerMessageAlert;
import org.jed2k.alert.ServerStatusAlert;
import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.server.SharedFileEntry;
import org.jed2k.protocol.server.search.SearchRequest;
import org.jed2k.protocol.server.search.SearchResult;

public class Conn {
    private static Logger log = Logger.getLogger(Conn.class.getName());

    public static void main(String[] args) throws IOException {
        Logger logger = Logger.getLogger("");
        logger.setUseParentHandlers(false);
        //Handler[] handlers = logger.getHandlers();
        //for(Handler handler : handlers) {
        //    logger.removeHandler(handler);
        //}

        System.out.println("Conn started");
        final Session s = new Session();
        s.start();

        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

        ScheduledFuture scheduledFuture =
            scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
                    @Override
                    public void run() {
                    Alert a = s.popAlert();
                    while(a != null) {
                        if (a instanceof SearchResultAlert) {
                            SearchResult sr = ((SearchResultAlert)a).results;
                            for(SharedFileEntry entry: sr.files) {
                                System.out.println(entry.toString());
                            }

                            System.out.println("More results: " + (sr.hasMoreResults()?"yes":"no"));
                        }
                        else if (a instanceof ServerMessageAlert) {
                            System.out.println("Server message: " + ((ServerMessageAlert)a).msg);
                        }
                        else if (a instanceof ServerStatusAlert) {
                            ServerStatusAlert ssa = (ServerStatusAlert)a;
                            System.out.println("Files count = " + ssa.filesCount + " users count = " + ssa.usersCount);
                        }
                        else {
                            System.out.println("Unknown alert received: " + a.toString());
                        }

                        a = s.popAlert();
                    }
                }
            },
        1, 1,
        TimeUnit.SECONDS);

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String command;

        while ((command = in.readLine()) != null) {
            String[] parts = command.split("\\s+");

            if (parts[0].compareTo("exit") == 0 || parts[0].compareTo("quit") == 0) {
                s.interrupt();
                try {
                    s.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }

            if (parts[0].compareTo("listen") == 0 && parts.length == 2) {
            	Settings settings = new Settings();
            	settings.listenPort = (short)Integer.parseInt(parts[1]);
            	s.configureSession(settings);
            }
            if (parts[0].compareTo("connect") == 0 && parts.length == 3) {
                s.connectoTo(new InetSocketAddress(parts[1], (short)Integer.parseInt(parts[2])));
            }
            else if (parts[0].compareTo("search") == 0 && parts.length > 1) {
                String searchExpression = command.substring("search".length());
                log.info("search expressionr:" + searchExpression);
                try {
                    log.info("search request: " + s);
                    s.search(SearchRequest.makeRequest(0, 0, 0, 0, "", "", "", 0, 0, searchExpression));
                } catch(JED2KException e) {
                    log.warning(e.getMessage());
                }
            } else if (parts[0].compareTo("peer") == 0 && parts.length == 3) {
                s.connectToPeer(new NetworkIdentifier(Integer.parseInt(parts[1]), (short)Integer.parseInt(parts[2])));
            }

        }

        scheduledExecutorService.shutdown();
        log.info("Conn finished");
    }
}