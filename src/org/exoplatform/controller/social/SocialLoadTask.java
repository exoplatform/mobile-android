package org.exoplatform.controller.social;

import java.util.ArrayList;

import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialDetailHelper;
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
import org.exoplatform.ui.social.SocialActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.view.View;

public class SocialLoadTask extends UserTask<Integer, Void, ArrayList<SocialActivityInfo>> {
  private SocialWaitingDialog _progressDialog;

  private Context             mContext;

  private String              loadingData;

  private String              okString;

  private String              titleString;

  private String              contentString;

  private SocialController    socialController;

  public SocialLoadTask(Context context, SocialController controller) {
    mContext = context;
    socialController = controller;
    changeLanguage();
  }

  private void changeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    loadingData = location.getString("LoadingData");
    okString = location.getString("OK");
    titleString = location.getString("Warning");
    contentString = location.getString("LoadingDataError");

  }

  @Override
  public void onPreExecute() {
    _progressDialog = new SocialWaitingDialog(mContext, null, loadingData);
    _progressDialog.show();

  }

  @Override
  public ArrayList<SocialActivityInfo> doInBackground(Integer... params) {

    try {
      ArrayList<SocialActivityInfo> listActivity = new ArrayList<SocialActivityInfo>();
      int loadSize = params[0];
      ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance()
                                                                         .getActivityService();
      IdentityService<?> identityService = SocialServiceHelper.getInstance().getIdentityService();
      RestIdentity identity;
      identity = (RestIdentity) identityService.get(SocialServiceHelper.getInstance().getUserId());
      QueryParams queryParams = new QueryParamsImpl();
      queryParams.append(QueryParams.NUMBER_OF_LIKES_PARAM.setValue(ExoConstants.NUMBER_OF_LIKES_PARAM));
      queryParams.append(QueryParams.NUMBER_OF_COMMENTS_PARAM.setValue(ExoConstants.NUMBER_OF_COMMENTS_PARAM));
      queryParams.append(QueryParams.POSTER_IDENTITY_PARAM.setValue(true));
      RealtimeListAccess<RestActivity> list = activityService.getFeedActivityStream(identity,
                                                                                    queryParams);
      ArrayList<RestActivity> activityList = (ArrayList<RestActivity>) list.loadAsList(0, loadSize);

      if (activityList.size() > 0) {
        SocialActivityInfo streamInfo = null;
        RestProfile profile = null;
        for (int i = 0; i < activityList.size(); i++) {
          RestActivity act = activityList.get(i);
          streamInfo = new SocialActivityInfo();
          profile = act.getPosterIdentity().getProfile();
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
    }catch (Exception e) {
      return null;
    }
  }

  @Override
  public void onCancelled() {
    super.onCancelled();
    _progressDialog.dismiss();
  }

  @Override
  public void onPostExecute(ArrayList<SocialActivityInfo> result) {
    if (result != null) {
      if (result.size() == 0) {
        SocialActivity.socialActivity.setEmptyView(View.VISIBLE);
      } else {
        SocialActivity.socialActivity.setEmptyView(View.GONE);
      }
      socialController.setActivityList(result);
    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }
    _progressDialog.dismiss();
    SocialDetailHelper.getInstance().taskIsFinish = true;

  }

  private class SocialWaitingDialog extends WaitingDialog {

    public SocialWaitingDialog(Context context, String titleString, String contentString) {
      super(context, titleString, contentString);
    }

    @Override
    public void onBackPressed() {
      super.onBackPressed();
      socialController.onCancelLoad();
    }

  }

}
