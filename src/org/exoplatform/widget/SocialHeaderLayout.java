package org.exoplatform.widget;

import org.exoplatform.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SocialHeaderLayout extends RelativeLayout {
  public TextView titleView;

  public SocialHeaderLayout(Context context) {
    super(context);
    LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflate.inflate(R.layout.activityheadersection, this);
    titleView = (TextView) view.findViewById(R.id.textView_Section_Title);
  }

}
