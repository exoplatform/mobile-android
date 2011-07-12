package eXo.eXoPlatform.Activity.Social;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.social.client.api.ClientServiceFactory;
import org.exoplatform.social.client.api.SocialClientContext;
import org.exoplatform.social.client.api.common.RealtimeListAccess;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestComment;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestLike;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.cyrilmottier.android.greendroid.R;

import eXo.eXoPlatform.DataManager.Model.Social.eXoSocialActivity;
import eXo.eXoPlatform.GDSubClasses.MyActionBar;
import eXo.eXoPlatform.utils.UserTask;
import eXo.eXoPlatform.utils.eXoConstants;
import greendroid.widget.ActionBarItem;
import greendroid.widget.AsyncImageView;

public class SocialActivity extends MyActionBar {

  public static eXoSocialActivity selectedStreamInfo;

  public static ActivityService   activityService;

  public static IdentityService   identityService;

  public static String            userIdentity;

  private Resources               resource;

  private LinearLayout            activityStreamWrap;

  private ActivityLoadTask        mLoadTask;

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

    onLoad(20);
  }

  @Override
  protected void onResume() {
    // TODO Auto-generated method stub
    super.onResume();
    onLoad(20);
  }

  private void setActivityAdapter(ArrayList<eXoSocialActivity> result) {
    LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    activityStreamWrap.removeAllViews();
    for (int i = result.size() - 1; i >= 0; i--) {
      final eXoSocialActivity streamInfo = result.get(i);
      ActivityStreamItem item = new ActivityStreamItem(this, streamInfo);
      item.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          selectedStreamInfo = streamInfo;
          Intent intent = new Intent(SocialActivity.this, ActivityStreamDisplay.class);
          startActivity(intent);

        }
      });
      activityStreamWrap.addView(item, params);

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
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      onCancelLoad();
      finish();
      break;
    case 0:
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(eXoConstants.COMPOSE_TYPE, eXoConstants.COMPOSE_POST_TYPE);
      startActivity(intent);

      break;

    }
    return true;

  }

  private class ActivityLoadTask extends UserTask<Integer, Void, ArrayList<eXoSocialActivity>> {
    private ProgressDialog _progressDialog;

    @Override
    public void onPreExecute() {
      _progressDialog = ProgressDialog.show(SocialActivity.this, null, "Loading Data ...");
    }

    @Override
    public ArrayList<eXoSocialActivity> doInBackground(Integer... params) {

      int loadSize = params[0];

      ArrayList<eXoSocialActivity> streamInfoList = new ArrayList<eXoSocialActivity>();

      SocialClientContext.setProtocol(eXoConstants.ACTIVITY_PROTOCOL);
      SocialClientContext.setHost(eXoConstants.ACTIVITY_HOST);
      SocialClientContext.setPort(eXoConstants.ACTIVITY_PORT);
      SocialClientContext.setPortalContainerName(eXoConstants.ACTIVITY_PORTAL_CONTAINER);
      SocialClientContext.setRestContextName(eXoConstants.ACTIVITY_REST_CONTEXT);
      SocialClientContext.setRestVersion(eXoConstants.ACTIVITY_REST_VERSION);
      SocialClientContext.setUsername("root");
      SocialClientContext.setPassword("gtn");

      ClientServiceFactory clientServiceFactory = ClientServiceFactoryHelper.getClientServiceFactory();
      activityService = clientServiceFactory.createActivityService();
      identityService = clientServiceFactory.createIdentityService();
      userIdentity = identityService.getIdentityId(eXoConstants.ACTIVITY_ORGANIZATION, "root");

      RestIdentity identity = (RestIdentity) identityService.get(userIdentity);

      RealtimeListAccess<RestActivity> list = activityService.getActivityStream(identity);

      ArrayList<RestActivity> activityList = (ArrayList<RestActivity>) list.loadAsList(0, loadSize);
      for (RestActivity act : activityList) {
        eXoSocialActivity streamInfo = new eXoSocialActivity();
        RestProfile profile = act.getPosterIdentity().getProfile();
        streamInfo.setActivityId(act.getId());
        streamInfo.setImageUrl(profile.getAvatarUrl());
        streamInfo.setUserName(profile.getFullName());
        streamInfo.setTitle(act.getTitle());
        streamInfo.setPostedTime(act.getPostedTime());
        List<RestLike> likeList = act.getLikes();
        streamInfo.setLikelist(likeList);
        streamInfo.setLikeNumber(likeList.size());
        List<RestComment> commentList = act.getAvailableComments();
        streamInfo.setCommentList(commentList);
        streamInfo.setCommentNumber(commentList.size());
        streamInfoList.add(streamInfo);
      }

      return streamInfoList;
    }

    @Override
    public void onPostExecute(ArrayList<eXoSocialActivity> result) {
      if (result != null) {
        setActivityAdapter(result);
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

    private TextView       textViewShowMore;

    private ActivityStreamItem(Context context, eXoSocialActivity streamInfo) {
      super(context);
      LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflate.inflate(R.layout.activitybrowserviewcell, this);
      imageViewAvatar = (AsyncImageView) view.findViewById(R.id.imageView_Avatar);
      textViewName = (TextView) view.findViewById(R.id.textView_Name);
      textViewMessage = (TextView) view.findViewById(R.id.textView_Message);
      buttonComment = (TextView) view.findViewById(R.id.button_Comment);
      buttonLike = (TextView) view.findViewById(R.id.button_Like);
      textViewTime = (TextView) view.findViewById(R.id.textView_Time);
      textViewShowMore = (TextView) view.findViewById(R.id.textView_Show_More);
      // imageViewAvatar.setUrl(streamInfo.getImageUrl());
      imageViewAvatar.setUrl("http://a3.twimg.com/profile_images/740897825/AndroidCast-350_normal.png");
      textViewName.setText(streamInfo.getUserName());
      textViewMessage.setText(Html.fromHtml(streamInfo.getTitle()));
      textViewTime.setText(SocialStreamUtil.getPostedTimeString(streamInfo.getPostedTime()));
      buttonComment.setText("" + streamInfo.getCommentNumber());
      buttonLike.setText("" + streamInfo.getLikeNumber());
    }

  }

}
