/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.widget;

import org.exoplatform.ui.WebViewActivity;
import org.exoplatform.utils.CompatibleFileOpen;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.style.ClickableSpan;
import android.view.View;
import android.webkit.MimeTypeMap;

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

  /**
   * Check if the document should be opened in a webview or not, based on its mimetype.
   * Open in a WebView if
   * <ul>
   * <li>mimetype is unknown (null)</li>
   * <li>mimetype is image/*</li>
   * <li>mimetype is text/* except text/rtf</li>
   * </ul>
   *
   * @param mimeType the document's mime type to check
   * @return
   */
  private boolean openInWebView(String mimeType) {
    return ( mimeType == null ||
             mimeType.startsWith(ExoDocumentUtils.IMAGE_TYPE) ||
             (mimeType.startsWith(ExoDocumentUtils.TEXT_TYPE) && !mimeType.equalsIgnoreCase("text/rtf")) );
  }

  @Override
  public void onClick(View widget) {
    Context context = widget.getContext();
    String mimeType = ExoDocumentUtils.mimeTypeFromUrl(getURL());
    if (openInWebView(mimeType)) {
      Intent intent = new Intent(context, WebViewActivity.class);
      intent.putExtra(ExoConstants.WEB_VIEW_URL, getURL());
      intent.putExtra(ExoConstants.WEB_VIEW_TITLE, getURL());
      intent.putExtra(ExoConstants.WEB_VIEW_ALLOW_JS, "false");
      if (mimeType != null) intent.putExtra(ExoConstants.WEB_VIEW_MIME_TYPE, mimeType);
      context.startActivity(intent);
    } else {
      new CompatibleFileOpen(context, mimeType, getURL(), ExoDocumentUtils.getLastPathComponent(getURL()));
    }
  }

  @Override
  public int getSpanTypeId() {
    return 11;
  }
  
  // Workaround needed to work on Android 6.0
  // cf https://jira.exoplatform.org/browse/MOB-1974
  
  public int getSpanTypeIdInternal() {
    return getSpanTypeId();
  }
  
  public void writeToParcelInternal(Parcel dest, int flags) {
    writeToParcel(dest, flags);
  }
  
  //
}
