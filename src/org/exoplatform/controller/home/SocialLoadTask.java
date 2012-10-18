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
import android.util.Log;

import com.cyrilmottier.android.greendroid.R;

public class SocialLoadTask extends AsyncTask<Integer, Void, ArrayList<SocialActivityInfo>> {

  private Context             mContext;

  private String              okString;

  private String              titleString;

  private String              contentString;

  private LoaderActionBarItem loaderItem;

  private int                 feedType = 0;
  
  private boolean				isLoadingMoreActivities = false;

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
    if (loaderItem != null)
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
      Log.d("EXO_MOB", "*** Task started, updating stream: "+feedType);
      RealtimeListAccess<RestActivity> list = null;
      ArrayList<SocialActivityInfo> socialList = null;
      switch (feedType) {
      case SocialTabsActivity.ALL_UPDATES:
        list = activityService.getFeedActivityStream(identity, queryParams);
        socialList = SocialServiceHelper.getInstance().socialInfoList;
        break;
      case SocialTabsActivity.MY_CONNECTIONS:
        list = activityService.getConnectionsActivityStream(identity, queryParams);
        socialList = SocialServiceHelper.getInstance().myConnectionsList;
        break;
      case SocialTabsActivity.MY_SPACES:
        list = activityService.getSpacesActivityStream(identity, queryParams);
        socialList = SocialServiceHelper.getInstance().mySpacesList;
        break;
      case SocialTabsActivity.MY_STATUS:
        list = activityService.getActivityStream(identity, queryParams);
        socialList = SocialServiceHelper.getInstance().myStatusList;
        break;
      }

      ArrayList<RestActivity> activityList = null;
      if (params.length == 3 && socialList != null) {
    	  isLoadingMoreActivities = true;
    	  SocialActivityInfo socialActiv = socialList.get(params[2]);
    	  RestActivity rActiv = new RestActivity();
    	  rActiv.setId(socialActiv.getActivityId());
    	  Log.d("EXO_MOB", "**** Load more activities before "+socialActiv.getActivityId());
    	  activityList = (ArrayList<RestActivity>) list.loadOlderAsList(rActiv, loadSize);
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
	  Log.d("EXO_MOB", "*** Task done");
    if (result != null) {

      setResult(result);

    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }
    if (loaderItem != null)
      loaderItem.setLoading(false);
    isLoadingMoreActivities = false;
  }

  public void setResult(ArrayList<SocialActivityInfo> result) {

    switch (feedType) {
    case SocialTabsActivity.ALL_UPDATES:
    	if (!isLoadingMoreActivities)
    		SocialServiceHelper.getInstance().socialInfoList = result;
    	else
    		SocialServiceHelper.getInstance().socialInfoList.addAll(result);
      if (HomeActivity.homeActivity != null) {
        HomeActivity.homeActivity.setSocialInfo(result);
      }
      break;
    case SocialTabsActivity.MY_CONNECTIONS:
    	if (!isLoadingMoreActivities)
    		SocialServiceHelper.getInstance().myConnectionsList = result;
    	else
    		SocialServiceHelper.getInstance().myConnectionsList.addAll(result);
      break;
    case SocialTabsActivity.MY_SPACES:
    	if (!isLoadingMoreActivities)
    		SocialServiceHelper.getInstance().mySpacesList = result;
    	else
    		SocialServiceHelper.getInstance().mySpacesList.addAll(result);
      break;
    case SocialTabsActivity.MY_STATUS:
    	if (!isLoadingMoreActivities)
    		SocialServiceHelper.getInstance().myStatusList = result;
    	else
    		SocialServiceHelper.getInstance().myStatusList.addAll(result);
      break;
    }
    if (isLoadingMoreActivities) {
      Log.d("EXO_MOB", "*** Adding "+result.size()+" activities");
  	  SocialTabsActivity.instance.number_of_activity += result.size();
  	  isLoadingMoreActivities = false;
    }
    notifyDataChange();
  }

  public void notifyDataChange() {

  }

}
