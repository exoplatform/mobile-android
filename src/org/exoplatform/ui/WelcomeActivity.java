package org.exoplatform.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import org.exoplatform.R;
import org.exoplatform.controller.login.LaunchController;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.utils.AssetUtils;
import org.exoplatform.utils.ExoConstants;

import java.util.Date;


/**
 * Welcome screen<br/>
 *
 * Requires setting
 *
 * Overall time: +925ms +692ms +845ms +1s35ms
 */
public class WelcomeActivity extends FragmentActivity {

  private static final String TAG = "eXoWelcomeActivity";

  private static final int NUM_PAGES = 3;

  private ViewPager mPager;

  private PagerAdapter mPagerAdapter;

  private AccountSetting  mSetting;

  public void onCreate(Bundle savedInstanceState) {

    long start = new Date().getTime();

    mSetting = getIntent().getParcelableExtra(ExoConstants.ACCOUNT_SETTING);
    if( mSetting==null) mSetting = AccountSetting.getInstance();

    new LaunchController(this, mSetting);              // 29ms  - 36ms
    Log.i(TAG, "launch controller runs - duration: " + (new Date().getTime() - start));

    super.onCreate(savedInstanceState);

    setContentView(R.layout.welcome);

    setUpScreenOrientationSetting();

    ViewGroup vg = (ViewGroup) findViewById(R.id.welcome_layout);
    AssetUtils.setTypeFace(AssetUtils.getCustomTypeface(AssetUtils.ROBOTO_BOLD), vg);

    // Instantiate a ViewPager and a PagerAdapter.
    mPager = (ViewPager) findViewById(R.id.pager);
    mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
    mPager.setAdapter(mPagerAdapter);

    CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.circle_page_indicator);
    circlePageIndicator.setViewPager(mPager);
    circlePageIndicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

      @Override
      public void onPageSelected(int position) {
        Log.i(TAG, "onPageSelected - position: " + position);
      }
    });

    Button btn_signUp = (Button) findViewById(R.id.welcome_btn_signup);
    btn_signUp.setText(Html.fromHtml("<big>" + getResources().getString(R.string.SignUp) + "</big><br/>" +
        "<small>" + getResources().getString(R.string.CreateAnAccount) + "</small>"));
    //AssetUtils.setTypeFace(mRobotoTF, btn_signUp);

    Button btn_logIn = (Button) findViewById(R.id.welcome_btn_login);
    btn_logIn.setText(Html.fromHtml("<big>" + getResources().getString(R.string.LogIn) + "</big><br/>" +
        "<small>" + getResources().getString(R.string.AlreadyAnUser) + "</small>"));


    long startLoadingImg = new Date().getTime();
    //Log.i(TAG, "start time loading image: " + start);

    // pre-loading image for screen slider
    for (int k = 0; k < ScreenSlidePageFragment.SLIDER_IMGS.length; k++) {
      final int _k = k;
      new Thread(new Runnable() {

        @Override
        public void run() {
          long start1 = new Date().getTime();
          //Log.i(TAG, "start for " + _k + ": " + start1);
          ScreenSlidePageFragment.SLIDER_BITMAPS[_k]
              = BitmapFactory.decodeResource(getResources(), ScreenSlidePageFragment.SLIDER_IMGS[_k]);
          long end1 = new Date().getTime();
          Log.i(TAG, "diff for loading image " + _k + ": " + (end1 - start1));   // 1: 160ms, 2: 195ms, 3: 211ms
        }
      }).start();
    }

    Log.i(TAG, "overall loading time: " + (new Date().getTime() - start) + "/r/n loading image time: " + (new Date().getTime() - startLoadingImg));
    // 181ms - 11ms, 168ms - 14ms
  }


  private void setUpScreenOrientationSetting() {

    int size = getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK;

    switch (size) {
      case Configuration.SCREENLAYOUT_SIZE_NORMAL:
        Log.i(TAG, "normal");       // 320x470 dp units
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        break;
      case Configuration.SCREENLAYOUT_SIZE_SMALL:
        Log.i(TAG, "small");        // 320x426 dp units
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        break;
      default:
        break;
    }
  }

  // Called when user clicks on Signup btn
  public void redirectToSignUp(View view) {
    Log.i(TAG, "redirectToSignUp");

    Intent next = new Intent(this, SignUpActivity.class);
    startActivity(next);
  }

  /**
   * Redirect user to login screen
   *
   * @param view
   */
  public void redirectToLogIn(View view) {
    Log.i(TAG, "redirectToLogIn");

    Intent next = new Intent(this, LoginActivity.class);
    next.putExtra(ExoConstants.ACCOUNT_SETTING, mSetting);
    startActivity(next);
  }

  public void logUserIn(View view) {
    Log.i(TAG, "logUserIn");

    Intent next = new Intent(this, SignInActivity.class);
    startActivity(next);
  }

  /**
   * A simple pager adapter that represents 4 {@link ScreenSlidePageFragment} objects, in
   * sequence.
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
      return NUM_PAGES;
    }
  }
}