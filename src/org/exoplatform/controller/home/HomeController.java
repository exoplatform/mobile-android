package org.exoplatform.controller.home;

import java.util.ArrayList;

import org.exoplatform.model.HomeItem;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.HomeHelper;
import org.exoplatform.singleton.LocalizationHelper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cyrilmottier.android.greendroid.R;

public class HomeController {
  private String  activityStreamsText;

  private String  chatText;

  private String  documentText;

  private String  dashboardText;

  private String  settingText;

  private Context mContext;

  public HomeController(Context context) {
    mContext = context;
    initScreen();
  }

  private void initScreen() {
    changeLanguage();
    Resources resource = mContext.getResources();
    ArrayList<HomeItem> itemList = new ArrayList<HomeItem>();
    Bitmap bm = BitmapFactory.decodeResource(resource, R.drawable.homeactivitystreamsiconiphone);
    HomeItem item;
    if (AccountSetting.getInstance().getIsNewVersion() == true) {
      item = new HomeItem(bm, 1, activityStreamsText);
      itemList.add(item);
    }

    bm = BitmapFactory.decodeResource(resource, R.drawable.homechaticoniphone);
    item = new HomeItem(bm, 2, chatText);
    itemList.add(item);

    bm = BitmapFactory.decodeResource(resource, R.drawable.homedocumentsiconiphone);
    item = new HomeItem(bm, 3, documentText);
    itemList.add(item);

    bm = BitmapFactory.decodeResource(resource, R.drawable.homedashboardiconiphone);
    item = new HomeItem(bm, 4, dashboardText);
    itemList.add(item);

    bm = BitmapFactory.decodeResource(resource, R.drawable.homesettingsiconiphone);
    item = new HomeItem(bm, 5, settingText);
    itemList.add(item);

    HomeHelper.getInstance().setHomeItemList(itemList);
  }

  private void changeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    activityStreamsText = location.getString("ActivityStream");
    chatText = location.getString("ChatApplication");
    documentText = location.getString("Documents");
    dashboardText = location.getString("Dashboard");
    settingText = location.getString("Settings");
  }

}
