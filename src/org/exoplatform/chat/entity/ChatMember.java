package org.exoplatform.chat.entity;

public class ChatMember {
  public String  address;

  public boolean isOnline;

  public ChatMember(String addr, boolean bool) {
    address = addr;
    isOnline = bool;

  }

  public String getChatName() {
    int index = this.address.indexOf("@");
    return this.address.substring(0, index);

  }
}
