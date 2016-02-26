package org.jed2k;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;
import java.net.InetSocketAddress;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.NetworkIdentifier;
import org.jed2k.protocol.server.search.SearchRequest;

public class Conn {
    private static Logger log = Logger.getLogger(Conn.class.getName());
    
    public static void main(String[] args) throws IOException {        
        System.out.println("Conn started");
        Session s = new Session();
        s.start();
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
        
        log.info("Conn finished");        
    }
}