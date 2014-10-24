package org.jed2k.protocol;

public interface Dispatcher {
    public boolean onServerIdChange(ServerIdChange value);
    public boolean onServerInfo(ServerInfo value);
    public boolean onServerList(ServerList value);
    public boolean onServerMessage(ServerMessage value);
    public boolean onServerStatus(ServerStatus value);
    public boolean onSearchResult(SearchResult value);
}
