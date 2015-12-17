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
package org.exoplatform.ui.setting;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.R;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.login.LoginProxy;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoUtils;
import org.exoplatform.utils.ServerConfigurationUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This screen is used to delete or modify an existing account
 */
public class ServerEditionActivity extends Activity {

  private EditText            mServerNameEditTxt;

  private EditText            mServerUrlEditTxt;

  private EditText            mUserEditTxt;

  private EditText            mPassEditTxt;

  private Button              mOkBtn;

  private Button              mDeleteBtn;

  private ExoAccount          mAccountObj;

  private int                 mServerIdx;

  private boolean             mIsEditDisabled;

  private Intent              mIntent;

  private AccountSetting      mSetting;

  private Resources           mResources;

  private Animation           mEnabledAnim;

  private Animation           mDisabledAnim;

  /** === Constants === **/
  public static final String  SETTING_OPERATION = "SETTING_OPERATION";

  public static final int     SETTING_UPDATE    = 1;

  public static final int     SETTING_DELETE    = 2;

  public static final String  SERVER_IDX        = "SERVER_IDX";

  private static final String TAG               = ServerEditionActivity.class.getName();

  public void onCreate(Bundle savedInstanceState) {
    requestScreenOrientation();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.server_edition);

    initAnimations();
    mResources = getResources();

    // we use extra passed along with Intent to specify: add server or
    // modify one
    mServerNameEditTxt = (EditText) findViewById(R.id.server_setting_server_name_edit_txt);
    mServerUrlEditTxt = (EditText) findViewById(R.id.server_setting_server_url_edit_txt);
    mUserEditTxt = (EditText) findViewById(R.id.server_setting_user_edit_txt);
    mPassEditTxt = (EditText) findViewById(R.id.server_setting_pass_edit_txt);
    mOkBtn = (Button) findViewById(R.id.server_setting_ok_btn);
    mDeleteBtn = (Button) findViewById(R.id.server_setting_delete_btn);

    /*
     * allowing to click on OK only if server name and server url are inputted
     */
    mOkBtn.setEnabled(false);
    mOkBtn.startAnimation(mDisabledAnim);
    TextWatcher watcher = onServerNameOrServerUrlChanged();
    mServerNameEditTxt.addTextChangedListener(watcher);
    mServerUrlEditTxt.addTextChangedListener(watcher);
    mPassEditTxt.setTypeface(Typeface.SANS_SERIF);

    /* change the title */
    mIntent = getIntent();
    mAccountObj = mIntent.getParcelableExtra(ExoConstants.EXO_SERVER_OBJ);
    mSetting = AccountSetting.getInstance();
    if (mAccountObj != null) {
      mServerIdx = ServerSettingHelper.getInstance().getServerInfoList(this).indexOf(mAccountObj);
      // edit is disabled if the current account is the selected account,
      // and we are logged-in
      mIsEditDisabled = (Integer.valueOf(mSetting.getDomainIndex()) == mServerIdx && LoginProxy.userIsLoggedIn);
      initScreen();
    }
  }

  /**
   * Force screen orientation for small and medium size devices
   */
  private void requestScreenOrientation() {

    int size = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;

    switch (size) {
    case Configuration.SCREENLAYOUT_SIZE_NORMAL: // 320x470 dp units
      Log.i(TAG, "normal");
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      break;
    case Configuration.SCREENLAYOUT_SIZE_SMALL: // 320x426 dp units
      Log.i(TAG, "small");
      setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
      break;
    case Configuration.SCREENLAYOUT_SIZE_LARGE: // 480x640 dp units
      Log.i(TAG, "large");
      break;
    case Configuration.SCREENLAYOUT_SIZE_XLARGE: // 720x960 dp units
      Log.i(TAG, "xlarge");
      break;
    default:
      break;
    }
  }

  private void initAnimations() {
    mEnabledAnim = new AlphaAnimation(1.0f, 1.0f);
    mEnabledAnim.setDuration(0);
    mEnabledAnim.setFillAfter(true);

    mDisabledAnim = new AlphaAnimation(0.4f, 0.4f);
    mDisabledAnim.setDuration(0);
    mDisabledAnim.setFillAfter(true);
  }

  private TextWatcher onServerNameOrServerUrlChanged() {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
      }

      public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        String name = mServerNameEditTxt.getText().toString();
        String url = mServerUrlEditTxt.getText().toString();

        if (name.isEmpty() || url.isEmpty()) {
          mOkBtn.setEnabled(false);
          mOkBtn.startAnimation(mDisabledAnim);
          return;
        }

        mOkBtn.setEnabled(true);
        mOkBtn.startAnimation(mEnabledAnim);

        mOkBtn.setOnClickListener(onUpdateServer());
      }

      @Override
      public void afterTextChanged(Editable editable) {
      }
    };
  }

  private void initScreen() {
    Log.i(TAG, "init Edit Account");

    setTitle(mAccountObj.accountName);
    mDeleteBtn.setVisibility(View.VISIBLE);
    mDeleteBtn.setOnClickListener(onDeleteServer());

    mServerNameEditTxt.setText(mAccountObj.accountName);
    mServerUrlEditTxt.setText(mAccountObj.serverUrl);
    mUserEditTxt.setText(mAccountObj.username);
    mPassEditTxt.setText(mAccountObj.password);

    // set text fields enabled/disabled
    mServerUrlEditTxt.setEnabled(!mIsEditDisabled);
    mUserEditTxt.setEnabled(!mIsEditDisabled);
    mPassEditTxt.setEnabled(!mIsEditDisabled);
    if (mIsEditDisabled) {
      // remove the delete button if edit is disabled for this account
      mDeleteBtn.setVisibility(View.GONE);
    }
  }

  /**
   * Return to setting screen
   */
  private void returnToSetting(int operation, int serverIdx) {
    Intent next = new Intent(this, SettingActivity.class);
    next.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    next.putExtra(ExoConstants.SETTING_TYPE, mIntent.getIntExtra(ExoConstants.SETTING_TYPE, SettingActivity.GLOBAL_TYPE));
    next.putExtra(SETTING_OPERATION, operation);
    next.putExtra(SERVER_IDX, serverIdx);
    startActivity(next);
  }

  private View.OnClickListener onDeleteServer() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        // decrease the selected account domain by 1 if the deleted
        // account is before in the list
        int selectedServerIndex = Integer.parseInt(mSetting.getDomainIndex());
        if (mServerIdx < selectedServerIndex)
          mSetting.setDomainIndex(String.valueOf(selectedServerIndex - 1));

        List<ExoAccount> listServer = ServerSettingHelper.getInstance().getServerInfoList(ServerEditionActivity.this);
        listServer.remove(mServerIdx);

        // remove current account setting if the last account was just
        // deleted
        if (listServer.isEmpty()) {
          mSetting.setCurrentAccount(null);
        } else {
          // otherwise, check if there is only one remaining account
          // and if yes select it
          autoSelectLastAccount();
        }
        onSave();
        Toast.makeText(ServerEditionActivity.this, mResources.getString(R.string.ServerDeleted), Toast.LENGTH_SHORT).show();
        returnToSetting(SETTING_DELETE, mServerIdx);
      }
    };
  }

  private View.OnClickListener onUpdateServer() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        ExoAccount newAccount = retrieveInput();
        if (!isServerValid(newAccount))
          return;

        List<ExoAccount> listServer = ServerSettingHelper.getInstance().getServerInfoList(ServerEditionActivity.this);
        /* check whether server is duplicated with other server */
        int serverIdx = listServer.indexOf(newAccount);
        if ((serverIdx != mServerIdx) && (serverIdx != -1)) {
          Toast.makeText(ServerEditionActivity.this, mResources.getString(R.string.WarningServerAlreadyExists), Toast.LENGTH_LONG)
               .show();
          return;
        }
        listServer.remove(mServerIdx);
        listServer.add(mServerIdx, newAccount);
        if (String.valueOf(mServerIdx).equals(AccountSetting.getInstance().getDomainIndex())) {
          // replaces the instance of the saved current server in
          // AccountSettings by the updated server
          AccountSetting.getInstance().setCurrentAccount(newAccount);
        }
        autoSelectLastAccount();
        onSave();
        Toast.makeText(ServerEditionActivity.this, mResources.getString(R.string.ServerUpdated), Toast.LENGTH_SHORT).show();
        returnToSetting(SETTING_UPDATE, mServerIdx);
      }
    };
  }

  /**
   * Check if only one account is currently configured in
   * ServerSettingHelper.getServerInfoList(). If yes, this server is
   * automatically selected as AccountSetting.setCurrentServer() and
   * AccountSetting.setDomainIndex(0).
   */
  private void autoSelectLastAccount() {
    List<ExoAccount> listAccounts = ServerSettingHelper.getInstance().getServerInfoList(ServerEditionActivity.this);
    if (listAccounts.size() == 1) {
      ExoAccount account = listAccounts.get(0);
      AccountSetting settings = AccountSetting.getInstance();
      settings.setCurrentAccount(account);
      settings.setDomainIndex(String.valueOf(0));
      Log.i(TAG, "Last account selected: " + account.accountName);
    }
  }

  /**
   * Check whether account information is valid
   * 
   * @param myServerObj
   * @return
   */
  private boolean isServerValid(ExoAccount myServerObj) {

    // Account name and server are mandatory
    if (myServerObj.accountName == null || myServerObj.serverUrl == null || myServerObj.accountName.length() == 0
        || myServerObj.serverUrl.length() == 0) {
      Toast.makeText(this, mResources.getString(R.string.WarningServerNameIsEmpty), Toast.LENGTH_LONG).show();
      return false;
    }
    // Account name must not contain special characters
    if (!ExoUtils.isServerNameValid(myServerObj.accountName)) {
      Toast.makeText(this, mResources.getString(R.string.AccountNameInvalid), Toast.LENGTH_LONG).show();
      return false;
    }
    // Account server URL must be a valid URL
    myServerObj.serverUrl = ExoUtils.stripUrl(myServerObj.serverUrl);
    if (!ExoUtils.isUrlValid(myServerObj.serverUrl)) {
      Toast.makeText(this, mResources.getString(R.string.AccountServerInvalid), Toast.LENGTH_LONG).show();
      return false;
    }
    // Account server URL cannot be a forbidden URL
    if (ExoUtils.isURLForbidden(myServerObj.serverUrl)) {
      Toast.makeText(this, mResources.getString(R.string.AccountServerForbidden), Toast.LENGTH_LONG).show();
      return false;
    }
    // Only alphanumeric characters and - _ . + are allowed in the username
    // (optional)
    if (myServerObj.username != null && myServerObj.username.length() > 0) {
      if (!ExoUtils.isUsernameValid(myServerObj.username)) {
        Toast.makeText(this, mResources.getString(R.string.AccountUsernameInvalid), Toast.LENGTH_LONG).show();
        return false;
      }
    }

    return true;
  }

  /**
   * Save the list of servers on disk, in an XML file. Keep the list in memory
   * in ServerSettingHelper
   */
  private void onSave() {
    ArrayList<ExoAccount> listServer = ServerSettingHelper.getInstance().getServerInfoList(this);
    ServerConfigurationUtils.generateXmlFileWithServerList(this, listServer, ExoConstants.EXO_SERVER_SETTING_FILE, "");
    ServerSettingHelper.getInstance().setServerInfoList(listServer);
  }

  /**
   * Puts all values from the text fields into the current Account object
   */
  private ExoAccount retrieveInput() {
    ExoAccount newAccount = mAccountObj.clone();
    newAccount.serverUrl = mServerUrlEditTxt.getText().toString().trim();
    newAccount.accountName = mServerNameEditTxt.getText().toString().trim();
    newAccount.username = mUserEditTxt.getText().toString().trim();
    newAccount.password = mPassEditTxt.getText().toString().trim();
    return newAccount;
  }

}
