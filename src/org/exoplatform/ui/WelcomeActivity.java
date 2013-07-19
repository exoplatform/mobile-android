package org.exoplatform.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import org.exoplatform.R;
import org.exoplatform.controller.login.LaunchController;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.utils.AssetUtils;
import org.exoplatform.utils.ExoConstants;


/**
 * Welcome screen<br/>
 *
 * Requires setting
 *
 * Overall time: +925ms +692ms +845ms +1s35ms
 */
public class WelcomeActivity extends FragmentActivity {

  private static final String TAG = "eXoWelcomeActivity";

  private ViewPager       mPager;

  private PagerAdapter    mPagerAdapter;

  private AccountSetting  mSetting;

  private boolean         mIsTablet;

  private int             mCurrentPage = 0;

  public  void onCreate(Bundle savedInstanceState) {
    requestScreenOrientation();
    mSetting = AccountSetting.getInstance();

    new LaunchController(this);
    super.onCreate(savedInstanceState);

    init();
  }

  private void init() {

    setContentView(R.layout.welcome);
    if (mIsTablet) detectScreenOrientation();

    ViewGroup vg = (ViewGroup) findViewById(R.id.welcome_layout);
    AssetUtils.setTypeFace(AssetUtils.getCustomTypeface(AssetUtils.ROBOTO_BLACK), vg);

    // Instantiate a ViewPager and a PagerAdapter.
    mPager = (ViewPager) findViewById(R.id.pager);
    mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
    mPager.setAdapter(mPagerAdapter);
    mPager.setCurrentItem(mCurrentPage);

    CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.circle_page_indicator);
    circlePageIndicator.setViewPager(mPager);
    circlePageIndicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        mCurrentPage = (position!=0) ? position: 0;
      }
    });
    circlePageIndicator.setCurrentItem(mCurrentPage);

    // pre-loading image for screen slider
    for (int k = 0; k < ScreenSlidePageFragment.sSliderImgs.length; k++) {
      final int _k = k;
      new Thread(new Runnable() {

        @Override
        public void run() {
          ScreenSlidePageFragment.SLIDER_BITMAPS[_k]
              = BitmapFactory.decodeResource(getResources(), ScreenSlidePageFragment.sSliderImgs[_k]);
        }
      }).start();
    }
    //Overall loading time: 181ms - 11ms, 168ms - 14ms
  }

  /**
   * Force screen orientation for small and medium size devices
   */
  private void requestScreenOrientation() {

    int size = getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK;

    switch (size) {
      case Configuration.SCREENLAYOUT_SIZE_NORMAL:  // 320x470 dp units
        Log.i(TAG, "normal");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mIsTablet = false;
        break;
      case Configuration.SCREENLAYOUT_SIZE_SMALL:   // 320x426 dp units
        Log.i(TAG, "small");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mIsTablet = false;
        break;
      case Configuration.SCREENLAYOUT_SIZE_LARGE:   // 480x640 dp units
        Log.i(TAG, "large");
        mIsTablet = true;
        break;
      case Configuration.SCREENLAYOUT_SIZE_XLARGE:  // 720x960 dp units
        Log.i(TAG, "xlarge");
        mIsTablet = true;
        break;
      default:
        break;
    }
  }

  private void detectScreenOrientation() {
    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
      Log.i(TAG, "landscape");
      ScreenSlidePageFragment.sSliderImgs = ScreenSlidePageFragment.SLIDER_IMGS_LANDSCAPE;
    }
    else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
      Log.i(TAG, "portrait");
      ScreenSlidePageFragment.sSliderImgs = ScreenSlidePageFragment.SLIDER_IMGS_PORTRAIT;
    }
  }

  @Override
  public  void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);

    init();
  }

  public  void redirectToSignUp(View view) {
    Log.i(TAG, "redirectToSignUp");

    Intent next = new Intent(this, SignUpActivity.class);
    startActivity(next);
  }

  public  void redirectToLogIn(View view) {
    Log.i(TAG, "redirectToLogIn");

    Intent next = new Intent(this, LoginActivity.class);
    startActivity(next);
  }

  public  void redirectToSignIn(View view) {
    Log.i(TAG, "logUserIn");

    Intent next = new Intent(this, SignInActivity.class);
    startActivity(next);
  }

  /**
   * A simple pager adapter
   */
  private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
    public ScreenSlidePagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      Log.i(TAG, "getItem: " + position);
      return ScreenSlidePageFragment.create(position);
    }

    @Override
    public int getCount() {
      return ScreenSlidePageFragment.NUM_PAGES;
    }
  }

}