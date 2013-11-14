package org.exoplatform.widget;

/**
import greendroid.app.ActionBarActivity;
import greendroid.app.GDApplication;
import greendroid.graphics.drawable.ActionBarDrawable;
import greendroid.util.Config;
import greendroid.widget.ActionBar;
import greendroid.widget.ActionBar.OnActionBarListener;
import greendroid.widget.ActionBar.Type;
import greendroid.widget.ActionBarHost;
import greendroid.widget.ActionBarItem;
import greendroid.widget.NormalActionBarItem;

import org.exoplatform.R;
import org.exoplatform.utils.SettingUtils;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;

@SuppressLint("Registered")
public class MyActionBar extends FragmentActivity implements ActionBarActivity {

  private boolean       mDefaultConstructorUsed = false;

  private Type          mActionBarType          = Type.Normal;

  private ActionBarHost mActionBarHost;

  public MyActionBar() {

    mDefaultConstructorUsed = true;
  }

  public MyActionBar(ActionBar.Type actionBarType) {
    super();

    if (actionBarType == ActionBar.Type.Dashboard)
      mActionBarType = ActionBar.Type.Dashboard;
    else if (actionBarType == ActionBar.Type.Normal)
      mActionBarType = ActionBar.Type.Normal;
    else if (actionBarType == ActionBar.Type.Empty)
      mActionBarType = ActionBar.Type.Empty;
    else
      mActionBarType = actionBarType;

  }

  @Override
  protected void onRestoreInstanceState(Bundle savedInstanceState) {
    ensureLayout();
    super.onRestoreInstanceState(savedInstanceState);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (mDefaultConstructorUsed) {
      // HACK cyril: This should have been done is the default
      // constructor. Unfortunately, the getApplication() method returns
      // null there. Hence, this has to be done here.
      if (getClass().equals(getGDApplication().getHomeActivityClass())) {

        mActionBarType = Type.Normal;
      }
    }
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    ensureLayout();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    SettingUtils.setDefaultLanguage(this);
  }

  public ActionBar.Type getActionBarType() {
    return mActionBarType;
  }

  public int createLayout() {
    if (mActionBarType == Type.Dashboard)
      return R.layout.gd_content_dashboard;

    return R.layout.gd_content_normal;
  }

  protected void ensureLayout() {
    if (!verifyLayout()) {
      setContentView(createLayout());
    }
  }

  protected boolean verifyLayout() {
    return mActionBarHost != null;
  }

  @Override
  public void onContentChanged() {
    super.onContentChanged();

    onPreContentChanged();
    onPostContentChanged();
  }

  public void onPreContentChanged() {
    mActionBarHost = (ActionBarHost) findViewById(R.id.gd_action_bar_host);
    if (mActionBarHost == null) {
      throw new RuntimeException("Your content must have an ActionBarHost whose id attribute is R.id.gd_action_bar_host");
    }
    mActionBarHost.getActionBar().setOnActionBarListener(mActionBarListener);
  }

  public void onPostContentChanged() {

    boolean titleSet = false;

    final Intent intent = getIntent();
    if (intent != null) {
      String title = intent.getStringExtra(ActionBarActivity.GD_ACTION_BAR_TITLE);
      if (title != null) {
        titleSet = true;
        setTitle(title);
      }
    }

    if (!titleSet) {
      // No title has been set via the Intent. Let's look in the
      // ActivityInfo
      try {
        final ActivityInfo activityInfo = getPackageManager().getActivityInfo(getComponentName(), 0);
        if (activityInfo.labelRes != 0) {
          setTitle(activityInfo.labelRes);
        }
      } catch (NameNotFoundException e) {
        if (Config.GD_ERROR_LOGS_ENABLED)
          Log.e("NameNotFoundException", "Cannot find title name!");
        // Do nothing
      }
    }

    final int visibility = intent.getIntExtra(ActionBarActivity.GD_ACTION_BAR_VISIBILITY,
                                              View.VISIBLE);
    getActionBar().setVisibility(visibility);
  }

  @Override
  public void setTitle(CharSequence title) {
    getActionBar().setTitle(title);
  }

  @Override
  public void setTitle(int titleId) {
    setTitle(getString(titleId));
  }

  public ActionBar getActionBar() {
    ensureLayout();
    return mActionBarHost.getActionBar();
  }

  public ActionBarItem addActionBarItem(ActionBarItem item) {
    return getActionBar().addItem(item);
  }

  public ActionBarItem addActionBarItem() {

    ActionBarItem item = getActionBar().newActionBarItem(NormalActionBarItem.class);

    return getActionBar().addItem(item);

  }

  public ActionBarItem addActionBarItem(int itemId) {

    @SuppressWarnings("deprecation")
    final Drawable d = new ActionBarDrawable(this.getResources(), itemId);

    ActionBarItem item = getActionBar().newActionBarItem(NormalActionBarItem.class).setDrawable(d);

    return getActionBar().addItem(item);

  }

  public ActionBarItem addActionBarItem(ActionBarItem item, int itemId) {
    return getActionBar().addItem(item, itemId);
  }

  public ActionBarItem addActionBarItem(ActionBarItem.Type actionBarItemType) {
    return getActionBar().addItem(actionBarItemType);
  }

  public ActionBarItem addActionBarItem(ActionBarItem.Type actionBarItemType, int itemId) {

    return getActionBar().addItem(actionBarItemType, itemId);
  }

  public FrameLayout getContentView() {
    ensureLayout();
    return mActionBarHost.getContentView();
  }

  public void setActionBarContentView(int resID) {
    LayoutInflater.from(this).inflate(resID, getContentView());
    Drawable bg = getActionBar().getBackground();
    if (bg != null && (bg instanceof BitmapDrawable)) {
      BitmapDrawable bmDrawable = (BitmapDrawable) bg;
      bmDrawable.setTileModeX(TileMode.REPEAT);
      getActionBar().setBackgroundDrawable(bmDrawable);
    }
  }

  public void setActionBarContentView(View view, LayoutParams params) {
    getContentView().addView(view, params);
  }

  public void setActionBarContentView(View view) {
    getContentView().addView(view);
  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

    return true;
  }

  private OnActionBarListener mActionBarListener = new OnActionBarListener() {
                                                   public void onActionBarItemClicked(int position) {

                                                     onHandleActionBarItemClick(getActionBar().getItem(position),
                                                                                position);

                                                   }
                                                 };

  public GDApplication getGDApplication() {
    return (GDApplication) getApplication();
  }

}
 **/
