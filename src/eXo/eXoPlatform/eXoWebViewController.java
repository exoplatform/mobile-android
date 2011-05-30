package eXo.eXoPlatform;

import greendroid.app.GDActivity;

import java.util.List;
import java.util.ResourceBundle;

import org.apache.http.cookie.Cookie;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//Display gadget
public class eXoWebViewController extends GDActivity {
  public static eXoWebViewController eXoWebViewControllerInstance; // Instance

  WebView                     _wvGadget;
  
  public static String               _url;
  public static String               _titlebar;

  String                      strCannotBackToPreviousPage;

  public void onCreate(Bundle icicle) {

    super.onCreate(icicle);
    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.webview);

    eXoWebViewControllerInstance = this;
    
    setupCookies();

    _wvGadget = (WebView) findViewById(R.id.WebView);

    _wvGadget.getSettings().setJavaScriptEnabled(true);
    _wvGadget.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
    _wvGadget.getSettings().setPluginsEnabled(true);
    _wvGadget.getSettings().setLoadsImagesAutomatically(true);
    _wvGadget.addJavascriptInterface(this, "MainScreen");

    _wvGadget.getSettings().setBuiltInZoomControls(false);

    if (eXoApplicationsController2.webViewMode == 0) {

      
      
    } else if (eXoApplicationsController2.webViewMode == 1) {
     
      _url = Environment.getExternalStorageDirectory() + "/eXo/" + eXoFilesController.myFile.fileName;
      _titlebar = eXoFilesController.myFile.fileName;
      
    } else if (eXoApplicationsController2.webViewMode == 2) {
      
      _titlebar = "User guide";
      
      String locallize = AppController.sharedPreference.getString(AppController.EXO_PRF_LOCALIZE,
                                                                  "exo_prf_localize");
      if (locallize.equalsIgnoreCase("LocalizeEN.properties"))
        _url = "file:///android_asset/HowtoUse-EN.htm";
      else if (locallize.equalsIgnoreCase("LocalizeFR.properties"))
        _url = "file:///android_asset/HowtoUse-FR.htm";
      else
        _url = "file:///android_asset/HowtoUse-VN.htm";
    }
    
    setTitle(_titlebar.replace("%20", " "));
    _wvGadget.loadUrl(_url);
    
    changeLanguage(AppController.bundle);
  }

  
  public void finishMe()
  {
    if (eXoApplicationsController2.webViewMode == 1) {
      int index = eXoFilesController.myFile.urlStr.lastIndexOf("/");
      eXoFilesController.myFile.urlStr = eXoFilesController.myFile.urlStr.substring(0, index);

    }
    else if (eXoApplicationsController2.webViewMode == 2)
    {
      Intent next = new Intent(eXoWebViewController.this, eXoSetting.class);
      startActivity(next);  
    }
    else
    {
      Intent next = new Intent(eXoWebViewController.this, eXoDashboard.class);
      startActivity(next);
    }

    eXoWebViewControllerInstance = null;
    GDActivity.TYPE = 1;
    
  }
 
  private void setupCookies() {
    List<Cookie> cookies = AppController._eXoConnection._sessionCookies;// .authenticateAndReturnCookies();

    if (cookies != null) {

      // CookieManager.getInstance().removeSessionCookie();

      for (Cookie cookie : cookies) {
        String cookieString = cookie.getName() + "=" + cookie.getValue();
        CookieManager.getInstance().setCookie(cookie.getDomain(), cookieString);
      }

      // CookieManager.getInstance().setCookie("mobile.demo.exoplatform.org","JSESSIONID="+
      // cookie.getValue()+";domain=mobile.demo.exoplatform.org");

      CookieSyncManager.getInstance().sync();
    }
  }

  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Save data to the server once the user hits the back button
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      Toast.makeText(eXoWebViewController.this, strCannotBackToPreviousPage, Toast.LENGTH_LONG)
           .show();
    }
    return false;
  }

  public void changeLanguage(ResourceBundle resourceBundle) {

    String str_btnBack = "";
    try {
      str_btnBack = new String(resourceBundle.getString("CloseButton").getBytes("ISO-8859-1"),
                               "UTF-8");
      strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage")
                                                             .getBytes("ISO-8859-1"), "UTF-8");
    } catch (Exception e) {

    }
  }
}
