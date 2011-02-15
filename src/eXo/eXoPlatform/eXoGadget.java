package eXo.eXoPlatform;

import android.graphics.Bitmap;

public class eXoGadget {
	public final String _strGadgetName;
	public final String _strGadgetDescription;
	public final String _strGadgetUrl;
	public final Bitmap _btmGadgetIcon;
	
	public eXoGadget(String gadgetName, String gadgetDescription, String gadgetUrl, Bitmap gadgetIcon) 
	{
		_strGadgetName = gadgetName;
		_strGadgetDescription = gadgetDescription;
		_strGadgetUrl = gadgetUrl;
		_btmGadgetIcon = gadgetIcon;
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
}



