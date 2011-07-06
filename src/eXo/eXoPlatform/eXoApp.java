package eXo.eXoPlatform;

//eXo application info
public class eXoApp extends Object {
  // App name
  public String _streXoAppName;

  // App description
  public String _streXoAppDescription;

  // Constructor
  public eXoApp(String appName, String appDescription) {
    _streXoAppName = appName;
    _streXoAppDescription = appDescription;
  }

  // Gettors
  public String getAppName() {
    return _streXoAppName;
  }

  public String getAppDescription() {
    return _streXoAppDescription;
  }
}
