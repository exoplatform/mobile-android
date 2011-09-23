package org.exoplatform.model;

//Message info
public class ChatMessageContent {

  public String name;   // Message owner

  public String content; // Content

  // Constructor
  public ChatMessageContent(String strName, String strContent) {
    name = strName;
    content = strContent;

  }

}
