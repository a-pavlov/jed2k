package org.jed2k.protocol;

import org.jed2k.exception.JED2KException;

public interface Dispatcher {
    public void onServerIdChange(ServerIdChange value) throws JED2KException;
    public void onServerInfo(ServerInfo value) throws JED2KException;
    public void onServerList(ServerList value) throws JED2KException;
    public void onServerMessage(ServerMessage value) throws JED2KException;
    public void onServerStatus(ServerStatus value) throws JED2KException;
    public void onSearchResult(SearchResult value) throws JED2KException;
    
    // client to client packets
    public void onClientHello(ClientHello value) throws JED2KException;
    public void onClientHelloAnswer(ClientHelloAnswer value)  throws JED2KException;
    public void onClientExtHello(ClientExtHello value)  throws JED2KException;
    public void onClientExtHelloAnswer(ClientExtHelloAnswer value) throws JED2KException;
}
