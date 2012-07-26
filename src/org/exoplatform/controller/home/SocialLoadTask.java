package org.exoplatform.controller.home;

import greendroid.widget.LoaderActionBarItem;

import java.util.ArrayList;

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
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import com.cyrilmottier.android.greendroid.R;

public class SocialLoadTask extends AsyncTask<Integer, Void, ArrayList<SocialActivityInfo>> {

  private Context             mContext;

  private String              okString;

  private String              titleString;

  private String              contentString;

  private LoaderActionBarItem loaderItem;

  private int                 feedType = 0;

  public SocialLoadTask(Context context, LoaderActionBarItem loader) {
    mContext = context;
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

  @Override
  public ArrayList<SocialActivityInfo> doInBackground(Integer... params) {

    try {
      ArrayList<SocialActivityInfo> listActivity = new ArrayList<SocialActivityInfo>();
      int loadSize = params[0];
      ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance().activityService;
      IdentityService<?> identityService = SocialServiceHelper.getInstance().identityService;
      RestIdentity identity = (RestIdentity) identityService.get(SocialServiceHelper.getInstance().userIdentity);
      QueryParams queryParams = new QueryParamsImpl();
      queryParams.append(QueryParams.NUMBER_OF_LIKES_PARAM.setValue(ExoConstants.NUMBER_OF_LIKES_PARAM));
      queryParams.append(QueryParams.NUMBER_OF_COMMENTS_PARAM.setValue(ExoConstants.NUMBER_OF_COMMENTS_PARAM));
      queryParams.append(QueryParams.POSTER_IDENTITY_PARAM.setValue(true));
      feedType = params[1];
      RealtimeListAccess<RestActivity> list = null;
      switch (feedType) {
      case SocialTabsActivity.ALL_UPDATES:
        list = activityService.getFeedActivityStream(identity, queryParams);
        break;
      case SocialTabsActivity.MY_CONNECTIONS:
        list = activityService.getConnectionsActivityStream(identity, queryParams);
        break;
      case SocialTabsActivity.MY_SPACES:
        list = activityService.getSpacesActivityStream(identity, queryParams);
        break;
      case SocialTabsActivity.MY_STATUS:
        list = activityService.getActivityStream(identity, queryParams);
        break;
      }

      ArrayList<RestActivity> activityList = (ArrayList<RestActivity>) list.loadAsList(0, loadSize);

      if (activityList.size() > 0) {
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
          streamInfo.setLikeNumber(act.getTotalNumberOfLikes());
          streamInfo.setCommentNumber(act.getTotalNumberOfComments());
          streamInfo.setType(act.getType());
          streamInfo.templateParams = act.getTemplateParams();
          listActivity.add(streamInfo);
        }
      }
      return listActivity;
    } catch (SocialClientLibException e) {
      return null;
    } catch (RuntimeException e) {
      return null;
    }
  }

  @Override
  public void onPostExecute(ArrayList<SocialActivityInfo> result) {
    if (result != null) {

      setResult(result);

    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }
    loaderItem.setLoading(false);
  }

  public void setResult(ArrayList<SocialActivityInfo> result) {
    // if (SocialTabsActivity.instance != null)
    // SocialTabsActivity.instance.setActivityList(result);

    switch (feedType) {
    case SocialTabsActivity.ALL_UPDATES:
      SocialServiceHelper.getInstance().socialInfoList = result;
      if (HomeActivity.homeActivity != null) {
        HomeActivity.homeActivity.setSocialInfo(result);
      }
      // if (SocialTabsActivity.instance != null)
      // SocialTabsActivity.instance.setActivityList(result);
      break;
    case SocialTabsActivity.MY_CONNECTIONS:
      SocialServiceHelper.getInstance().myConnectionsList = result;
      break;
    case SocialTabsActivity.MY_SPACES:
      SocialServiceHelper.getInstance().mySpacesList = result;
      break;
    case SocialTabsActivity.MY_STATUS:
      SocialServiceHelper.getInstance().myStatusList = result;
      break;
    }
  }

}
