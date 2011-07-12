package eXo.eXoPlatform.DataManager.Model.Chat;

//Message info
public class eXoChatMessageContent {

  public String name;   // Message owner

  public String content; // Content

  // Constructor
  public eXoChatMessageContent(String strName, String strContent) {
    name = strName;
    content = strContent;

  }

}
