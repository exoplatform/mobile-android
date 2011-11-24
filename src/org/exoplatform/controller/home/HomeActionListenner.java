package org.exoplatform.controller.home;

import org.exoplatform.model.HomeItem;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.HomeHelper;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.ClientServiceFactory;
import org.exoplatform.social.client.api.SocialClientContext;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.IdentityService;
import org.exoplatform.social.client.core.ClientServiceFactoryHelper;
import org.exoplatform.ui.ChatListActivity;
import org.exoplatform.ui.DashboardActivity;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.ui.SettingActivity;
import org.exoplatform.ui.social.SocialActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

/*
 * 
 */
public class HomeActionListenner implements OnItemClickListener {
  private String              okString;

  private String              titleString;

  private String              contentString;

  private String              protocol;

  private String              host;

  private int                 port;

  private NewsServiceLoadTask mLoadTask;

  private Context             mContext;

  public HomeActionListenner(Context context) {
    mContext = context;
    changeLanguage();
  }

  // @Override
  public void onItemClick(AdapterView<?> adapter, View view, int postion, long id) {
    HomeItem item = HomeHelper.getInstance().getHomeItemList().get(postion);
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
    case 5:
      launchSettingApp();
      break;
    default:
      break;

    }

  }

  private boolean checkDocumentConnection() {
    return true;
  }

  private void launchNewsService() {

    if (mLoadTask == null || mLoadTask.getStatus() == NewsServiceLoadTask.Status.FINISHED) {
      mLoadTask = (NewsServiceLoadTask) new NewsServiceLoadTask().execute();
    }

  }
  
  public void onCancelLoadNewsService(){
    if (mLoadTask != null && mLoadTask.getStatus() == NewsServiceLoadTask.Status.RUNNING) {
      mLoadTask.onCancelled();
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  private void launchChatApp() {
    Intent next = new Intent(mContext, ChatListActivity.class);
    next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mContext.startActivity(next);

  }

  private void launchDocumentApp() {

    if (checkDocumentConnection() == true) {
      Intent next = new Intent(mContext, DocumentActivity.class);
      next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      mContext.startActivity(next);
    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }
  }

  private void launchDashboardApp() {
    Intent intent = new Intent(mContext, DashboardActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    mContext.startActivity(intent);
  }

  private void launchSettingApp() {
    Intent next = new Intent(mContext, SettingActivity.class);
    next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    next.putExtra(ExoConstants.SETTING_TYPE, 1);
    mContext.startActivity(next);

  }

  private void changeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    okString = location.getString("OK");
    titleString = location.getString("Warning");
    contentString = location.getString("ConnectionError");
  }

  private void parseDomain() {
    String domain = SocialActivityUtil.getDomain();
    String[] domainSplits = domain.split("//");
    protocol = domainSplits[0].substring(0, domainSplits[0].length() - 1);
    if (domainSplits[1].contains(":")) {
      String[] hostAddr = domainSplits[1].split(":");
      host = hostAddr[0];
      port = Integer.valueOf(hostAddr[1]);
    } else {
      host = domainSplits[1];
      port = ExoConstants.ACTIVITY_PORT;
    }
  }

  private class NewsServiceLoadTask extends UserTask<Void, Void, Boolean> {

    @Override
    public Boolean doInBackground(Void... params) {
      try {
        parseDomain();
        String userName = AccountSetting.getInstance().getUsername();
        String password = AccountSetting.getInstance().getPassword();

        SocialClientContext.setProtocol(protocol);
        SocialClientContext.setHost(host);
        SocialClientContext.setPort(port);
        SocialClientContext.setPortalContainerName(ExoConstants.ACTIVITY_PORTAL_CONTAINER);
        SocialClientContext.setRestContextName(ExoConstants.ACTIVITY_REST_CONTEXT);
        SocialClientContext.setRestVersion(ExoConstants.ACTIVITY_REST_VERSION);
        SocialClientContext.setUsername(userName);
        SocialClientContext.setPassword(password);

        ClientServiceFactory clientServiceFactory = ClientServiceFactoryHelper.getClientServiceFactory();
        ActivityService<RestActivity> activityService = clientServiceFactory.createActivityService();
        IdentityService<?> identityService = clientServiceFactory.createIdentityService();
        String userIdentity = identityService.getIdentityId(ExoConstants.ACTIVITY_ORGANIZATION,
                                                            userName);
        SocialServiceHelper.getInstance().setActivityService(activityService);
        SocialServiceHelper.getInstance().setIdentityService(identityService);
        SocialServiceHelper.getInstance().setUserId(userIdentity);

        return true;
      } catch (RuntimeException e) {
        return false;
      }
    }

    @Override
    public void onPostExecute(Boolean result) {
      if (result) {
        Intent next = new Intent(mContext, SocialActivity.class);
        next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(next);
      } else {
        WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
        dialog.show();
      }
    }
  }
}
