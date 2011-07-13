package org.exoplatform.model;

//eXo application info
public class ExoApp extends Object {
  // App name
  public String _streXoAppName;

  // App description
  public String _streXoAppDescription;

  // Constructor
  public ExoApp(String appName, String appDescription) {
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
