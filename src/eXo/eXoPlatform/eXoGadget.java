package eXo.eXoPlatform;

import android.graphics.Bitmap;

//gadget info
public class eXoGadget {
	public final String _strGadgetName;	//Gadget name
	public final String _strGadgetDescription;	//Gadget description
	public String _strGadgetUrl;	//Gadget url
	public final Bitmap _btmGadgetIcon;	//Gadget icon
	public final String _strGadgetID;	//Gadget ID
//	Constructor
	public eXoGadget(String gadgetName, String gadgetDescription, String gadgetUrl, Bitmap gadgetIcon, String gadgetID) 
	{
		_strGadgetName = gadgetName;
		_strGadgetDescription = gadgetDescription;
		_strGadgetUrl = gadgetUrl;
		_btmGadgetIcon = gadgetIcon;
		_strGadgetID = gadgetID;
	}
//	Gettors
	public String getGadgetName()
	{
		return _strGadgetName;
	}
	
	public String getGadgetDescription()
	{
		return _strGadgetDescription;
	}
	
	public String getGadgetUrl()
	{
		return _strGadgetUrl;
	}
	
	public Bitmap getGadgetIcon()
	{
		return _btmGadgetIcon;
	}
	
	public String getGadgetID()
	{
		return _strGadgetID;
	}
}



