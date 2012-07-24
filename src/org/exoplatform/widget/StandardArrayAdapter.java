package org.exoplatform.widget;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.ui.social.ComposeMessageActivity;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.ui.social.SocialItem;
import org.exoplatform.utils.ExoConstants;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StandardArrayAdapter extends ArrayAdapter<SocialActivityInfo> {

  private final ArrayList<SocialActivityInfo> items;

  private Context                             mContext;

  private LayoutInflater                      mInflater;

  public StandardArrayAdapter(Context context, int layoutId, ArrayList<SocialActivityInfo> items) {
    super(context, layoutId, items);
    mContext = context;
    this.items = items;
    mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    final SocialActivityInfo actInfo = items.get(position);

    ViewHolder holder = null;
    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.activitybrowserviewcell, null);
      holder = new ViewHolder();
      holder.imageViewAvatar = (ShaderImageView) convertView.findViewById(R.id.imageView_Avatar);
      holder.imageViewAvatar.setDefaultImageResource(R.drawable.default_avatar);
      holder.contentLayoutWrap = (LinearLayout) convertView.findViewById(R.id.relativeLayout_Content);
      holder.textViewName = (TextView) convertView.findViewById(R.id.textView_Name);
      holder.textViewName.setLinkTextColor(Color.rgb(21, 94, 173));
      holder.textViewMessage = (TextView) convertView.findViewById(R.id.textView_Message);
      holder.textViewTempMessage = (TextView) convertView.findViewById(R.id.textview_temp_message);
      holder.textViewCommnet = (TextView) convertView.findViewById(R.id.activity_comment_view);
      holder.buttonComment = (Button) convertView.findViewById(R.id.button_Comment);
      holder.buttonLike = (Button) convertView.findViewById(R.id.button_Like);
      holder.typeImageView = (ImageView) convertView.findViewById(R.id.activity_image_type);
      holder.textViewTime = (TextView) convertView.findViewById(R.id.textView_Time);
      holder.attachStubView = ((ViewStub) convertView.findViewById(R.id.attached_image_stub_activity)).inflate();
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    SocialItem socialItem = new SocialItem(mContext, holder, actInfo, false);
    socialItem.initCommonInfo();
    holder.contentLayoutWrap.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        String activityId = actInfo.getActivityId();
        SocialDetailHelper.getInstance().setActivityId(activityId);
        SocialDetailHelper.getInstance().setAttachedImageUrl(actInfo.getAttachedImageUrl());
        Intent intent = new Intent(mContext, SocialDetailActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mContext.startActivity(intent);
      }
    });

    holder.buttonComment.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        SocialDetailHelper.getInstance().setActivityId(actInfo.getActivityId());

        Intent intent = new Intent(mContext, ComposeMessageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_COMMENT_TYPE);
        mContext.startActivity(intent);

      }
    });

    return convertView;
  }

  public static class ViewHolder {
    public LinearLayout    contentLayoutWrap;

    public ShaderImageView imageViewAvatar;

    public TextView        textViewName;

    public TextView        textViewMessage;

    public TextView        textViewTempMessage;

    public TextView        textViewCommnet;

    public Button          buttonComment;

    public Button          buttonLike;

    public ImageView       typeImageView;

    public TextView        textViewTime;

    public View            attachStubView;
  }

}
