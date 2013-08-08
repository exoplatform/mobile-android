package org.exoplatform.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import org.exoplatform.R;
import org.exoplatform.controller.setting.SettingController;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;

/**
 * This screen is used to add new server or modify existing server<br/>
 *
 */
public class ServerEditionActivity extends Activity {

  private Intent   mIntent;

  private TextView mTitleTxt;

  private EditText mServerNameEditTxt;

  private EditText mServerUrlEditTxt;

  private EditText mUserEditTxt;

  private EditText mPassEditTxt;

  private Button   mOkBtn;

  private Button   mDeleteBtn;

  private boolean  mIsAddingServer;

  private ServerObjInfo mServerObj;

  private int           mServerIdx;

  private AccountSetting    mSetting;

  private SettingController mSettingController;

  private Handler mHandler = new Handler();

  private static final String TAG = "eXoServerEditionActivity";

  public void onCreate(Bundle savedInstanceState) {
    requestScreenOrientation();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.server_edition);

    // we use extra passed along with Intent to specify: add server or modify one
    mTitleTxt = (TextView) findViewById(R.id.server_setting_title_txt);
    mSettingController = SettingController.getInstance();
    mServerNameEditTxt = (EditText) findViewById(R.id.server_setting_server_name_edit_txt);
    mServerUrlEditTxt  = (EditText) findViewById(R.id.server_setting_server_url_edit_txt);
    mUserEditTxt       = (EditText) findViewById(R.id.server_setting_user_edit_txt);
    mPassEditTxt       = (EditText) findViewById(R.id.server_setting_pass_edit_txt);
    mOkBtn             = (Button) findViewById(R.id.server_setting_ok_btn);
    mDeleteBtn         = (Button) findViewById(R.id.server_setting_delete_btn);

    /* allowing to click on OK only if server name and server url are inputted */
    mOkBtn.setEnabled(false);
    TextWatcher watcher = onServerNameOrServerUrlChanged();
    mServerNameEditTxt.addTextChangedListener(watcher);
    mServerUrlEditTxt.addTextChangedListener(watcher);
    mPassEditTxt.setTypeface(Typeface.SANS_SERIF);

    /* change the title */
    mIntent  = getIntent();
    mServerObj = mIntent.getParcelableExtra(ExoConstants.EXO_SERVER_OBJ);
    mServerIdx = ServerSettingHelper.getInstance().getServerInfoList().indexOf(mServerObj);
    mSetting = AccountSetting.getInstance();
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

  private TextWatcher onServerNameOrServerUrlChanged() {
    return new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

      public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        Log.i(TAG, "onServerNameOrServerUrlChanged");

        String name = mServerNameEditTxt.getText().toString();
        String url  = mServerUrlEditTxt.getText().toString();

        if (name.isEmpty() || url.isEmpty()) {
          mOkBtn.setEnabled(false);
          return;
        }

        mOkBtn.setEnabled(true);
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
  private void returnToSetting() {
    Intent next = new Intent(this, SettingActivity.class);
    next.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    next.putExtra(ExoConstants.SETTING_TYPE,
        mIntent.getIntExtra(ExoConstants.SETTING_TYPE, SettingActivity.GLOBAL_TYPE));
    startActivity(next);
  }

  private View.OnClickListener onDeleteServer() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(TAG, "onDeleteServer");

        mSettingController.onDelete(mServerIdx);
        returnToSetting();
      }
    };
  }

  private View.OnClickListener onUpdateServer() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(TAG, "onUpdateServer");

        if (!checkServerUrl(view)) return;
        mSettingController.onUpdate(retrieveInput(), mServerIdx);

        returnToSetting();
      }
    };
  }

  private View.OnClickListener onAddServer() {
    return new View.OnClickListener() {

      @Override
      public void onClick(View view) {
        Log.i(TAG, "onAddServer");

        if (!checkServerUrl(view)) return;
        mSettingController.onAdd(retrieveInput());

        returnToSetting();
      }
    };
  }

  private boolean checkServerUrl(View view) {
    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
    if (inputMethodManager!= null) inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);

    String url = mServerUrlEditTxt.getText().toString();
    if (!url.startsWith(ExoConstants.HTTP_PROTOCOL)) url = ExoConstants.HTTP_PROTOCOL + "://" + url;
    if (!ExoConnectionUtils.validateUrl(url)) {

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

    return true;
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