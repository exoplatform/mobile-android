package org.exoplatform.ui;

import greendroid.util.Config;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.LoaderActionBarItem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.cookie.Cookie;
import org.exoplatform.ui.social.SocialActivity;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.MyActionBar;

import android.app.Activity;
import android.os.AsyncTask;
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
  private static final String COOKIESTORE = "cookie_store";

  private WebViewLoadTask     mLoadTask;

  private WebView             _wvGadget;

  private String              _url;

  private String              _titlebar;

  private LoaderActionBarItem loaderItem;

  public void onCreate(Bundle icicle) {

    super.onCreate(icicle);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.webview);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem(Type.Refresh, R.id.action_bar_refresh);
    if (icicle != null) {
      _url = icicle.getString(ExoConstants.WEB_VIEW_URL);
      _titlebar = icicle.getString(ExoConstants.WEB_VIEW_TITLE);
      ArrayList<String> cookieList = icicle.getStringArrayList(COOKIESTORE);
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, cookieList);
    } else {
      _url = getIntent().getStringExtra(ExoConstants.WEB_VIEW_URL);
      _url = _url.replaceAll(" ", "%20");
      _titlebar = getIntent().getStringExtra(ExoConstants.WEB_VIEW_TITLE);
    }

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
        loaderItem = (LoaderActionBarItem) getActionBar().getItem(0);
        if (loaderItem != null) {
          loaderItem.setLoading(true);
        }
        if (progress == 100) {
          setTitle(_titlebar);
          getActionBar().removeItem(0);
        }

      }

    });
    _wvGadget.setWebViewClient(new NewsWebviewClient());
    onLoad();

  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(ExoConstants.WEB_VIEW_URL, _url);
    outState.putString(ExoConstants.WEB_VIEW_TITLE, _titlebar);
    outState.putStringArrayList(COOKIESTORE,
                                ExoConnectionUtils.getCookieList(ExoConnectionUtils.cookiesStore));
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
    List<Cookie> cookies = ExoConnectionUtils.cookiesStore.getCookies();
    String strCookie = "";
    if (!cookies.isEmpty()) {
      for (int i = 0; i < cookies.size(); i++) {
        strCookie = cookies.get(i).getName().toString() + "="
            + cookies.get(i).getValue().toString();
        CookieManager.getInstance().setCookie(url, strCookie);
      }
     
    }
    CookieSyncManager.getInstance().sync();

  }

  @Override
  protected void onDestroy() {
    cleaCache();
    super.onDestroy();

  }

  @Override
  public void onBackPressed() {
    onCancelLoad();
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

  private void onLoad() {
    if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
      if (mLoadTask == null || mLoadTask.getStatus() == WebViewLoadTask.Status.FINISHED) {
        mLoadTask = (WebViewLoadTask) new WebViewLoadTask().execute();
      }
    } else {
      new ConnectionErrorDialog(this).show();
    }

  }

  //
  private void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == WebViewLoadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  //
  private class WebViewLoadTask extends AsyncTask<Void, Void, Boolean> {
    @Override
    protected void onPreExecute() {
      setTitle(getResources().getString(R.string.LoadingData));
      loaderItem = (LoaderActionBarItem) getActionBar().getItem(0);
      loaderItem.setLoading(true);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      /*
       * Checking the response code and re logging in if session timeout
       */
      try {
        int code = ExoConnectionUtils.getResponseCode(_url);
        if (code != 1) {
          ExoConnectionUtils.onReLogin();
        }
        setupCookies(_url);
        return true;
      } catch (IOException e) {
        return false;
      }

    }

    @Override
    protected void onPostExecute(Boolean result) {
      if (result) {
        _wvGadget.loadUrl(_url);
      } else {
        getActionBar().removeItem(0);
        finish();
      }

    }

  }

  private class NewsWebviewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      view.loadUrl(url);
      return true;

    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view,
                                          HttpAuthHandler handler,
                                          String host,
                                          String realm) {
      super.onReceivedHttpAuthRequest(view, handler, host, realm);
      view.reload();
    }
  }
}
