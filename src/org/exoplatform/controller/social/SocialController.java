package org.exoplatform.controller.social;

import java.util.ArrayList;
import java.util.HashMap;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.ui.social.ComposeMessageActivity;
import org.exoplatform.ui.social.SocialActivity;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.utils.SocialCache;
import org.exoplatform.widget.SocialActivityStreamItem;
import org.exoplatform.widget.SocialHeaderLayout;
import org.exoplatform.widget.SocialShowMoreItem;
import org.exoplatform.widget.WarningDialog;

import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;

public class SocialController {
  private int            number_of_activity;

  private int            number_of_more_activity;

  private SocialActivity mContext;

  private SocialLoadTask mLoadTask;

  private LinearLayout   activityStreamWrap;

  // private boolean isShowMore = false;

  private String         showMoreText;

  private String         today;

  private Resources      resource;

  private String         minute;

  private String         minutes;

  private String         hour;

  private String         hours;

  private String         okString;

  private String         titleString;

  public SocialController(SocialActivity context, LinearLayout layout) {
    mContext = context;
    activityStreamWrap = layout;
    resource = context.getResources();
    onChangeLanguage();
    number_of_activity = ExoConstants.NUMBER_OF_ACTIVITY;
    number_of_more_activity = ExoConstants.NUMBER_OF_MORE_ACTIVITY;
  }

  public void onLoad() {
    if (mLoadTask == null || mLoadTask.getStatus() == SocialLoadTask.Status.FINISHED) {
      mLoadTask = (SocialLoadTask) new SocialLoadTask(mContext, this).execute(number_of_activity);
    }
  }

  public void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == SocialLoadTask.Status.RUNNING) {
      mLoadTask.onCancelled();
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  public void setActivityList(ArrayList<SocialActivityInfo> result) {

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
          headerLayout.titleView.setBackgroundDrawable(resource.getDrawable(R.drawable.social_activity_browse_header_highlighted_bg));
        else
          headerLayout.titleView.setBackgroundDrawable(resource.getDrawable(R.drawable.social_activity_browse_header_normal_bg));

        actHeaderTitle.put(postedTimeTitle, "YES");
        activityStreamWrap.addView(headerLayout, params);
      }

      SocialActivityStreamItem item = new SocialActivityStreamItem(mContext, activityInfo, false);

      Button likeButton = item.likeButton();
      likeButton.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {
          try {
            RestActivity activity = SocialServiceHelper.getInstance()
                                                       .getActivityService()
                                                       .get(activityInfo.getActivityId());
            if (activity.isLiked())
              SocialServiceHelper.getInstance().getActivityService().unlike(activity);
            else
              SocialServiceHelper.getInstance().getActivityService().like(activity);

            onLoad();
          } catch (RuntimeException e) {
            WarningDialog dialog = new WarningDialog(mContext,
                                                     titleString,
                                                     e.getMessage(),
                                                     okString);
            dialog.show();
          }

        }
      });

      Button commentButton = item.commentButton();
      commentButton.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {

          SocialDetailHelper.getInstance().setActivityId(activityInfo.getActivityId());

          Intent intent = new Intent(mContext, ComposeMessageActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
          intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          mContext.startActivity(intent);
        }
      });
      activityStreamWrap.addView(item, params);

    }
    // System.out.println("result size =   " + result.size());
    if (result.size() > number_of_activity || result.size() == number_of_activity) {
      SocialShowMoreItem showmore = new SocialShowMoreItem(mContext);
      showmore.showMoreBtn.setText(showMoreText);
      showmore.showMoreBtn.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          number_of_activity += number_of_more_activity;
          onLoad();

        }
      });
      activityStreamWrap.addView(showmore, params);
    }
  }

  private String getActivityStreamHeader(long postedTime) {

    String strSection = SocialActivityUtil.getPostedTimeString(postedTime);
    // Check activities of today
    if (strSection.contains(minute) || strSection.contains(minutes) || strSection.contains(hour)
        || strSection.contains(hours)) {

      // Search the current array of activities for today
      return today;
    } else {
      return strSection;
    }

  }

  private void onChangeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    showMoreText = location.getString("ShowMore");
    minute = location.getString("Minute");
    minutes = location.getString("Minutes");
    hour = location.getString("Hour");
    hours = location.getString("Hours");
    today = location.getString("Today");
    okString = location.getString("OK");
    titleString = location.getString("Warning");

  }

}
