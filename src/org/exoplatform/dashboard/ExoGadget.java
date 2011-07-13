package org.exoplatform.dashboard;

import android.graphics.Bitmap;

//gadget info
public class ExoGadget {
  public final String _strGadgetName;       // Gadget name

  public final String _strGadgetDescription; // Gadget description

  public String       _strGadgetUrl;        // Gadget url

  public String       _strGadgetIcon;       // Gadget icon string
  public final Bitmap _btmGadgetIcon;       // Gadget icon

  public final String _strGadgetID;         // Gadget ID

  // Constructor

  public ExoGadget(String gadgetName,
                   String gadgetDescription,
                   String gadgetUrl,
                   String strGadgetIcon,
                   Bitmap gadgetIcon,
                   String gadgetID) {
    _strGadgetName = gadgetName;
    _strGadgetDescription = gadgetDescription;
    _strGadgetUrl = gadgetUrl;
    _strGadgetIcon = strGadgetIcon;
    _btmGadgetIcon = gadgetIcon;
    _strGadgetID = gadgetID;
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

  public Bitmap getGadgetIcon() {
    return _btmGadgetIcon;
  }

  public String getGadgetID() {
    return _strGadgetID;
  }
}
