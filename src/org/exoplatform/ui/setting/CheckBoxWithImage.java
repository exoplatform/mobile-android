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
package org.exoplatform.ui.setting;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import org.exoplatform.R;

/**
 * A check contains a head image
 */
public class CheckBoxWithImage extends CheckBox {

  private ImageView mHeadImage;

  public CheckBoxWithImage(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }

  public CheckBoxWithImage(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public CheckBoxWithImage(Context context) {
    super(context);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    mHeadImage = (ImageView) findViewById(R.id.checkbox_head_img);
  }

  public CheckBoxWithImage setHeadImage(int resId) {
    mHeadImage.setBackgroundResource(resId);
    return this;
  }

  /**
   * Behavior is different with normal checkbox, once this view is checked
   * click on it won't change it to unchecked
   *
   * @param view
   */
  @Override
  public void onClick(View view) {
    if (view.equals(this)) {
      if (mChecked) return;
      setChecked(!mChecked, true);
    }
  }

}
