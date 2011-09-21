package org.exoplatform.controller.social;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestComment;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestLike;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.social.entity.ExoSocialComment;
import org.exoplatform.social.entity.ExoSocialLike;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.WarningDialog;

import android.app.ProgressDialog;
import android.content.Context;

public class SocialDetailLoadTask extends UserTask<Void, Void, Integer> {
  private RestActivity                selectedRestActivity;

  private LinkedList<ExoSocialLike>   likeLinkedList    = new LinkedList<ExoSocialLike>();

  private ArrayList<ExoSocialComment> socialCommentList = new ArrayList<ExoSocialComment>();

  private ProgressDialog              _progressDialog;

  private Context                     mContext;

  private String                      loadingData;

  private String                      youText;

  private String                      okString;

  private String                      titleString;

  private String                      contentString;

  private SocialDetailController      detailController;

  private RestProfile                 profile;

  private String                      title;

  private long                        postedTime;

  public SocialDetailLoadTask(Context context, SocialDetailController controller) {
    mContext = context;
    detailController = controller;
    changeLanguage();
  }

  @Override
  public void onPreExecute() {
    _progressDialog = ProgressDialog.show(mContext, null, loadingData);
  }

  @Override
  public Integer doInBackground(Void... params) {

    try {
      String activityId = SocialDetailHelper.getInstance().getActivityId();
      selectedRestActivity = SocialServiceHelper.getInstance().getActivityService().get(activityId);
      SocialDetailHelper.getInstance().setLiked(false);
      profile = selectedRestActivity.getPosterIdentity().getProfile();
      title = selectedRestActivity.getTitle();
      postedTime = selectedRestActivity.getPostedTime();
      List<RestLike> likeList = selectedRestActivity.getLikes();
      List<RestComment> commentList = selectedRestActivity.getAvailableComments();
      if (likeList != null) {
        for (RestLike like : likeList) {
          ExoSocialLike socialLike = new ExoSocialLike();
          String identity = like.getIdentityId();
          RestIdentity restId = (RestIdentity) SocialServiceHelper.getInstance()
                                                                  .getIdentityService()
                                                                  .get(identity);
          socialLike.setLikeID(identity);
          if (identity.equalsIgnoreCase(SocialServiceHelper.getInstance().getUserId())) {
            socialLike.setLikeName(youText);
            likeLinkedList.addFirst(socialLike);
            SocialDetailHelper.getInstance().setLiked(true);
          } else {
            socialLike.setLikeName(restId.getProfile().getFullName());
            likeLinkedList.add(socialLike);
          }

        }
      }

      if (commentList != null) {
        for (RestComment comment : commentList) {
          ExoSocialComment socialComment = new ExoSocialComment();
          String identity = comment.getIdentityId();
          RestIdentity restId = (RestIdentity) SocialServiceHelper.getInstance()
                                                                  .getIdentityService()
                                                                  .get(identity);
          RestProfile profile = restId.getProfile();
          socialComment.setCommentId(identity);
          socialComment.setCommentName(profile.getFullName());
          socialComment.setImageUrl(profile.getAvatarUrl());
          socialComment.setCommentTitle(comment.getText());
          socialComment.setPostedTime(comment.getPostedTime());

          socialCommentList.add(socialComment);
        }
      }

      return 1;
    } catch (RuntimeException e) {
      return 0;
    }
  }

  @Override
  public void onPostExecute(Integer result) {
    if (result == 1) {
      detailController.setComponentInfo(profile, title, postedTime);
      detailController.createCommentList(socialCommentList);
      detailController.setLikeInfo(likeLinkedList);
    } else {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }
    _progressDialog.dismiss();

  }

  private void changeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    loadingData = location.getString("LoadingData");
    youText = location.getString("You");
    okString = location.getString("OK");
    titleString = location.getString("Warning");
    contentString = location.getString("ConnectionError");

  }

}
