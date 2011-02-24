package eXo.eXoPlatform;

import android.graphics.Bitmap;

public class eXoGadget {
	public final String _strGadgetName;
	public final String _strGadgetDescription;
	public String _strGadgetUrl;
	public final Bitmap _btmGadgetIcon;
	public final String _strGadgetID;
	
	public eXoGadget(String gadgetName, String gadgetDescription, String gadgetUrl, Bitmap gadgetIcon, String gadgetID) 
	{
		_strGadgetName = gadgetName;
		_strGadgetDescription = gadgetDescription;
		_strGadgetUrl = gadgetUrl;
		_btmGadgetIcon = gadgetIcon;
		_strGadgetID = gadgetID;
	}
	
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



