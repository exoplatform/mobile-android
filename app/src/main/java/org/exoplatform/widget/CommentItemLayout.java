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

import org.exoplatform.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CommentItemLayout extends RelativeLayout {
  public ImageView comAvatarImage;

  public TextView  comTextViewName;

  public TextView  comTextViewMessage;

  public TextView  comPostedTime;

  public CommentItemLayout(Context context) {
    super(context);

    LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View view = inflate.inflate(R.layout.activitydisplayviewcell, this);
    comAvatarImage = (ImageView) view.findViewById(R.id.imageView_Avatar);
    comTextViewName = (TextView) view.findViewById(R.id.textView_Name);
    comTextViewMessage = (TextView) view.findViewById(R.id.textView_Message);
    comPostedTime = (TextView) view.findViewById(R.id.textView_Time);
  }
}
