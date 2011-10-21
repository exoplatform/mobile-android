package org.exoplatform.widget;

import greendroid.widget.AsyncImageView;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.SocialActivityUtil;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SocialActivityStreamItem extends LinearLayout {

  private View           view;

  private AsyncImageView imageViewAvatar;

  private TextView       textViewName;

  private TextView       textViewMessage;

  private Button       buttonComment;

  private Button       buttonLike;

  private TextView       textViewTime;

  private View           attachStubView;

  private String         domain;

  private Context        mContext;

  public SocialActivityStreamItem(Context context, SocialActivityInfo activityInfo) {
    super(context);
    mContext = context;
    LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    domain = SocialActivityUtil.getDomain();
    view = inflate.inflate(R.layout.activitybrowserviewcell, this);
    imageViewAvatar = (AsyncImageView) view.findViewById(R.id.imageView_Avatar);
    textViewName = (TextView) view.findViewById(R.id.textView_Name);
    textViewMessage = (TextView) view.findViewById(R.id.textView_Message);
    buttonComment = (Button) view.findViewById(R.id.button_Comment);
    buttonLike = (Button) view.findViewById(R.id.button_Like);
    textViewTime = (TextView) view.findViewById(R.id.textView_Time);
    String avatarUrl = activityInfo.getImageUrl();
    if (avatarUrl == null) {
      imageViewAvatar.setImageResource(ExoConstants.DEFAULT_AVATAR);
    } else
      imageViewAvatar.setUrl(avatarUrl);
    textViewName.setText(Html.fromHtml(activityInfo.getUserName()));
    textViewMessage.setText(Html.fromHtml(activityInfo.getTitle()));
    textViewTime.setText(SocialActivityUtil.getPostedTimeString(activityInfo.getPostedTime()));
    buttonComment.setText("" + activityInfo.getCommentNumber());
    buttonLike.setText("" + activityInfo.getLikeNumber());
    String attachUrl = activityInfo.getAttachedImageUrl();
    if (attachUrl != null) {
      displayAttachImage(attachUrl);
    }

  }

  public void displayAttachImage(String url) {
    if (attachStubView == null) {
      initAttachStubView(domain + url);
    }
    attachStubView.setVisibility(View.VISIBLE);
  }

  private void initAttachStubView(final String url) {
    attachStubView = ((ViewStub) findViewById(R.id.attached_image_stub_activity)).inflate();
    AsyncImageView attachImage = (AsyncImageView) attachStubView.findViewById(R.id.attached_image_view);
     BitmapFactory.Options options = new BitmapFactory.Options();
     options.inTempStorage = new byte[16*1024];
     attachImage.setOptions(options);
     attachImage.setDefaultImageResource(R.drawable.documenticonforunknown);

    attachImage.setUrl(url);
  }
  
  public Button likeButton() {
    return buttonLike;
  }
  
  public Button commentButton() {
    return buttonComment;
  }
}
