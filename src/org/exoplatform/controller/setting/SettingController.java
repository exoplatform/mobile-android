package org.exoplatform.controller.setting;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.ServerEditionActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.ServerConfigurationUtils;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.utils.URLAnalyzer;
import org.exoplatform.widget.ServerEditionDialog;
import org.exoplatform.widget.ServerItemLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class SettingController {
  private Context                  mContext;

  private SharedPreferences        prefs;

  private ArrayList<ServerObjInfo> serverInfoList;

  private LinearLayout             listServerWrap;

  private int                      selectedServerIndex;

  private String                   serverIsEmpty;

  private String                   serverNameIsExisted;

  private String                   serverUrlIsExisted;

  private String                   serverNameURLInvalid;

  private static  SettingController _instance;

  private static final String TAG = "eXoSettingController";

  public SettingController(Context context, LinearLayout listServerWrap) {
    mContext = context;
    prefs = mContext.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);
    this.listServerWrap = listServerWrap;
    init();
    _instance = this;
  }

  public static SettingController getInstance() {
    return _instance;
  }

  public static void useInstance(SettingController instance) {
    _instance = instance;
  }

  public void initLocation(ImageView imgViewE, ImageView imgViewF) {
    String locallize = prefs.getString(ExoConstants.EXO_PRF_LOCALIZE,
                                       ExoConstants.ENGLISH_LOCALIZATION);
    ;
    if (locallize != null) {
      if (locallize.equalsIgnoreCase(ExoConstants.FRENCH_LOCALIZATION)) {
        setFrenchLocation(imgViewE, imgViewF);
      } else {
        setEnglishLocation(imgViewE, imgViewF);
      }
    } else {
      setEnglishLocation(imgViewE, imgViewF);
    }
  }

  public void initSocialFilter(ImageView socialChecked) {
    boolean isSocialFilter = prefs.getBoolean(AccountSetting.getInstance().socialKey, false);
    if (isSocialFilter) {
      socialChecked.setBackgroundResource(R.drawable.authenticate_checkmark_on);
    } else
      socialChecked.setBackgroundResource(R.drawable.authenticate_checkmark_off);

  }

  /**
   * Retrieve login setting from preferences
   */
  public void initLoginSetting(ImageView rememberMeImg, ImageView autoLoginImg) {
    boolean isRememberMeEnabled = prefs.getBoolean(ExoConstants.SETTING_REMEMBER_ME, false);
    boolean isAutoLoginEnabled  = prefs.getBoolean(ExoConstants.SETTING_AUTOLOGIN, false);
    Log.i(TAG, "init login setting - remember me: " + isRememberMeEnabled + " - auto login: " + isAutoLoginEnabled);

    final ImageView _rememberMeImg = rememberMeImg;
    final ImageView _autoLoginImg  = autoLoginImg;

    if (isRememberMeEnabled) {
      _autoLoginImg.setEnabled(true);
      _autoLoginImg.setOnClickListener(onClickAutoLogin());
    }
    else _autoLoginImg.setEnabled(false);

    toggleCheckBoxImage(_rememberMeImg, isRememberMeEnabled);
    toggleCheckBoxImage(_autoLoginImg, isAutoLoginEnabled);

    _rememberMeImg.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        boolean isRememberMeEnabled = !prefs.getBoolean(ExoConstants.SETTING_REMEMBER_ME, false);
        Log.i(TAG, "click remember me: " + isRememberMeEnabled);
        if (isRememberMeEnabled) {
          _autoLoginImg.setEnabled(true);
          _autoLoginImg.setOnClickListener(onClickAutoLogin());
        }
        else _autoLoginImg.setEnabled(false);

        toggleCheckBoxImage(_rememberMeImg, isRememberMeEnabled);
        Editor editor = prefs.edit();
        editor.putBoolean(ExoConstants.SETTING_REMEMBER_ME, isRememberMeEnabled);
        editor.commit();
      }
    });
  }

  private View.OnClickListener onClickAutoLogin() {
    return new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        boolean isAutoLoginEnabled = !prefs.getBoolean(ExoConstants.SETTING_AUTOLOGIN, false);
        Log.i(TAG, "click autologin: " + isAutoLoginEnabled);
        toggleCheckBoxImage((ImageView) v, isAutoLoginEnabled);

        Editor editor = prefs.edit();
        editor.putBoolean(ExoConstants.SETTING_AUTOLOGIN, isAutoLoginEnabled);
        editor.commit();
      }
    };
  }

  private void toggleCheckBoxImage(ImageView imageView, boolean isEnabled) {
    if (isEnabled) imageView.setBackgroundResource(R.drawable.authenticate_checkmark_on);
    else imageView.setBackgroundResource(R.drawable.authenticate_checkmark_off);

  }

  public void setSocialFilter(ImageView socialChecked) {
    boolean isSocialFilter = prefs.getBoolean(AccountSetting.getInstance().socialKey, false);
    Editor editor = prefs.edit();
    if (isSocialFilter) {
      socialChecked.setBackgroundResource(R.drawable.authenticate_checkmark_off);
      editor.putInt(AccountSetting.getInstance().socialKeyIndex, 0);
    } else {
      socialChecked.setBackgroundResource(R.drawable.authenticate_checkmark_on);
    }

    editor.putBoolean(AccountSetting.getInstance().socialKey, !isSocialFilter);
    editor.commit();

  }

  public void setDocumentShowPrivateDrive(ImageView documentChecked) {
    boolean isShowHidden = prefs.getBoolean(AccountSetting.getInstance().documentKey, true);
    Editor editor = prefs.edit();
    if (isShowHidden) {
      documentChecked.setBackgroundResource(R.drawable.authenticate_checkmark_off);
    } else {
      documentChecked.setBackgroundResource(R.drawable.authenticate_checkmark_on);
    }

    editor.putBoolean(AccountSetting.getInstance().documentKey, !isShowHidden);
    editor.commit();
  }

  public void initDocumentHiddenFile(ImageView documentChecked) {
    boolean isShowHidden = prefs.getBoolean(AccountSetting.getInstance().documentKey, true);
    if (isShowHidden) {
      documentChecked.setBackgroundResource(R.drawable.authenticate_checkmark_on);
    } else
      documentChecked.setBackgroundResource(R.drawable.authenticate_checkmark_off);

  }

  public boolean updateLocallize(String localize) {
    SettingUtils.setLocale(mContext, localize);
    return true;
  }

  public void setEnglishLocation(ImageView imgViewE, ImageView imgViewF) {
    imgViewE.setVisibility(View.VISIBLE);
    imgViewF.setVisibility(View.INVISIBLE);
  }

  public void setFrenchLocation(ImageView imgViewE, ImageView imgViewF) {
    imgViewE.setVisibility(View.INVISIBLE);
    imgViewF.setVisibility(View.VISIBLE);
  }

  public void setServerList() {
    List<ServerObjInfo> serverList = ServerSettingHelper.getInstance().getServerInfoList();
    listServerWrap.removeAllViews();
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 0, 0, -1);
    for (int i = 0; i < serverList.size(); i++) {
      final ServerObjInfo serverObj = serverList.get(i);
      ServerItemLayout serverItem = new ServerItemLayout(mContext);
      serverItem.serverName.setText(serverObj._strServerName);
      serverItem.serverUrl.setText(serverObj._strServerUrl);
      if (Integer.valueOf(AccountSetting.getInstance().getDomainIndex()) == i)
        serverItem.serverImageView.setVisibility(View.VISIBLE);
      else
        serverItem.serverImageView.setVisibility(View.INVISIBLE);
      final int pos = i;

      /* onclick server item */
      serverItem.layout.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          int domainIndex = Integer.valueOf(AccountSetting.getInstance().getDomainIndex());
          if (domainIndex == pos) {
            String strCannotEdit = mContext.getString(R.string.CannotEditServer);
            Toast.makeText(mContext, strCannotEdit, Toast.LENGTH_SHORT).show();
            return;
          }

          Intent next  = new Intent(mContext, ServerEditionActivity.class);
          Bundle bundle = new Bundle();
          next.putExtra(ExoConstants.SETTING_ADDING_SERVER, false);
          bundle.putParcelable(ExoConstants.SETTING_SERVER_OBJ, serverObj);
          next.putExtra(ExoConstants.SETTING_SERVER_INDEX, pos);
          next.putExtras(bundle);
          next.putExtra(ExoConstants.SETTING_USERNAME, serverObj.username);
          next.putExtra(ExoConstants.SETTING_PASSWORD, serverObj.password);
          mContext.startActivity(next);
        }
      });

      listServerWrap.addView(serverItem, params);
    }

  }

  private void init() {
    serverInfoList      = ServerSettingHelper.getInstance().getServerInfoList();
    selectedServerIndex = Integer.parseInt(AccountSetting.getInstance().getDomainIndex());
    changeLanguage();
  }

  private boolean isServerValid(ServerObjInfo myServerObj) {
    if (myServerObj._strServerName.length() == 0 || myServerObj._strServerUrl.length() == 0) {

      Toast.makeText(mContext, serverIsEmpty, Toast.LENGTH_SHORT).show();
      return false;
    }
    URLAnalyzer urlAnanyzer = new URLAnalyzer();
    myServerObj._strServerUrl = urlAnanyzer.parserURL(myServerObj._strServerUrl);

    if (ExoDocumentUtils.isContainSpecialChar(myServerObj._strServerName,
                                              ExoConstants.SPECIAL_CHAR_NAME_SET)
        || ExoDocumentUtils.isContainSpecialChar(myServerObj._strServerUrl,
                                                 ExoConstants.SPECIAL_CHAR_URL_SET)) {

      Toast.makeText(mContext, serverNameURLInvalid, Toast.LENGTH_SHORT).show();
      return false;

    }
    return true;
  }

  public boolean onAdd(ServerObjInfo myServerObj) {
    if (isServerValid(myServerObj)) {
      int serverSize = serverInfoList.size();
      for (int i = 0; i < serverSize; i++) {
        ServerObjInfo tmp = serverInfoList.get(i);

        // TODO Does not make sense to compare server name, only server url matters
        //if (myServerObj._strServerName.equalsIgnoreCase(tmp._strServerName)) {
        //  Toast.makeText(mContext, serverNameIsExisted, Toast.LENGTH_SHORT).show();
          // break;
        //  return false;
        //}

        if (myServerObj._strServerUrl.equalsIgnoreCase(tmp._strServerUrl)) {
          Toast.makeText(mContext, serverUrlIsExisted, Toast.LENGTH_SHORT).show();
          // break;
          return false;
        }

      }

      //myServerObj._bSystemServer = false;
      serverInfoList.add(myServerObj);

      onSave();
      return true;

    }
    return false;
  }

  public boolean onUpdate(ServerObjInfo myServerObj, int serverIndex) {
    if (isServerValid(myServerObj)) {

      /**  no need to compare anything
      for (int i = 0; i < serverInfoList.size(); i++) {
        ServerObjInfo tmp = serverInfoList.get(i);

        if (i == serverIndex) {
          continue;
        }

        //if (myServerObj._strServerName.equalsIgnoreCase(tmp._strServerName)) {
        //  Toast.makeText(mContext, serverNameIsExisted, Toast.LENGTH_SHORT).show();
        //  return false;
        //}

        if (myServerObj._strServerUrl.equalsIgnoreCase(tmp._strServerUrl)) {
          Toast.makeText(mContext, serverUrlIsExisted, Toast.LENGTH_SHORT).show();
          return false;
        }
      }
       **/

      serverInfoList.remove(serverIndex);
      serverInfoList.add(serverIndex, myServerObj);
      onSave();
      return true;
    }
    return false;
  }

  public void onDelete(int serverIndex) {
    /* delete currently-used server */
    if (serverIndex == selectedServerIndex) {
      /* clear setting */
      AccountSetting.getInstance().setDomainIndex(String.valueOf(-1));
      AccountSetting.getInstance().setDomainName("");
      AccountSetting.getInstance().setUsername("");
      AccountSetting.getInstance().setPassword("");
    } else if (serverIndex < selectedServerIndex) {
      int index = selectedServerIndex - 1;
      AccountSetting.getInstance().setDomainIndex(String.valueOf(index));
    }

    serverInfoList.remove(serverIndex);
    onSave();
  }

  /**
   * make change to shared perf
   * and generate xml file
   */
  private void onSave() {
    ServerConfigurationUtils.generateXmlFileWithServerList(mContext,
        serverInfoList, ExoConstants.EXO_SERVER_SETTING_FILE, "");

    ServerSettingHelper.getInstance().setServerInfoList(serverInfoList);
    SharedPreferences.Editor editor = mContext.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0)
                                              .edit();
    editor.putString(ExoConstants.EXO_PRF_DOMAIN, AccountSetting.getInstance().getDomainName());
    editor.putString(ExoConstants.EXO_PRF_DOMAIN_INDEX, AccountSetting.getInstance()
                                                                      .getDomainIndex());
    editor.putString(ExoConstants.EXO_PRF_USERNAME, AccountSetting.getInstance().getUsername());
    editor.putString(ExoConstants.EXO_PRF_PASSWORD, AccountSetting.getInstance().getPassword());
    editor.commit();
  }

  private void changeLanguage() {
    Resources resource = mContext.getResources();
    serverIsEmpty = resource.getString(R.string.WarningServerNameIsEmpty);
    serverNameIsExisted = resource.getString(R.string.WarningServerNameIsExist);
    serverUrlIsExisted = resource.getString(R.string.WarningServerUrlIsExist);
    serverNameURLInvalid = resource.getString(R.string.SpecialCharacters);
  }

}
