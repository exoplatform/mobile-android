package org.exoplatform.controller.social;

import java.util.ArrayList;
import java.util.HashMap;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.ui.social.SocialActivity;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.SocialActivityStreamItem;
import org.exoplatform.widget.SocialHeaderLayout;
import org.exoplatform.widget.SocialShowMoreItem;

import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class SocialController {
  private int            number_of_activity;

  private SocialActivity mContext;

  private SocialLoadTask mLoadTask;

  private LinearLayout   activityStreamWrap;

  private boolean        isShowMore = false;

  private String         showMoreText;

  private String         today;

  private Resources      resource;

  private String         minute;

  private String         minutes;

  private String         hour;

  private String         hours;

  public SocialController(SocialActivity context, LinearLayout layout) {
    mContext = context;
    activityStreamWrap = layout;
    resource = context.getResources();
    onChangeLanguage();
    number_of_activity = ExoConstants.NUMBER_OF_ACTIVITY;
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
      final SocialActivityInfo activityInfo = result.get(i);

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

      SocialActivityStreamItem item = new SocialActivityStreamItem(mContext, activityInfo);
      item.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          String activityId = activityInfo.getActivityId();
          Intent intent = new Intent(mContext, SocialDetailActivity.class);
          SocialDetailHelper.getInstance().setActivityId(activityId);
          mContext.startActivity(intent);
        }
      });
      activityStreamWrap.addView(item, params);

    }
    if (result.size() > number_of_activity || result.size() == number_of_activity) {
      if (isShowMore == false) {
        SocialShowMoreItem showmore = new SocialShowMoreItem(mContext);
        showmore.showMoreBtn.setText(showMoreText);
        showmore.showMoreBtn.setOnClickListener(new OnClickListener() {

          public void onClick(View v) {
            number_of_activity += 5;
            onLoad();
            isShowMore = true;

          }
        });
        activityStreamWrap.addView(showmore, params);
      }
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
  }

}
