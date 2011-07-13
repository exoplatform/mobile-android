/*
 * Copyright (C) 2010 Cyril Mottier (http://www.cyrilmottier.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.exoplatform.social;

import greendroid.widget.item.Item;
import greendroid.widget.itemview.ItemView;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class ActivityStreamBrowserCellView extends RelativeLayout implements ItemView {

  private ImageView imageViewAvatar;

  private TextView  textViewName;

  private TextView  textViewMessage;

  private Button    buttonComment;

  private Button    buttonLike;

  private TextView  textViewTime;

  private TextView  textViewShowMore;

  public ActivityStreamBrowserCellView(Context context) {
    this(context, null);
  }

  public ActivityStreamBrowserCellView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ActivityStreamBrowserCellView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public void prepareItemView() {

    imageViewAvatar = (ImageView) findViewById(R.id.imageView_Avatar);

    textViewName = (TextView) findViewById(R.id.textView_Name);

    textViewMessage = (TextView) findViewById(R.id.textView_Message);

    buttonComment = (Button) findViewById(R.id.button_Comment);

    buttonLike = (Button) findViewById(R.id.button_Like);

    textViewTime = (TextView) findViewById(R.id.textView_Time);

    textViewShowMore = (TextView) findViewById(R.id.textView_Show_More);
  }

  public void setObject(Item object) {
    final ActivityStreamBrowserItem item = (ActivityStreamBrowserItem) object;
    if (item.isShowMore) {
      textViewShowMore.setVisibility(View.VISIBLE);

      // LayoutParams params = (LayoutParams) this.getLayoutParams();
      // params.height = 40;
      // this.setLayoutParams(new LayoutParams(this.getWidth(), 40));

      imageViewAvatar.setVisibility(View.INVISIBLE);
      textViewName.setVisibility(View.INVISIBLE);
      textViewMessage.setVisibility(View.INVISIBLE);
      buttonComment.setVisibility(View.INVISIBLE);
      buttonLike.setVisibility(View.INVISIBLE);
      textViewTime.setVisibility(View.INVISIBLE);
    } else {
      imageViewAvatar.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                                                                  R.drawable.homeactivitystreamsiconiphone));
      textViewName.setText(item.strName);
      textViewMessage.setText(item.strMessage);
      buttonComment.setText(Integer.toString(item.nbComment));
      buttonLike.setText(Integer.toString(item.nbLike));
      textViewTime.setText(item.strTime);
    }
  }

}
