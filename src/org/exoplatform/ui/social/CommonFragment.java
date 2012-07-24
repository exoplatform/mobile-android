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
package org.exoplatform.ui.social;

import greendroid.widget.LoaderActionBarItem;

import java.util.ArrayList;
import java.util.HashMap;

import org.exoplatform.R;
import org.exoplatform.controller.home.HomeController;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.SocialActivityStreamItem;
import org.exoplatform.widget.SocialHeaderLayout;
import org.exoplatform.widget.SocialShowMoreItem;
import org.exoplatform.widget.WarningDialog;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jul
 * 23, 2012
 */
public class CommonFragment {

  // private LinearLayout activityStreamWrap;

  public static SocialActivity socialActivity;

  // private View emptyStubView;

  private String               showMoreText;

  private String               today;

  private Resources            resource;

  private String               minute;

  private String               minutes;

  private String               hour;

  private String               hours;

  private String               okString;

  private String               titleString;

  private int                  title_high_light = R.drawable.social_activity_browse_header_highlighted_bg;

  private int                  title_normal     = R.drawable.social_activity_browse_header_normal_bg;

  private HomeController       homeController;

  public LoaderActionBarItem  loaderItem;

  public int                  number_of_activity;

  public int                  number_of_more_activity;

  private Context              mContext;

  // public static CommonFragment newInstance() {
  // CommonFragment fragment = new CommonFragment();
  // return fragment;
  // }

  public CommonFragment(Context context,
                        HomeController controller,
                        LoaderActionBarItem loader,
                        int numAct,
                        int numMoreAct) {
    mContext = context;
    resource = context.getResources();
    homeController = controller;
    loaderItem = loader;
    number_of_activity = numAct;
    number_of_more_activity = numMoreAct;
    changeLanguage();
  }

  // public void setData(ArrayList<SocialActivityInfo> result) {
  // socialList = result;
  // }

  private void changeLanguage() {
    showMoreText = resource.getString(R.string.ShowMore);
    minute = resource.getString(R.string.Minute);
    minutes = resource.getString(R.string.Minutes);
    hour = resource.getString(R.string.Hour);
    hours = resource.getString(R.string.Hours);
    today = resource.getString(R.string.Today);
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
  }

  public ArrayList<SocialActivityInfo> getMySpaces(ArrayList<SocialActivityInfo> list) {
    ArrayList<SocialActivityInfo> result = new ArrayList<SocialActivityInfo>();
    if (list == null) {
      return result;
    }
    for (SocialActivityInfo info : list) {
      int type = SocialActivityUtil.getActivityTypeId(info.getType());
      if (type == SocialActivityUtil.CONTENT_SPACE || type == SocialActivityUtil.KS_FORUM_SPACE
          || type == SocialActivityUtil.KS_WIKI_SPACE
          || type == SocialActivityUtil.EXO_SOCIAL_SPACE) {
        result.add(info);
      }
    }

    return result;
  }

  public ArrayList<SocialActivityInfo> getMyStatus(ArrayList<SocialActivityInfo> list) {

    ArrayList<SocialActivityInfo> result = new ArrayList<SocialActivityInfo>();
    if (list == null) {
      return result;
    }
    for (SocialActivityInfo info : list) {
      int type = SocialActivityUtil.getActivityTypeId(info.getType());
      if (type == SocialActivityUtil.DEFAULT_ACTIVITY) {
        result.add(info);
      }
    }

    return result;
  }

  public ArrayList<SocialActivityInfo> getMyConnections(ArrayList<SocialActivityInfo> list) {
    ArrayList<SocialActivityInfo> result = new ArrayList<SocialActivityInfo>();
    if (list == null) {
      return result;
    }
    for (SocialActivityInfo info : list) {
      int type = SocialActivityUtil.getActivityTypeId(info.getType());
      if (type == SocialActivityUtil.EXO_SOCIAL_PEOPLE
          || type == SocialActivityUtil.EXO_SOCIAL_RELATIONSHIP) {
        result.add(info);
      }
    }

    return result;
  }
  
  public void onReload(){
    homeController.onLoad(number_of_activity, loaderItem);
  }

  public void setActivityList(ArrayList<SocialActivityInfo> result,
                              LinearLayout activityStreamWrap,
                              View emptyStubView) {
    if (result == null || result.size() == 0) {
      setEmptyView(emptyStubView, View.VISIBLE);
      return;
    }
    setEmptyView(emptyStubView, View.GONE);

    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    activityStreamWrap.removeAllViews();
    HashMap<String, String> actHeaderTitle = new HashMap<String, String>();

    for (int i = 0; i < result.size(); i++) {
      final SocialActivityInfo activityInfo = (SocialActivityInfo) result.get(i);

      String postedTimeTitle = getActivityStreamHeader(activityInfo.getPostedTime());
      if (actHeaderTitle.get(postedTimeTitle) == null) {
        SocialHeaderLayout headerLayout = new SocialHeaderLayout(mContext);
        headerLayout.titleView.setText(postedTimeTitle);

        if (postedTimeTitle.equalsIgnoreCase(today))
          headerLayout.titleView.setBackgroundDrawable(resource.getDrawable(title_high_light));
        else {
          headerLayout.titleView.setBackgroundDrawable(resource.getDrawable(title_normal));
          headerLayout.titleView.setTextColor(Color.rgb(59, 59, 59));
        }

        actHeaderTitle.put(postedTimeTitle, "YES");
        activityStreamWrap.addView(headerLayout, params);
      }

      SocialActivityStreamItem item = new SocialActivityStreamItem(mContext, activityInfo, false);

      Button likeButton = item.likeButton();
      likeButton.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {
          if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
            try {
              RestActivity activity = SocialServiceHelper.getInstance().activityService.get(activityInfo.getActivityId());
              if (activity.isLiked())
                SocialServiceHelper.getInstance().activityService.unlike(activity);
              else
                SocialServiceHelper.getInstance().activityService.like(activity);

//              homeController.onLoad(number_of_activity, loaderItem);
              onReload();
            } catch (SocialClientLibException e) {
              WarningDialog dialog = new WarningDialog(mContext,
                                                       titleString,
                                                       e.getMessage(),
                                                       okString);
              dialog.show();
            }
          } else {
            new ConnectionErrorDialog(mContext).show();
          }

        }
      });

      Button commentButton = item.commentButton();
      commentButton.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {

          SocialDetailHelper.getInstance().setActivityId(activityInfo.getActivityId());

          Intent intent = new Intent(mContext, ComposeMessageActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
          intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_COMMENT_TYPE);
          mContext.startActivity(intent);

        }
      });

      item.contentLayoutWrap.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          String activityId = activityInfo.getActivityId();
          SocialDetailHelper.getInstance().setActivityId(activityId);
          SocialDetailHelper.getInstance().setAttachedImageUrl(activityInfo.getAttachedImageUrl());
          Intent intent = new Intent(mContext, SocialDetailActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
          mContext.startActivity(intent);
        }
      });
      activityStreamWrap.addView(item, params);

    }
    if (result.size() > number_of_activity || result.size() == number_of_activity) {
      final SocialShowMoreItem showmore = new SocialShowMoreItem(mContext);
      showmore.showMoreBtn.setText(showMoreText);
      showmore.showMoreBtn.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          showmore.showMoreBtn.setClickable(false);
          number_of_activity += number_of_more_activity;
          homeController.onLoad(number_of_activity, loaderItem);
          showmore.showMoreBtn.setClickable(true);
        }
      });
      activityStreamWrap.addView(showmore, params);
    }
  }

  private String getActivityStreamHeader(long postedTime) {

    String strSection = SocialActivityUtil.getPostedTimeString(mContext, postedTime);
    // Check activities of today
    if (strSection.contains(minute) || strSection.contains(minutes) || strSection.contains(hour)
        || strSection.contains(hours)) {

      // Search the current array of activities for today
      return today;
    } else {
      return strSection;
    }

  }

  private void setEmptyView(View emptyStubView, int status) {
    emptyStubView.setVisibility(status);
  }

}
