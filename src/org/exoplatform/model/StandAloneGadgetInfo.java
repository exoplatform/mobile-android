package org.exoplatform.model;

// Standalone gadget info
public class StandAloneGadgetInfo {

  public String _strGadgetID; // Gadget ID

  public String _urlContent; // Gadget URL

  public StandAloneGadgetInfo() {

  }

  public StandAloneGadgetInfo(String gadgetID, String url) {
    _strGadgetID = gadgetID;
    _urlContent = url;
  }
}
