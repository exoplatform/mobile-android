package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.LoaderActionBarItem;

import java.util.ArrayList;
import java.util.HashMap;

import org.exoplatform.controller.home.HomeController;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.SocialActivityStreamItem;
import org.exoplatform.widget.SocialHeaderLayout;
import org.exoplatform.widget.SocialShowMoreItem;
import org.exoplatform.widget.WarningDialog;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class SocialActivity extends MyActionBar {

  private static final String           NUMBER_OF_ACTIVITY      = "NUMBER_OF_ACTIVITY";

  private static final String           NUMBER_OF_MORE_ACTIVITY = "NUMBER_OF_MORE_ACTIVITY";

  private static final String           ACCOUNT_SETTING         = "account_setting";

  private static final String           DOCUMENT_HELPER         = "document_helper";

  private LinearLayout                  activityStreamWrap;

  public static SocialActivity          socialActivity;

  private View                          emptyStubView;

  private String                        title;

  private String                        emptyString;

  private String                        showMoreText;

  private String                        today;

  private Resources                     resource;

  private String                        minute;

  private String                        minutes;

  private String                        hour;

  private String                        hours;

  private String                        okString;

  private String                        titleString;

  public int                            number_of_activity;

  public int                            number_of_more_activity;

  private int                           title_high_light        = R.drawable.social_activity_browse_header_highlighted_bg;

  private int                           title_normal            = R.drawable.social_activity_browse_header_normal_bg;

  private HomeController                homeController;

  private ArrayList<SocialActivityInfo> socialList;

  private LoaderActionBarItem           loaderItem;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem(Type.Refresh);
    getActionBar().getItem(0).setDrawable(R.drawable.action_bar_icon_refresh);
    addActionBarItem();
    getActionBar().getItem(1).setDrawable(R.drawable.action_bar_icon_compose);

    setActionBarContentView(R.layout.activitybrowserview);
    resource = getResources();
    socialActivity = this;
    activityStreamWrap = (LinearLayout) findViewById(R.id.activity_stream_wrap);
    changeLanguage();
    /*
     * Restore the previous state
     */
    if (savedInstanceState != null) {
      number_of_activity = savedInstanceState.getInt(NUMBER_OF_ACTIVITY);
      number_of_more_activity = savedInstanceState.getInt(NUMBER_OF_MORE_ACTIVITY);
      AccountSetting accountSetting = savedInstanceState.getParcelable(ACCOUNT_SETTING);
      AccountSetting.getInstance().setInstance(accountSetting);
      ArrayList<String> cookieList = AccountSetting.getInstance().cookiesList;
      ExoConnectionUtils.setCookieStore(ExoConnectionUtils.cookiesStore, cookieList);
      DocumentHelper helper = savedInstanceState.getParcelable(DOCUMENT_HELPER);
      DocumentHelper.getInstance().setInstance(helper);
    } else {
      number_of_activity = ExoConstants.NUMBER_OF_ACTIVITY;
      number_of_more_activity = ExoConstants.NUMBER_OF_MORE_ACTIVITY;
    }

    loaderItem = (LoaderActionBarItem) getActionBar().getItem(0);

    loadActivity(false);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(NUMBER_OF_ACTIVITY, number_of_activity);
    outState.putInt(NUMBER_OF_MORE_ACTIVITY, number_of_more_activity);
    outState.putParcelable(ACCOUNT_SETTING, AccountSetting.getInstance());
    outState.putParcelable(DOCUMENT_HELPER, DocumentHelper.getInstance());
  }

  public void setEmptyView(int status) {
    if (emptyStubView == null) {
      initStubView();
    }
    emptyStubView.setVisibility(status);
  }

  private void initStubView() {
    emptyStubView = ((ViewStub) findViewById(R.id.social_empty_stub)).inflate();
    ImageView emptyImage = (ImageView) emptyStubView.findViewById(R.id.empty_image);
    emptyImage.setBackgroundResource(R.drawable.icon_for_no_activities);
    TextView emptyStatus = (TextView) emptyStubView.findViewById(R.id.empty_status);
    emptyStatus.setText(emptyString);
  }

  public void loadActivity(boolean isRefresh) {
    homeController = new HomeController(this);
    socialList = SocialServiceHelper.getInstance().socialInfoList;

    if (socialList == null) {
      if (SocialServiceHelper.getInstance().activityService == null) {
        homeController.launchNewsService(loaderItem);
      } else
        homeController.onLoad(number_of_activity, loaderItem);
    } else {
      if (isRefresh) {
        homeController.onLoad(number_of_activity, loaderItem);
      } else {
        setActivityList(socialList);
      }

    }
  }

  public void setActivityList(ArrayList<SocialActivityInfo> result) {
    if (result.size() == 0) {
      SocialActivity.socialActivity.setEmptyView(View.VISIBLE);
    } else {
      SocialActivity.socialActivity.setEmptyView(View.GONE);
    }

    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    activityStreamWrap.removeAllViews();
    HashMap<String, String> actHeaderTitle = new HashMap<String, String>();

    for (int i = 0; i < result.size(); i++) {
      final SocialActivityInfo activityInfo = (SocialActivityInfo) result.get(i);

      String postedTimeTitle = getActivityStreamHeader(activityInfo.getPostedTime());
      if (actHeaderTitle.get(postedTimeTitle) == null) {
        SocialHeaderLayout headerLayout = new SocialHeaderLayout(this);
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

      SocialActivityStreamItem item = new SocialActivityStreamItem(this, activityInfo, false);

      Button likeButton = item.likeButton();
      likeButton.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {
          if (ExoConnectionUtils.isNetworkAvailableExt(SocialActivity.this)) {
            try {
              RestActivity activity = SocialServiceHelper.getInstance().activityService.get(activityInfo.getActivityId());
              if (activity.isLiked())
                SocialServiceHelper.getInstance().activityService.unlike(activity);
              else
                SocialServiceHelper.getInstance().activityService.like(activity);

              homeController.onLoad(number_of_activity, loaderItem);
            } catch (SocialClientLibException e) {
              WarningDialog dialog = new WarningDialog(SocialActivity.this,
                                                       titleString,
                                                       e.getMessage(),
                                                       okString);
              dialog.show();
            }
          } else {
            new ConnectionErrorDialog(SocialActivity.this).show();
          }

        }
      });

      Button commentButton = item.commentButton();
      commentButton.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {

          SocialDetailHelper.getInstance().setActivityId(activityInfo.getActivityId());

          Intent intent = new Intent(SocialActivity.this, ComposeMessageActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
          intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_COMMENT_TYPE);
          startActivity(intent);

        }
      });

      item.contentLayoutWrap.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          String activityId = activityInfo.getActivityId();
          SocialDetailHelper.getInstance().setActivityId(activityId);
          SocialDetailHelper.getInstance().setAttachedImageUrl(activityInfo.getAttachedImageUrl());
          Intent intent = new Intent(SocialActivity.this, SocialDetailActivity.class);
          intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
          startActivity(intent);
        }
      });
      activityStreamWrap.addView(item, params);

    }
    if (result.size() > number_of_activity || result.size() == number_of_activity) {
      final SocialShowMoreItem showmore = new SocialShowMoreItem(this);
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

    String strSection = SocialActivityUtil.getPostedTimeString(this, postedTime);
    // Check activities of today
    if (strSection.contains(minute) || strSection.contains(minutes) || strSection.contains(hour)
        || strSection.contains(hours)) {

      // Search the current array of activities for today
      return today;
    } else {
      return strSection;
    }

  }

  @Override
  public void onBackPressed() {
    homeController.finishService();
    finish();
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      finish();
      break;
    case 0:
      loaderItem = (LoaderActionBarItem) item;
      loadActivity(true);
      break;
    case 1:
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_POST_TYPE);
      startActivity(intent);
      break;

    }
    return true;

  }

  private void changeLanguage() {
    title = resource.getString(R.string.ActivityStream);
    setTitle(title);
    emptyString = resource.getString(R.string.EmptyActivity);
    showMoreText = resource.getString(R.string.ShowMore);
    minute = resource.getString(R.string.Minute);
    minutes = resource.getString(R.string.Minutes);
    hour = resource.getString(R.string.Hour);
    hours = resource.getString(R.string.Hours);
    today = resource.getString(R.string.Today);
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
  }

}
