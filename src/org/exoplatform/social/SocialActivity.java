package org.exoplatform.social;

import greendroid.widget.ActionBarItem;

import java.util.ArrayList;
import java.util.HashMap;

import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.common.RealtimeListAccess;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.IdentityService;
import org.exoplatform.social.entity.ExoSocialActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.SocialActivityStreamItem;
import org.exoplatform.widget.SocialHeaderLayout;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.SocialShowMoreItem;
import org.exoplatform.widget.SocialWaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.cyrilmottier.android.greendroid.R;

public class SocialActivity extends MyActionBar {

  public static SocialActivity socialActivity;

  public static String         activityId;

  private LinearLayout         activityStreamWrap;

  private ActivityLoadTask     mLoadTask;

  private int                  number_of_activity = 20;

  private boolean              isShowMore         = false;

  private String               loadingData;

  private String               showMoreText;

  private String               today;

  private Resources            resource;

  private String               minute;

  private String               minutes;

  private String               hour;

  private String               hours;

  private String               okString;

  private String               titleString;

  private String               contentString;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setTheme(R.style.Theme_eXo);
    socialActivity = this;

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem();
    getActionBar().getItem(0).setDrawable(R.drawable.gd_action_bar_refresh);
    addActionBarItem();
    getActionBar().getItem(1).setDrawable(R.drawable.gd_action_bar_compose);

    setActionBarContentView(R.layout.activitybrowserview);
    onChangeLanguage();
    activityStreamWrap = (LinearLayout) findViewById(R.id.activity_stream_wrap);
    resource = getResources();
    onLoad();

  }

  // @Override
  // protected void onResume() {
  // super.onResume();
  // onLoad(number_of_activity);
  // }

  private void destroy() {
    super.onDestroy();
    finish();
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

  private void setActivityList(ArrayList<ExoSocialActivity> result) {
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    activityStreamWrap.removeAllViews();

    HashMap<String, String> actHeaderTitle = new HashMap<String, String>();

    for (int i = 0; i < result.size(); i++) {
      final ExoSocialActivity activityInfo = result.get(i);

      // String postedTimeTitle =
      // SocialActivityUtil.getHeader(activityInfo.getPostedTime(),
      // AppController.bundle);
      String postedTimeTitle = getActivityStreamHeader(activityInfo.getPostedTime());
      if (actHeaderTitle.get(postedTimeTitle) == null) {
        SocialHeaderLayout headerLayout = new SocialHeaderLayout(this);
        headerLayout.titleView.setText(postedTimeTitle);

        if (postedTimeTitle.equalsIgnoreCase(today))
          headerLayout.titleView.setBackgroundDrawable(resource.getDrawable(R.drawable.social_activity_browse_header_highlighted_bg));
        else
          headerLayout.titleView.setBackgroundDrawable(resource.getDrawable(R.drawable.social_activity_browse_header_normal_bg));

        actHeaderTitle.put(postedTimeTitle, "YES");
        activityStreamWrap.addView(headerLayout, params);
      }

      SocialActivityStreamItem item = new SocialActivityStreamItem(this, activityInfo);
      item.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          activityId = activityInfo.getActivityId();
          Intent intent = new Intent(SocialActivity.this, ActivityStreamDisplay.class);
          startActivity(intent);
        }
      });
      activityStreamWrap.addView(item, params);

    }
    if (result.size() > number_of_activity || result.size() == number_of_activity) {
      if (isShowMore == false) {
        SocialShowMoreItem showmore = new SocialShowMoreItem(this);
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

  private void onLoad() {
    if (mLoadTask == null || mLoadTask.getStatus() == ActivityLoadTask.Status.FINISHED) {
      mLoadTask = (ActivityLoadTask) new ActivityLoadTask().execute(number_of_activity);
    }
  }

  private void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == ActivityLoadTask.Status.RUNNING) {
      System.out.println("onCancelLoad");
      mLoadTask.onCancelled();
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    onCancelLoad();
    destroy();
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      onCancelLoad();
      finish();
      break;
    case 0:
      onLoad();
      break;
    case 1:
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_POST_TYPE);
      startActivity(intent);
      onCancelLoad();

      break;

    }
    return true;

  }

  private void onChangeLanguage() {
    LocalizationHelper location = LocalizationHelper.getInstance();
    String title = location.getString("ActivityStream");
    setTitle(title);
    loadingData = location.getString("LoadingData");
    showMoreText = location.getString("ShowMore");
    minute = location.getString("Minute");
    minutes = location.getString("Minutes");
    hour = location.getString("Hour");
    hours = location.getString("Hours");
    today = location.getString("Today");
    okString = location.getString("OK");
    titleString = location.getString("Warning");
    contentString = location.getString("ConnectionError");
  }

  private class ActivityLoadTask extends UserTask<Integer, Void, ArrayList<ExoSocialActivity>> {
    private SocialWaitingDialog _progressDialog;

    @Override
    public void onPreExecute() {
      _progressDialog = new SocialWaitingDialog(SocialActivity.this, null, loadingData);
      _progressDialog.show();

    }

    @Override
    public ArrayList<ExoSocialActivity> doInBackground(Integer... params) {

      try {
        ArrayList<ExoSocialActivity> streamInfoList = new ArrayList<ExoSocialActivity>();

        int loadSize = params[0];
        // RestIdentity identity = (RestIdentity)
        // ExoApplicationsController2.identityService.get(ExoApplicationsController2.userIdentity);
        ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance()
                                                                           .getActivityService();
        IdentityService<?> identityService = SocialServiceHelper.getInstance().getIdentityService();
        RestIdentity identity = (RestIdentity) identityService.get(SocialServiceHelper.getInstance()
                                                                                      .getUserId());
        RealtimeListAccess<RestActivity> list = activityService.getActivityStream(identity);
        ArrayList<RestActivity> activityList = (ArrayList<RestActivity>) list.loadAsList(0,
                                                                                         loadSize);
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
          // List<RestLike> likeList = act.getLikes();
          // streamInfo.setLikelist(likeList);
          // streamInfo.setLikeNumber(likeList.size());
          streamInfo.setLikeNumber(act.getLikes().size());
          // List<RestComment> commentList = act.getAvailableComments();
          // streamInfo.setCommentList(commentList);
          // streamInfo.setCommentNumber(commentList.size());
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
        setActivityList(result);
      } else {
        WarningDialog dialog = new WarningDialog(SocialActivity.this,
                                                 titleString,
                                                 contentString,
                                                 okString);
        dialog.show();
      }
      _progressDialog.dismiss();

    }

  }

}
