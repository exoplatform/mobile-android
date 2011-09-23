package org.exoplatform.model;

public class ChatMemberInfo {
  public String  address;

  public boolean isOnline;

  public ChatMemberInfo(String addr, boolean bool) {
    address = addr;
    isOnline = bool;

  }

  public String getChatName() {
    int index = this.address.indexOf("@");
    return this.address.substring(0, index);

  }
}
