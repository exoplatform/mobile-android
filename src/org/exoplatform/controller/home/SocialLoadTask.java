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

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.common.RealtimeListAccess;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.IdentityService;
import org.exoplatform.social.client.api.service.QueryParams;
import org.exoplatform.social.client.core.service.QueryParamsImpl;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.ui.social.SocialTabsActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.view.MenuItem;

/**
 * The asynchronous task that loads activities from the Social REST service.
 */
public abstract class SocialLoadTask extends AsyncTask<Integer, Void, ArrayList<SocialActivityInfo>> {

  private Context                         mContext;

  private String                          okString;

  private String                          titleString;

  private String                          contentString;

  private MenuItem                        loaderItem;

  private int                             feedType                = 0;

  private boolean                         isLoadingMoreActivities = false;

  protected ActivityService<RestActivity> activityService;

  private static final String             TAG                     = "eXo____SocialLoadTask____";

  public SocialLoadTask(Context context, MenuItem loader) {
    mContext = context;
    loaderItem = loader;
    changeLanguage();
    activityService = SocialServiceHelper.getInstance().activityService;
  }

  private void changeLanguage() {
    Resources resource = mContext.getResources();
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    contentString = resource.getString(R.string.LoadingDataError);

  }

  @Override
  public void onPreExecute() {
    if (loaderItem != null)
      loaderItem.setActionView(R.layout.action_bar_loading_indicator);
  }

  /**
   * Get the list of RestActivity from the Social REST service.
   * 
   * @param identity The RestIdentity of the user.
   * @param params The parameters to send to the REST service.
   * @return The list of RestActivity.
   * @throws SocialClientLibException
   */
  protected abstract RealtimeListAccess<RestActivity> getRestActivityList(RestIdentity identity, QueryParams params) throws SocialClientLibException;

  /**
   * Get the list of SocialActivity for the current stream.
   * 
   * @return the list of SocialActivityInfo.
   */
  protected abstract ArrayList<SocialActivityInfo> getSocialActivityList();

  @Override
  /*
   * Parameters are expected as follows: - The number of activities to load
   * (params[0]). - The current activity stream (params[1]). - [optional] The
   * position of the activity from which to load more activities (params[2]). If
   * set, the task will add more activities to the current stream.
   */
  public ArrayList<SocialActivityInfo> doInBackground(Integer... params) {
    Log.i(TAG, "load social activities - number: " + params[0] + " - type: " + params[1]);

    try {
      ArrayList<SocialActivityInfo> listActivity = new ArrayList<SocialActivityInfo>();
      int loadSize = params[0];

      IdentityService<?> identityService = SocialServiceHelper.getInstance().identityService;
      RestIdentity identity = (RestIdentity) identityService.get(SocialServiceHelper.getInstance().userIdentity);
      QueryParams queryParams = new QueryParamsImpl();
      queryParams.append(QueryParams.NUMBER_OF_LIKES_PARAM.setValue(ExoConstants.NUMBER_OF_LIKES_PARAM));
      queryParams.append(QueryParams.NUMBER_OF_COMMENTS_PARAM.setValue(ExoConstants.NUMBER_OF_COMMENTS_PARAM));
      queryParams.append(QueryParams.POSTER_IDENTITY_PARAM.setValue(true));

      feedType = params[1];

      RealtimeListAccess<RestActivity> list = getRestActivityList(identity, queryParams);
      ArrayList<SocialActivityInfo> socialList = getSocialActivityList();

      ArrayList<RestActivity> activityList = null;
      if (params.length == 3 && socialList != null) {
        isLoadingMoreActivities = true;
        SocialActivityInfo socialActiv = socialList.get(params[2]);
        RestActivity restActiv = new RestActivity();
        restActiv.setId(socialActiv.getActivityId());
        activityList = (ArrayList<RestActivity>) list.loadOlderAsList(restActiv, loadSize);
      } else {
        activityList = (ArrayList<RestActivity>) list.loadAsList(0, loadSize);
      }

      if (activityList != null && activityList.size() > 0) {
        SocialActivityInfo streamInfo = null;
        RestProfile profile = null;
        for (int i = 0; i < activityList.size(); i++) {
          RestActivity act = activityList.get(i);
          streamInfo = new SocialActivityInfo();
          profile = act.getPosterIdentity().getProfile();
          streamInfo.restActivityStream = act.getActivityStream();
          streamInfo.setActivityId(act.getId());
          streamInfo.setImageUrl(profile.getAvatarUrl());
          streamInfo.setUserName(profile.getFullName());
          streamInfo.setTitle(act.getTitle());
          streamInfo.setBody(act.getBody());
          streamInfo.setPostedTime(act.getPostedTime());

          if (SocialActivityUtil.getPlatformVersion() >= 4.0f) {
            streamInfo.setUpdatedTime(act.getLastUpdated());
          }

          streamInfo.setLikeNumber(act.getTotalNumberOfLikes());
          streamInfo.setCommentNumber(act.getTotalNumberOfComments());
          streamInfo.setType(act.getType());
          streamInfo.templateParams = act.getTemplateParams();
          listActivity.add(streamInfo);
        }
      }

      return listActivity;
    } catch (SocialClientLibException e) {
      Log.d(TAG, "SocialClientLibException: " + e.getLocalizedMessage());
      return null;
    } catch (RuntimeException e) {
      Log.d(TAG, "RuntimeException: " + e.getLocalizedMessage());
      return null;
    }
  }

  @Override
  public void onPostExecute(ArrayList<SocialActivityInfo> result) {
    stopLoadingIndicator();
    if (result != null) {
      setResult(result);
    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }
  }

  @Override
  protected void onCancelled(ArrayList<SocialActivityInfo> result) {
    stopLoadingIndicator();
    onCancelled();
  }

  // TODO refactor the loading indicator
  private void stopLoadingIndicator() {
    if (loaderItem == null && feedType != HomeController.FLIPPER_VIEW && SocialTabsActivity.instance != null)
      loaderItem = SocialTabsActivity.instance.loaderItem;

    if (loaderItem != null)
      loaderItem.setActionView(null);
  }

  public void setResult(ArrayList<SocialActivityInfo> result) {
    /** load activities for view flipper in home */
    if (feedType == HomeController.FLIPPER_VIEW && HomeActivity.homeActivity != null)
      HomeActivity.homeActivity.setSocialInfo(result);

    if (isLoadingMoreActivities) {
      SocialTabsActivity.instance.number_of_activity += result.size();
      isLoadingMoreActivities = false;
    }
  }
}
