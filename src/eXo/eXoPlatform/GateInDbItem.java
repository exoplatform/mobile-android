package eXo.eXoPlatform;

import java.util.List;

//	Dashboard tab
public class GateInDbItem {

  public String          _strDbItemName;   // Dashboard name

  public String          _strUrlDbItem;    // Dashboard URL

  public List<eXoGadget> _arrGadgetsInItem; // Gadget list

  // Constructor

  public GateInDbItem(String name, String url, List<eXoGadget> arrGadgets) {

    _strDbItemName = name;
    _strUrlDbItem = url;
    _arrGadgetsInItem = arrGadgets;

  }
}
