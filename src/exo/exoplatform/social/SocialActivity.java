package exo.exoplatform.social;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.client.api.ClientServiceFactory;
import org.exoplatform.social.client.api.SocialClientContext;
import org.exoplatform.social.client.api.common.RealtimeListAccess;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.IdentityService;
import org.exoplatform.social.client.core.ClientServiceFactoryHelper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

import exo.exoplatform.social.entity.ExoSocialActivity;
import exo.exoplatform.utils.SocialActivityUtil;
import exo.exoplatform.utils.UserTask;
import exo.exoplatform.utils.ExoConstants;
import exo.exoplatform.widget.MyActionBar;
import greendroid.widget.ActionBarItem;
import greendroid.widget.AsyncImageView;

public class SocialActivity extends MyActionBar {

  // public static eXoSocialActivity selectedStreamInfo;

  public static RestActivity            selectedRestActivity;

  public static ActivityService         activityService;

  public static IdentityService         identityService;

  public static String                  userIdentity;

  private Resources                     resource;

  private LinearLayout                  activityStreamWrap;

  private ActivityLoadTask              mLoadTask;

  private int                           number_of_activity = 20;

  private boolean                       isShowMore         = false;

  private ArrayList<ActivityStreamItem> itemlist;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setTheme(R.style.Theme_eXo);

    setTitle("Activity Streams");
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    addActionBarItem();
    getActionBar().getItem(0).setDrawable(R.drawable.gd_action_bar_compose);

    setActionBarContentView(R.layout.activitybrowserview);
    resource = getResources();
    activityStreamWrap = (LinearLayout) findViewById(R.id.activity_stream_wrap);
    if (createConnetion()) {
      onLoad(number_of_activity);
    }

  }

  @Override
  protected void onResume() {
    super.onResume();
    onLoad(number_of_activity);
  }

  private void setActivityList(List<RestActivity> result) {
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    activityStreamWrap.removeAllViews();
    itemlist = new ArrayList<SocialActivity.ActivityStreamItem>();
    for (int i = result.size() - 1; i >= 0; i--) {
      final RestActivity streamInfo = result.get(i);
      ActivityStreamItem item = new ActivityStreamItem(this, streamInfo);
      item.setOnClickListener(new OnClickListener() {

        @Override
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

          @Override
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
      finish();
      return false;
    }

  }

  private class ActivityLoadTask extends UserTask<Integer, Void, List<RestActivity>> {
    private ProgressDialog _progressDialog;

    @Override
    public void onPreExecute() {
      _progressDialog = ProgressDialog.show(SocialActivity.this, null, "Loading Data ...");
    }

    @Override
    public List<RestActivity> doInBackground(Integer... params) {

      int loadSize = params[0];

      ArrayList<ExoSocialActivity> streamInfoList = new ArrayList<ExoSocialActivity>();

      RestIdentity identity = (RestIdentity) identityService.get(userIdentity);

      RealtimeListAccess<RestActivity> list = activityService.getActivityStream(identity);

      ArrayList<RestActivity> activityList = (ArrayList<RestActivity>) list.loadAsList(0, loadSize);
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
      textViewTime.setText(SocialActivityUtil.getPostedTimeString(streamInfo.getPostedTime()));
      buttonComment.setText("" + streamInfo.getAvailableComments().size());
      buttonLike.setText("" + streamInfo.getLikes().size());
      // System.out.println("number of comment " +
      // streamInfo.getTotalNumberOfComments());
    }

  }

  private class ShowMoreItem extends LinearLayout {

    private Button showMoreBtn;

    public ShowMoreItem(Context context) {
      super(context);
      LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflate.inflate(R.layout.social_show_more_layout, this);
      showMoreBtn = (Button) view.findViewById(R.id.social_show_more_btn);
    }

  }

}