package org.exoplatform.model;

import java.util.List;

import org.exoplatform.dashboard.ExoGadget;




//	Dashboard tab
public class GateInDbItem {

  public String          _strDbItemName;   // Dashboard name

  public String          _strUrlDbItem;    // Dashboard URL

  public List<ExoGadget> _arrGadgetsInItem; // Gadget list

  // Constructor

  public GateInDbItem(String name, String url, List<ExoGadget> arrGadgets) {

    _strDbItemName = name;
    _strUrlDbItem = url;
    _arrGadgetsInItem = arrGadgets;

  }
}
