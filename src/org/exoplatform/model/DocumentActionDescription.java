package org.exoplatform.model;

public class DocumentActionDescription extends Object {

  public int    actionId;

  public String actionName;

  public int    imageID;

  public DocumentActionDescription(int id, String name, int image) {
    actionId   = id;
    actionName = name;
    imageID    = image;
  }

  public String getActionName() {
    return actionName;
  }

  public int getImageID() {
    return imageID;
  }

}
