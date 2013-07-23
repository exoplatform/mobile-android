package org.exoplatform.ui;


import android.content.SharedPreferences;
import org.exoplatform.R;
import org.exoplatform.controller.login.LaunchController;
import org.exoplatform.controller.login.LoginController;
import org.exoplatform.controller.login.ServerAdapter;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ServerConfigurationUtils;
import org.exoplatform.utils.SettingUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Date;

/**
 * Represents screen for authentication
 */
public class LoginActivity extends Activity implements OnClickListener {

  private ImageView         _imageAccount;

  private ImageView         _imageServer;

  private Button            _btnAccount;

  private Button            _btnServer;

  private Button            _btnLogIn;

  private EditText          _edtxUserName;

  private EditText          _edtxPassword;

  private ListView          _listViewServer;

  private String            strSignIn;

  private String            settingText;

  private String            userNameHint;

  private String            passWordHint;

  private String            username;

  private String            password;

  private LinearLayout      listviewPanel;

  private LinearLayout      userpassPanel;

  private AccountSetting    mSetting;

  private static final String TAG = "eXoLoginActivity";

  public void onCreate(Bundle savedInstanceState) {
    long start = new Date().getTime();
    Log.i(TAG, "start time login activity: " + start);

    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.login);
    mSetting = AccountSetting.getInstance();

    /* launch app from custom url - need to init setting */
    boolean isLaunchedFromUrl = (getIntent().getData() != null);
    if (isLaunchedFromUrl) {
      new LaunchController(this);
      setUpUserAndServerFromUrl();
    }

    init();
    Log.i(TAG, "end time login: " + (new Date().getTime() - start) );
  }

  @Override
  protected void onResume() {
    super.onResume();
    SettingUtils.setDefaultLanguage(this);
    username = _edtxUserName.getText().toString();
    password = _edtxPassword.getText().toString();
    setInformation();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setContentView(R.layout.login);
    username = _edtxUserName.getText().toString();
    password = _edtxPassword.getText().toString();
    init();
  }

  private void init() {
    /* init app setting */
    _imageAccount = (ImageView) findViewById(R.id.Image_Account);
    _imageServer  = (ImageView) findViewById(R.id.Image_Server);
    _edtxUserName = (EditText)  findViewById(R.id.EditText_UserName);
    _edtxPassword = (EditText)  findViewById(R.id.EditText_Password);
    _btnAccount   = (Button)    findViewById(R.id.Button_Account);
    _btnAccount.setOnClickListener(this);
    _btnServer    = (Button)    findViewById(R.id.Button_Server);
    _btnServer.setOnClickListener(this);
    _btnLogIn     = (Button)    findViewById(R.id.Button_Login);
    _btnLogIn.setOnClickListener(this);
    listviewPanel = (LinearLayout) findViewById(R.id.login_listview_panel);
    userpassPanel = (LinearLayout) findViewById(R.id.login_userpass_panel);
    _listViewServer = (ListView) findViewById(R.id.ListView_Servers);
    _listViewServer.setVisibility(View.INVISIBLE);
    _listViewServer.setCacheColorHint(Color.TRANSPARENT);
    _listViewServer.setFadingEdgeLength(0);
    _listViewServer.setDivider(null);
    _listViewServer.setDividerHeight(1);

    setInformation();
  }

  /**
   * Set up user and server from eXo url scheme
   * Saves this server and make it as the current server
   *
   * Current support url scheme:
   *
   * exomobile://
   * exomobile://serverUrl=xxx
   * exomobile://username=xx?serverUrl=xxx
   */
  private void setUpUserAndServerFromUrl() {
    Uri eXoUri = getIntent().getData();
    /* exomobile:// */
    if ((eXoUri == null) || (eXoUri.getHost() == null)) return;

    String host      = eXoUri.getHost();
    String serverUrl;
    String username  = "";

    /* exomobile://serverUrl=xxx */
    if (host.contains(ExoConstants.EXO_URL_SERVER)) {

      int equalIdx = host.indexOf("=");
      if ((equalIdx == -1) || (equalIdx == host.length())) return;
      serverUrl = host.substring(equalIdx + 1, host.length());
    }
    /* exomobile://username=xxx */
    else if (host.contains(ExoConstants.EXO_URL_USERNAME)) {
      /* automatic decode URL */
      serverUrl = eXoUri.getQueryParameter(ExoConstants.EXO_URL_SERVER);
      if (serverUrl == null) return;
      int equalIdx = host.indexOf("=");
      if ((equalIdx != -1) && (equalIdx < host.length()))
        username = host.substring(equalIdx + 1, host.length());
    }
    else return ;

    /* Add server to server list if server is new */
    ServerObjInfo serverObj  = new ServerObjInfo();
    serverObj.serverName = Uri.parse(serverUrl).getAuthority();
    serverObj.serverUrl  = serverUrl;
    serverObj.username   = username;

    ArrayList<ServerObjInfo> serverList = ServerSettingHelper.getInstance().getServerInfoList();
    String domainIdx;
    int serverIdx = serverList.indexOf(serverObj);
    if (serverIdx > -1) {
      domainIdx = String.valueOf(serverIdx);
    }
    else {
      serverList.add(serverObj);
      serverIdx = serverList.size() - 1;
      domainIdx = String.valueOf(serverIdx);

      // Persist config - TODO: check whether this need to be done in a separate Thread
      SettingUtils.persistServerSetting(this);
    }

    // set current selected server to the new server
    mSetting.setDomainIndex(String.valueOf(domainIdx));
    mSetting.setCurrentServer(serverList.get(serverIdx));
  }

  /**
   * Retrieve username and password from input field or from account setting
   */
  private void setInformation() {
    changeLanguage();
    _edtxUserName.setHint(userNameHint);
    _edtxUserName.setText( (username==null || username.equals(""))
        && mSetting.isRememberMeEnabled()
        ? mSetting.getUsername(): username);

    _edtxPassword.setHint(passWordHint);
    _edtxPassword.setText( (password==null || password.equals(""))
        && mSetting.isRememberMeEnabled()
        ? mSetting.getPassword():password);

    _btnLogIn.setText(strSignIn);
    setServerAdapter();
  }

  private void setServerAdapter() {
    _listViewServer.setAdapter(new ServerAdapter(this, _listViewServer));
  }

  private void onLogin() {
    username = _edtxUserName.getText().toString();
    password = _edtxPassword.getText().toString();
    new LoginController(this, username, password, true);
  }

  public void changeLanguage() {
    Resources resource = getResources();
    strSignIn = resource.getString(R.string.SignInButton);
    settingText = resource.getString(R.string.Settings);
    userNameHint = resource.getString(R.string.UserNameCellTitle);
    passWordHint = resource.getString(R.string.PasswordCellTitle);
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    menu.add(0, 1, 0, settingText).setIcon(R.drawable.optionsettingsbutton);
    return true;
  }

  public boolean onOptionsItemSelected(MenuItem item) {

    int selectedItemIndex = item.getItemId();

    if (selectedItemIndex == 1) {
      Intent next = new Intent(this, SettingActivity.class);
      next.putExtra(ExoConstants.SETTING_TYPE, SettingActivity.GLOBAL_TYPE);
      startActivity(next);
    }
    return false;
  }

  @Override
  public void onClick(View view) {
    if (view.equals(_btnLogIn)) {
      onLogin();
    }

    if (view.equals(_btnServer)) {
      view.setBackgroundResource(R.drawable.authenticatepanelbuttonbgon);
      _imageServer.setBackgroundResource(R.drawable.authenticateserversiconiphoneon);
      _btnAccount.setBackgroundResource(R.drawable.authenticatepanelbuttonbgoff);
      _imageAccount.setBackgroundResource(R.drawable.authenticate_credentials_icon_off);
      _edtxUserName.setVisibility(View.INVISIBLE);
      _edtxPassword.setVisibility(View.INVISIBLE);
      _btnLogIn.setVisibility(View.INVISIBLE);
      userpassPanel.setVisibility(View.INVISIBLE);
      _listViewServer.setVisibility(View.VISIBLE);
      listviewPanel.setVisibility(View.VISIBLE);
      InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      mgr.showSoftInput(_edtxUserName, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    if (view.equals(_btnAccount)) {
      Log.i(TAG, "switch to account panel");
      view.setBackgroundResource(R.drawable.authenticatepanelbuttonbgon);
      _imageAccount.setBackgroundResource(R.drawable.authenticate_credentials_icon_on);
      _btnServer.setBackgroundResource(R.drawable.authenticatepanelbuttonbgoff);
      _imageServer.setBackgroundResource(R.drawable.authenticateserversiconiphoneoff);

      _edtxUserName.setVisibility(View.VISIBLE);
      Log.i(TAG, "remember me: " + mSetting.isRememberMeEnabled());
      if (mSetting.getCurrentServer()!=null) Log.i(TAG, "server: " + mSetting.getCurrentServer().serverUrl);
      Log.i(TAG, "user: " + mSetting.getUsername());

      _edtxUserName.setText(mSetting.isRememberMeEnabled() ? mSetting.getUsername(): username);
      _edtxPassword.setVisibility(View.VISIBLE);
      _edtxPassword.setText(mSetting.isRememberMeEnabled() ? mSetting.getPassword(): password);

      _btnLogIn.setVisibility(View.VISIBLE);
      _listViewServer.setVisibility(View.INVISIBLE);
      userpassPanel.setVisibility(View.VISIBLE);
      listviewPanel.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  protected void onPause(){
    Log.i(TAG, "onPause");
    super.onPause();

    if (!mSetting.getDomainIndex().equals("-1")) {
      SharedPreferences.Editor editor = getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0).edit();
      editor.putString(ExoConstants.EXO_PRF_DOMAIN_INDEX, mSetting.getDomainIndex());
      editor.commit();
    }
  }
}
