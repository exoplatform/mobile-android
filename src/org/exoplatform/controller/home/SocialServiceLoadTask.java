package org.exoplatform.controller.home;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import org.exoplatform.poc.tabletversion.R;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.ClientServiceFactory;
import org.exoplatform.social.client.api.SocialClientContext;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.IdentityService;
import org.exoplatform.social.client.api.service.VersionService;
import org.exoplatform.social.client.core.ClientServiceFactoryHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Load and connect the app to the Social services and objects:<br/>
 * - Identity service <br/>
 * - Activity service <br/>
 * - User identity <br/>
 * - User profile <br/>
 * Initialize SocialServiceHelper with these objects.
 */
public class SocialServiceLoadTask extends AsyncTask<Void, Void, String[]> {

  private Context                       mContext;

  private ActivityService<RestActivity> mActivityService;

  private IdentityService<RestIdentity> mIdentityService;

  private String                        mUserIdentity;

  private AsyncTaskListener mListener;


  private static final String TAG = "eXo____SocialServiceLoadTask____";


  public SocialServiceLoadTask(Context context) {
    mContext = context;
  }


  @SuppressWarnings({ "deprecation", "unchecked" })
  @Override
  public String[] doInBackground(Void... params) {
    Log.i(TAG, "accessing social service");

    try {
      String userName = AccountSetting.getInstance().getUsername();
      String password = AccountSetting.getInstance().getPassword();
      URL url = new URL(SocialActivityUtil.getDomain());

      Log.i(TAG, "userName: " + userName);
      Log.i(TAG, "url: " + url.toString());

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

      mActivityService = clientServiceFactory.createActivityService();
      mIdentityService = clientServiceFactory.createIdentityService();
      mUserIdentity    = mIdentityService.getIdentityId(ExoConstants.ACTIVITY_ORGANIZATION, userName);
      RestIdentity restIdent = mIdentityService.getIdentity(ExoConstants.ACTIVITY_ORGANIZATION,
                                                           userName);
      RestProfile profile = restIdent.getProfile();
      String[] profileArray = new String[2];
      profileArray[0] = profile.getAvatarUrl();
      profileArray[1] = profile.getFullName();

      return profileArray;
    } catch (SocialClientLibException e) {
      Log.d(TAG, "SocialClientLibException: " + e.getLocalizedMessage());
      return null;
    } catch (RuntimeException e) {
      Log.d(TAG, "RuntimeException: " + e.getLocalizedMessage());
      return null;
    } catch (MalformedURLException e) {
      Log.d(TAG, "MalformedURLException: " + e.getLocalizedMessage());
      return null;
    }
  }

  @Override
  public void onPostExecute(String[] result) {

    if (result != null) {
      SocialServiceHelper.getInstance().userIdentity    = mUserIdentity;
      SocialServiceHelper.getInstance().activityService = mActivityService;
      SocialServiceHelper.getInstance().identityService = mIdentityService;
      SocialServiceHelper.getInstance().userProfile = result;
    }

    if (mListener != null) mListener.onLoadingSocialServiceFinished(result);
  }


  public void setListener(AsyncTaskListener listener) {
    mListener = listener;
  }

  public interface AsyncTaskListener {

    void onLoadingSocialServiceFinished(String[] result);
  }
}
