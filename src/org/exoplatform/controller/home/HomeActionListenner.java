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
import org.exoplatform.ui.SettingActivity;
import org.exoplatform.ui.social.SocialActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
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
  private String  okString;

  private String  titleString;

  private String  contentString;

  private String  protocol;

  private String  host;

  private int     port;

  private Context mContext;

  public HomeActionListenner(Context context) {
    mContext = context;
    changeLanguage();
  }

  @Override
  public void onItemClick(AdapterView<?> adapter, View view, int postion, long id) {
    HomeItem item = HomeHelper.getInstance().getHomeItemList().get(postion);
    switch (item._index) {
    case 1:
      launchActivityStreamApp();
      break;
    case 2:
      break;
    case 3:
      break;
    case 4:
      break;
    case 5:
      launchSettingApp();
      break;

    }

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

  private boolean createConnetion() {

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

  private void launchActivityStreamApp() {

    if (createConnetion() == true) {
      Intent next = new Intent(mContext, SocialActivity.class);
      mContext.startActivity(next);
    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }

  }

  private void launchSettingApp() {
    Intent next = new Intent(mContext, SettingActivity.class);
    next.putExtra(ExoConstants.SETTING_TYPE, 1);
    mContext.startActivity(next);

  }

  private void changeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    okString = location.getString("OK");
    titleString = location.getString("Warning");
    contentString = location.getString("ConnectionError");
  }
}
