package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import java.util.List;

import org.apache.http.cookie.Cookie;
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

    setTitle(_titlebar);
//    final Activity activity = this;
//
//    _wvGadget.setWebChromeClient(new WebChromeClient() {
//      public void onProgressChanged(WebView view, int progress) {
//        // Activities and WebViews measure progress with different scales.
//        // The progress meter will automatically disappear when we reach 100%
//        activity.setProgress(progress * 1000);
//      }
//    });
//    _wvGadget.setWebViewClient(new WebViewClient() {
//      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
//        Toast.makeText(activity, "Oh no! " + description, Toast.LENGTH_SHORT).show();
//      }
//    });
    
//    _url = getIntent().getStringExtra(ExoConstants.SELECTED_IMAGE_EXTRA);
    System.out.println(_url);
    _wvGadget.loadUrl(_url);

    changeLanguage();
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

  }
}
