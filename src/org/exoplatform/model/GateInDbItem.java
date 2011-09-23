package org.exoplatform.model;

import java.util.List;





//	Dashboard tab
public class GateInDbItem {

  public String          _strDbItemName;   // Dashboard name

  public String          _strUrlDbItem;    // Dashboard URL

  public List<GadgetInfo> _arrGadgetsInItem; // Gadget list

  // Constructor

  public GateInDbItem(String name, String url, List<GadgetInfo> arrGadgets) {

    _strDbItemName = name;
    _strUrlDbItem = url;
    _arrGadgetsInItem = arrGadgets;

  }
}
