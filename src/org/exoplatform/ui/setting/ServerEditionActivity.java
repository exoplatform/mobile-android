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
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConnectionUtils;
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
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This screen is used to add new server or modify existing server<br/>
 *
 */
public class ServerEditionActivity extends Activity {

  private TextView mTitleTxt;

  private EditText mServerNameEditTxt;

  private EditText mServerUrlEditTxt;

  private EditText mUserEditTxt;

  private EditText mPassEditTxt;

  private Button   mOkBtn;

  private Button   mDeleteBtn;

  private boolean  mIsAddingServer;

  private ServerObjInfo    mServerObj;

  private int              mServerIdx;

  private Intent           mIntent;

  private AccountSetting   mSetting;

  private Resources        mResources;

  private Handler          mHandler = new Handler();

  private Animation        mEnabledAnim;

  private Animation        mDisabledAnim;

  /**=== Constants ===**/
  public static final String  SETTING_OPERATION  = "SETTING_OPERATION";

  public static final int     SETTING_ADD        = 0;
  public static final int     SETTING_UPDATE     = 1;
  public static final int     SETTING_DELETE     = 2;
  public static final String  SERVER_IDX         = "SERVER_IDX";

  private static final String TAG = "eXoServerEditionActivity";

  public void onCreate(Bundle savedInstanceState) {
    requestScreenOrientation();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.server_edition);

    initAnimations();
    mResources = getResources();

    // we use extra passed along with Intent to specify: add server or modify one
    mTitleTxt = (TextView) findViewById(R.id.server_setting_title_txt);
    mServerNameEditTxt = (EditText) findViewById(R.id.server_setting_server_name_edit_txt);
    mServerUrlEditTxt  = (EditText) findViewById(R.id.server_setting_server_url_edit_txt);
    mUserEditTxt       = (EditText) findViewById(R.id.server_setting_user_edit_txt);
    mPassEditTxt       = (EditText) findViewById(R.id.server_setting_pass_edit_txt);
    mOkBtn             = (Button) findViewById(R.id.server_setting_ok_btn);
    mDeleteBtn         = (Button) findViewById(R.id.server_setting_delete_btn);

    /* allowing to click on OK only if server name and server url are inputted */
    mOkBtn.setEnabled(false);
    mOkBtn.startAnimation(mDisabledAnim);
    TextWatcher watcher = onServerNameOrServerUrlChanged();
    mServerNameEditTxt.addTextChangedListener(watcher);
    mServerUrlEditTxt.addTextChangedListener(watcher);
    mPassEditTxt.setTypeface(Typeface.SANS_SERIF);

    /* change the title */
    mIntent    = getIntent();
    mServerObj = mIntent.getParcelableExtra(ExoConstants.EXO_SERVER_OBJ);
    mServerIdx = ServerSettingHelper.getInstance().getServerInfoList(this).indexOf(mServerObj);
    mSetting   = AccountSetting.getInstance();
    mIsAddingServer = mIntent.getBooleanExtra(ExoConstants.SETTING_ADDING_SERVER, true);

    if (mIsAddingServer) initAddingServer();
    else initModifyingServer();
  }

  /**
   * Force screen orientation for small and medium size devices
   */
  private void requestScreenOrientation() {

    int size = getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK;

    switch (size) {
      case Configuration.SCREENLAYOUT_SIZE_NORMAL:  // 320x470 dp units
        Log.i(TAG, "normal");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        break;
      case Configuration.SCREENLAYOUT_SIZE_SMALL:   // 320x426 dp units
        Log.i(TAG, "small");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        break;
      case Configuration.SCREENLAYOUT_SIZE_LARGE:   // 480x640 dp units
        Log.i(TAG, "large");
        break;
      case Configuration.SCREENLAYOUT_SIZE_XLARGE:  // 720x960 dp units
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
      public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

      public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        String name = mServerNameEditTxt.getText().toString();
        String url  = mServerUrlEditTxt.getText().toString();

        if (name.isEmpty() || url.isEmpty()) {
          mOkBtn.setEnabled(false);
          mOkBtn.startAnimation(mDisabledAnim);
          return;
        }

        mOkBtn.setEnabled(true);
        mOkBtn.startAnimation(mEnabledAnim);

        if (mIsAddingServer) mOkBtn.setOnClickListener(onAddServer());
        else mOkBtn.setOnClickListener(onUpdateServer());
      }

      @Override
      public void afterTextChanged(Editable editable) { }
    };
  }

  private void initAddingServer() {
    Log.i(TAG, "initAddingServer");

    mTitleTxt.setText(R.string.AddServer);
    mDeleteBtn.setVisibility(View.INVISIBLE);
  }

  private void initModifyingServer() {
    Log.i(TAG, "initModifyingServer");

    mTitleTxt.setText(R.string.ModifyServer);
    mDeleteBtn.setVisibility(View.VISIBLE);
    mDeleteBtn.setOnClickListener(onDeleteServer());

    mServerNameEditTxt.setText(mServerObj.serverName);
    mServerUrlEditTxt.setText(mServerObj.serverUrl);
    mUserEditTxt.setText(mServerObj.username);
    mPassEditTxt.setText(mServerObj.password);
  }

  /**
   * Return to setting screen
   */
  private void returnToSetting(int operation, int serverIdx) {
    Intent next = new Intent(this, SettingActivity.class);
    next.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    next.putExtra(ExoConstants.SETTING_TYPE,
        mIntent.getIntExtra(ExoConstants.SETTING_TYPE, SettingActivity.GLOBAL_TYPE));
    next.putExtra(SETTING_OPERATION, operation);
    next.putExtra(SERVER_IDX, serverIdx);
    startActivity(next);
  }

  private View.OnClickListener onDeleteServer() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        int selectedServerIndex = Integer.parseInt(mSetting.getDomainIndex());
        if (mServerIdx < selectedServerIndex)
          mSetting.setDomainIndex(String.valueOf(selectedServerIndex - 1));

        List<ServerObjInfo> listServer = ServerSettingHelper.getInstance().getServerInfoList(ServerEditionActivity.this);
        listServer.remove(mServerIdx);
        onSave();
        Toast.makeText(ServerEditionActivity.this,
            mResources.getString(R.string.ServerDeleted), Toast.LENGTH_SHORT).show();
        returnToSetting(SETTING_DELETE, mServerIdx);
      }
    };
  }

  private View.OnClickListener onUpdateServer() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {

        if (!checkServerUrl(view)) return;
        ServerObjInfo myServerObj = retrieveInput();
        if (!isServerValid(myServerObj)) return ;

        List<ServerObjInfo> listServer = ServerSettingHelper.getInstance().getServerInfoList(ServerEditionActivity.this);
        /* check whether server is duplicated with other server */
        int serverIdx = listServer.indexOf(myServerObj);
        if ((serverIdx != mServerIdx) && (serverIdx != -1)) {
          Toast.makeText(ServerEditionActivity.this, mResources.getString(R.string.WarningServerUrlAndUserAlreadyExist)
              , Toast.LENGTH_SHORT).show();
          return ;
        }

        listServer.remove(mServerIdx);
        listServer.add(mServerIdx, myServerObj);
        onSave();
        Toast.makeText(ServerEditionActivity.this,
            mResources.getString(R.string.ServerUpdated), Toast.LENGTH_SHORT).show();
        returnToSetting(SETTING_UPDATE, mServerIdx);
      }
    };
  }

  private View.OnClickListener onAddServer() {
    return new View.OnClickListener() {

      @Override
      public void onClick(View view) {

        if (!checkServerUrl(view)) return;
        ServerObjInfo myServerObj = retrieveInput();
        if (!isServerValid(myServerObj)) return ;

        List<ServerObjInfo> listServer = ServerSettingHelper.getInstance().getServerInfoList(ServerEditionActivity.this);
        if (listServer.contains(myServerObj)) {
          Toast.makeText(ServerEditionActivity.this,
              mResources.getString(R.string.WarningServerUrlAndUserAlreadyExist), Toast.LENGTH_SHORT).show();
          return ;
        }

        listServer.add(myServerObj);
        onSave();
        Toast.makeText(ServerEditionActivity.this,
            mResources.getString(R.string.ServerAdded), Toast.LENGTH_SHORT).show();
        returnToSetting(SETTING_ADD, listServer.size() - 1);
      }
    };
  }

  private boolean checkServerUrl(View view) {
    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
    if (inputMethodManager!= null) inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

    String url = mServerUrlEditTxt.getText().toString();
    if (!url.startsWith(ExoConnectionUtils.HTTP) && !url.startsWith(ExoConnectionUtils.HTTPS))
      url = ExoConnectionUtils.HTTP + url;
    if (!ExoUtils.isUrlValid(url) || ExoUtils.urlHasWrongTenant(url)) {

      if (inputMethodManager == null)
        Toast.makeText(ServerEditionActivity.this, R.string.ServerInvalid, Toast.LENGTH_SHORT).show();
      else
        mHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            Toast.makeText(ServerEditionActivity.this, R.string.ServerInvalid, Toast.LENGTH_SHORT).show();
          }
        }, 500);

      return false;
    }

    mServerUrlEditTxt.setText(url);
    return true;
  }

  /**
   * Check whether server information is valid
   *
   * @param myServerObj
   * @return
   */
  private boolean isServerValid(ServerObjInfo myServerObj) {
    boolean isValid = true;
    
    // neither server name, URL nor username can be empty
    if (myServerObj.serverName == null ||       myServerObj.serverUrl == null ||       myServerObj.username == null ||
        myServerObj.serverName.length() == 0 || myServerObj.serverUrl.length() == 0 || myServerObj.username.length() == 0) {
      Toast.makeText(this, mResources.getString(R.string.WarningServerNameIsEmpty), Toast.LENGTH_SHORT).show();
      isValid = false;
    }
    // server name must not contain special characters
    if (!ExoUtils.isServerNameValid(myServerObj.serverName)) {
      Toast.makeText(this, mResources.getString(R.string.SpecialCharacters), Toast.LENGTH_SHORT).show();
      isValid = false;
    }
    // server URL must be a valid URL
    myServerObj.serverUrl = ExoUtils.stripUrl(myServerObj.serverUrl);
    if (!ExoUtils.isUrlValid(myServerObj.serverUrl)) {
      Toast.makeText(this, mResources.getString(R.string.ServerInvalid), Toast.LENGTH_SHORT).show();
      isValid = false;
    }
    
    return isValid;
  }

  /**
   * Save the list of servers on disk, in an XML file.
   * Keep the list in memory in ServerSettingHelper
   */
  private void onSave() {
    ArrayList<ServerObjInfo> listServer = ServerSettingHelper.getInstance().getServerInfoList(this);
    ServerConfigurationUtils.generateXmlFileWithServerList(this,
        listServer, ExoConstants.EXO_SERVER_SETTING_FILE, "");
    ServerSettingHelper.getInstance().setServerInfoList(listServer);
  }

  private ServerObjInfo retrieveInput() {
    ServerObjInfo serverObj = new ServerObjInfo();
    serverObj.serverUrl  = mServerUrlEditTxt.getText().toString();
    serverObj.serverName = mServerNameEditTxt.getText().toString();
    serverObj.username   = mUserEditTxt.getText().toString();
    serverObj.password   = mPassEditTxt.getText().toString();
    return serverObj;
  }

}