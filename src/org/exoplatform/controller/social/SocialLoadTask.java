package org.exoplatform.controller.social;

import java.util.ArrayList;

import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.common.RealtimeListAccess;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.IdentityService;
import org.exoplatform.social.entity.ExoSocialActivity;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.SocialWaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;

public class SocialLoadTask extends UserTask<Integer, Void, ArrayList<ExoSocialActivity>> {
  private SocialWaitingDialog _progressDialog;

  private Context             mContext;

  private String              loadingData;

  private String              okString;

  private String              titleString;

  private String              contentString;

  private SocialController socialController;
  public SocialLoadTask(Context context,SocialController controller) {
    mContext = context;
    socialController = controller;
    changeLanguage();
  }

  private void changeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    loadingData = location.getString("LoadingData");
    okString = location.getString("OK");
    titleString = location.getString("Warning");
    contentString = location.getString("ConnectionError");
  }

  @Override
  public void onPreExecute() {
    _progressDialog = new SocialWaitingDialog(mContext, null, loadingData);
    _progressDialog.show();

  }

  @Override
  public ArrayList<ExoSocialActivity> doInBackground(Integer... params) {

    try {
      ArrayList<ExoSocialActivity> streamInfoList = new ArrayList<ExoSocialActivity>();

      int loadSize = params[0];
      ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance()
                                                                         .getActivityService();
      IdentityService<?> identityService = SocialServiceHelper.getInstance().getIdentityService();
      RestIdentity identity = (RestIdentity) identityService.get(SocialServiceHelper.getInstance()
                                                                                    .getUserId());
      RealtimeListAccess<RestActivity> list = activityService.getActivityStream(identity);
      ArrayList<RestActivity> activityList = (ArrayList<RestActivity>) list.loadAsList(0, loadSize);
      ExoSocialActivity streamInfo = null;
      RestProfile profile = null;
      for (RestActivity act : activityList) {
        streamInfo = new ExoSocialActivity();
        profile = act.getPosterIdentity().getProfile();
        streamInfo.setActivityId(act.getId());
        streamInfo.setImageUrl(profile.getAvatarUrl());
        streamInfo.setUserName(profile.getFullName());
        streamInfo.setTitle(act.getTitle());
        streamInfo.setPostedTime(act.getPostedTime());
        streamInfo.setLikeNumber(act.getLikes().size());
        streamInfo.setCommentNumber(act.getAvailableComments().size());
        streamInfoList.add(streamInfo);
      }
      return streamInfoList;
    } catch (RuntimeException e) {
      return null;
    }
  }

  @Override
  public void onCancelled() {
    super.onCancelled();
    _progressDialog.dismiss();
  }

  @Override
  public void onPostExecute(ArrayList<ExoSocialActivity> result) {

    if (result != null) {
      socialController.setActivityList(result);
    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }
    _progressDialog.dismiss();

  }

}
