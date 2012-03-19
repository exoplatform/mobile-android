package org.exoplatform.controller.home;

import java.net.MalformedURLException;
import java.net.URL;

import org.exoplatform.model.HomeItem;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.HomeHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.ClientServiceFactory;
import org.exoplatform.social.client.api.SocialClientContext;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.IdentityService;
import org.exoplatform.social.client.api.service.VersionService;
import org.exoplatform.social.client.core.ClientServiceFactoryHelper;
import org.exoplatform.ui.ChatListActivity;
import org.exoplatform.ui.DashboardActivity;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.ui.social.SocialActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.cyrilmottier.android.greendroid.R;

public class HomeActionListenner implements OnItemClickListener {
  private String              okString;

  private String              titleString;

  private String              contentString;

  private String              socialErrorString;

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

    if (mLoadTask == null || mLoadTask.getStatus() == NewsServiceLoadTask.Status.FINISHED) {
      mLoadTask = (NewsServiceLoadTask) new NewsServiceLoadTask().execute();
    }

  }

  public void onCancelLoadNewsService() {
    if (mLoadTask != null && mLoadTask.getStatus() == NewsServiceLoadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
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

  // private void launchSettingApp() {
  // Intent next = new Intent(mContext, SettingActivity.class);
  // next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
  // next.putExtra(ExoConstants.SETTING_TYPE, 1);
  // mContext.startActivity(next);
  //
  // }

  private void changeLanguage() {
    okString = mContext.getResources().getString(R.string.OK);
    titleString = mContext.getResources().getString(R.string.Warning);
    contentString = mContext.getResources().getString(R.string.ConnectionError);
    socialErrorString = mContext.getResources().getString(R.string.LoadingDataError);

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

  private class NewsServiceLoadTask extends AsyncTask<Void, Void, Boolean> {

    @SuppressWarnings("deprecation")
    @Override
    public Boolean doInBackground(Void... params) {
      try {
//        parseDomain();
        String userName = AccountSetting.getInstance().getUsername();
        String password = AccountSetting.getInstance().getPassword();
        URL url = new URL(SocialActivityUtil.getDomain());
        SocialClientContext.setProtocol(url.getProtocol());
        SocialClientContext.setHost(url.getHost());
        SocialClientContext.setPort(url.getPort());
        SocialClientContext.setPortalContainerName(ExoConstants.ACTIVITY_PORTAL_CONTAINER);
        SocialClientContext.setRestContextName(ExoConstants.ACTIVITY_REST_CONTEXT);
        SocialClientContext.setUsername(userName);
        SocialClientContext.setPassword(password);

        ClientServiceFactory clientServiceFactory = ClientServiceFactoryHelper.getClientServiceFactory();
        VersionService versionService = clientServiceFactory.createVersionService();
        SocialClientContext.setRestVersion(versionService.getLatest());
        clientServiceFactory = ClientServiceFactoryHelper.getClientServiceFactory();

        @SuppressWarnings("unchecked")
        ActivityService<RestActivity> activityService = clientServiceFactory.createActivityService();
        IdentityService<?> identityService = clientServiceFactory.createIdentityService();
        String userIdentity;
        userIdentity = identityService.getIdentityId(ExoConstants.ACTIVITY_ORGANIZATION, userName);
        SocialServiceHelper.getInstance().setUserId(userIdentity);

        SocialServiceHelper.getInstance().setActivityService(activityService);
        SocialServiceHelper.getInstance().setIdentityService(identityService);

        return true;
      } catch (SocialClientLibException e) {
        return false;
      } catch (RuntimeException e) {
        return false;
      } catch (MalformedURLException e) {
        return false;
      }
    }

    @Override
    public void onPostExecute(Boolean result) {
      if (result) {
        Intent next = new Intent(mContext, SocialActivity.class);
        mContext.startActivity(next);
      } else {
        WarningDialog dialog = new WarningDialog(mContext, titleString, socialErrorString, okString);
        dialog.show();
      }
    }
  }
}
