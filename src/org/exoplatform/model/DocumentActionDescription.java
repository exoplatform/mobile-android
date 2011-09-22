package org.exoplatform.model;

public class DocumentActionDescription extends Object {

  public String actionName;

  public int    imageID;

  public DocumentActionDescription(String name, int image) {
    actionName = name;
    imageID = image;
  }

  public String getActionName() {
    return actionName;
  }

  public int getImageID() {
    return imageID;
  }

}
