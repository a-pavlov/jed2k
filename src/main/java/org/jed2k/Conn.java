package org.jed2k;

import java.util.logging.Logger;

public class Conn {
    private static Logger log = Logger.getLogger(Conn.class.getName());
    
    public static void main(String[] args) {        
        System.out.println("Conn started");
        Session s = new Session();
        s.start();
        try {
            Thread.sleep(15000);
            s.interrupt();
            s.join();
        }
        catch(InterruptedException e) {
            log.severe(e.getMessage());
        }
        
        log.info("Conn finished");        
    }
}