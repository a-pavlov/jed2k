package org.jed2k.protocol;

enum ClientServerTcp{
  OP_LOGINREQUEST(0x01), // <HASH 16><ID 4><PORT 2><1 Tag_set>
  OP_REJECT(0x05), // (null)
  OP_GETSERVERLIST(0x14), // (null)client->server
  OP_OFFERFILES(0x15), // <count 4>(<HASH 16><ID 4><PORT 2><1
                       // Tag_set>)[count]
  OP_SEARCHREQUEST(0x16), // <Query_Tree>
  OP_DISCONNECT(0x18), // (not verified)
  OP_GETSOURCES(0x19), // <HASH 16>
                       // v2 <HASH 16><SIZE_4> (17.3) (mandatory on 17.8)
                       // v2large <HASH 16><FILESIZE 4(0)><FILESIZE 8>
                       // (17.9) (large files only)
  OP_SEARCH_USER(0x1A), // <Query_Tree>
  OP_CALLBACKREQUEST(0x1C), // <ID 4>
  // OP_QUERY_CHATS = 0x1D, // (deprecated, not supported by server any
  // longer)
  // OP_CHAT_MESSAGE = 0x1E, // (deprecated, not supported by server any
  // longer)
  // OP_JOIN_ROOM = 0x1F, // (deprecated, not supported by server any
  // longer)
  OP_QUERY_MORE_RESULT(0x21), // ?
  OP_GETSOURCES_OBFU(0x23), OP_SERVERLIST(0x32), // <count 1>(<IP 4><PORT
                                                 // 2>)[count]
                                                 // server->client
  OP_SEARCHRESULT(0x33), // <count 4>(<HASH 16><ID 4><PORT 2><1
                         // Tag_set>)[count]
  OP_SERVERSTATUS(0x34), // <USER 4><FILES 4>
  OP_CALLBACKREQUESTED(0x35), // <IP 4><PORT 2>
  OP_CALLBACK_FAIL(0x36), // (null notverified)
  OP_SERVERMESSAGE(0x38), // <len 2><Message len>
  // OP_CHAT_ROOM_REQUEST = 0x39, // (deprecated, not supported by server
  // any longer)
  // OP_CHAT_BROADCAST = 0x3A, // (deprecated, not supported by server any
  // longer)
  // OP_CHAT_USER_JOIN = 0x3B, // (deprecated, not supported by server any
  // longer)
  // OP_CHAT_USER_LEAVE = 0x3C, // (deprecated, not supported by server
  // any longer)
  // OP_CHAT_USER = 0x3D, // (deprecated, not supported by server any
  // longer)
  OP_IDCHANGE(0x40), // <NEW_ID 4>
  OP_SERVERIDENT(0x41), // <HASH 16><IP 4><PORT 2>{1 TAG_SET}
  OP_FOUNDSOURCES(0x42), // <HASH 16><count 1>(<ID 4><PORT 2>)[count]
  OP_USERS_LIST(0x43), // <count 4>(<HASH 16><ID 4><PORT 2><1
                       // Tag_set>)[count]
  OP_FOUNDSOURCES_OBFU(0x44); // <HASH 16><count 1>(<ID 4><PORT 2><obf
                              // settings 1>(UserHash16 if
                              // obf&0x08))[count]

  public byte value;

  ClientServerTcp(int v) {
      value = (byte)v;
  }
}