package org.exoplatform.widget;

import org.exoplatform.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ServerItemLayout extends RelativeLayout {

  public TextView  serverName;

  public TextView  serverUrl;

  public ImageView serverImageView;

  public ServerItemLayout(Context context) {
    super(context);
    LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflate.inflate(R.layout.serverlistitemforsetting, this);
    serverName = (TextView) view.findViewById(R.id.TextView_ServerName);
    serverUrl = (TextView) view.findViewById(R.id.TextView_URL);
    serverImageView = (ImageView) view.findViewById(R.id.ImageView_Checked);
  }

}
