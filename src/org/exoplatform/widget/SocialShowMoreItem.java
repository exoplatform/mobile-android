package org.exoplatform.widget;

import org.exoplatform.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class SocialShowMoreItem extends LinearLayout {

    public Button showMoreBtn;

    public SocialShowMoreItem(Context context) {
      super(context);
      LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflate.inflate(R.layout.social_show_more_layout, this);
      showMoreBtn = (Button) view.findViewById(R.id.social_show_more_btn);
    }

  }