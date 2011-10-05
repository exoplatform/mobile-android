package org.exoplatform.model;

import android.graphics.Bitmap;

//gadget info
public class GadgetInfo {
  private final String _strGadgetName;       // Gadget name

  private final String _strGadgetDescription; // Gadget description

  private String       _strGadgetUrl;        // Gadget url

  private String       _strGadgetIcon;       // Gadget icon string
  private final Bitmap _btmGadgetIcon;       // Gadget icon

  private final String _strGadgetID;         // Gadget ID
  
  private int          _intGatdetIndex;      //index for background setting

  // Constructor

  public GadgetInfo(String gadgetName,
                   String gadgetDescription,
                   String gadgetUrl,
                   String strGadgetIcon,
                   Bitmap gadgetIcon,
                   String gadgetID,
                   int gadgetIndex) {
    _strGadgetName = gadgetName;
    _strGadgetDescription = gadgetDescription;
    _strGadgetUrl = gadgetUrl;
    _strGadgetIcon = strGadgetIcon;
    _btmGadgetIcon = gadgetIcon;
    _strGadgetID = gadgetID;
    _intGatdetIndex = gadgetIndex;
  }

  // Gettors
  public String getGadgetName() {
    return _strGadgetName;
  }

  public String getGadgetDescription() {
    return _strGadgetDescription;
  }

  public String getGadgetUrl() {
    return _strGadgetUrl;
  }
  
  public void setGadgetUrl(String url) {
    _strGadgetUrl = url;
  }
  
  public String getStrGadgetIcon() {
    return _strGadgetIcon;
  }

  public Bitmap getGadgetIcon() {
    return _btmGadgetIcon;
  }

  public String getGadgetID() {
    return _strGadgetID;
  }
  
  public int getGadgetIndex() {
    return _intGatdetIndex;
  }
}
