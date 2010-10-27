package eXo.eXoPlatform;

import java.util.List;

public class GateInDbItem {
	
	String	_strDbItemName;
	String	_strUrlDbItem;
	List<eXoGadget>	_arrGadgetsInItem;
	
	public GateInDbItem(String name, String url, List<eXoGadget> arrGadgets) {
		
		_strDbItemName = name;
		_strUrlDbItem = url;
		_arrGadgetsInItem = arrGadgets;
		
	}
}
	
	

