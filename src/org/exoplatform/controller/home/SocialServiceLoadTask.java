/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.controller.home;

import greendroid.widget.LoaderActionBarItem;

import java.net.MalformedURLException;
import java.net.URL;

import org.exoplatform.R;
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
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

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

  private String                        okString;

  private String                        titleString;

  private String                        contentString;

  private HomeController                homeController;

  private LoaderActionBarItem           loaderItem;

  private static final String TAG = "eXo____SocialServiceLoadTask____";


  public SocialServiceLoadTask(Context context,
                               HomeController controller,
                               LoaderActionBarItem loader) {
    mContext = context;
    homeController = controller;
    loaderItem = loader;
    changeLanguage();
  }

  private void changeLanguage() {
    Resources resource = mContext.getResources();
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    contentString = resource.getString(R.string.LoadingDataError);

  }

  @Override
  public void onPreExecute() {
    loaderItem.setLoading(true);
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
      if (HomeActivity.homeActivity != null) {
        HomeActivity.homeActivity.setProfileInfo(result);
      }
      if (AccountSetting.getInstance().shouldSaveProfileInfo(result[1], result[0])) {
        SettingUtils.persistServerSetting(mContext);
      }

      /** Load activities for view flipper */
      homeController.onLoad(ExoConstants.HOME_SOCIAL_MAX_NUMBER, HomeController.FLIPPER_VIEW);

    } else {
      loaderItem.setLoading(false);
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }
  }
}
