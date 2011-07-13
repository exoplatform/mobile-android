package exo.exoplatform.dashboard;

import exo.exoplatform.controller.AppController;
import exo.exoplatform.controller.ExoApplicationsController2;
import exo.exoplatform.document.ExoFilesController;
import exo.exoplatform.setting.ExoSetting;
import exo.exoplatform.widget.MyActionBar;
import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;

import java.util.List;
import java.util.ResourceBundle;

import org.apache.http.cookie.Cookie;

import com.cyrilmottier.android.greendroid.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import android.widget.Toast;

//Display gadget
public class ExoWebViewController extends MyActionBar {
  public static ExoWebViewController eXoWebViewControllerInstance; // Instance

  WebView                            _wvGadget;

  public static String               _url;

  public static String               _titlebar;

  String                             strCannotBackToPreviousPage;

  public void onCreate(Bundle icicle) {

    super.onCreate(icicle);
    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.webview);

    eXoWebViewControllerInstance = this;
    
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    setupCookies();

    _wvGadget = (WebView) findViewById(R.id.WebView);

    _wvGadget.getSettings().setJavaScriptEnabled(true);
    _wvGadget.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
    _wvGadget.getSettings().setPluginsEnabled(true);
    _wvGadget.getSettings().setLoadsImagesAutomatically(true);
    _wvGadget.addJavascriptInterface(this, "MainScreen");

    _wvGadget.getSettings().setBuiltInZoomControls(false);

    if (ExoApplicationsController2.webViewMode == 0) {

    } else if (ExoApplicationsController2.webViewMode == 1) {

      _url = Environment.getExternalStorageDirectory() + "/eXo/"
          + ExoFilesController.myFile.fileName;
      _titlebar = ExoFilesController.myFile.fileName;

    } else if (ExoApplicationsController2.webViewMode == 2) {

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

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

//    finishMe();

    finish();
     
    return true;
  }
  
  public void finishMe() {
    if (ExoApplicationsController2.webViewMode == 1) {
      int index = ExoFilesController.myFile.urlStr.lastIndexOf("/");
      ExoFilesController.myFile.urlStr = ExoFilesController.myFile.urlStr.substring(0, index);

    } else if (ExoApplicationsController2.webViewMode == 2) {
      Intent next = new Intent(ExoWebViewController.this, ExoSetting.class);
      startActivity(next);
    } else {
      Intent next = new Intent(ExoWebViewController.this, ExoDashboard.class);
      startActivity(next);
    }

    eXoWebViewControllerInstance = null;
//    GDActivity.TYPE = 1;

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
      Toast.makeText(ExoWebViewController.this, strCannotBackToPreviousPage, Toast.LENGTH_LONG)
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