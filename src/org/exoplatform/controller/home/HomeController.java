package org.exoplatform.controller.home;

import java.util.ArrayList;

import org.exoplatform.model.HomeItem;
import org.exoplatform.singleton.ChatServiceHelper;
import org.exoplatform.singleton.HomeHelper;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.cyrilmottier.android.greendroid.R;

public class HomeController {
  private String  activityStreamsText;

//  private String  chatText;

  private String  documentText;

  private String  dashboardText;

  private Context mContext;

  public HomeController(Context context) {
    mContext = context;
  }

  public void initScreen() {
    changeLanguage();
    Resources resource = mContext.getResources();
    ArrayList<HomeItem> itemList = new ArrayList<HomeItem>();
    Bitmap bm = BitmapFactory.decodeResource(resource, R.drawable.homeactivitystreamsiconiphone);
    HomeItem item;
    item = new HomeItem(bm, 1, activityStreamsText);
    itemList.add(item);
    
//    bm = BitmapFactory.decodeResource(resource, R.drawable.homechaticoniphone);
//    item = new HomeItem(bm, 2, chatText);
//    itemList.add(item);

    bm = BitmapFactory.decodeResource(resource, R.drawable.homedocumentsiconiphone);
    item = new HomeItem(bm, 3, documentText);
    itemList.add(item);

    bm = BitmapFactory.decodeResource(resource, R.drawable.homedashboardiconiphone);
    item = new HomeItem(bm, 4, dashboardText);
    itemList.add(item);

    HomeHelper.getInstance().setHomeItemList(itemList);
  }

  public void onFinish() {
    if (HomeHelper.getInstance().httpClient != null) {
      HomeHelper.getInstance().httpClient.getConnectionManager().shutdown();
      HomeHelper.getInstance().httpClient = null;
    }
    if (ChatServiceHelper.getInstance().getXMPPConnection() != null) {
      ChatServiceHelper.getInstance().getXMPPConnection().disconnect();
      ChatServiceHelper.getInstance().setXMPPConnection(null);
    }

    ((Activity) mContext).finish();
  }

  private void changeLanguage() {
    activityStreamsText = mContext.getResources().getString(R.string.ActivityStream);
    documentText = mContext.getResources().getString(R.string.Documents);
    dashboardText = mContext.getResources().getString(R.string.Dashboard);
  }

}
