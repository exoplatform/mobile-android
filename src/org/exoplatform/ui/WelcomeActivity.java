package org.exoplatform.ui;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import org.exoplatform.R;

import java.util.Date;


/**
 * Welcome screen
 */
public class WelcomeActivity extends FragmentActivity {

  private static final String TAG = "eXoWelcomeActivity";

  private static final int NUM_PAGES = 3;

  private ViewPager mPager;

  private PagerAdapter mPagerAdapter;

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.welcome);

    // Instantiate a ViewPager and a PagerAdapter.
    mPager = (ViewPager) findViewById(R.id.pager);
    mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
    mPager.setAdapter(mPagerAdapter);
    //mPager.setOnPageChangeListener();

    LinePageIndicator linePageIndicator = (LinePageIndicator) findViewById(R.id.line_page_indicator);
    linePageIndicator.setViewPager(mPager);
    linePageIndicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        Log.i(TAG, "onPageSelected - position: " + position);
      }
    });

    Button btn_signUp = (Button) findViewById(R.id.welcome_btn_signup);
    btn_signUp.setText(Html.fromHtml("<font>" + getResources().getString(R.string.SignUp) + "</font><br/>" +
        "<font size='20'><i>" + getResources().getString(R.string.CreateAnAccount) + "</i></font>"));

    Button btn_logIn = (Button) findViewById(R.id.welcome_btn_login);
    btn_logIn.setText(Html.fromHtml("<font>" + getResources().getString(R.string.LogIn) + "</font><br/>" +
        "<font size='20'><i>" + getResources().getString(R.string.AlreadyAnUser) + "</i></font>"));

    long start = new Date().getTime();
    Log.i(TAG, "start time loading image: " + start);

    // pre-loading image for screen slider
    for (int k = 0; k < ScreenSlidePageFragment.SLIDER_IMGS.length; k++) {
      final int _k = k;
      new Thread(new Runnable() {

        @Override
        public void run() {
          long start1 = new Date().getTime();
          Log.i(TAG, "start for " + _k + ": " + start1);
          ScreenSlidePageFragment.SLIDER_BITMAPS[_k]
              = BitmapFactory.decodeResource(getResources(), ScreenSlidePageFragment.SLIDER_IMGS[_k]);
          long end1 = new Date().getTime();
          Log.i(TAG, "diff for " + _k + ": " + (end1 - start1));
        }
      }).start();
    }

    Log.i(TAG, "diff loading image: " + (new Date().getTime() - start));
  }

  // Called when user clicks on Signup btn
  public void redirectToSignUp(View view) {
    Log.i(TAG, "redirectToSignUp");

    Intent next = new Intent(this, SignUpActivity.class);
    startActivity(next);
  }

  // Called when user clicks on Skip this for now txt
  public void redirectToLogIn(View view) {
    Log.i(TAG, "redirectToLogIn");

    Intent next = new Intent(this, LoginActivity.class);
    startActivity(next);
  }

  public void logUserIn(View view) {
    Log.i(TAG, "logUserIn");
  }

  /**
   * A simple pager adapter that represents 5 {@link ScreenSlidePageFragment} objects, in
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