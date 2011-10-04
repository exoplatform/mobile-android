package org.exoplatform.singleton;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.model.ChatMemberInfo;
import org.exoplatform.model.ChatMessageContent;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;

public class ChatServiceHelper {

  private XMPPConnection                      connection;

  private PacketListener                      packetListener;

  private ArrayList<ChatMemberInfo>           listChatRosterEntry;

  private ArrayList<List<ChatMessageContent>> arrListChat;

  private static ChatServiceHelper            chatService = new ChatServiceHelper();

  private ChatServiceHelper() {

  }

  public static ChatServiceHelper getInstance() {
    return chatService;
  }

  public void setXMPPConnection(XMPPConnection conn) {
    connection = conn;
  }

  public XMPPConnection getXMPPConnection() {
    return connection;
  }

  public void setPacketListener(PacketListener packet) {
    packetListener = packet;
  }

  public PacketListener getPacketListener() {
    return packetListener;
  }

  public void setChatListRosterEntry(ArrayList<ChatMemberInfo> list) {
    listChatRosterEntry = list;
  }

  public ArrayList<ChatMemberInfo> getChatListRosterEntry() {
    return listChatRosterEntry;
  }

  public void setListChat(ArrayList<List<ChatMessageContent>> listChat) {
    arrListChat = listChat;
  }

  public ArrayList<List<ChatMessageContent>> getListChat() {
    return arrListChat;
  }
}
