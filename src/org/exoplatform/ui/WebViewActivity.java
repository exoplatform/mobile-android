package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import java.util.List;

import org.apache.http.cookie.Cookie;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.widget.MyActionBar;

import android.os.Bundle;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.cyrilmottier.android.greendroid.R;

//Display gadget
public class WebViewActivity extends MyActionBar {
  private WebView      _wvGadget;

  public static String _url;

  public static String _titlebar;

  private String       strCannotBackToPreviousPage;

  public void onCreate(Bundle icicle) {

    super.onCreate(icicle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.webview);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    setupCookies();

    _wvGadget = (WebView) findViewById(R.id.WebView);
    _wvGadget.getSettings().setSupportZoom(true);
    // _wvGadget.getSettings().setUseWideViewPort(true);
    _wvGadget.getSettings().setJavaScriptEnabled(true);
    _wvGadget.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
    _wvGadget.getSettings().setPluginsEnabled(true);
    _wvGadget.getSettings().setLoadsImagesAutomatically(true);
    _wvGadget.addJavascriptInterface(this, "MainScreen");

    _wvGadget.getSettings().setBuiltInZoomControls(true);

//    if (ExoApplicationsController2.webViewMode == 0) {
//
//    } else if (ExoApplicationsController2.webViewMode == 1) {
//
//      _url = Environment.getExternalStorageDirectory() + "/eXo/"
//          + ExoFilesController.myFile.fileName;
//      _titlebar = ExoFilesController.myFile.fileName;
//
//    }

    setTitle(_titlebar.replace("%20", " "));
    _wvGadget.loadUrl(_url);

    changeLanguage();
  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    if (DashboardActivity.dashboardActivity != null) {
      DashboardActivity.dashboardActivity.finish();
    }
    finish();

    return true;
  }

//  public void finishMe() {
//    if (ExoApplicationsController2.webViewMode == 1) {
//      int index = ExoFilesController.myFile.urlStr.lastIndexOf("/");
//      ExoFilesController.myFile.urlStr = ExoFilesController.myFile.urlStr.substring(0, index);
//
//    } else if (ExoApplicationsController2.webViewMode == 2) {
//      Intent next = new Intent(WebViewActivity.this, SettingActivity.class);
//      startActivity(next);
//    } else {
//      Intent next = new Intent(WebViewActivity.this, DashboardActivity.class);
//      startActivity(next);
//    }
//    finish();
//
//  }

  private void setupCookies() {
    CookieSyncManager.createInstance(this);
    List<Cookie> cookies = ExoConnectionUtils._sessionCookies;// .authenticateAndReturnCookies();

    if (cookies != null) {

      for (Cookie cookie : cookies) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(cookie.getName());
        buffer.append("=");
        buffer.append(cookie.getValue());
        CookieManager.getInstance().setCookie(cookie.getDomain(), buffer.toString());
      }

      CookieSyncManager.getInstance().sync();

    }
  }

  @Override
  public void onBackPressed() {
    finish();
  }

  public void changeLanguage() {
    LocalizationHelper local = LocalizationHelper.getInstance();

    String str_btnBack = "";
    str_btnBack = local.getString("CloseButton");
    strCannotBackToPreviousPage = local.getString("CannotBackToPreviousPage");
  }
}
