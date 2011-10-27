package org.exoplatform.model;

//gadget info
public class GadgetInfo {
  private String _strGadgetName;       // Gadget name

  private String _strGadgetDescription; // Gadget description

  private String _strGadgetUrl;        // Gadget url

  private String _strGadgetIcon;       // Gadget icon string
  
  private int    _intGatdetIndex;      //index for background setting
  
  private String _strTabName;

  // Constructor

  public GadgetInfo(String gadgetName,
                   String gadgetDescription,
                   String gadgetUrl,
                   String strGadgetIcon,
                   String strTabName,
                   int gadgetIndex) {
    _strGadgetName = gadgetName;
    _strGadgetDescription = gadgetDescription;
    _strGadgetUrl = gadgetUrl;
    _strGadgetIcon = strGadgetIcon;
    _strTabName = strTabName;
    _intGatdetIndex = gadgetIndex;
  }
  
  public GadgetInfo(String tabName) {
     _strTabName = tabName;
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

  public String getTabName() {
    return _strTabName;
  }
  
  public int getGadgetIndex() {
    return _intGatdetIndex;
  }
}
