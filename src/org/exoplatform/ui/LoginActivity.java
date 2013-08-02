package org.exoplatform.ui;


import android.content.SharedPreferences;
import android.view.*;
import android.widget.*;
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
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;

import java.util.ArrayList;
import java.util.Date;

/**
 * Represents screen for authentication
 */
public class LoginActivity extends Activity implements OnClickListener, AdapterView.OnItemClickListener {

  private ImageView         mAccountBtn;

  private ImageView         mServerBtn;

  private Button            mLogInBtn;

  private EditText          mUserEditTxt;

  private EditText          mPassEditTxt;

  /** list view that contains list of servers */
  private ListView          mServerListView;

  private String            strSignIn;

  private String            settingText;

  private String            userNameHint;

  private String            passWordHint;

  private String            username;

  private String            password;

  private LinearLayout      mServerPanel;

  private LinearLayout      mAccountPanel;

  private AccountSetting    mSetting;

  /** Default is set to show account panel */
  private String            mPanelMode     = ACCOUNT_PANEL;

  public static final String ACCOUNT_PANEL = "ACCOUNT_PANEL";

  public static final String SERVER_PANEL  = "SERVER_PANEL";

  private static final String TAG = "eXoLoginActivity";

  public void onCreate(Bundle savedInstanceState) {
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

    initSubViews();
  }

  @Override
  protected void onResume() {
    Log.i(TAG, "onResume");
    super.onResume();
    SettingUtils.setDefaultLanguage(this);
    username = mUserEditTxt.getText().toString();
    password = mPassEditTxt.getText().toString();
    initSubViews();
    setInformation();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setContentView(R.layout.login);
    username = mUserEditTxt.getText().toString();
    password = mPassEditTxt.getText().toString();
    initSubViews();
  }

  private void initSubViews() {
    Log.i(TAG, "initSubViews");
    /* init account panel */
    mAccountPanel = (LinearLayout) findViewById(R.id.login_account_panel);
    mUserEditTxt  = (EditText) mAccountPanel.findViewById(R.id.EditText_UserName);
    mPassEditTxt  = (EditText) mAccountPanel.findViewById(R.id.EditText_Password);
    mLogInBtn     = (Button) mAccountPanel.findViewById(R.id.Button_Login);
    mLogInBtn.setOnClickListener(this);

    /* init button */
    mAccountBtn   = (ImageView) findViewById(R.id.login_account_btn);
    mAccountBtn.setOnClickListener(this);
    mServerBtn    = (ImageView) findViewById(R.id.login_server_btn);
    mServerBtn.setOnClickListener(this);

    /* init server panel */
    mServerPanel = (LinearLayout) findViewById(R.id.login_server_panel);
    mServerListView = (ListView) mServerPanel.findViewById(R.id.ListView_Servers);
    mServerListView.setCacheColorHint(Color.TRANSPARENT);
    mServerListView.setFadingEdgeLength(0);
    mServerListView.setDivider(null);
    mServerListView.setDividerHeight(1);

    mServerListView.setOnItemClickListener(this);
    mServerListView.setAdapter(new ServerAdapter(this));

    switchPanel(mPanelMode);
    setInformation();
  }


  private void switchPanel(String panel) {
    mPanelMode = panel;

    if (mPanelMode.equals(ACCOUNT_PANEL)) {
      Log.i(TAG, "switch to account panel");
      mAccountPanel.setVisibility(View.VISIBLE);
      mServerPanel.setVisibility(View.INVISIBLE);
      mServerListView.setVisibility(View.INVISIBLE);
      mAccountBtn.setSelected(true);
      mServerBtn.setSelected(false);

      Log.i(TAG, "server: " + mSetting.getCurrentServer());
      Log.i(TAG, "remember me: " + mSetting.isRememberMeEnabled());
      if (mSetting.getCurrentServer()!=null) Log.i(TAG, "server: " + mSetting.getCurrentServer().serverUrl);
      Log.i(TAG, "user: " + mSetting.getUsername());
      if (mSetting.getCurrentServer() != null) {
        mUserEditTxt.setText(mSetting.isRememberMeEnabled() ? mSetting.getUsername(): username);
        mPassEditTxt.setText(mSetting.isRememberMeEnabled() ? mSetting.getPassword(): password);
      }
    }
    else {
      Log.i(TAG, "switch to server panel");
      mAccountPanel.setVisibility(View.INVISIBLE);
      mServerPanel.setVisibility(View.VISIBLE);
      mServerListView.setVisibility(View.VISIBLE);
      mAccountBtn.setSelected(false);
      mServerBtn.setSelected(true);
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View rowView, int position, long id) {
    int selectedIdx = Integer.valueOf(mSetting.getDomainIndex());
    int firstVisiblePosition = parent.getFirstVisiblePosition();
    if ((firstVisiblePosition <= selectedIdx) && (selectedIdx <= parent.getLastVisiblePosition()))
      parent.getChildAt(selectedIdx - firstVisiblePosition)
          .findViewById(R.id.ImageView_Checked)
          .setBackgroundResource(R.drawable.authenticate_checkmark_off);

    rowView.findViewById(R.id.ImageView_Checked)
        .setBackgroundResource(R.drawable.authenticate_checkmark_on);
    ArrayList<ServerObjInfo> serverList = ServerSettingHelper.getInstance().getServerInfoList();
    mSetting.setDomainIndex(String.valueOf(position));
    mSetting.setCurrentServer(serverList.get(position));
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
    mUserEditTxt.setHint(userNameHint);
    mUserEditTxt.setText( (username==null || username.equals(""))
        && mSetting.isRememberMeEnabled()
        ? mSetting.getUsername(): username);

    mPassEditTxt.setHint(passWordHint);
    mPassEditTxt.setText( (password==null || password.equals(""))
        && mSetting.isRememberMeEnabled()
        ? mSetting.getPassword():password);

    mLogInBtn.setText(strSignIn);
  }

  private void onLogin() {
    username = mUserEditTxt.getText().toString();
    password = mPassEditTxt.getText().toString();
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
    if (view.equals(mLogInBtn))   onLogin();
    if (view.equals(mServerBtn))  switchPanel(SERVER_PANEL);
    if (view.equals(mAccountBtn)) switchPanel(ACCOUNT_PANEL);
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
