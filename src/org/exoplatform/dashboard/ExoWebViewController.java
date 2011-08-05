package org.exoplatform.dashboard;

import greendroid.widget.ActionBarItem;

import java.util.List;
import java.util.ResourceBundle;

import org.apache.http.cookie.Cookie;
import org.exoplatform.controller.AppController;
import org.exoplatform.controller.ExoApplicationsController2;
import org.exoplatform.document.ExoFilesController;
import org.exoplatform.setting.ExoSetting;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.widget.MyActionBar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.cyrilmottier.android.greendroid.R;

//Display gadget
public class ExoWebViewController extends MyActionBar {
  WebView              _wvGadget;

  public static String _url;

  public static String _titlebar;

  String               strCannotBackToPreviousPage;

  public void onCreate(Bundle icicle) {

    super.onCreate(icicle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.webview);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    setupCookies();

    _wvGadget = (WebView) findViewById(R.id.WebView);
    _wvGadget.getSettings().setSupportZoom(true);
//    _wvGadget.getSettings().setUseWideViewPort(true);
    _wvGadget.getSettings().setJavaScriptEnabled(true);
    _wvGadget.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
    _wvGadget.getSettings().setPluginsEnabled(true);
    _wvGadget.getSettings().setLoadsImagesAutomatically(true);
    _wvGadget.addJavascriptInterface(this, "MainScreen");

    _wvGadget.getSettings().setBuiltInZoomControls(true);

    if (ExoApplicationsController2.webViewMode == 0) {

    } else if (ExoApplicationsController2.webViewMode == 1) {

      _url = Environment.getExternalStorageDirectory() + "/eXo/"
          + ExoFilesController.myFile.fileName;
      _titlebar = ExoFilesController.myFile.fileName;

    } else if (ExoApplicationsController2.webViewMode == 2) {

      String userGuider = AppController.bundle.getString("UserGuide");
      _titlebar = userGuider;

      String locallize = AppController.sharedPreference.getString(AppController.EXO_PRF_LOCALIZE,
                                                                  "exo_prf_localize");
      if (locallize == null || locallize.equalsIgnoreCase("exo_prf_localize"))
        locallize = "LocalizeEN.properties";

      if (locallize.equalsIgnoreCase("LocalizeEN.properties"))
        _url = "file:///android_asset/HowtoUse-EN.htm";
      else if (locallize.equalsIgnoreCase("LocalizeFR.properties"))
        _url = "file:///android_asset/HowtoUse-FR.htm";
      // else
      // _url = "file:///android_asset/HowtoUse-VN.htm";

    }

    setTitle(_titlebar.replace("%20", " "));
    _wvGadget.loadUrl(_url);

    changeLanguage(AppController.bundle);
  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

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
    finish();

  }

  private void setupCookies() {
    CookieSyncManager.createInstance(this);
    List<Cookie> cookies = ExoConnectionUtils._sessionCookies;// .authenticateAndReturnCookies();

    if (cookies != null) {

      for (Cookie cookie : cookies) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(cookie.getName());
        buffer.append("=");
        buffer.append(cookie.getValue());
        System.out.println("Cookies " + buffer.toString());
        CookieManager.getInstance().setCookie(cookie.getDomain(), buffer.toString());
      }

      CookieSyncManager.getInstance().sync();

    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finishMe();
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
