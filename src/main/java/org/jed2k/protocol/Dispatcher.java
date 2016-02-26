package org.jed2k.protocol;

import org.jed2k.exception.JED2KException;
import org.jed2k.protocol.server.FoundFileSources;
import org.jed2k.protocol.server.IdChange;
import org.jed2k.protocol.server.Message;
import org.jed2k.protocol.server.SearchResult;
import org.jed2k.protocol.server.ServerInfo;
import org.jed2k.protocol.server.ServerList;
import org.jed2k.protocol.server.Status;

public interface Dispatcher {
    // client - server messages
    public void onServerIdChange(IdChange value) throws JED2KException;
    public void onServerInfo(ServerInfo value) throws JED2KException;
    public void onServerList(ServerList value) throws JED2KException;
    public void onServerMessage(Message value) throws JED2KException;
    public void onServerStatus(Status value) throws JED2KException;
    public void onSearchResult(SearchResult value) throws JED2KException;
    public void onFoundFileSources(FoundFileSources value) throws JED2KException;
    
    // client to client packets
    public void onClientHello(ClientHello value) throws JED2KException;
    public void onClientHelloAnswer(ClientHelloAnswer value)  throws JED2KException;
    public void onClientExtHello(ClientExtHello value)  throws JED2KException;
    public void onClientExtHelloAnswer(ClientExtHelloAnswer value) throws JED2KException;
    
    // client to client file information
    public void onClientFileRequest(ClientFileRequest value) throws JED2KException;
    public void onClientFileAnswer(ClientFileAnswer value) throws JED2KException;
    public void onClientFileStatusRequest(ClientFileStatusRequest value) throws JED2KException;
    public void onClientFileStatusAnswer(ClientFileStatusAnswer value) throws JED2KException;
    public void onClientHashSetRequest(ClientHashSetRequest value) throws JED2KException;
    public void onClientHashSetAnswer(ClientHashSetAnswer value) throws JED2KException;
    public void onClientNoFileStatus(ClientNoFileStatus value) throws JED2KException;
    public void onClientOutOfParts(ClientOutOfParts value) throws JED2KException;
    public void onClientSendingPart32(ClientSendingPart32 value) throws JED2KException;
    public void onClientSendingPart64(ClientSendingPart64 value) throws JED2KException;
}
