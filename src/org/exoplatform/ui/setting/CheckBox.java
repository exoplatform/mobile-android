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
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import org.exoplatform.R;

/**
 * A checkbox that has a text and a image<br/>
 * The checkbox will change state when being clicked upon<br/>
 * It can be disabled, in that case, it will not be clicked<br/>
 *
 */
public class CheckBox extends RelativeLayout implements
    View.OnClickListener {

  private Context   mContext;

  /** state: checked or not */
  protected boolean mChecked = false;

  /** state: enabled or disabled */
  private boolean   mEnabled = true;

  private TextView  mText;

  private ImageView mImage;

  /** Disabled animation */
  private Animation mDisabledAnim;

  private Animation mEnabledAnim;

  private int       mCheckedImg   = R.drawable.authenticate_checkmark_on;

  private int       mUncheckedImg = R.drawable.authenticate_checkmark_off;

  private static final String TAG = "eXo____CheckBox____";


  public CheckBox(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mContext = context;
  }

  public CheckBox(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
  }

  public CheckBox(Context context) {
    super(context);
    mContext = context;
  }


  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    initSubViews();
    initStates();
  }

  private void initSubViews() {
    mText  = (TextView)  findViewById(R.id.checkbox_txt);
    mImage = (ImageView) findViewById(R.id.checkbox_img);

    setOnClickListener(this);
    mDisabledAnim = new AlphaAnimation(0.4f, 0.4f);
    mDisabledAnim.setDuration(0);
    mDisabledAnim.setFillAfter(true);

    mEnabledAnim  = new AlphaAnimation(1.0f, 1.0f);
    mEnabledAnim.setDuration(0);
    mEnabledAnim.setFillAfter(true);
  }

  private void initStates() {
    setChecked(mChecked, false);
    enabled(mEnabled);
  }

  public CheckBox setText(String text) {
    mText.setText(text);
    return this;
  }

  public CheckBox setChecked(boolean isChecked, boolean triggerListener) {
    if (mChecked == isChecked) return this;
    mChecked = isChecked;
    mImage.setBackgroundResource(mChecked ? mCheckedImg : mUncheckedImg);
    if (mViewListener != null && triggerListener) mViewListener.onClickCheckBox(this, mChecked);
    return this;
  }

  public CheckBox enabled(boolean isEnabled) {
    mEnabled = isEnabled;
    if (mEnabled) startAnimation(mEnabledAnim);
    else startAnimation(mDisabledAnim);
    setEnabled(mEnabled);
    return this;
  }

  public CheckBox setCheckBoxImage(int resIdCheckedImage, int resIdUncheckedImage) {
    mCheckedImg = resIdCheckedImage;
    mUncheckedImg = resIdUncheckedImage;
    return this;
  }

  public boolean isChecked() {
    return mChecked;
  }

  private ViewListener mViewListener;

  @Override
  public void onClick(View view) {
    if (view.equals(this)) setChecked(!mChecked, true);
  }

  /* interface to listen to view event */
  public interface ViewListener {

    void onClickCheckBox(CheckBox checkBox,boolean isChecked);
  }

  public void setViewListener(ViewListener l) {
    mViewListener = l;
  }
}
