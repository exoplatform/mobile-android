package org.exoplatform.social;

import greendroid.widget.ActionBarItem;
import greendroid.widget.AsyncImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.controller.AppController;
import org.exoplatform.social.client.api.ClientServiceFactory;
import org.exoplatform.social.client.api.SocialClientContext;
import org.exoplatform.social.client.api.common.RealtimeListAccess;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.IdentityService;
import org.exoplatform.social.client.core.ClientServiceFactoryHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.MyActionBar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cyrilmottier.android.greendroid.R;

public class SocialActivity extends MyActionBar {

  // public static eXoSocialActivity selectedStreamInfo;

  public static RestActivity                  selectedRestActivity;

  public static ActivityService<RestActivity> activityService;

  public static IdentityService<?>            identityService;

  public static String                        userIdentity;

  private LinearLayout                        activityStreamWrap;

  private ActivityLoadTask                    mLoadTask;

  private int                                 number_of_activity = 20;

  private boolean                             isShowMore         = false;

  private ArrayList<ActivityStreamItem>       itemlist;

  private String                              loadingData;

  private String                              showMoreText;

  private String                              noService;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setTheme(R.style.Theme_eXo);

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem();
    getActionBar().getItem(0).setDrawable(R.drawable.gd_action_bar_compose);
    setActionBarContentView(R.layout.activitybrowserview);
    onChangeLanguage(AppController.bundle);
    activityStreamWrap = (LinearLayout) findViewById(R.id.activity_stream_wrap);
    if (createConnetion() == true) {
      onLoad(number_of_activity);
    }

  }

  @Override
  protected void onResume() {
    super.onResume();
    onLoad(number_of_activity);
  }

  private void destroy() {
    super.onDestroy();
    finish();
  }

  private String getActivityStreamHeader(long postedTime) {

    String returnValue = "";

    String strSection = SocialActivityUtil.getPostedTimeString(postedTime, AppController.bundle);
    // Check activities of today
    if (strSection.contains("minute") || strSection.contains("minutes")
        || strSection.contains("hour") || strSection.contains("hours")) {

      // Search the current array of activities for today
      returnValue = "Today";
    } else {
      returnValue = strSection;
    }

    return returnValue;
  }

  private void setActivityList(List<RestActivity> result) {
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    activityStreamWrap.removeAllViews();

    HashMap<String, String> actHeaderTitle = new HashMap<String, String>();

    itemlist = new ArrayList<SocialActivity.ActivityStreamItem>();

    for (int i = result.size() - 1; i >= 0; i--) {
      final RestActivity streamInfo = result.get(i);

      String postedTimeTitle = getActivityStreamHeader(streamInfo.getPostedTime());
      if (actHeaderTitle.get(postedTimeTitle) == null) {

        TextView header = new TextView(this);
        header.setText(postedTimeTitle);
        header.setTextColor(Color.BLACK);
        if (postedTimeTitle.equalsIgnoreCase("Today"))
          header.setBackgroundDrawable(getResources().getDrawable(R.drawable.social_activity_browse_header_highlighted_bg));
        else
          header.setBackgroundDrawable(getResources().getDrawable(R.drawable.social_activity_browse_header_normal_bg));

        actHeaderTitle.put(postedTimeTitle, "YES");
        activityStreamWrap.addView(header, params);
      }

      ActivityStreamItem item = new ActivityStreamItem(this, streamInfo);
      item.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          // selectedStreamInfo = streamInfo;
          selectedRestActivity = streamInfo;
          Intent intent = new Intent(SocialActivity.this, ActivityStreamDisplay.class);
          startActivity(intent);

        }
      });
      itemlist.add(item);
      activityStreamWrap.addView(item, params);

    }
    if (result.size() > number_of_activity || result.size() == number_of_activity) {
      if (isShowMore == false) {
        ShowMoreItem showmore = new ShowMoreItem(this);
        showmore.showMoreBtn.setOnClickListener(new OnClickListener() {

          public void onClick(View v) {
            number_of_activity += 5;
            onLoad(number_of_activity);
            isShowMore = true;

          }
        });
        activityStreamWrap.addView(showmore, params);
      }
    }

  }

  private void onLoad(int loadSize) {
    if (mLoadTask == null || mLoadTask.getStatus() == ActivityLoadTask.Status.FINISHED) {
      mLoadTask = (ActivityLoadTask) new ActivityLoadTask().execute(loadSize);
    }
  }

  private void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == ActivityLoadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    onCancelLoad();
    finish();
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      onCancelLoad();
      finish();
      break;
    case 0:
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_POST_TYPE);
      startActivity(intent);

      break;

    }
    return true;

  }

  private void onChangeLanguage(ResourceBundle resourceBundle) {

    String title = resourceBundle.getString("ActivityStream");
    setTitle(title);
    loadingData = resourceBundle.getString("LoadingData");
    showMoreText = resourceBundle.getString("ShowMore");
    noService = resourceBundle.getString("NoService");

  }

  private boolean createConnetion() {
    try {
      SocialClientContext.setProtocol(ExoConstants.ACTIVITY_PROTOCOL);
      SocialClientContext.setHost(ExoConstants.ACTIVITY_HOST);
      SocialClientContext.setPort(ExoConstants.ACTIVITY_PORT);
      SocialClientContext.setPortalContainerName(ExoConstants.ACTIVITY_PORTAL_CONTAINER);
      SocialClientContext.setRestContextName(ExoConstants.ACTIVITY_REST_CONTEXT);
      SocialClientContext.setRestVersion(ExoConstants.ACTIVITY_REST_VERSION);
      SocialClientContext.setUsername("root");
      SocialClientContext.setPassword("gtn");

      ClientServiceFactory clientServiceFactory = ClientServiceFactoryHelper.getClientServiceFactory();
      activityService = clientServiceFactory.createActivityService();
      identityService = clientServiceFactory.createIdentityService();
      userIdentity = identityService.getIdentityId(ExoConstants.ACTIVITY_ORGANIZATION, "root");
      return true;
    } catch (RuntimeException e) {
      Toast toast = Toast.makeText(this, noService, Toast.LENGTH_LONG);
      toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
      toast.show();
      destroy();
      return false;
    }

  }

  private class ActivityLoadTask extends UserTask<Integer, Void, List<RestActivity>> {
    private ProgressDialog _progressDialog;

    @Override
    public void onPreExecute() {
      _progressDialog = ProgressDialog.show(SocialActivity.this, null, loadingData);
    }

    @Override
    public List<RestActivity> doInBackground(Integer... params) {

      try {
        Log.e("Param1:", " " + params[0]);
        // Log.e("Param2:", " " + params[1]);

        int loadSize = params[0];

        // ArrayList<ExoSocialActivity> streamInfoList = new
        // ArrayList<ExoSocialActivity>();

        RestIdentity identity = (RestIdentity) identityService.get(userIdentity);

        RealtimeListAccess<RestActivity> list = activityService.getActivityStream(identity);

        ArrayList<RestActivity> activityList = (ArrayList<RestActivity>) list.loadAsList(0,
                                                                                         loadSize);

        // for (RestActivity act : activityList) {
        // eXoSocialActivity streamInfo = new eXoSocialActivity();
        // RestProfile profile = act.getPosterIdentity().getProfile();
        // streamInfo.setActivityId(act.getId());
        // streamInfo.setImageUrl(profile.getAvatarUrl());
        // streamInfo.setUserName(profile.getFullName());
        // streamInfo.setTitle(act.getTitle());
        // streamInfo.setPostedTime(act.getPostedTime());
        // List<RestLike> likeList = act.getLikes();
        // streamInfo.setLikelist(likeList);
        // streamInfo.setLikeNumber(likeList.size());
        // List<RestComment> commentList = act.getAvailableComments();
        // streamInfo.setCommentList(commentList);
        // streamInfo.setCommentNumber(commentList.size());
        // streamInfoList.add(streamInfo);
        // }

        return activityList;
      } catch (RuntimeException e) {

        return null;
      }
    }

    @Override
    public void onPostExecute(List<RestActivity> result) {

      if (result != null) {
        setActivityList(result);
      }
      _progressDialog.dismiss();

    }

  }

  private class ActivityStreamItem extends LinearLayout {
    private AsyncImageView imageViewAvatar;

    private TextView       textViewName;

    private TextView       textViewMessage;

    private TextView       buttonComment;

    private TextView       buttonLike;

    private TextView       textViewTime;

    private ActivityStreamItem(Context context, RestActivity streamInfo) {
      super(context);
      LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflate.inflate(R.layout.activitybrowserviewcell, this);
      imageViewAvatar = (AsyncImageView) view.findViewById(R.id.imageView_Avatar);
      textViewName = (TextView) view.findViewById(R.id.textView_Name);
      textViewMessage = (TextView) view.findViewById(R.id.textView_Message);
      buttonComment = (TextView) view.findViewById(R.id.button_Comment);
      buttonLike = (TextView) view.findViewById(R.id.button_Like);
      textViewTime = (TextView) view.findViewById(R.id.textView_Time);
      RestProfile profile = streamInfo.getPosterIdentity().getProfile();
      // imageViewAvatar.setUrl(profile.getAvatarUrl());
      imageViewAvatar.setUrl("http://a3.twimg.com/profile_images/740897825/AndroidCast-350_normal.png");
      textViewName.setText(profile.getFullName());
      textViewMessage.setText(Html.fromHtml(streamInfo.getTitle()));
      textViewTime.setText(SocialActivityUtil.getPostedTimeString(streamInfo.getPostedTime(),
                                                                  AppController.bundle));
      buttonComment.setText("" + streamInfo.getAvailableComments().size());
      buttonLike.setText("" + streamInfo.getLikes().size());
    }

  }

  private class ShowMoreItem extends LinearLayout {

    private Button showMoreBtn;

    public ShowMoreItem(Context context) {
      super(context);
      LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflate.inflate(R.layout.social_show_more_layout, this);
      showMoreBtn = (Button) view.findViewById(R.id.social_show_more_btn);
      showMoreBtn.setText(showMoreText);
    }

  }

}
