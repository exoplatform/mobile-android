/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.mobile.tests;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.robolectric.Shadows.shadowOf;

import org.exoplatform.R;
import org.exoplatform.ui.CirclePageIndicator;
import org.exoplatform.ui.SignInActivity;
import org.exoplatform.ui.SignUpActivity;
import org.exoplatform.ui.WelcomeActivity;
import org.exoplatform.ui.login.LoginActivity;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowView;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com Apr 8, 2014
 */
public class WelcomeActivityTest extends ExoActivityTestUtils<WelcomeActivity> {

  Resources           res;

  Button              btnLogin, btnSignUp;

  TextView            skipTxt;

  ViewPager           pager;

  CirclePageIndicator indicator;

  @Before
  public void setup() {
    controller = Robolectric.buildActivity(WelcomeActivity.class);
  }

  @Override
  public void create() {
    super.create();
    init();
  }

  @Override
  public void createWithBundle(Bundle b) {
    super.createWithBundle(b);
    init();
  }

  private void init() {
    res = activity.getResources();
    btnLogin = (Button) activity.findViewById(R.id.welcome_btn_login);
    btnSignUp = (Button) activity.findViewById(R.id.welcome_btn_signup);
    skipTxt = (TextView) activity.findViewById(R.id.welcome_txt_skipStep);
    pager = (ViewPager) activity.findViewById(org.exoplatform.R.id.pager);
    indicator = (CirclePageIndicator) activity.findViewById(R.id.circle_page_indicator);
  }

  @Test
  public void verifyDefaultLayout() {
    create();

    assertThat(pager.getAdapter()).hasCount(5); // pager shows 5 screenshots

    // check Log In button label
    assertThat(btnLogin).containsText(R.string.LogIn);
    org.junit.Assert.assertThat(res.getString(R.string.LogIn), equalTo("Log In"));

    // check Sign Up button label
    assertThat(btnSignUp).containsText(R.string.SignUp);
    org.junit.Assert.assertThat(res.getString(R.string.SignUp), equalTo("Sign Up"));

    assertThat(skipTxt).containsText(R.string.SkipStep);
    org.junit.Assert.assertThat(res.getString(R.string.SkipStep), equalTo("or skip this for now"));

  }

  @Test
  public void shouldRedirectToSignInScreen() {
    create();

    ShadowView.clickOn(btnLogin);
    ShadowActivity shadowActivity = shadowOf(activity);
    Intent startedIntent = shadowActivity.getNextStartedActivity();
    ShadowIntent shadowIntent = shadowOf(startedIntent);
    org.junit.Assert.assertThat(shadowIntent.getComponent().getClassName(), equalTo(SignInActivity.class.getName()));
  }

  @Test
  public void shouldRedirectToSignUpScreen() {
    create();

    ShadowView.clickOn(btnSignUp);
    ShadowActivity shadowActivity = shadowOf(activity);
    Intent startedIntent = shadowActivity.getNextStartedActivity();
    ShadowIntent shadowIntent = shadowOf(startedIntent);
    org.junit.Assert.assertThat(shadowIntent.getComponent().getClassName(), equalTo(SignUpActivity.class.getName()));
  }

  @Test
  public void shouldRedirectToLoginScreen() {
    create();

    ShadowView.clickOn(skipTxt);
    ShadowActivity shadowActivity = shadowOf(activity);
    Intent startedIntent = shadowActivity.getNextStartedActivity();
    ShadowIntent shadowIntent = shadowOf(startedIntent);
    org.junit.Assert.assertThat(shadowIntent.getComponent().getClassName(), equalTo(LoginActivity.class.getName()));
  }

  @Test
  public void shouldSaveInstanceState() {
    create();

    Bundle bundle = new Bundle();
    bundle.putInt("CURRENT_SLIDER", -1); // init current page to -1 to make
                                         // sure the value is saved by the
                                         // activity

    controller.saveInstanceState(bundle); // save the activity state in the
                                          // bundle

    // check that the current page number has been saved
    org.junit.Assert.assertThat(bundle.getInt("CURRENT_SLIDER"), equalTo(0));

  }

  @Test
  public void shoudRetrieveInstanceState() {
    Bundle bundle = new Bundle();
    bundle.putInt("CURRENT_SLIDER", 1); // change the value of the current
                                        // page
    createWithBundle(bundle);

    assertThat(pager).hasCurrentItem(1); // check that the previous state
                                         // was retrieved
  }

}
