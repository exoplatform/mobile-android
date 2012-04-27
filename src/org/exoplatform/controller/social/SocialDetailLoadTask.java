package org.exoplatform.controller.social;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.model.SocialCommentInfo;
import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestComment;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.QueryParams;
import org.exoplatform.social.client.core.service.QueryParamsImpl;
import org.exoplatform.ui.social.SocialActivity;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.SocialDetailWaitingDialog;
import org.exoplatform.widget.SocialDetailsWarningDialog;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.view.View;

import com.cyrilmottier.android.greendroid.R;

public class SocialDetailLoadTask extends AsyncTask<Boolean, Void, Integer> {
  private RestActivity                 selectedRestActivity;

  private LinkedList<SocialLikeInfo>   likeLinkedList    = new LinkedList<SocialLikeInfo>();

  private ArrayList<SocialCommentInfo> socialCommentList = new ArrayList<SocialCommentInfo>();

  private SocialDetailWaitingDialog    _progressDialog;

  private Context                      mContext;

  private String                       loadingData;

  private String                       youText;

  private String                       okString;

  private String                       titleString;

  private String                       detailsErrorStr;

  private SocialDetailController       detailController;

  private String                       activityType;

  private SocialActivityInfo           streamInfo;

  private boolean                      hasContent        = false;

  private boolean                      isLikeAction      = false;

  public SocialDetailLoadTask(Context context,
                              SocialDetailController controller,
                              SocialDetailWaitingDialog progressDialog) {
    mContext = context;
    detailController = controller;
    _progressDialog = progressDialog;
    changeLanguage();

  }

  @Override
  public void onPreExecute() {
    _progressDialog = new SocialDetailWaitingDialog(mContext, detailController, null, loadingData);
    _progressDialog.show();
  }

  @Override
  public Integer doInBackground(Boolean... params) {
    isLikeAction = params[0];

    try {
      if (ExoConnectionUtils.getResponseCode(AccountSetting.getInstance().getDomainName()) == 0) {
        ExoConnectionUtils.onReLogin();
      }
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
      streamInfo.setBody(selectedRestActivity.getBody());
      streamInfo.setPostedTime(selectedRestActivity.getPostedTime());
      streamInfo.setLikeNumber(selectedRestActivity.getTotalNumberOfLikes());
      streamInfo.setCommentNumber(selectedRestActivity.getTotalNumberOfComments());
      activityType = selectedRestActivity.getType();
      streamInfo.setType(activityType);
      streamInfo.restActivityStream = selectedRestActivity.getActivityStream();
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
            String likeName = like.getProfile().getFullName();
            socialLike.setLikeName(likeName);
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
    } catch (SocialClientLibException e) {
      return 0;
    } catch (IOException e) {
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
      if (isLikeAction) {
        SocialActivity.socialActivity.loadActivity();
      }
    } else {
      dialog = new SocialDetailsWarningDialog(mContext,
                                              titleString,
                                              detailsErrorStr,
                                              okString,
                                              hasContent);
      dialog.show();
    }
    SocialDetailActivity.socialDetailActivity.startScreen.setVisibility(View.GONE);
    _progressDialog.dismiss();

  }

  private void changeLanguage() {
    Resources resource = mContext.getResources();
    loadingData = resource.getString(R.string.LoadingData);
    youText = resource.getString(R.string.You);
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    detailsErrorStr = resource.getString(R.string.DetailsNotAvaiable);

  }

}
