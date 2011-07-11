package eXo.eXoPlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestComment;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestLike;
import org.exoplatform.social.client.api.model.RestProfile;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

import eXo.eXoPlatform.social.SocialActivity;
import eXo.eXoPlatform.social.SocialStreamUtil;
import eXo.eXoPlatform.social.eXoSocialActivity;
import eXo.eXoPlatform.social.eXoSocialComment;
import eXo.eXoPlatform.social.eXoSocialLike;
import eXo.eXoPlatform.util.UserTask;
import eXo.eXoPlatform.util.eXoConstants;
import greendroid.widget.ActionBarItem;
import greendroid.widget.AsyncImageView;

//Chat list view controller
public class ActivityStreamDisplay extends MyActionBar implements OnClickListener {

  public static eXoSocialActivity selectedStreamInfo;

  private String                  activityId;

  private LinearLayout            commentLayoutWrap;

  private EditText                editTextComment;

  private AsyncImageView          imageView_Avatar;

  private TextView                textView_Name;

  private TextView                textView_Message;

  private TextView                textView_Time;

  private TextView                textView_Like_Count;

  private DetailLoadTask          mLoadTask;

  private Button                  likeButton;

  private boolean                 liked = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo_Back);
    setActionBarContentView(R.layout.activity_display_view);

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    selectedStreamInfo = SocialActivity.selectedStreamInfo;
    activityId = SocialActivity.selectedStreamInfo.getActivityId();
    changeLanguage(AppController.bundle);
    onLoad();

  }

  @Override
  protected void onResume() {
    super.onResume();
    onReload();
  }

  private void onReload() {
    RestActivity restActivity = (RestActivity) SocialActivity.activityService.get(activityId);
    selectedStreamInfo = new eXoSocialActivity();
    selectedStreamInfo.setActivityId(restActivity.getId());
    RestProfile profile = restActivity.getPosterIdentity().getProfile();
    selectedStreamInfo.setImageUrl(profile.getAvatarUrl());
    selectedStreamInfo.setUserName(profile.getFullName());
    selectedStreamInfo.setTitle(restActivity.getTitle());
    selectedStreamInfo.setPostedTime(restActivity.getPostedTime());
    List<RestLike> likeList = restActivity.getLikes();
    selectedStreamInfo.setLikelist(likeList);
    selectedStreamInfo.setLikeNumber(likeList.size());
    List<RestComment> commentList = restActivity.getAvailableComments();
    selectedStreamInfo.setCommentList(commentList);
    selectedStreamInfo.setCommentNumber(commentList.size());
    onLoad();

  }

  private void initComponent() {
    commentLayoutWrap = (LinearLayout) findViewById(R.id.activity_display_comment_wrap);
    imageView_Avatar = (AsyncImageView) findViewById(R.id.imageView_Avatar);
    textView_Name = (TextView) findViewById(R.id.textView_Name);
    textView_Message = (TextView) findViewById(R.id.textView_Message);
    textView_Time = (TextView) findViewById(R.id.textView_Time);
    textView_Like_Count = (TextView) findViewById(R.id.textView_Like_Count);

    imageView_Avatar.setUrl(selectedStreamInfo.getImageUrl());

    textView_Name.setText(selectedStreamInfo.getUserName());

    textView_Message.setText(Html.fromHtml(selectedStreamInfo.getTitle()));

    textView_Time.setText(SocialStreamUtil.getPostedTimeString(selectedStreamInfo.getPostedTime()));

    editTextComment = (EditText) findViewById(R.id.editText_Comment);
    editTextComment.setOnClickListener(this);

    likeButton = (Button) findViewById(R.id.like_button);
    likeButton.setOnClickListener(this);
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
      finish();
      // your method here
      break;
    case 0:
      // your method here
      break;

    case 1:
      // your method here
      break;

    default:
      // home button is clicked
      // finishMe();
      break;
    }

    return super.onHandleActionBarItemClick(item, position);
  }

  private void createCommentList(ArrayList<eXoSocialComment> commentList) {
    if (commentList != null) {

      LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
      commentLayoutWrap.removeAllViews();
      for (int i = commentList.size() - 1; i >= 0; i--) {
        eXoSocialComment comment = commentList.get(i);
        CommentItemLayout commentItem = new CommentItemLayout(this);
        commentItem.comAvatarImage.setUrl(comment.getImageUrl());
        commentItem.comTextViewName.setText(comment.getCommentName());
        commentItem.comTextViewMessage.setText(Html.fromHtml(comment.getCommentTitle()));
        commentItem.comPostedTime.setText(SocialStreamUtil.getPostedTimeString(comment.getPostedTime()));
        commentLayoutWrap.addView(commentItem, params);

      }
    }

  }

  // Set language
  public void changeLanguage(ResourceBundle resourceBundle) {

    // String strTitle =
    // getIntent().getStringExtra(eXoConstants.ACTIVITY_DETAIL_EXTRA);
    String strTitle = "Activity Detail";

    try {
      // strTitle = new
      // String(resourceBundle.getString("ActivityStream").getBytes("ISO-8859-1"),
      // "UTF-8");
      // strCannotBackToPreviousPage = new
      // String(resourceBundle.getString("CannotBackToPreviousPage")
      // .getBytes("ISO-8859-1"), "UTF-8");
    } catch (Exception e) {

    }

    setTitle(strTitle);

    // _delegate.changeLanguage(resourceBundle);
    // _delegate.createAdapter();
  }

  public void onClick(View view) {
    if (view == editTextComment) {
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(eXoConstants.COMPOSE_TYPE, eXoConstants.COMPOSE_COMMENT_TYPE);
      startActivity(intent);
    }
    if (view == likeButton) {
      Object activity = SocialActivity.activityService.get(activityId);
      if (liked == true) {
        SocialActivity.activityService.unlike(activity);
        liked = false;
      } else {
        SocialActivity.activityService.like(activity);
      }
      onReload();

    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finishFromChild(this);
  }

  private String getCommentString(ArrayList<eXoSocialLike> socialLikeList) {
    StringBuffer buffer = new StringBuffer();
    int count = socialLikeList.size();
    eXoSocialLike socialLike = null;

    if (count == 0) {
      buffer.append("No like for the moment");
    } else if (count == 1) {
      socialLike = socialLikeList.get(0);
      buffer.append(socialLike.getLikeName());

      buffer.append(" liked this");
    } else if (count < 4) {
      for (int i = 0; i < count - 1; i++) {
        socialLike = socialLikeList.get(i);
        buffer.append(socialLike.getLikeName());
        buffer.append(", ");
      }
      buffer.deleteCharAt(buffer.length() - 2);
      socialLike = socialLikeList.get(count - 1);
      buffer.append("and ");
      buffer.append(socialLike.getLikeName());
      buffer.append(" liked this");
    } else {
      for (int i = 0; i < 3; i++) {
        socialLike = socialLikeList.get(i);
        buffer.append(socialLike.getLikeName());
        buffer.append(", ");
      }
      buffer.deleteCharAt(buffer.length() - 2);
      int remain = count - 3;
      buffer.append("and ");
      buffer.append(remain);
      if (remain > 1) {
        buffer.append(" peoples liked this");
      } else
        buffer.append(" people liked this");

    }

    return buffer.toString();
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

    private ArrayList<eXoSocialLike>    socialLikeList    = new ArrayList<eXoSocialLike>();

    private ArrayList<eXoSocialComment> socialCommentList = new ArrayList<eXoSocialComment>();

    private ProgressDialog              _progressDialog;

    @Override
    public void onPreExecute() {
      _progressDialog = ProgressDialog.show(ActivityStreamDisplay.this, null, "Loading Data ...");
    }

    @Override
    public Integer doInBackground(Void... params) {
      List<RestLike> likeList = selectedStreamInfo.getLikeList();
      List<RestComment> commentList = selectedStreamInfo.getCommentList();
      if (likeList != null) {
        for (RestLike like : likeList) {
          eXoSocialLike socialLike = new eXoSocialLike();
          String identity = like.getIdentityId();
          RestIdentity restId = (RestIdentity) SocialActivity.identityService.get(identity);
          socialLike.setLikeID(identity);
          if (identity.equalsIgnoreCase(SocialActivity.userIdentity)) {
            socialLike.setLikeName("You");
            liked = true;
          } else {
            socialLike.setLikeName(restId.getProfile().getFullName());
          }
          socialLikeList.add(socialLike);

        }
      }

      if (commentList != null) {
        for (RestComment comment : commentList) {
          eXoSocialComment socialComment = new eXoSocialComment();
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
      initComponent();
      textView_Like_Count.setText(getCommentString(socialLikeList));
      createCommentList(socialCommentList);
      _progressDialog.dismiss();

    }

  }

}
