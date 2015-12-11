/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.widget.CompatibleFileOpenDialog;
import org.exoplatform.widget.ConnectionErrorDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.PluginState;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity {

  private static final String ACCOUNT_SETTING = "account_setting";

  private static final String LOG_TAG         = "eXo____WebViewActivity____";

  private WebViewLoadTask     mLoadTask;

  private WebView             _wvGadget;

  private String              _url;

  private String              _titlebar;

  private String              contentType;

  private MenuItem            loaderItem;

  private MenuItem            documentItem;

  public void onCreate(Bundle icicle) {

    super.onCreate(icicle);
    setContentView(R.layout.webview);
    setTitle("");

    /*
     * restore the previous state
     */
    if (icicle != null) {
      _url = icicle.getString(ExoConstants.WEB_VIEW_URL);
      _titlebar = icicle.getString(ExoConstants.WEB_VIEW_TITLE);
      contentType = icicle.getString(ExoConstants.WEB_VIEW_MIME_TYPE);
      AccountSetting accountSetting = icicle.getParcelable(ACCOUNT_SETTING);
      AccountSetting.getInstance().setInstance(accountSetting);
      ArrayList<String> cookieList = AccountSetting.getInstance().cookiesList;
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, cookieList);
    } else {
      _url = getIntent().getStringExtra(ExoConstants.WEB_VIEW_URL);
      _url = _url.replaceAll(" ", "%20");
      _titlebar = getIntent().getStringExtra(ExoConstants.WEB_VIEW_TITLE);
      contentType = getIntent().getStringExtra(ExoConstants.WEB_VIEW_MIME_TYPE);
    }

    setupCookies(_url);
    _wvGadget = (WebView) findViewById(R.id.WebView);
    /*
     * Ensure we can clear Webview cache and set no cache when loading data.
     */
    _wvGadget.clearCache(true);
    _wvGadget.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    _wvGadget.getSettings().setSupportZoom(true);
    if (getIntent().getStringExtra(ExoConstants.WEB_VIEW_ALLOW_JS) != null) {
      boolean allowJS = Boolean.parseBoolean(getIntent().getStringExtra(ExoConstants.WEB_VIEW_ALLOW_JS));
      setJavascript(allowJS);
    } else {
      // Disable JS by default
      setJavascript(false);
    }

    // TODO: plugins should be disabled
    _wvGadget.getSettings().setPluginState(PluginState.ON);
    _wvGadget.getSettings().setLoadsImagesAutomatically(true);
    _wvGadget.getSettings().setBuiltInZoomControls(true);

    if (contentType != null && contentType.startsWith(ExoDocumentUtils.IMAGE_TYPE)) {
      // Display images fully in the screen by default
      _wvGadget.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
      _wvGadget.getSettings().setUseWideViewPort(true);
      _wvGadget.getSettings().setLoadWithOverviewMode(true);
    }
    final Activity activity = this;

    _wvGadget.setWebChromeClient(new WebChromeClient() {
      public void onProgressChanged(WebView view, int progress) {
        setTitle(getResources().getString(R.string.LoadingData));
        activity.setProgress(progress * 100);
        setLoaderItemVisible(true);
        if (progress == 100) {
          setTitle(_titlebar);
          setLoaderItemVisible(false);
          if (contentType != null && documentItem != null)
            documentItem.setVisible(true);
        }
      }
    });
    _wvGadget.setWebViewClient(new NewsWebviewClient());
    onLoad();

  }

  @SuppressLint("SetJavaScriptEnabled")
  public void setJavascript(boolean allowJS) {
    _wvGadget.getSettings().setJavaScriptEnabled(allowJS);
    _wvGadget.getSettings().setJavaScriptCanOpenWindowsAutomatically(allowJS);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(ExoConstants.WEB_VIEW_URL, _url);
    outState.putString(ExoConstants.WEB_VIEW_TITLE, _titlebar);
    outState.putString(ExoConstants.WEB_VIEW_MIME_TYPE, contentType);
    outState.putParcelable(ACCOUNT_SETTING, AccountSetting.getInstance());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.webview, menu);
    loaderItem = menu.findItem(R.id.menu_webview_refresh);
    documentItem = menu.findItem(R.id.menu_webview_open_document);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {

    case R.id.menu_webview_open_document:
      new CompatibleFileOpenDialog(this, contentType, _url, _titlebar).show();
      break;

    default:
      break;
    }

    return true;
  }

  private void setupCookies(String url) {
    String domain = "";
    try {
      URI uri = new URI(url);
      domain = uri.getHost();
      if (uri.getPort() != -1) {
        domain = domain + ":" + uri.getPort();
      }

    } catch (URISyntaxException e) {
      domain = url;
      Log.w(LOG_TAG, "Setting cookie for invalid URL :" + url);
    }

    CookieSyncManager.createInstance(this);
    ArrayList<String> cookies = AccountSetting.getInstance().cookiesList;
    if (cookies != null) {
      for (String strCookie : cookies) {
        CookieManager.getInstance().setCookie(url, strCookie);
      }
      CookieSyncManager.getInstance().sync();
    }
  }

  private void setLoaderItemVisible(boolean visible) {
    if (loaderItem != null) {
      loaderItem.setVisible(visible);
    }
  }

  @Override
  protected void onPause() {
    onCancelLoad();
    super.onPause();
  }

  @Override
  public void finish() {
    // Avoids leaking the ZoomButtonsController when leaving the activity
    ViewGroup layout = (ViewGroup) getWindow().getDecorView();
    layout.removeAllViews();
    super.finish();
  }

  @Override
  public void onBackPressed() {
    onCancelLoad();
    if (_wvGadget != null && _wvGadget.canGoBack()) {
      _wvGadget.goBack();
    } else
      finish();
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

  private void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == WebViewLoadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  private class WebViewLoadTask extends AsyncTask<Void, Void, Boolean> {
    @Override
    protected void onPreExecute() {
      setTitle(getResources().getString(R.string.LoadingData));
      setLoaderItemVisible(true);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      setupCookies(_url);
      return true;

    }

    @Override
    protected void onPostExecute(Boolean result) {
      if (result) {
        _wvGadget.loadUrl(_url);
      } else {
        setLoaderItemVisible(false);
        finish();
      }

    }

    @Override
    protected void onCancelled() {
      setLoaderItemVisible(false);
      super.onCancelled();
    }

  }

  private class NewsWebviewClient extends WebViewClient {
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      view.loadUrl(url);
      return true;
    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
      super.onReceivedHttpAuthRequest(view, handler, host, realm);
      view.reload();
    }
  }
}
