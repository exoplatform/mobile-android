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
package org.exoplatform.controller.social;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.model.SocialCommentInfo;
import org.exoplatform.model.SocialLikeInfo;
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
import org.exoplatform.ui.social.AllUpdatesFragment;
import org.exoplatform.ui.social.MyConnectionsFragment;
import org.exoplatform.ui.social.MySpacesFragment;
import org.exoplatform.ui.social.MyStatusFragment;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.ui.social.SocialTabsActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.Log;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.SocialDetailsWarningDialog;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.view.View;

public class SocialDetailLoadTask extends AsyncTask<Boolean, Void, Integer> {
  
  private RestActivity                 selectedRestActivity;

  private LinkedList<SocialLikeInfo>   likeLinkedList    = new LinkedList<SocialLikeInfo>();

  private ArrayList<SocialCommentInfo> socialCommentList = new ArrayList<SocialCommentInfo>();

  private Context                      mContext;

  private String                       youText;

  private String                       okString;

  private String                       titleString;

  private String                       detailsErrorStr;

  private SocialDetailController       detailController;

  private String                       activityType;

  private SocialActivityInfo           streamInfo;

  private boolean                      hasContent        = false;

  private boolean                      isLikeAction      = false;

  private int                          currentPosition;

  public SocialDetailLoadTask(Context context, SocialDetailController controller, int pos) {
    mContext = context;
    detailController = controller;
    currentPosition = pos;
    changeLanguage();
  }

  @Override
  public void onPreExecute() {
    detailController.setLoading(true);
  }

  @Override
  public Integer doInBackground(Boolean... params) {
    isLikeAction = params[0];

    try {
      ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance().activityService;

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

      if (SocialActivityUtil.getPlatformVersion() >= 4.0f) {
        streamInfo.setUpdatedTime(selectedRestActivity.getLastUpdated());
      }

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
          RestProfile likeProfile = like.getProfile();
          SocialLikeInfo socialLike = new SocialLikeInfo();
          socialLike.likedImageUrl = likeProfile.getAvatarUrl();
          String identity = like.getId();
          if (identity.equalsIgnoreCase(SocialServiceHelper.getInstance().userIdentity)) {
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
      if (Log.LOGD)
        Log.d(getClass().getSimpleName(), "doInBackground ", Log.getStackTraceString(e));
      return 0;
    } catch (RuntimeException e) {
      if (Log.LOGD)
        Log.d(getClass().getSimpleName(), "doInBackground ", Log.getStackTraceString(e));
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
      detailController.setLikeInfoText(likeLinkedList);
      detailController.setLikedInfo(likeLinkedList);
      if (isLikeAction) {
        if (SocialTabsActivity.instance != null) {
          int tabId = SocialTabsActivity.instance.mPager.getCurrentItem();
          switch (tabId) {
          case SocialTabsActivity.ALL_UPDATES:
            AllUpdatesFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, currentPosition);
            break;
          case SocialTabsActivity.MY_CONNECTIONS:
            MyConnectionsFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, currentPosition);
            break;
          case SocialTabsActivity.MY_SPACES:
            MySpacesFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, currentPosition);
            break;
          case SocialTabsActivity.MY_STATUS:
            MyStatusFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, currentPosition);
            break;
          }
        }
      }
    } else {
      dialog = new SocialDetailsWarningDialog(mContext, titleString, detailsErrorStr, okString, hasContent);
      dialog.show();
    }
    detailController.setLoading(false);
    SocialDetailActivity.socialDetailActivity.startScreen.setVisibility(View.GONE);
  }

  @Override
  protected void onCancelled(Integer result) {
    detailController.setLoading(false);
  }

  private void changeLanguage() {
    Resources resource = mContext.getResources();
    youText = resource.getString(R.string.You);
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    detailsErrorStr = resource.getString(R.string.DetailsNotAvaiable);
  }
}
