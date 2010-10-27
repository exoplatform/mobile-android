package eXo.eXoPlatform;

public class eXoApp extends Object
{
	public String _streXoAppName;
	public String _streXoAppDescription;
	
	public eXoApp(String appName, String appDescription)
	{
		_streXoAppName = appName;
		_streXoAppDescription = appDescription;
	}
	
	public String getAppName()
	{
		return _streXoAppName;
	}
	
	public String getAppDescription()
	{
		return _streXoAppDescription;
	}
}