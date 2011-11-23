package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import java.util.List;

import org.apache.http.cookie.Cookie;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.cyrilmottier.android.greendroid.R;

//Display gadget
public class WebViewActivity extends MyActionBar {
  private WebView      _wvGadget;

  public static String _url;

  public static String _titlebar;

  private String       loadingStr;

  public void onCreate(Bundle icicle) {

    super.onCreate(icicle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.webview);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    setupCookies();
    changeLanguage();

    _wvGadget = (WebView) findViewById(R.id.WebView);
    _wvGadget.getSettings().setSupportZoom(true);
    _wvGadget.getSettings().setAppCacheEnabled(true);
    _wvGadget.getSettings().setJavaScriptEnabled(true);
    _wvGadget.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
    _wvGadget.getSettings().setPluginsEnabled(true);
    _wvGadget.getSettings().setLoadsImagesAutomatically(true);
    _wvGadget.addJavascriptInterface(this, "MainScreen");

    _wvGadget.getSettings().setBuiltInZoomControls(true);

    final Activity activity = this;

    _wvGadget.setWebChromeClient(new WebChromeClient() {
      public void onProgressChanged(WebView view, int progress) {
        setTitle(loadingStr);
        activity.setProgress(progress * 100);
        if (progress == 100)
          setTitle(_titlebar);

      }
    });

    _wvGadget.loadUrl(_url);

  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    if (DashboardActivity.dashboardActivity != null) {
      DashboardActivity.dashboardActivity.finish();
    }
    if (DocumentActivity._documentActivityInstance != null) {
      DocumentActivity._documentActivityInstance.finish();
    }
    finish();

    return true;
  }

  private void setupCookies() {
    CookieSyncManager.createInstance(this);

    CookieManager.getInstance().setCookie(_url, ExoConnectionUtils._strCookie);
    // CookieSyncManager.getInstance().sync();
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
    loadingStr = local.getString("LoadingData");

  }
}
