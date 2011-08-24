package org.exoplatform.dashboard.entity;

// Standalone gadget info
public class ExoStandAloneGadget {

  public String _strGadgetID; // Gadget ID

  public String _urlContent; // Gadget URL

  public ExoStandAloneGadget() {

  }

  public ExoStandAloneGadget(String gadgetID, String url) {
    _strGadgetID = gadgetID;
    _urlContent = url;
  }
}
