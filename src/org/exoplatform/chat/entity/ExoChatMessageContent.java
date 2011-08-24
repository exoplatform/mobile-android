package org.exoplatform.chat.entity;

//Message info
public class ExoChatMessageContent {

  public String name;   // Message owner

  public String content; // Content

  // Constructor
  public ExoChatMessageContent(String strName, String strContent) {
    name = strName;
    content = strContent;

  }

}
