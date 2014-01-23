package org.exoplatform.widget;

import org.exoplatform.poc.userprofiles.R;
import org.exoplatform.controller.profile.UserProfile;
import org.exoplatform.ui.ProfileActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommentItemLayout extends RelativeLayout implements OnTouchListener{
  public ShaderImageView comAvatarImage;

  public TextView      comTextViewName;

  public TextView      comTextViewMessage;

  public TextView      comPostedTime;
  public String userId;

  public CommentItemLayout(Context context) {
    super(context);

    LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflate.inflate(R.layout.activitydisplayviewcell, this);
    comAvatarImage = (ShaderImageView) view.findViewById(R.id.imageView_Avatar);
    comAvatarImage.setDefaultImageResource(R.drawable.default_avatar);
    comAvatarImage.setOnTouchListener(this);
    comTextViewName = (TextView) view.findViewById(R.id.textView_Name);
    //comTextViewName.setOnTouchListener(this);
    comTextViewMessage = (TextView) view.findViewById(R.id.textView_Message);
    comPostedTime = (TextView) view.findViewById(R.id.textView_Time);
  }

@Override
public boolean onTouch(View v, MotionEvent event) {
	Intent next = new Intent(v.getContext(), ProfileActivity.class);
	next.putExtra(UserProfile.USER_ID, userId);
	v.getContext().startActivity(next);
	return false;
}
}
