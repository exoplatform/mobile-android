package org.exoplatform.widget;

import org.exoplatform.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommentItemLayout extends RelativeLayout {
  public ShaderImageView comAvatarImage;

  public TextView      comTextViewName;

  public TextView      comTextViewMessage;

  public TextView      comPostedTime;

  public CommentItemLayout(Context context) {
    super(context);

    LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflate.inflate(R.layout.activitydisplayviewcell, this);
    comAvatarImage = (ShaderImageView) view.findViewById(R.id.imageView_Avatar);
    comAvatarImage.setDefaultImageResource(R.drawable.default_avatar);
    comTextViewName = (TextView) view.findViewById(R.id.textView_Name);
    comTextViewMessage = (TextView) view.findViewById(R.id.textView_Message);
    comPostedTime = (TextView) view.findViewById(R.id.textView_Time);
  }
}
