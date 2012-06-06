/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.controller.social;

import java.util.LinkedList;
import java.util.List;

import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.QueryParams;
import org.exoplatform.social.client.core.service.QueryParamsImpl;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.SocialDetailsWarningDialog;

import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;

import com.cyrilmottier.android.greendroid.R;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jun
 * 6, 2012
 */
public class LikeLoadTask extends AsyncTask<String, Void, LinkedList<SocialLikeInfo>> {

  private Context                mContext;

  private String                 youText;

  private String                 okString;

  private String                 titleString;

  private String                 detailsErrorStr;

  private SocialDetailController detailController;

  public LikeLoadTask(Context context, SocialDetailController controller) {
    mContext = context;
    detailController = controller;
    changeLanguage();
  }

  @Override
  protected LinkedList<SocialLikeInfo> doInBackground(String... params) {
    try {
      String activityId = params[0];
      boolean liked = SocialDetailHelper.getInstance().getLiked();
      RestActivity activity = SocialServiceHelper.getInstance().activityService.get(activityId);

      if (liked == true) {
        SocialServiceHelper.getInstance().activityService.unlike(activity);
        SocialDetailHelper.getInstance().setLiked(false);
      } else {
        SocialServiceHelper.getInstance().activityService.like(activity);
      }

      LinkedList<SocialLikeInfo> likeLinkedList = new LinkedList<SocialLikeInfo>();

      ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance().activityService;
      QueryParams queryParams = new QueryParamsImpl();
      queryParams.append(QueryParams.NUMBER_OF_LIKES_PARAM.setValue(ExoConstants.NUMBER_OF_LIKES_PARAM));
      // queryParams.append(QueryParams.NUMBER_OF_COMMENTS_PARAM.setValue(ExoConstants.NUMBER_OF_COMMENTS_PARAM));
      queryParams.append(QueryParams.POSTER_IDENTITY_PARAM.setValue(true));
      RestActivity restActivity = activityService.get(activityId, queryParams);
      List<RestIdentity> likeList = restActivity.getAvailableLikes();
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
      return likeLinkedList;
    } catch (SocialClientLibException e) {
      return null;
    }
  }

  @Override
  protected void onPostExecute(LinkedList<SocialLikeInfo> result) {
    if (result != null) {
      detailController.setLikedState();
      detailController.setLikeInfoText(result);
      detailController.setLikedInfo(result);
    } else {
      SocialDetailsWarningDialog dialog = new SocialDetailsWarningDialog(mContext,
                                                                         titleString,
                                                                         detailsErrorStr,
                                                                         okString,
                                                                         false);
      dialog.show();
    }
  }

  private void changeLanguage() {
    Resources resource = mContext.getResources();
    youText = resource.getString(R.string.You);
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    detailsErrorStr = resource.getString(R.string.DetailsNotAvaiable);

  }

}
