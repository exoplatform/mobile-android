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
