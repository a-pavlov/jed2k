package org.jed2k;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;

import org.jed2k.protocol.ProtocolException;

public class Session extends Thread {
    private static Logger log = Logger.getLogger(Session.class.getName()); 
    Selector selector = null;
    private ConcurrentLinkedQueue<Runnable> commands = new ConcurrentLinkedQueue<Runnable>();
    private ServerConnection sc = null;
    private ServerSocketChannel ssc = null;
    
    @Override
    public void run() {
        try {
            log.info("Session started");
            selector = Selector.open();
            ssc = ServerSocketChannel.open();
            ssc.socket().bind(new InetSocketAddress(4661));
            ssc.configureBlocking(false);
            ssc.register(selector, SelectionKey.OP_ACCEPT);
            ServerConnection sc = new ServerConnection(this, new InetSocketAddress("emule.is74.ru", 4661));
            sc.connect();
            boolean once = true;
            while(!isInterrupted()) {
                int channelCount = selector.select(1000);
                if (channelCount != 0) {
                    // process channels
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
    
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
    
                    while(keyIterator.hasNext()) {
    
                      SelectionKey key = keyIterator.next();
    
                      if(key.isAcceptable()) {
                          // a connection was accepted by a ServerSocketChannel.
                          log.info("Key is acceptable");
                          SocketChannel socket = ssc.accept();
                          socket.close();
    
                      } else if (key.isConnectable()) {
                          // a connection was established with a remote server.
                          log.info("Key is connectable");
                          ServerConnection sconn = (ServerConnection)key.attachment();
                          sconn.finishConnection();
                          key.interestOps(SelectionKey.OP_WRITE);
                      } else if (key.isReadable()) {
                          // a channel is ready for reading
                          log.info("Key is readable");
                          ServerConnection sconn = (ServerConnection)key.attachment();
                          sconn.read();
    
                      } else if (key.isWritable()) {
                          // a channel is ready for writing
                          log.info("Key is writeable");
                          ServerConnection sconn = (ServerConnection)key.attachment();
                          if (once) {
                              sconn.writeHello();
                              once = false;
                          }
                          
                          key.interestOps(SelectionKey.OP_READ);
                      }
    
                      keyIterator.remove();
                    }
                }
                
                // execute user's commands
                Runnable r = commands.poll();
                while(r != null) {
                    r.run();
                    r = commands.poll();
                }
            }
        }
        catch(IOException e) {
            log.severe(e.getMessage());
        }
        catch(ProtocolException e) {
            log.severe(e.getMessage());
        }
        finally {
            log.info("Session finished");
            try {
                if (selector != null) selector.close();
            }
            catch(IOException e) {
                
            }
        }
    }
    
    public void connectoTo(final InetSocketAddress address) {
        commands.add(new Runnable() {
            @Override
            public void run() {
                if (sc != null) {
                    sc.close();
                }
                
                sc = new ServerConnection(Session.this, address);
            }
        });
    }
    
    public void disconnectFrom() {
        commands.add(new Runnable() {
            @Override
            public void run() {
                if (sc != null) {
                    sc.close();
                    sc = null;
                }
            }
        });
    }
}