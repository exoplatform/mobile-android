package org.exoplatform.dashboard.entity;


public class DashBoardItem {
 public String    strTabName;

 public ExoGadget gadget;

  public DashBoardItem(String _strTabName, ExoGadget _gadget) {
    this.strTabName = _strTabName;
    this.gadget = _gadget;
  }

}
