package org.exoplatform.controller.social;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.model.SocialCommentInfo;
import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestComment;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.QueryParams;
import org.exoplatform.social.client.core.service.QueryParamsImpl;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.SocialDetailsWarningDialog;
import org.exoplatform.widget.WaitingDialog;

import android.content.Context;
import android.view.View;

public class SocialDetailLoadTask extends UserTask<Void, Void, Integer> {
  private RestActivity                 selectedRestActivity;

  private LinkedList<SocialLikeInfo>   likeLinkedList    = new LinkedList<SocialLikeInfo>();

  private ArrayList<SocialCommentInfo> socialCommentList = new ArrayList<SocialCommentInfo>();

  private SocialDetailWaitingDialog    _progressDialog;

  private Context                      mContext;

  private String                       loadingData;

  private String                       youText;

  private String                       okString;

  private String                       titleString;

  private String                       networkNotReachable;

  private String                       detailsErrorStr;

  private SocialDetailController       detailController;

  private String                       activityType;

  private SocialActivityInfo           streamInfo;

  private boolean                      hasContent        = false;

  public SocialDetailLoadTask(Context context, SocialDetailController controller) {
    mContext = context;
    detailController = controller;
    changeLanguage();
  }

  @Override
  public void onPreExecute() {
    _progressDialog = new SocialDetailWaitingDialog(mContext, null, loadingData);
    _progressDialog.show();
  }

  @Override
  public Integer doInBackground(Void... params) {

    try {
      ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance()
                                                                         .getActivityService();

      String activityId = SocialDetailHelper.getInstance().getActivityId();
      QueryParams queryParams = new QueryParamsImpl();
      queryParams.append(QueryParams.NUMBER_OF_LIKES_PARAM.setValue(ExoConstants.NUMBER_OF_LIKES_PARAM));
      queryParams.append(QueryParams.NUMBER_OF_COMMENTS_PARAM.setValue(ExoConstants.NUMBER_OF_COMMENTS_PARAM));
      queryParams.append(QueryParams.POSTER_IDENTITY_PARAM.setValue(true));
      selectedRestActivity = activityService.get(activityId, queryParams);
      SocialDetailHelper.getInstance().setLiked(false);

      streamInfo = new SocialActivityInfo();
      RestProfile restProfile = selectedRestActivity.getPosterIdentity().getProfile();
      streamInfo.setActivityId(selectedRestActivity.getId());
      streamInfo.setImageUrl(restProfile.getAvatarUrl());
      streamInfo.setUserName(restProfile.getFullName());
      streamInfo.setTitle(selectedRestActivity.getTitle());
      streamInfo.setPostedTime(selectedRestActivity.getPostedTime());
      streamInfo.setLikeNumber(selectedRestActivity.getTotalNumberOfLikes());
      streamInfo.setCommentNumber(selectedRestActivity.getTotalNumberOfComments());
      activityType = selectedRestActivity.getType();
      streamInfo.setType(activityType);
      System.out.println("activityType: " + activityType);
      streamInfo.templateParams = selectedRestActivity.getTemplateParams();

      List<RestIdentity> likeList = selectedRestActivity.getAvailableLikes();
      List<RestComment> commentList = selectedRestActivity.getAvailableComments();
      if (likeList != null) {
        for (RestIdentity like : likeList) {
          SocialLikeInfo socialLike = new SocialLikeInfo();
          String identity = like.getId();
          if (identity.equalsIgnoreCase(SocialServiceHelper.getInstance().getUserId())) {
            socialLike.setLikeName(youText);
            likeLinkedList.addFirst(socialLike);
            SocialDetailHelper.getInstance().setLiked(true);
          } else {
            try {
              String likeName = new String(like.getProfile().getFullName().getBytes("ISO-8859-1"),
                                           "UTF-8");
              socialLike.setLikeName(likeName);
            } catch (UnsupportedEncodingException e) {

            }

            likeLinkedList.add(socialLike);
          }

        }
      }

      if (commentList != null) {
        for (RestComment comment : commentList) {
          SocialCommentInfo socialComment = new SocialCommentInfo();
          RestIdentity restId = comment.getPosterIdentity();

          RestProfile profile = restId.getProfile();
          socialComment.setCommentId(restId.getId());
          socialComment.setCommentName(profile.getFullName());
          socialComment.setImageUrl(profile.getAvatarUrl());
          socialComment.setCommentTitle(comment.getText());
          socialComment.setPostedTime(comment.getPostedTime());

          socialCommentList.add(socialComment);
        }
      }

      return 1;
    } catch (RuntimeException e) {
      String error = e.getMessage().toLowerCase();

      if (error.contains("http")) {
        return 0;
      }

      return -1;
    }
  }

  @Override
  public void onPostExecute(Integer result) {
    SocialDetailsWarningDialog dialog;
    if (result == 1) {
      hasContent = true;
      detailController.setComponentInfo(streamInfo);
      detailController.createCommentList(socialCommentList);
      detailController.setLikeInfo(likeLinkedList);
    } else if (result == 0) {
      dialog = new SocialDetailsWarningDialog(mContext,
                                              titleString,
                                              detailsErrorStr,
                                              okString,
                                              hasContent);
      dialog.show();
    } else {
      dialog = new SocialDetailsWarningDialog(mContext,
                                              titleString,
                                              networkNotReachable,
                                              okString,
                                              hasContent);
      dialog.show();
    }
    SocialDetailActivity.socialDetailActivity.startScreen.setVisibility(View.GONE);
    _progressDialog.dismiss();

  }

  private void changeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    loadingData = location.getString("LoadingData");
    youText = location.getString("You");
    okString = location.getString("OK");
    titleString = location.getString("Warning");
    networkNotReachable = location.getString("ConnectionError");
    detailsErrorStr = location.getString("DetailsNotAvaiable");

  }

  private class SocialDetailWaitingDialog extends WaitingDialog {

    public SocialDetailWaitingDialog(Context context, String titleString, String contentString) {
      super(context, titleString, contentString);
    }

    @Override
    public void onBackPressed() {
      super.onBackPressed();
      detailController.onCancelLoad();
    }

  }

}
