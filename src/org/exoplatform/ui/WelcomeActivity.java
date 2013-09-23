package org.exoplatform.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
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
import android.widget.ImageView;
import android.widget.RelativeLayout;
import org.exoplatform.R;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.ui.login.LoginActivity;
import org.exoplatform.utils.AssetUtils;


/**
 * Welcome screen<br/>
 *
 * Overall time: +925ms +692ms +845ms +1s35ms
 */
public class WelcomeActivity extends FragmentActivity {

  private static final String TAG = "eXoWelcomeActivity";

  private ViewPager       mPager;

  private PagerAdapter    mPagerAdapter;

  private CirclePageIndicator mCirclePageIndicator;

  private ImageView       mSeparatorImg;

  private AccountSetting  mSetting;

  private RelativeLayout  mButtonSection;

  public  static boolean  mIsTablet;

  private int             mCurrentPage = 0;

  public  void onCreate(Bundle savedInstanceState) {
    requestScreenOrientation();
    mSetting = AccountSetting.getInstance();
    super.onCreate(savedInstanceState);
    init();
  }

  private void init() {
    setContentView(R.layout.welcome);
    if (mIsTablet) detectScreenOrientation();

    ViewGroup vg = (ViewGroup) findViewById(R.id.welcome_layout);
    AssetUtils.setContext(this);
    AssetUtils.setTypeFace(AssetUtils.getCustomTypeface(AssetUtils.ROBOTO_BLACK), vg);

    mSeparatorImg  = (ImageView) findViewById(R.id.welcome_separator_img);
    mButtonSection = (RelativeLayout) findViewById(R.id.welcome_button_section);

    // Instantiate a ViewPager and a PagerAdapter.
    mPager = (ViewPager) findViewById(R.id.pager);
    mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
    mPager.setAdapter(mPagerAdapter);
    mPager.setCurrentItem(mCurrentPage);

    mCirclePageIndicator = (CirclePageIndicator) findViewById(R.id.circle_page_indicator);
    mCirclePageIndicator.setViewPager(mPager);
    mCirclePageIndicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        mCurrentPage = (position!=0) ? position: 0;
      }
    });
    mCirclePageIndicator.setCurrentItem(mCurrentPage);
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

  @Override
  public void onBackPressed() {
    setResult(RESULT_CANCELED);
    finish();
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