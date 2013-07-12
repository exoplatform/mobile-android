package org.exoplatform.ui;


import org.exoplatform.R;
import org.exoplatform.controller.login.LaunchController;
import org.exoplatform.controller.login.LoginController;
import org.exoplatform.controller.login.ServerAdapter;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SettingUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class LoginActivity extends Activity implements OnClickListener {
  private SharedPreferences sharedPreference;

  private ImageView         _imageAccount;

  private ImageView         _imageServer;

  private Button            _btnAccount;

  private Button            _btnServer;

  private Button            _btnLogIn;

  private EditText          _edtxUserName;

  private EditText          _edtxPassword;

  private ListView          _listViewServer;

  private Button            mSettingBtn;

  private String            strSignIn;

  private String            settingText;

  private String            userNameHint;

  private String            passWordHint;

  private String            username;

  private String            password;

  private LinearLayout      listviewPanel;

  private LinearLayout      userpassPanel;



  private static final String TAG = "eXoLoginActivity";

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    this.setContentView(R.layout.login);
    init();
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
    this.setContentView(R.layout.login);
    username = _edtxUserName.getText().toString();
    password = _edtxPassword.getText().toString();
    init();
  }

  private void init() {
    new LaunchController(this, sharedPreference);
    _imageAccount = (ImageView) findViewById(R.id.Image_Account);
    _imageServer = (ImageView) findViewById(R.id.Image_Server);
    _edtxUserName = (EditText) findViewById(R.id.EditText_UserName);
    _edtxPassword = (EditText) findViewById(R.id.EditText_Password);
    _btnAccount = (Button) findViewById(R.id.Button_Account);
    _btnAccount.setOnClickListener(this);
    _btnServer = (Button) findViewById(R.id.Button_Server);
    _btnServer.setOnClickListener(this);
    _btnLogIn = (Button) findViewById(R.id.Button_Login);
    _btnLogIn.setOnClickListener(this);
    listviewPanel = (LinearLayout) findViewById(R.id.login_listview_panel);
    userpassPanel = (LinearLayout) findViewById(R.id.login_userpass_panel);
    _listViewServer = (ListView) findViewById(R.id.ListView_Servers);
    _listViewServer.setVisibility(View.INVISIBLE);
    _listViewServer.setCacheColorHint(Color.TRANSPARENT);
    _listViewServer.setFadingEdgeLength(0);
    _listViewServer.setDivider(null);
    _listViewServer.setDividerHeight(1);

    mSettingBtn = (Button) findViewById(R.id.login_setting_btn);
    mSettingBtn.setOnClickListener(this);

    boolean isLaunchedFromUrl = (getIntent().getData() != null);
    if (isLaunchedFromUrl) setUpUserAndServerFromUrl();

    setInformation();
  }

  private void setUpUserAndServerFromUrl() {
    Uri eXoUri = getIntent().getData();
    /* exomobile:// */
    if ((eXoUri == null) || (eXoUri.getHost() == null)) return;

    String host      = eXoUri.getHost();
    String serverUrl;
    String username  = null;

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
      if ((equalIdx == -1) || (equalIdx == host.length())) username = null;
      username = host.substring(equalIdx + 1, host.length());
    }
    else return ;

    /* saves to account setting */
    AccountSetting.getInstance().setUsername(username == null ? "": username);
    AccountSetting.getInstance().setDomainName(serverUrl);
    AccountSetting.getInstance().isRememberMeEnabled = true;
    AccountSetting.getInstance().isAutoLoginEnabled  = true;

    /* Add server to server list */
    ServerObjInfo serverObj  = new ServerObjInfo();
    //serverObj._bSystemServer = false;
    serverObj._strServerName = Uri.parse(serverUrl).getAuthority();
    serverObj._strServerUrl  = serverUrl;

    ServerSettingHelper settingHelper = ServerSettingHelper.getInstance();
    settingHelper.getServerInfoList().add(serverObj);
    // set current selected server to the new server
    AccountSetting.getInstance().setDomainIndex(String.valueOf(settingHelper.getServerInfoList().size() -1));
  }

  /**
   * Retrieve username and password from input field or from account setting
   */
  private void setInformation() {
    changeLanguage();
    _edtxUserName.setHint(userNameHint);
    _edtxUserName.setText(username==null || username.equals("")
        ? AccountSetting.getInstance().getUsername(): username);
    _edtxPassword.setHint(passWordHint);
    _edtxPassword.setText(password==null || password.equals("")
        ? AccountSetting.getInstance().getPassword():password);
    _btnLogIn.setText(strSignIn);
    setServerAdapter();
  }

  private void setServerAdapter() {
    _listViewServer.setAdapter(new ServerAdapter(this, _listViewServer));
  }

  private void onLogin() {
    username = _edtxUserName.getText().toString();
    password = _edtxPassword.getText().toString();
    new LoginController(this, username, password);
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
      Intent next = new Intent(LoginActivity.this, SettingActivity.class);
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
      view.setBackgroundResource(R.drawable.authenticatepanelbuttonbgon);
      _imageAccount.setBackgroundResource(R.drawable.authenticate_credentials_icon_on);
      _btnServer.setBackgroundResource(R.drawable.authenticatepanelbuttonbgoff);
      _imageServer.setBackgroundResource(R.drawable.authenticateserversiconiphoneoff);

      _edtxUserName.setVisibility(View.VISIBLE);
      _edtxUserName.setText(AccountSetting.getInstance().getUsername());
      _edtxPassword.setVisibility(View.VISIBLE);
      _edtxPassword.setText(AccountSetting.getInstance().getPassword());

      _btnLogIn.setVisibility(View.VISIBLE);
      _listViewServer.setVisibility(View.INVISIBLE);
      userpassPanel.setVisibility(View.VISIBLE);
      listviewPanel.setVisibility(View.INVISIBLE);
    }

    if (view.equals(mSettingBtn)) {
      Intent next = new Intent(this, SettingActivity.class);
      startActivity(next);
    }
  }
}
