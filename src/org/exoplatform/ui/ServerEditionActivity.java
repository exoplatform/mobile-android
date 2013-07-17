package org.exoplatform.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.exoplatform.R;
import org.exoplatform.controller.setting.SettingController;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.utils.ExoConstants;

/**
 * This screen is used to add new server or modify existing server<br/>
 *
 * Requires setting, adding_server
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

  private AccountSetting    mSetting;

  private SettingController mSettingController;

  private static final String TAG = "eXoServerEditionActivity";

  public void onCreate(Bundle savedInstanceState) {
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
    mSetting = mIntent.getParcelableExtra(ExoConstants.ACCOUNT_SETTING);
    if (mSetting == null) mSetting = AccountSetting.getInstance();
    mIsAddingServer = mIntent.getBooleanExtra(ExoConstants.SETTING_ADDING_SERVER, true);
    if (mIsAddingServer) initAddingServer();
    else initModifyingServer();
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

    String user   = mSetting.getUsername();
    String pass   = mSetting.getPassword();

    mServerNameEditTxt.setText(mSetting.getServerName());
    mServerUrlEditTxt.setText(mSetting.getDomainName());
    mUserEditTxt.setText(user!=null? user: "");
    mPassEditTxt.setText(pass!=null? pass: "");
  }

  /**
   * Return to setting screen
   *
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

        mSettingController.onDelete(Integer.valueOf(mSetting.getDomainIndex()));
        returnToSetting();
      }
    };
  }


  private View.OnClickListener onUpdateServer() {
    return new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Log.i(TAG, "onUpdateServer");

        ServerObjInfo serverObj = mSetting.getCurrentServer().clone();
        serverObj.serverUrl  = mServerUrlEditTxt.getText().toString();
        serverObj.serverName = mServerNameEditTxt.getText().toString();
        serverObj.username   = mUserEditTxt.getText().toString();
        serverObj.password   = mPassEditTxt.getText().toString();
        mSettingController.onUpdate(serverObj, Integer.valueOf(mSetting.getDomainIndex()));

        returnToSetting();
      }
    };
  }

  private View.OnClickListener onAddServer() {
    return new View.OnClickListener() {

      @Override
      public void onClick(View view) {
        Log.i(TAG, "onAddServer");

        ServerObjInfo serverObj = new ServerObjInfo();
        serverObj.serverUrl  = mServerUrlEditTxt.getText().toString();
        serverObj.serverName = mServerNameEditTxt.getText().toString();
        serverObj.username   = mUserEditTxt.getText().toString();
        serverObj.password   = mPassEditTxt.getText().toString();
        mSettingController.onAdd(serverObj);

        returnToSetting();
      }
    };
  }


}