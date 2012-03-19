package org.exoplatform.ui;

import greendroid.util.Config;
import greendroid.widget.ActionBarItem;

import java.io.File;
import java.util.List;

import org.apache.http.cookie.Cookie;
import org.exoplatform.ui.social.SocialActivity;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.widget.MyActionBar;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
    _url = _url.replaceAll(" ", "%20");
    setupCookies(_url);
    _wvGadget = (WebView) findViewById(R.id.WebView);
    _wvGadget.getSettings().setSupportZoom(true);
    _wvGadget.getSettings().setJavaScriptEnabled(true);
    _wvGadget.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
    _wvGadget.getSettings().setPluginsEnabled(true);
    _wvGadget.getSettings().setLoadsImagesAutomatically(true);
    _wvGadget.addJavascriptInterface(this, "MainScreen");
    _wvGadget.getSettings().setBuiltInZoomControls(true);

    final Activity activity = this;

    _wvGadget.setWebChromeClient(new WebChromeClient() {
      public void onProgressChanged(WebView view, int progress) {
        setTitle(getResources().getString(R.string.LoadingData));
        activity.setProgress(progress * 100);
        if (progress == 100)
          setTitle(_titlebar);

      }

      @Override
      public boolean onJsTimeout() {
        ExoConnectionUtils.onReLogin();
        setupCookies(_url);
        return super.onJsTimeout();
      }
    });
    _wvGadget.setWebViewClient(new NewsWebviewClient());

    _wvGadget.loadUrl(_url);

  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    if (DashboardActivity.dashboardActivity != null) {
      DashboardActivity.dashboardActivity.finish();
    }
    if (DocumentActivity._documentActivityInstance != null) {
      DocumentActivity._documentActivityInstance.finish();
    }
    if (SocialDetailActivity.socialDetailActivity != null) {
      SocialDetailActivity.socialDetailActivity.finish();
      if (SocialActivity.socialActivity != null) {
        SocialActivity.socialActivity.finish();
      }
    }
    cleaCache();
    finish();
    return true;
  }

  private void setupCookies(String url) {
    CookieSyncManager.createInstance(this);
    List<Cookie> cookies = ExoConnectionUtils._sessionCookies;
    String strCookie = "";
    if (!cookies.isEmpty()) {
      for (int i = 0; i < cookies.size(); i++) {
        strCookie = cookies.get(i).getName().toString() + "="
            + cookies.get(i).getValue().toString();
      }
    }

    CookieManager.getInstance().setCookie(url, strCookie);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    cleaCache();
  }

  @Override
  public void onBackPressed() {
    if (_wvGadget.canGoBack()) {
      cleaCache();
      _wvGadget.goBack();
    } else
      finish();
  }

  private void cleaCache() {
    File dir = getCacheDir();
    if (dir != null && dir.isDirectory()) {
      try {
        File[] children = dir.listFiles();
        if (children.length > 0) {
          for (int i = 0; i < children.length; i++) {
            File[] temp = children[i].listFiles();
            for (int x = 0; x < temp.length; x++) {
              temp[x].delete();
            }
          }
        }
      } catch (Exception e) {
        if (Config.GD_ERROR_LOGS_ENABLED)
          Log.e("Exception", "Cannot clear  file cache!");
      }
    }

  }

  private class NewsWebviewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      setupCookies(url);
      view.loadUrl(url);
      return true;

    }

    // Re logging in if receive the Http Authorization Request

    @Override
    public void onReceivedHttpAuthRequest(WebView view,
                                          HttpAuthHandler handler,
                                          String host,
                                          String realm) {
      super.onReceivedHttpAuthRequest(view, handler, host, realm);
      // Relogin
      ExoConnectionUtils.onReLogin();
      setupCookies(host);
      view.reload();
    }
  }
}
