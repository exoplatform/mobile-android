package org.exoplatform.controller.setting;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
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

  public SettingController(Context context, LinearLayout listServerWrap) {
    mContext = context;
    prefs = mContext.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);
    this.listServerWrap = listServerWrap;
    init();
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
      serverItem.layout.setOnClickListener(new OnClickListener() {

        @Override
        public void onClick(View v) {
          int domainIndex = Integer.valueOf(AccountSetting.getInstance().getDomainIndex());
          if (domainIndex == pos) {
            String strCannotEdit = mContext.getString(R.string.CannotEditServer);
            Toast.makeText(mContext, strCannotEdit, Toast.LENGTH_SHORT).show();
          } else {
            new ServerEditionDialog(mContext, SettingController.this, serverObj, pos).show();
          }

        }
      });
      listServerWrap.addView(serverItem, params);

    }

  }

  private void init() {
    serverInfoList = ServerSettingHelper.getInstance().getServerInfoList();
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
        if (myServerObj._strServerName.equalsIgnoreCase(tmp._strServerName)) {
          Toast.makeText(mContext, serverNameIsExisted, Toast.LENGTH_SHORT).show();
          // break;
          return false;
        }

        if (myServerObj._strServerUrl.equalsIgnoreCase(tmp._strServerUrl)) {
          Toast.makeText(mContext, serverUrlIsExisted, Toast.LENGTH_SHORT).show();
          // break;
          return false;
        }

      }

      myServerObj._bSystemServer = false;
      serverInfoList.add(myServerObj);
      ServerConfigurationUtils.createXmlDataWithServerList(serverInfoList,
                                                           "DefaultServerList.xml",
                                                           "");
      onSave();
      return true;

    }
    return false;
  }

  public boolean onUpdate(ServerObjInfo myServerObj, int serverIndex) {
    if (isServerValid(myServerObj)) {
      for (int i = 0; i < serverInfoList.size(); i++) {
        ServerObjInfo tmp = serverInfoList.get(i);

        if (i == selectedServerIndex) {
          continue;
        }

        if (myServerObj._strServerName.equalsIgnoreCase(tmp._strServerName)) {
          Toast.makeText(mContext, serverNameIsExisted, Toast.LENGTH_SHORT).show();
          return false;
        }

        if (myServerObj._strServerUrl.equalsIgnoreCase(tmp._strServerUrl)) {
          Toast.makeText(mContext, serverUrlIsExisted, Toast.LENGTH_SHORT).show();
          return false;
        }
      }

      serverInfoList.remove(serverIndex);
      myServerObj._strServerName = myServerObj._strServerName;
      myServerObj._strServerUrl = myServerObj._strServerUrl;
      serverInfoList.add(serverIndex, myServerObj);
      ServerConfigurationUtils.createXmlDataWithServerList(serverInfoList,
                                                           "DefaultServerList.xml",
                                                           "");
      onSave();
      return true;
    }
    return false;
  }

  public void onDelete(int serverIndex) {
    if (serverIndex == selectedServerIndex) {
      AccountSetting.getInstance().setDomainIndex(String.valueOf(-1));
      AccountSetting.getInstance().setDomainName("");
    } else if (serverIndex < selectedServerIndex) {
      int index = selectedServerIndex - 1;
      AccountSetting.getInstance().setDomainIndex(String.valueOf(index));
    }
    serverInfoList.remove(serverIndex);
    ServerConfigurationUtils.createXmlDataWithServerList(serverInfoList,
                                                         "DefaultServerList.xml",
                                                         "");

    onSave();
  }

  private void onSave() {

    ServerSettingHelper.getInstance().setServerInfoList(serverInfoList);
    SharedPreferences.Editor editor = mContext.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0)
                                              .edit();
    editor.putString(ExoConstants.EXO_PRF_DOMAIN, AccountSetting.getInstance().getDomainName());
    editor.putString(ExoConstants.EXO_PRF_DOMAIN_INDEX, AccountSetting.getInstance()
                                                                      .getDomainIndex());
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
