package org.exoplatform.social;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.controller.AppController;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestComment;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestLike;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.social.entity.ExoSocialComment;
import org.exoplatform.social.entity.ExoSocialLike;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.MyActionBar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

import greendroid.widget.ActionBarItem;
import greendroid.widget.AsyncImageView;

public class ActivityStreamDisplay extends MyActionBar implements OnClickListener {

  public static RestActivity selectedRestActivity;

  private String             activityId;

  private LinearLayout       commentLayoutWrap;

  private EditText           editTextComment;

  private AsyncImageView     imageView_Avatar;

  private TextView           textView_Name;

  private TextView           textView_Message;

  private TextView           textView_Time;

  private TextView           textView_Like_Count;

  private DetailLoadTask     mLoadTask;

  private Button             likeButton;

  private boolean            liked = false;

  private String             yourCommentText;

  private String             loadingData;

  private String             youText;

  private String             noService;

  private String             url;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo_Back);
    setActionBarContentView(R.layout.activity_display_view);

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    selectedRestActivity = SocialActivity.selectedRestActivity;
    activityId = selectedRestActivity.getId();
    // selectedRestActivity = (RestActivity)
    // SocialActivity.activityService.get(activityId);
    changeLanguage(AppController.bundle);
    url = SocialActivityUtil.getUrl();
    initComponent();
    onLoad();

  }

  @Override
  protected void onResume() {
    super.onResume();
    onReload();
  }

  private void destroy() {
    super.onDestroy();
    onCancelLoad();
    finish();
  }

  private void onReload() {
    try {
      selectedRestActivity = (RestActivity) SocialActivity.activityService.get(activityId);
      onLoad();
    } catch (RuntimeException e) {
      destroy();
    }

  }

  private void initComponent() {
    commentLayoutWrap = (LinearLayout) findViewById(R.id.activity_display_comment_wrap);
    imageView_Avatar = (AsyncImageView) findViewById(R.id.imageView_Avatar);
    textView_Name = (TextView) findViewById(R.id.textView_Name);
    textView_Message = (TextView) findViewById(R.id.textView_Message);
    textView_Time = (TextView) findViewById(R.id.textView_Time);
    textView_Like_Count = (TextView) findViewById(R.id.textView_Like_Count);
    editTextComment = (EditText) findViewById(R.id.editText_Comment);
    editTextComment.setHint(yourCommentText);
    editTextComment.setOnClickListener(this);
    likeButton = (Button) findViewById(R.id.like_button);
    likeButton.setOnClickListener(this);
  }

  private void setComponentInfo() {
    RestProfile profile = selectedRestActivity.getPosterIdentity().getProfile();
    imageView_Avatar.setUrl(url + profile.getAvatarUrl());
    textView_Name.setText(profile.getFullName());

    textView_Message.setText(Html.fromHtml(selectedRestActivity.getTitle()));

    textView_Time.setText(SocialActivityUtil.getPostedTimeString(selectedRestActivity.getPostedTime(),
                                                                 AppController.bundle));
  }

  private void onLoad() {
    if (mLoadTask == null || mLoadTask.getStatus() == DetailLoadTask.Status.FINISHED) {
      mLoadTask = (DetailLoadTask) new DetailLoadTask().execute();
    }
  }

  private void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == DetailLoadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      destroy();
      break;
    case 0:
      break;

    }

    return super.onHandleActionBarItemClick(item, position);
  }

  private void createCommentList(ArrayList<ExoSocialComment> commentList) {
    if (commentList != null) {

      LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
      commentLayoutWrap.removeAllViews();
      for (int i = 0; i < commentList.size(); i++) {
        ExoSocialComment comment = commentList.get(i);
        CommentItemLayout commentItem = new CommentItemLayout(this);
        commentItem.comAvatarImage.setUrl(url + comment.getImageUrl());
        commentItem.comTextViewName.setText(comment.getCommentName());
        commentItem.comTextViewMessage.setText(Html.fromHtml(comment.getCommentTitle()));
        commentItem.comPostedTime.setText(SocialActivityUtil.getPostedTimeString(comment.getPostedTime(),
                                                                                 AppController.bundle));
        commentLayoutWrap.addView(commentItem, params);

      }
    }

  }

  // Set language
  public void changeLanguage(ResourceBundle resourceBundle) {
    String strTitle = resourceBundle.getString("ActivityDetail");
    setTitle(strTitle);
    yourCommentText = resourceBundle.getString("YourComment");
    loadingData = resourceBundle.getString("LoadingData");
    youText = resourceBundle.getString("You");
    noService = resourceBundle.getString("NoService");

  }

  public void onClick(View view) {
    if (view == editTextComment) {
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_COMMENT_TYPE);
      startActivity(intent);
    }
    if (view == likeButton) {
      try {
        RestActivity activity = SocialActivity.activityService.get(activityId);
        if (liked == true) {
          SocialActivity.activityService.unlike(activity);
          liked = false;
        } else {
          SocialActivity.activityService.like(activity);
        }
        onReload();
      } catch (RuntimeException e) {
        Toast toast = Toast.makeText(this, noService, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
      }

    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    destroy();
  }

  // Comment item layout

  private class CommentItemLayout extends RelativeLayout {
    private AsyncImageView comAvatarImage;

    private TextView       comTextViewName;

    private TextView       comTextViewMessage;

    private TextView       comPostedTime;

    public CommentItemLayout(Context context) {
      super(context);

      LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflate.inflate(R.layout.activitydisplayviewcell, this);
      comAvatarImage = (AsyncImageView) view.findViewById(R.id.imageView_Avatar);
      comTextViewName = (TextView) view.findViewById(R.id.textView_Name);
      comTextViewMessage = (TextView) view.findViewById(R.id.textView_Message);
      comPostedTime = (TextView) view.findViewById(R.id.textView_Time);
    }
  }

  private class DetailLoadTask extends UserTask<Void, Void, Integer> {

    private LinkedList<ExoSocialLike> likeLinkedList = new LinkedList<ExoSocialLike>();

    private ArrayList<ExoSocialComment> socialCommentList = new ArrayList<ExoSocialComment>();

    private ProgressDialog              _progressDialog;

    @Override
    public void onPreExecute() {
      _progressDialog = ProgressDialog.show(ActivityStreamDisplay.this, null, loadingData);
    }

    @Override
    public Integer doInBackground(Void... params) {
      List<RestLike> likeList = selectedRestActivity.getLikes();
      List<RestComment> commentList = selectedRestActivity.getAvailableComments();
      if (likeList != null) {
        for (RestLike like : likeList) {
          ExoSocialLike socialLike = new ExoSocialLike();
          String identity = like.getIdentityId();
          RestIdentity restId = (RestIdentity) SocialActivity.identityService.get(identity);
          socialLike.setLikeID(identity);
          if (identity.equalsIgnoreCase(SocialActivity.userIdentity)) {
            socialLike.setLikeName(youText);
            likeLinkedList.addFirst(socialLike);
            liked = true;
          } else {
            socialLike.setLikeName(restId.getProfile().getFullName());
            likeLinkedList.add(socialLike);
          }

        }
      }

      if (commentList != null) {
        for (RestComment comment : commentList) {
          ExoSocialComment socialComment = new ExoSocialComment();
          String identity = comment.getIdentityId();
          RestIdentity restId = (RestIdentity) SocialActivity.identityService.get(identity);
          RestProfile profile = restId.getProfile();
          socialComment.setCommentId(identity);
          socialComment.setCommentName(profile.getFullName());
          socialComment.setImageUrl(profile.getAvatarUrl());
          socialComment.setCommentTitle(comment.getText());
          socialComment.setPostedTime(comment.getPostedTime());

          socialCommentList.add(socialComment);
        }
      }

      return null;
    }

    @Override
    public void onPostExecute(Integer result) {
      setComponentInfo();
      textView_Like_Count.setText(SocialActivityUtil.getCommentString(likeLinkedList,
                                                                      AppController.bundle));
      createCommentList(socialCommentList);
      _progressDialog.dismiss();

    }

  }

}
