package org.dkf.jed2k.protocol;

import org.dkf.jed2k.exception.JED2KException;
import org.dkf.jed2k.protocol.client.*;
import org.dkf.jed2k.protocol.server.*;
import org.dkf.jed2k.protocol.server.search.SearchResult;

public interface Dispatcher {
    // client - server messages
    public void onServerIdChange(IdChange value) throws JED2KException;
    public void onServerInfo(ServerInfo value) throws JED2KException;
    public void onServerList(ServerList value) throws JED2KException;
    public void onServerMessage(Message value) throws JED2KException;
    public void onServerStatus(Status value) throws JED2KException;
    public void onSearchResult(SearchResult value) throws JED2KException;
    public void onFoundFileSources(FoundFileSources value) throws JED2KException;
    public void onCallbackRequestFailed(CallbackRequestFailed value) throws JED2KException;
    public void onCallbackRequestIncoming(CallbackRequestIncoming value) throws JED2KException;

    // client to client packets
    public void onClientHello(Hello value) throws JED2KException;
    public void onClientHelloAnswer(HelloAnswer value)  throws JED2KException;
    public void onClientExtHello(ExtHello value)  throws JED2KException;
    public void onClientExtHelloAnswer(ExtHelloAnswer value) throws JED2KException;

    // client to client messages
    public void onClientFileRequest(FileRequest value) throws JED2KException;
    public void onClientFileAnswer(FileAnswer value) throws JED2KException;
    public void onClientFileStatusRequest(FileStatusRequest value) throws JED2KException;
    public void onClientFileStatusAnswer(FileStatusAnswer value) throws JED2KException;
    public void onClientNoFileStatus(NoFileStatus value) throws JED2KException;
    public void onClientHashSetRequest(HashSetRequest value) throws JED2KException;
    public void onClientHashSetAnswer(HashSetAnswer value) throws JED2KException;
    public void onClientOutOfParts(OutOfParts value) throws JED2KException;
    public void onAcceptUpload(AcceptUpload value) throws JED2KException;
    public void onQueueRanking(QueueRanking value) throws JED2KException;
    public void onClientSendingPart32(SendingPart32 value) throws JED2KException;
    public void onClientSendingPart64(SendingPart64 value) throws JED2KException;
    public void onClientCompressedPart32(CompressedPart32 value) throws JED2KException;
    public void onClientCompressedPart64(CompressedPart64 value) throws JED2KException;
}
