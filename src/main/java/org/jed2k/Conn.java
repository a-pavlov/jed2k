package org.jed2k;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Logger;

public class Conn {
    private static Logger log = Logger.getLogger(Conn.class.getName());
    
    public static void main(String[] args) throws IOException {        
        System.out.println("Conn started");
        Session s = new Session();
        s.start();
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String command;
        
        while ((command = in.readLine()) != null){            
            if (command.compareTo("exit") == 0 || command.compareTo("quit") == 0) {
                s.interrupt();                
                try {
                    s.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
            }
        }       
        
        log.info("Conn finished");        
    }
}