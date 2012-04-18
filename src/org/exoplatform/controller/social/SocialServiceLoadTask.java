package org.exoplatform.controller.social;

import java.net.MalformedURLException;
import java.net.URL;

import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.ClientServiceFactory;
import org.exoplatform.social.client.api.SocialClientContext;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.IdentityService;
import org.exoplatform.social.client.api.service.VersionService;
import org.exoplatform.social.client.core.ClientServiceFactoryHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.SocialWaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import com.cyrilmottier.android.greendroid.R;

public class SocialServiceLoadTask extends AsyncTask<Void, Void, Boolean> {
  private Context          mContext;

  private String           loadingData;

  private String           okString;

  private String           titleString;

  private String           contentString;

  private SocialController socialController;

  public SocialServiceLoadTask(Context context, SocialController controller) {
    mContext = context;
    socialController = controller;
    changeLanguage();
  }

  private void changeLanguage() {
    Resources resource = mContext.getResources();
    loadingData = resource.getString(R.string.LoadingData);
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    contentString = resource.getString(R.string.LoadingDataError);

  }

  @Override
  public void onPreExecute() {
    if (socialController._progressDialog == null) {
      socialController._progressDialog = new SocialWaitingDialog(mContext,
                                                                 socialController,
                                                                 null,
                                                                 loadingData);
      socialController._progressDialog.show();
    }

  }

  @SuppressWarnings("deprecation")
  @Override
  public Boolean doInBackground(Void... params) {
    try {
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
      socialController.onLoad(ExoConstants.NUMBER_OF_ACTIVITY);
    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }
  }
}
