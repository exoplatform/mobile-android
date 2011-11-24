package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.MyActionBar;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

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
    finish();

    return true;
  }

  private void setupCookies() {
    try {
      CookieSyncManager.createInstance(this);

      HttpParams httpParameters = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(httpParameters, 30000);
      HttpConnectionParams.setSoTimeout(httpParameters, 30000);
      HttpConnectionParams.setTcpNoDelay(httpParameters, true);

      DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

      HttpGet httpGet = new HttpGet(_url);

      httpClient.execute(httpGet);
      CookieStore cookiesStore = httpClient.getCookieStore();
      List<Cookie> cookies = cookiesStore.getCookies();
      String strCookie = "";
      if (!cookies.isEmpty()) {
        for (int i = 0; i < cookies.size(); i++) {
          strCookie = cookies.get(i).getName().toString() + "="
              + cookies.get(i).getValue().toString();
        }
      }

      CookieManager.getInstance().setCookie(_url, strCookie);
      CookieSyncManager.getInstance().sync();
    } catch (Exception e) {
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

  private class NewsWebviewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      view.loadUrl(url);
      return super.shouldOverrideUrlLoading(view, url);

    }
  }
}
