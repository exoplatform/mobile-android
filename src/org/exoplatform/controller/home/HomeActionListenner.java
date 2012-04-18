package org.exoplatform.controller.home;

import org.exoplatform.model.HomeItem;
import org.exoplatform.singleton.HomeHelper;
import org.exoplatform.ui.ChatListActivity;
import org.exoplatform.ui.DashboardActivity;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.ui.social.SocialActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.cyrilmottier.android.greendroid.R;

public class HomeActionListenner implements OnItemClickListener {
  private String  okString;

  private String  titleString;

  private String  contentString;

  private Context mContext;

  public HomeActionListenner(Context context) {
    mContext = context;
    changeLanguage();
  }

  // @Override
  public void onItemClick(AdapterView<?> adapter, View view, int postion, long id) {
    HomeItem item = HomeHelper.getInstance().getHomeItemList().get(postion);
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      switch (item._index) {
      case 1:
        launchNewsService();
        break;
      case 2:
        launchChatApp();
        break;
      case 3:
        launchDocumentApp();
        break;
      case 4:
        launchDashboardApp();
        break;
      // case 5:
      // launchSettingApp();
      // break;
      default:
        break;

      }
    } else {
      new WarningDialog(mContext, titleString, contentString, okString).show();
    }

  }

  private boolean checkDocumentConnection() {
    return true;
  }

  private void launchNewsService() {
    Intent next = new Intent(mContext, SocialActivity.class);
    mContext.startActivity(next);
  }

  private void launchChatApp() {
    Intent next = new Intent(mContext, ChatListActivity.class);
    mContext.startActivity(next);

  }

  private void launchDocumentApp() {

    if (checkDocumentConnection() == true) {
      Intent next = new Intent(mContext, DocumentActivity.class);
      mContext.startActivity(next);
    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }
  }

  private void launchDashboardApp() {
    Intent intent = new Intent(mContext, DashboardActivity.class);
    mContext.startActivity(intent);
  }

  private void changeLanguage() {
    okString = mContext.getResources().getString(R.string.OK);
    titleString = mContext.getResources().getString(R.string.Warning);
    contentString = mContext.getResources().getString(R.string.ConnectionError);

  }

}
