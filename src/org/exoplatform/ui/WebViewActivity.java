package org.exoplatform.ui;

//import greendroid.widget.ActionBarItem;
//import greendroid.widget.ActionBarItem.Type;
//import greendroid.widget.LoaderActionBarItem;

import java.util.ArrayList;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import org.exoplatform.poc.tabletversion.R;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.ui.social.SocialTabsActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.widget.CompatibleFileOpenDialog;
import org.exoplatform.widget.ConnectionErrorDialog;
//import org.exoplatform.widget.MyActionBar;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * The web view to show gadget
 */
public class WebViewActivity extends ActionBarActivity {
    //extends MyActionBar {

  private static final String ACCOUNT_SETTING = "account_setting";

  private WebViewLoadTask     mLoadTask;

  private WebView             _wvGadget;

  private String              _url;

  private String              _titlebar;

  private String              contentType;

  private Menu                mOptionsMenu;

  //private LoaderActionBarItem loaderItem;

  private static final String TAG = "eXo____WebViewActivity____";


  public void onCreate(Bundle savedData) {

    super.onCreate(savedData);
    //requestWindowFeature(Window.FEATURE_NO_TITLE);

    //setActionBarContentView(R.layout.webview);
    setContentView(R.layout.webview);

    //getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    //addActionBarItem(Type.Refresh, R.id.action_bar_refresh);

    /** Restore the previous state */
    if (savedData != null) {
      _url        = savedData.getString(ExoConstants.WEB_VIEW_URL);
      _titlebar   = savedData.getString(ExoConstants.WEB_VIEW_TITLE);
      contentType = savedData.getString(ExoConstants.WEB_VIEW_MIME_TYPE);
      AccountSetting accountSetting = savedData.getParcelable(ACCOUNT_SETTING);
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
    /** Ensure we can clear Webview cache and set no cache when loading data */
    _wvGadget.clearCache(true);
    _wvGadget.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
    _wvGadget.getSettings().setSupportZoom(true);

    if (getIntent().getStringExtra(ExoConstants.WEB_VIEW_ALLOW_JS) != null) {
      boolean allowJS = Boolean.parseBoolean( getIntent().getStringExtra(ExoConstants.WEB_VIEW_ALLOW_JS) );
      setJavascript(allowJS);
    }
    else {
      // Disable JS by default 
      setJavascript(false);
    }

    _wvGadget.getSettings().setPluginsEnabled(true);
    _wvGadget.getSettings().setLoadsImagesAutomatically(true);
    _wvGadget.addJavascriptInterface(this, "MainScreen");
    _wvGadget.getSettings().setBuiltInZoomControls(true);

    /**
     * the method for controlling the layout of html. SINGLE_COLUMN moves all
     * content into one column that is the width of the view.
     */
    if (contentType != null && contentType.startsWith(ExoDocumentUtils.IMAGE_TYPE)) {
      _wvGadget.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
      _wvGadget.getSettings().setUseWideViewPort(false);
    }

    final Activity activity = this;

    _wvGadget.setWebChromeClient(new WebChromeClient() {
      public void onProgressChanged(WebView view, int progress) {
        setTitle(getResources().getString(R.string.LoadingData));
        activity.setProgress(progress * 100);

        //ActionBarItem item = getActionBar().getItem(0);
        //if (item instanceof LoaderActionBarItem) {
        //  loaderItem = (LoaderActionBarItem) item;
        //}
        //if (loaderItem != null) {
        //  loaderItem.setLoading(true);
        //}
        if (progress == 100) {
          setTitle(_titlebar);
          //getActionBar().removeItem(0);
          //if (contentType != null) {
          //  addActionBarItem();
          //  getActionBar().getItem(0).setDrawable(R.drawable.actionbar_icon_dodument);
          //}

        }
      }

    });

    _wvGadget.setWebViewClient(new NewsWebviewClient());
    startLoadingWebView();

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

  /**   TODO replace
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

    switch (position) {
    case -1:
      if (DashboardActivity.dashboardActivity != null) {
        DashboardActivity.dashboardActivity.finish();
      }
      if (DocumentActivity._documentActivityInstance != null) {
        DocumentActivity._documentActivityInstance.finish();
      }
      if (SocialDetailActivity.socialDetailActivity != null) {
        SocialDetailActivity.socialDetailActivity.finish();
        if (SocialTabsActivity.instance != null) {
          SocialTabsActivity.instance.finish();
        }
      }
      finish();

      break;

    case 0:
      if (item instanceof LoaderActionBarItem) {

      } else {
        new CompatibleFileOpenDialog(this, contentType, _url, _titlebar).show();
      }
      break;

    default:
      break;
    }

    return true;
  }
   **/

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    Log.i(TAG, "onCreateOptionsMenu");
    getMenuInflater().inflate(R.menu.home, menu);
    mOptionsMenu = menu;
    if (isLoading()) menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
    menu.findItem(R.id.menu_sign_out).setVisible(false);
    menu.findItem(R.id.menu_settings).setVisible(false);
    menu.findItem(R.id.menu_overflow).setVisible(false);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_refresh:
        startLoadingWebView();
        return true;

    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Change state of refresh icon on action bar
   *
   * @param refreshing
   */
  public void setRefreshActionButtonState(boolean refreshing) {
    Log.i(TAG, "setRefreshActionButtonState: " + refreshing);

    if (mOptionsMenu == null) return ;
    final MenuItem refreshItem = mOptionsMenu.findItem(R.id.menu_refresh);
    Log.i(TAG, "setRefreshActionButtonState - refreshItem: " + refreshItem);
    if (refreshItem == null) return ;

    //boolean currentState = refreshItem.getActionView() != null;

    if (refreshing)
      refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
    else {
      refreshItem.setActionView(null);
      //supportInvalidateOptionsMenu();
    }
  }

  private void setupCookies(String url) {
    CookieSyncManager.createInstance(this);
    ArrayList<String> cookies = AccountSetting.getInstance().cookiesList;
    for (String strCookie : cookies) {
      CookieManager.getInstance().setCookie(url, strCookie);
    }
    CookieSyncManager.getInstance().sync();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  public void onBackPressed() {
    onCancelLoad();
    if (_wvGadget.canGoBack()) {
      _wvGadget.goBack();
    } else
      finish();
  }

  private void startLoadingWebView() {
    if (!ExoConnectionUtils.isNetworkAvailableExt(this)) {
      new ConnectionErrorDialog(this).show();
      return ;
    }

    if (mLoadTask == null || mLoadTask.getStatus() == WebViewLoadTask.Status.FINISHED) {
      setRefreshActionButtonState(true);
      mLoadTask = (WebViewLoadTask) new WebViewLoadTask().execute();
    }
  }

  private void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == WebViewLoadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  private boolean isLoading() {
    if (mLoadTask == null) return false;

    if (mLoadTask.getStatus() == WebViewLoadTask.Status.RUNNING) {
      return true;
    }

    return false;
  }

  private class WebViewLoadTask extends AsyncTask<Void, Void, Boolean> {

    @Override
    protected void onPreExecute() {
      setTitle(getResources().getString(R.string.LoadingData));
      //loaderItem = (LoaderActionBarItem) getActionBar().getItem(0);
      //loaderItem.setLoading(true);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
      setupCookies(_url);
      return true;
    }

    @Override
    protected void onPostExecute(Boolean result) {
      Log.i(TAG, "onPostExecute");
      if (result) {
        _wvGadget.loadUrl(_url);
      } else {
        //getActionBar().removeItem(0);
        finish();
      }

      setRefreshActionButtonState(false);
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
