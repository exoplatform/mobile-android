package org.exoplatform.widget;

import java.lang.ref.WeakReference;

import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class AnimationLooper {

  private final WeakReference<View> vRef;

  private final int                 resId;

  public AnimationLooper(View v, int animationResId) {
    vRef = new WeakReference<View>(v);
    resId = animationResId;
  }

  public static void start(final View v, final int animationResId, int delayMillis) {
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        new AnimationLooper(v, animationResId).start();
      }
    }, delayMillis);
  }

  private void start() {
    View v = vRef.get();
    if (v != null) {
      Animation a = AnimationUtils.loadAnimation(v.getContext(), resId);
      a.setAnimationListener(listener);
      v.startAnimation(a);
    }
  }

  AnimationListener listener = new AnimationListener() {
                               @Override
                               public void onAnimationStart(Animation animation) {
                               }

                               @Override
                               public void onAnimationRepeat(Animation animation) {
                               }

                               @Override
                               public void onAnimationEnd(Animation animation) {
//                                 start();
                               }
                             };

}
