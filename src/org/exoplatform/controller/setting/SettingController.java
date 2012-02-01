package org.exoplatform.controller.setting;

import java.io.IOException;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.ServerItemLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class SettingController {
  private Context mContext;

  private ImageView imgViewECheckMark, imgViewFCheckMark;

  public SettingController(Context context, ImageView imgViewE, ImageView imgViewF) {
    mContext = context;
    imgViewECheckMark = imgViewE;
    imgViewFCheckMark = imgViewF;
    // ExoConnectionUtils.checkPLFVersion();
  }

  public void initLocation() {
    String locallize = LocalizationHelper.getInstance().getLocation();
    if (locallize.equalsIgnoreCase(ExoConstants.FRENCH_LOCALIZATION)) {
      setFrenchLocation();
    } else {
      setEnglishLocation();
    }
  }

  public boolean updateLocallize(String localize) {
    try {
      SharedPreferences.Editor editor = LocalizationHelper.getInstance().getSharePrefs().edit();
      editor.putString(ExoConstants.EXO_PRF_LOCALIZE, localize);
      editor.commit();
      LocalizationHelper.getInstance().setLocation(localize);
      ResourceBundle bundle = new PropertyResourceBundle(mContext.getAssets().open(localize));
      LocalizationHelper.getInstance().setResourceBundle(bundle);
      return true;
    } catch (IOException e) {
      return false;
    }

  }

  public void setEnglishLocation() {
    imgViewECheckMark.setVisibility(View.VISIBLE);
    imgViewFCheckMark.setVisibility(View.INVISIBLE);

  }

  public void setFrenchLocation() {
    imgViewECheckMark.setVisibility(View.INVISIBLE);
    imgViewFCheckMark.setVisibility(View.VISIBLE);
  }

  public void setServerList(LinearLayout listServerWrap) {
    List<ServerObjInfo> serverList = ServerSettingHelper.getInstance().getServerInfoList();
    listServerWrap.removeAllViews();
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    params.setMargins(0, 0, 0, -1);
    for (int i = 0; i < serverList.size(); i++) {
      ServerObjInfo serverObj = serverList.get(i);
      ServerItemLayout serverItem = new ServerItemLayout(mContext);
      serverItem.serverName.setText(serverObj._strServerName);
      serverItem.serverUrl.setText(serverObj._strServerUrl);
      if (Integer.valueOf(AccountSetting.getInstance().getDomainIndex()) == i)
        serverItem.serverImageView.setVisibility(View.VISIBLE);
      else
        serverItem.serverImageView.setVisibility(View.INVISIBLE);
      listServerWrap.addView(serverItem, params);

    }

  }

}
