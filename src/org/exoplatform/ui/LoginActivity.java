package org.exoplatform.ui;

import org.exoplatform.R;
import org.exoplatform.controller.login.LaunchController;
import org.exoplatform.controller.login.LoginController;
import org.exoplatform.controller.login.ServerAdapter;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

  private String            strSignIn;

  private String            settingText;

  private String            userNameHint;

  private String            passWordHint;

  private String            username;

  private String            password;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    this.setContentView(R.layout.login);
    new LaunchController(this, sharedPreference);
    init();
  }

  @Override
  protected void onResume() {
    super.onResume();
    changeLanguage();
    setServerAdapter();
  }

  private void init() {

    _imageAccount = (ImageView) findViewById(R.id.Image_Account);
    _imageServer = (ImageView) findViewById(R.id.Image_Server);

    _edtxUserName = (EditText) findViewById(R.id.EditText_UserName);
    _edtxUserName.setText(AccountSetting.getInstance().getUsername());

    _edtxPassword = (EditText) findViewById(R.id.EditText_Password);
    _edtxPassword.setText(AccountSetting.getInstance().getPassword());

    _btnAccount = (Button) findViewById(R.id.Button_Account);
    _btnAccount.setOnClickListener(this);
    _btnServer = (Button) findViewById(R.id.Button_Server);
    _btnServer.setOnClickListener(this);
    _btnLogIn = (Button) findViewById(R.id.Button_Login);
    _btnLogIn.setOnClickListener(this);
    _listViewServer = (ListView) findViewById(R.id.ListView_Servers);
    _listViewServer.setVisibility(View.INVISIBLE);
    _listViewServer.setCacheColorHint(Color.TRANSPARENT);
    _listViewServer.setFadingEdgeLength(0);
    _listViewServer.setDivider(null);
    _listViewServer.setDividerHeight(1);
    changeLanguage();
    setServerAdapter();

  }

  private void setServerAdapter() {
    _listViewServer.setAdapter(new ServerAdapter(this, _listViewServer));
  }

  private void onLogin() {
    username = _edtxUserName.getText().toString();
    password = _edtxPassword.getText().toString();

    ExoDocumentUtils.repositoryHomeURL = null;

    new LoginController(this, username, password);

  }

  public void changeLanguage() {
    LocalizationHelper local = LocalizationHelper.getInstance();
    strSignIn = local.getString("SignInButton");
    settingText = local.getString("Settings");
    userNameHint = local.getString("UserNameCellTitle");
    _edtxUserName.setHint(userNameHint);
    passWordHint = local.getString("PasswordCellTitle");
    _edtxPassword.setHint(passWordHint);
    _btnLogIn.setText(strSignIn);
  }

  public boolean onCreateOptionsMenu(Menu menu) {

    menu.add(0, 1, 0, settingText).setIcon(R.drawable.optionsettingsbutton);

    return true;

  }

  public boolean onOptionsItemSelected(MenuItem item) {

    int selectedItemIndex = item.getItemId();

    if (selectedItemIndex == 1) {
      Intent next = new Intent(LoginActivity.this, SettingActivity.class);
      next.putExtra(ExoConstants.SETTING_TYPE, 0);
      startActivity(next);
    }
    return false;
  }

  // @Override
  public void onClick(View view) {
    if (view == _btnLogIn) {
      onLogin();
    }

    if (view == _btnServer) {
      view.setBackgroundResource(R.drawable.authenticatepanelbuttonbgon);
      _imageServer.setBackgroundResource(R.drawable.authenticateserversiconiphoneon);
      _btnAccount.setBackgroundResource(R.drawable.authenticatepanelbuttonbgoff);
      _imageAccount.setBackgroundResource(R.drawable.authenticate_credentials_icon_off);
      _edtxUserName.setVisibility(View.INVISIBLE);
      _edtxPassword.setVisibility(View.INVISIBLE);
      _btnLogIn.setVisibility(View.INVISIBLE);
      _listViewServer.setVisibility(View.VISIBLE);
    }
    if (view == _btnAccount) {
      view.setBackgroundResource(R.drawable.authenticatepanelbuttonbgon);
      _imageAccount.setBackgroundResource(R.drawable.authenticate_credentials_icon_on);
      _btnServer.setBackgroundResource(R.drawable.authenticatepanelbuttonbgoff);
      _imageServer.setBackgroundResource(R.drawable.authenticateserversiconiphoneoff);
      _edtxUserName.setVisibility(View.VISIBLE);
      _edtxPassword.setVisibility(View.VISIBLE);
      _btnLogIn.setVisibility(View.VISIBLE);
      _listViewServer.setVisibility(View.INVISIBLE);
    }
  }

}
