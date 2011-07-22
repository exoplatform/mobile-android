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
import android.content.res.Resources;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cyrilmottier.android.greendroid.R;

public class SocialActivity extends MyActionBar {

  public static RestActivity                  selectedRestActivity;

  public static ActivityService<RestActivity> activityService;

  public static IdentityService<?>            identityService;

  public static String                        userIdentity;

  private LinearLayout                        activityStreamWrap;

  private ActivityLoadTask                    mLoadTask;

  private int                                 number_of_activity = 20;

  private boolean                             isShowMore         = false;

  private String                              loadingData;

  private String                              showMoreText;

  private String                              noService;

  private String                              today;

  private Resources                           resource;

  private String                              domain;

  private String                              protocol;

  private String                              host;

  private int                                 port;

  private String                              minute;

  private String                              minutes;

  private String                              hour;

  private String                              hours;

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
    resource = getResources();
    parseDomain();
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

    String strSection = SocialActivityUtil.getPostedTimeString(postedTime, AppController.bundle);
    // Check activities of today
    if (strSection.contains(minute) || strSection.contains(minutes) || strSection.contains(hour)
        || strSection.contains(hours)) {

      // Search the current array of activities for today
      return today;
    } else {
      return strSection;
    }

  }

  private void setActivityList(List<RestActivity> result) {
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    activityStreamWrap.removeAllViews();

    HashMap<String, String> actHeaderTitle = new HashMap<String, String>();

    for (int i = 0; i < result.size(); i++) {
      final RestActivity activityInfo = result.get(i);

      // String postedTimeTitle =
      // SocialActivityUtil.getHeader(activityInfo.getPostedTime(),
      // AppController.bundle);
      String postedTimeTitle = getActivityStreamHeader(activityInfo.getPostedTime());
      if (actHeaderTitle.get(postedTimeTitle) == null) {
        HeaderLayout headerLayout = new HeaderLayout(this);
        headerLayout.titleView.setText(postedTimeTitle);

        if (postedTimeTitle.equalsIgnoreCase(today))
          headerLayout.titleView.setBackgroundDrawable(resource.getDrawable(R.drawable.social_activity_browse_header_highlighted_bg));
        else
          headerLayout.titleView.setBackgroundDrawable(resource.getDrawable(R.drawable.social_activity_browse_header_normal_bg));

        actHeaderTitle.put(postedTimeTitle, "YES");
        activityStreamWrap.addView(headerLayout, params);
      }

      ActivityStreamItem item = new ActivityStreamItem(this, activityInfo);
      item.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          selectedRestActivity = activityInfo;
          Intent intent = new Intent(SocialActivity.this, ActivityStreamDisplay.class);
          startActivity(intent);

        }
      });
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
    minute = resourceBundle.getString("Minute");
    minutes = resourceBundle.getString("Minutes");
    hour = resourceBundle.getString("Hour");
    hours = resourceBundle.getString("Hours");
    today = resourceBundle.getString("Today");

  }

  private void parseDomain() {
    domain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN,
                                                      "exo_prf_domain");
    if (domain.endsWith("/")) {
      domain = domain.substring(0, domain.length() - 1);
    }
    String[] domainSplits = domain.split("//");
    protocol = domainSplits[0].substring(0, domainSplits[0].length() - 1);
    if (domainSplits[1].contains(":")) {
      String[] hostAddr = domainSplits[1].split(":");
      host = hostAddr[0];
      port = Integer.valueOf(hostAddr[1]);
    } else {
      host = domainSplits[1];
      port = ExoConstants.ACTIVITY_PORT;
    }
  }

  private boolean createConnetion() {
    try {
      String userName = AppController.sharedPreference.getString(AppController.EXO_PRF_USERNAME,
                                                                 "exo_prf_username");
      String password = AppController.sharedPreference.getString(AppController.EXO_PRF_PASSWORD,
                                                                 "exo_prf_password");

      SocialClientContext.setProtocol(protocol);
      SocialClientContext.setHost(host);
      SocialClientContext.setPort(port);
      SocialClientContext.setPortalContainerName(ExoConstants.ACTIVITY_PORTAL_CONTAINER);
      SocialClientContext.setRestContextName(ExoConstants.ACTIVITY_REST_CONTEXT);
      SocialClientContext.setRestVersion(ExoConstants.ACTIVITY_REST_VERSION);
      SocialClientContext.setUsername(userName);
      SocialClientContext.setPassword(password);

      ClientServiceFactory clientServiceFactory = ClientServiceFactoryHelper.getClientServiceFactory();
      activityService = clientServiceFactory.createActivityService();
      identityService = clientServiceFactory.createIdentityService();
      userIdentity = identityService.getIdentityId(ExoConstants.ACTIVITY_ORGANIZATION, userName);
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

        int loadSize = params[0];
        RestIdentity identity = (RestIdentity) identityService.get(userIdentity);
        RealtimeListAccess<RestActivity> list = activityService.getActivityStream(identity);
        ArrayList<RestActivity> activityList = (ArrayList<RestActivity>) list.loadAsList(0,
                                                                                         loadSize);
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

    private ActivityStreamItem(Context context, RestActivity activityInfo) {
      super(context);
      LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflate.inflate(R.layout.activitybrowserviewcell, this);
      imageViewAvatar = (AsyncImageView) view.findViewById(R.id.imageView_Avatar);
      textViewName = (TextView) view.findViewById(R.id.textView_Name);
      textViewMessage = (TextView) view.findViewById(R.id.textView_Message);
      buttonComment = (TextView) view.findViewById(R.id.button_Comment);
      buttonLike = (TextView) view.findViewById(R.id.button_Like);
      textViewTime = (TextView) view.findViewById(R.id.textView_Time);
      RestProfile profile = activityInfo.getPosterIdentity().getProfile();
      imageViewAvatar.setUrl(domain + profile.getAvatarUrl());
      textViewName.setText(profile.getFullName());
      textViewMessage.setText(Html.fromHtml(activityInfo.getTitle()));
      textViewTime.setText(SocialActivityUtil.getPostedTimeString(activityInfo.getPostedTime(),
                                                                  AppController.bundle));
      buttonComment.setText("" + activityInfo.getAvailableComments().size());
      buttonLike.setText("" + activityInfo.getLikes().size());
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

  private class HeaderLayout extends RelativeLayout {
    private TextView titleView;

    public HeaderLayout(Context context) {
      super(context);
      LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflate.inflate(R.layout.activityheadersection, this);
      titleView = (TextView) view.findViewById(R.id.textView_Section_Title);
    }

  }

}
