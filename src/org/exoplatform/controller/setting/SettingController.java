package org.exoplatform.controller.setting;

import java.util.List;

import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.widget.ServerItemLayout;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class SettingController {
  private Context           mContext;

  private ImageView         imgViewECheckMark, imgViewFCheckMark;

  private SharedPreferences prefs;

  public SettingController(Context context, ImageView imgViewE, ImageView imgViewF) {
    mContext = context;
    prefs = mContext.getSharedPreferences(ExoConstants.EXO_PREFERENCE, 0);
    imgViewECheckMark = imgViewE;
    imgViewFCheckMark = imgViewF;
  }

  public void initLocation() {
    String locallize = prefs.getString(ExoConstants.EXO_PRF_LOCALIZE,
                                       ExoConstants.ENGLISH_LOCALIZATION);
    ;
    if (locallize != null) {
      if (locallize.equalsIgnoreCase(ExoConstants.FRENCH_LOCALIZATION)) {
        setFrenchLocation();
      } else {
        setEnglishLocation();
      }
    } else {
      setEnglishLocation();
    }

  }

  public void initSocialFilter(ImageView socialChecked) {
    boolean isSocialFilter = prefs.getBoolean(ExoConstants.SETTING_SOCIAL_FILTER, false);
    if (isSocialFilter) {
      socialChecked.setBackgroundResource(R.drawable.authenticate_checkmark_on);
    } else
      socialChecked.setBackgroundResource(R.drawable.authenticate_checkmark_off);

  }

  public void setSocialFilter(ImageView socialChecked) {
    boolean isSocialFilter = prefs.getBoolean(ExoConstants.SETTING_SOCIAL_FILTER, false);
    boolean isChange = isSocialFilter;
    Editor editor = prefs.edit();
    if (isSocialFilter) {
      socialChecked.setBackgroundResource(R.drawable.authenticate_checkmark_off);
      editor.putInt(ExoConstants.SETTING_SOCIAL_FILTER_INDEX, 0);
      isChange = false;
    } else {
      socialChecked.setBackgroundResource(R.drawable.authenticate_checkmark_on);
      isChange = true;
    }

    editor.putBoolean(ExoConstants.SETTING_SOCIAL_FILTER, isChange);
    editor.commit();

  }

  public boolean updateLocallize(String localize) {
    SettingUtils.setLocale(mContext, localize);
    return true;

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
