package org.exoplatform.widget;

import org.exoplatform.ui.WebViewActivity;
import org.exoplatform.utils.ExoConstants;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.style.ClickableSpan;
import android.view.View;

public class TextUrlSpan extends ClickableSpan implements ParcelableSpan {
  private String mURL;

  public TextUrlSpan(String url) {
    mURL = url;
  }

  public TextUrlSpan(Parcel src) {
    mURL = src.readString();

  }

  public int describeContents() {

    return 0;

  }

  public void writeToParcel(Parcel dest, int flags) {

    dest.writeString(mURL);

  }

  public String getURL() {

    return mURL;

  }

  @Override
  public void onClick(View widget) {
    Context context = widget.getContext();
    Intent intent = new Intent(context, WebViewActivity.class);
    intent.putExtra(ExoConstants.WEB_VIEW_URL, getURL());
    intent.putExtra(ExoConstants.WEB_VIEW_TITLE, getURL());
    intent.putExtra(ExoConstants.WEB_VIEW_ALLOW_JS, "false");
    context.startActivity(intent);
  }

  // @Override
  public int getSpanTypeId() {
    return 11;
  }

}
