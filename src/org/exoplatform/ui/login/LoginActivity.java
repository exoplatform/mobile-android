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
package org.exoplatform.ui.login;


import android.content.SharedPreferences;
import android.view.*;
import android.widget.*;
import org.exoplatform.R;
import org.exoplatform.utils.LaunchUtils;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.ui.setting.SettingActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SettingUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View.OnClickListener;

import java.util.ArrayList;

/**
 * Represents screen for authentication
 * The screen contains 2 panels: an account panel allows user to enter credentials
 * and a server panel to select a server to connect to.
 *
 * It also contain 2 buttons to switch between panels
 */
public class LoginActivity extends Activity implements
    AccountPanel.ViewListener,
    OnClickListener,
    LoginProxy.ProxyListener {

  private AccountSetting    mSetting;

  private Resources         mResources;

  /**=== Components ===**/
  private ImageView         mAccountBtn;

  private ImageView         mServerBtn;

  private ServerPanel       mServerPanel;

  private AccountPanel      mAccountPanel;

  private LoginProxy        mLoginProxy;

  /** Default is set to show account panel */
  private String            mPanelMode     = ACCOUNT_PANEL;

  public static final String ACCOUNT_PANEL = "ACCOUNT_PANEL";

  public static final String SERVER_PANEL  = "SERVER_PANEL";


  /**=== Constants ===**/
  public static final String PANEL_MODE    = "PANEL_MODE";

  private static final String TAG = "eXoLoginActivity";

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.login);
    mResources = getResources();
    mSetting   = AccountSetting.getInstance();
    initSubViews();

    /* launch app from custom url - need to init setting */
    boolean isLaunchedFromUrl = (getIntent().getData() != null);
    if (isLaunchedFromUrl) {
      new LaunchUtils(this);
      setUpUserAndServerFromUrl();
    }

    /* restore previous saved state */
    if (savedInstanceState != null) {
      mPanelMode = savedInstanceState.getString(PANEL_MODE);
      mAccountPanel.onRestoreState(savedInstanceState);
    }
  }

  /**
   * Any changes in state should be updated here
   */
  @Override
  protected void onResume() {
    super.onResume();
    SettingUtils.setDefaultLanguage(this);
    onChangeLanguage();
    initState();
  }


  private void initSubViews() {
    /* init account panel */
    mAccountPanel = (AccountPanel) findViewById(R.id.login_account_panel);
    mAccountPanel.setViewListener(this);

    /* init server panel */
    mServerPanel = (ServerPanel) findViewById(R.id.login_server_panel);

    /* init button */
    mAccountBtn   = (ImageView) findViewById(R.id.login_account_btn);
    mAccountBtn.setOnClickListener(this);
    mServerBtn    = (ImageView) findViewById(R.id.login_server_btn);
    mServerBtn.setOnClickListener(this);
  }
  
  private void hideSwitchPanelIfOneAccountOnlyExists()
  {
	if (ServerSettingHelper.getInstance().twoOrMoreAccountsExist(this))
	{
		mServerBtn.setVisibility(View.VISIBLE);
	} 
	else 
	{
		mServerBtn.setVisibility(View.INVISIBLE);
		mPanelMode = ACCOUNT_PANEL; // make sure the panel with credential fields is displayed
	}
  }

  private void initState() {
	hideSwitchPanelIfOneAccountOnlyExists();
    switchPanel(mPanelMode);

    /* update new server list */
    mServerPanel.repopulateServerList();
  }

  @Override
  protected void onSaveInstanceState(Bundle saveState) {
    saveState.putString(PANEL_MODE, mPanelMode);

    mAccountPanel.onSaveState(saveState);
  }

  /**
   * Switch between 2 panel mode
   *
   * @param panel
   */
  private void switchPanel(String panel) {
    mPanelMode = panel;

    if (mPanelMode.equals(ACCOUNT_PANEL)) {
      Log.i(TAG, "switch to account panel");
      mAccountPanel.turnOn();
      mServerPanel.turnOff();
      mAccountBtn.setSelected(true);
      mServerBtn.setSelected(false);
    }
    else {
      Log.i(TAG, "switch to server panel");
      mAccountPanel.turnOff();
      mServerPanel.turnOn();
      mAccountBtn.setSelected(false);
      mServerBtn.setSelected(true);
    }
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

    ArrayList<ServerObjInfo> serverList = ServerSettingHelper.getInstance().getServerInfoList(this);
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

  public void onChangeLanguage() {
    mAccountPanel.onChangeLanguage();
  }

  @Override
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    menu.add(0, 1, 0, mResources.getString(R.string.Settings))
        .setIcon(R.drawable.optionsettingsbutton);
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
    if (view.equals(mServerBtn))  switchPanel(SERVER_PANEL);
    if (view.equals(mAccountBtn)) switchPanel(ACCOUNT_PANEL);
  }

  @Override
  protected void onPause(){
    super.onPause();

    if (!mSetting.getDomainIndex().equals("-1")) {
      SharedPreferences.Editor editor = getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0).edit();
      editor.putString(ExoConstants.EXO_PRF_DOMAIN_INDEX, mSetting.getDomainIndex());
      editor.commit();
    }
  }

  @Override
  public void onBackPressed() {
    setResult(RESULT_CANCELED);
    finish();
  }

  @Override
  public void onClickLogin(String username, String password) {
    if (mSetting.getCurrentServer() == null) {
      Toast toast = Toast.makeText(this, R.string.NoServerSelected, Toast.LENGTH_LONG);
      toast.setGravity(Gravity.CENTER, 0, 0);
      toast.show();
      return ;
    }

    /** performs login */
    Bundle loginData = new Bundle();
    loginData.putString(LoginProxy.USERNAME, username);
    loginData.putString(LoginProxy.PASSWORD, password);
    loginData.putString(LoginProxy.DOMAIN  , mSetting.getDomainName());

    mLoginProxy = new LoginProxy(this, LoginProxy.WITH_EXISTING_ACCOUNT, loginData);
    mLoginProxy.setListener(this);
    mLoginProxy.performLogin();
  }

  @Override
  public void onLoginFinished(boolean result) {
    if (!result) return ;
    Intent next = new Intent(this, HomeActivity.class);
    //next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(next);
  }
}
