package org.exoplatform.controller.setting;

import java.util.ArrayList;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewParent;
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

/**
 */
public class SettingController {
  private Context                  mContext;

  private Activity                 mActivity;

  private SharedPreferences        mSharedPreferences;

  private String                   serverIsEmpty;

  private String                   serverNameURLInvalid;

  private String                   serverURLAndUserExists;

  private AccountSetting           mSetting;

  private static LayoutParams      sListServerLayoutParams;

  private static ArrayList<ServerObjInfo> sServerList;

  private static LinearLayout      sListServerLayout;

  private static  SettingController _instance;

  public  boolean                   mIsSocialFilterEnabled;

  public  boolean                   mIsShowHidden;

  private static final String TAG = "eXoSettingController";

  public SettingController(Activity context, ViewGroup parent) {
    mContext  = context;
    mActivity = context;
    mSetting  = AccountSetting.getInstance();
    mSharedPreferences  = mContext.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);

    sListServerLayoutParams = (sListServerLayoutParams == null) ?
        new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT) : sListServerLayoutParams;
    sListServerLayoutParams.setMargins(0, 0, 0, -1);

    changeLanguage();

    if (isTwoListEqual(sServerList, ServerSettingHelper.getInstance().getServerInfoList())) {
      /* re-use old layout */
      ViewGroup oldParent = (ViewGroup) sListServerLayout.getParent();
      oldParent.removeView(sListServerLayout);
      return ;
    }

    sServerList      = ServerSettingHelper.getInstance().getServerInfoList();
    _instance = this;
    setServerList(parent);
  }

  public LinearLayout getServerListLayout() {
    return sListServerLayout;
  }

  /**
   * Used to quick check if 2 server list are equal
   *
   * @param list1
   * @param list2
   * @return
   */
  public static boolean isTwoListEqual(ArrayList<ServerObjInfo> list1, ArrayList<ServerObjInfo> list2) {
    if ((list1 == null) || (list2 == null)) return false;
    if (list1.size() != list2.size()) return false;

    for (int i = 0; i < list1.size(); i++) {
      if ( !list1.get(i).equals(list2.get(i)) ) return false;
    }
    return true;
  }

  public static SettingController getInstance() {
    return _instance;
  }

  public static void useInstance(SettingController instance) {
    _instance = instance;
  }

  public void initLocation(ImageView imgViewE, ImageView imgViewF) {
    String localize = mSharedPreferences.getString(ExoConstants.EXO_PRF_LOCALIZE,
                                       ExoConstants.ENGLISH_LOCALIZATION);
    if (localize != null) {
      if (localize.equalsIgnoreCase(ExoConstants.FRENCH_LOCALIZATION)) {
        setFrenchLocation(imgViewE, imgViewF);
      } else {
        setEnglishLocation(imgViewE, imgViewF);
      }
    } else {
      setEnglishLocation(imgViewE, imgViewF);
    }
  }

  /**
   * Retrieve login setting from preferences
   */
  public void initLoginSetting(ImageView rememberMeImg, ImageView autoLoginImg) {
    boolean isRememberMeEnabled = mSetting.isRememberMeEnabled();
    boolean isAutoLoginEnabled  = mSetting.isAutoLoginEnabled();
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
        boolean isRememberMeEnabled = !mSetting.isRememberMeEnabled();
        Log.i(TAG, "click remember me: " + isRememberMeEnabled);
        if (isRememberMeEnabled) {
          _autoLoginImg.setEnabled(true);
          _autoLoginImg.setOnClickListener(onClickAutoLogin());
        }
        else {
          _autoLoginImg.setEnabled(false);
          toggleCheckBoxImage(_autoLoginImg, false);
          mSetting.getCurrentServer().isAutoLoginEnabled = false;
        }

        toggleCheckBoxImage(_rememberMeImg, isRememberMeEnabled);
        mSetting.getCurrentServer().isRememberEnabled = isRememberMeEnabled;
      }
    });
  }

  private View.OnClickListener onClickAutoLogin() {
    return new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        boolean isAutoLoginEnabled = !mSetting.isAutoLoginEnabled();
        Log.i(TAG, "click autologin: " + isAutoLoginEnabled);
        toggleCheckBoxImage((ImageView) v, isAutoLoginEnabled);

        mSetting.getCurrentServer().isAutoLoginEnabled = isAutoLoginEnabled;
      }
    };
  }

  /**
   * Change checkbox image
   */
  private void toggleCheckBoxImage(ImageView imageView, boolean isEnabled) {
    if (isEnabled) imageView.setBackgroundResource(R.drawable.authenticate_checkmark_on);
    else imageView.setBackgroundResource(R.drawable.authenticate_checkmark_off);
  }

  public void initSocialFilter(ImageView socialChecked) {
    mIsSocialFilterEnabled = mSharedPreferences.getBoolean(mSetting.socialKey, false);
    toggleCheckBoxImage(socialChecked, mIsSocialFilterEnabled);
  }

  public void setSocialFilter(ImageView socialChecked) {
    mIsSocialFilterEnabled = !mIsSocialFilterEnabled;
    toggleCheckBoxImage(socialChecked, mIsSocialFilterEnabled);
  }

  public void setDocumentShowPrivateDrive(ImageView documentChecked) {
    mIsShowHidden = !mIsShowHidden;
    toggleCheckBoxImage(documentChecked, mIsShowHidden);
  }

  public void initDocumentHiddenFile(ImageView documentChecked) {
    mIsShowHidden = mSharedPreferences.getBoolean(AccountSetting.getInstance().documentKey, true);
    toggleCheckBoxImage(documentChecked, mIsShowHidden);
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

  /**
   * Populate server list
   * Run only in case of updating new list of server
   */
  private void setServerList(ViewGroup parent) {
    if (sListServerLayout == null) {
      sListServerLayout = (LinearLayout) mActivity.getLayoutInflater().inflate(R.layout.setting_server_list, parent ,false);
    }
    else sListServerLayout.removeAllViews();

    ServerItemLayout serverItemLayout;
    for (int i = 0; i < sServerList.size(); i++) {
      serverItemLayout = initServerItem(sServerList.get(i), i);
      sListServerLayout.addView(serverItemLayout, i, sListServerLayoutParams);
    }
  }

  /**
   * Generate layout for a server item
   *
   * @param _serverObj
   * @param serverIdx
   * @return
   */
  private ServerItemLayout initServerItem(ServerObjInfo _serverObj, int serverIdx) {
    final ServerObjInfo serverObj = _serverObj;
    ServerItemLayout serverItem = new ServerItemLayout(mContext);
    serverItem.serverName.setText(serverObj.serverName);
    serverItem.serverUrl.setText(serverObj.serverUrl);

    if (Integer.valueOf(mSetting.getDomainIndex()) == serverIdx)
      serverItem.serverImageView.setVisibility(View.VISIBLE);
    else
      serverItem.serverImageView.setVisibility(View.INVISIBLE);
    final int pos = serverIdx;

      /* onclick server item */
    serverItem.layout.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        int domainIndex = Integer.valueOf(mSetting.getDomainIndex());
        if (domainIndex == pos) {
          String strCannotEdit = mContext.getString(R.string.CannotEditServer);
          Toast.makeText(mContext, strCannotEdit, Toast.LENGTH_SHORT).show();
          return;
        }

        Intent next  = new Intent(mContext, ServerEditionActivity.class);
        next.putExtra(ExoConstants.SETTING_ADDING_SERVER, false);
        next.putExtra(ExoConstants.EXO_SERVER_OBJ, serverObj);
        mContext.startActivity(next);
      }
    });

    return serverItem;
  }


  /**
   * Check whether server information is valid
   *
   * @param myServerObj
   * @return
   */
  private boolean isServerValid(ServerObjInfo myServerObj) {
    if (myServerObj.serverName.length() == 0 || myServerObj.serverUrl.length() == 0) {
      Toast.makeText(mContext, serverIsEmpty, Toast.LENGTH_SHORT).show();
      return false;
    }

    URLAnalyzer urlAnanyzer = new URLAnalyzer();
    myServerObj.serverUrl = urlAnanyzer.parserURL(myServerObj.serverUrl);

    if (ExoDocumentUtils.isContainSpecialChar(myServerObj.serverName,
                                              ExoConstants.SPECIAL_CHAR_NAME_SET)
        || ExoDocumentUtils.isContainSpecialChar(myServerObj.serverUrl,
                                                 ExoConstants.SPECIAL_CHAR_URL_SET)) {

      Toast.makeText(mContext, serverNameURLInvalid, Toast.LENGTH_SHORT).show();
      return false;

    }
    return true;
  }

  /**
   * Add server to server list
   *
   * @param myServerObj
   * @return
   */
  public boolean onAdd(ServerObjInfo myServerObj) {
    if (!isServerValid(myServerObj)) return false;

    if (sServerList.contains(myServerObj)) {
      Toast.makeText(mContext, serverURLAndUserExists, Toast.LENGTH_SHORT).show();
      return false;
    }

    sServerList.add(myServerObj);
    sListServerLayout.addView(initServerItem(myServerObj, sServerList.size() -1),
        sListServerLayoutParams );
    onSave();
    return true;
  }

  /**
   * Update existing server
   *
   * @param myServerObj
   * @param serverIndex
   * @return
   */
  public boolean onUpdate(ServerObjInfo myServerObj, int serverIndex) {
    if (!isServerValid(myServerObj)) return false;

    /* check whether server is duplicated with other server */
    int serverIdx = sServerList.indexOf(myServerObj);
    if ((serverIdx != serverIndex) && (serverIdx != -1)) {
      Toast.makeText(mContext, serverURLAndUserExists, Toast.LENGTH_SHORT).show();
      return false;
    }

    sServerList.remove(serverIndex);
    sServerList.add(serverIndex, myServerObj);
    sListServerLayout.removeViewAt(serverIndex);
    sListServerLayout.addView( initServerItem(myServerObj, serverIdx), serverIndex,
        sListServerLayoutParams);
    onSave();
    return true;
  }

  /**
   * Delete existing server
   *
   * @param serverIndex
   */
  public void onDelete(int serverIndex) {
    int selectedServerIndex = Integer.parseInt(mSetting.getDomainIndex());
    if (serverIndex < selectedServerIndex)
      mSetting.setDomainIndex(String.valueOf(selectedServerIndex - 1));

    sServerList.remove(serverIndex);
    sListServerLayout.removeViewAt(serverIndex);
    onSave();
  }

  /**
   * Make change to shared perf and generate xml file
   */
  private void onSave() {
    Log.i(TAG, "onSave");
    ServerConfigurationUtils.generateXmlFileWithServerList(mContext,
        sServerList, ExoConstants.EXO_SERVER_SETTING_FILE, "");
    ServerSettingHelper.getInstance().setServerInfoList(sServerList);
  }

  private void changeLanguage() {
    Resources resource = mContext.getResources();
    serverIsEmpty = resource.getString(R.string.WarningServerNameIsEmpty);
    serverNameURLInvalid = resource.getString(R.string.SpecialCharacters);
    serverURLAndUserExists = resource.getString(R.string.WarningServerUrlAndUserAlreadyExist);
  }

}
