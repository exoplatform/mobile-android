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
package org.exoplatform.mobile.tests;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

import org.exoplatform.R;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.ui.SignInOnPremiseActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com May 13, 2014
 */
@RunWith(ExoRobolectricTestRunner.class)
public class SignInOnPremiseActivityTest extends ExoActivityTestUtils<SignInOnPremiseActivity> {

    EditText url, username, password;

    Button   login;

    @Override
    @Before
    public void setup() {
        controller = Robolectric.buildActivity(SignInOnPremiseActivity.class);

    }

    @Override
    public void create() {
        super.create();
        init();
    }

    public void createAndExpectHTTPRequests() {
        super.create();
        Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_PLATFORM_INFO),
                                        getResponseOKForRequest(REQ_PLATFORM_INFO));
        Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_JCR_USER),
                                        getResponseOKForRequest(REQ_JCR_USER));
        init();
    }

    private void init() {
        url = (EditText) activity.findViewById(R.id.onpremise_url_edit_txt);
        username = (EditText) activity.findViewById(R.id.onpremise_user_edit_txt);
        password = (EditText) activity.findViewById(R.id.onpremise_pass_edit_txt);
        login = (Button) activity.findViewById(R.id.onpremise_login_btn);
    }

    private void typeCorrectValues() {
        typeValues(TEST_USER_NAME, TEST_USER_PWD, TEST_SERVER_URL);
    }

    private void typeValues(String _user, String _pass, String _srv) {
        url.setText(_srv);
        username.setText(_user);
        password.setText(_pass);
    }

    @Test
    public void verifyDefaultLayout() {
        create();

        // Login btn is disabled at first
        assertThat(login).isDisabled();

    }

    @Test
    public void shouldEnableLoginButton() {

        create();

        typeCorrectValues();

        assertThat(login).isEnabled();

    }

    @Test
    public void shouldRedirectToHomeAfterSuccessfulLogin() {
        createAndExpectHTTPRequests();

        typeCorrectValues();

        Robolectric.clickOn(login);

        // After a successful login, app should redirect to the Home Activity
        ShadowActivity sActivity = shadowOf(activity);
        Intent homeIntent = sActivity.getNextStartedActivity();
        ShadowIntent sIntent = shadowOf(homeIntent);

        assertThat(sIntent.getComponent().getClassName(), equalTo(HomeActivity.class.getName()));

    }

    @Test
    public void shouldFailToLoginWithIncorrectURL() {
        // not calling createAndExpectHTTPRequests() here ensures the test will
        // fail if incorrect data does not prevent the sign-in request
        create();

        // test with incorrect URL format
        typeValues(TEST_USER_NAME, TEST_USER_PWD, TEST_WRONG_SERVER_URL);
        Robolectric.clickOn(login);

        // if we get here, it's because the login HTTP request was not sent,
        // which is normal since the URL is incorrect
        // if the login request is sent, the test will fail because we didn't
        // add any HttpResponseRules
        assertTrue(true);

        // test with empty URL
        typeCorrectValues(); // enable login
        typeValues(TEST_USER_NAME, TEST_USER_PWD, "");
        assertThat(login).isDisabled();

    }

    @Test
    public void shouldFailToLoginWithIncorrectUsernameAndPassword() {
        create();

        // test with empty password
        typeCorrectValues(); // enable login
        typeValues(TEST_USER_NAME, "", TEST_SERVER_URL);
        assertThat(login).isDisabled();

        // test with empty username
        typeCorrectValues(); // enable login
        typeValues("", TEST_USER_PWD, TEST_SERVER_URL);
        assertThat(login).isDisabled();

        // test with empty username and password
        typeCorrectValues(); // enable login
        typeValues("", "", TEST_SERVER_URL);
        assertThat(login).isDisabled();
    }

    @Test
    public void shouldAcceptURLWithHTTPS() {
        createAndExpectHTTPRequests();

        typeValues(TEST_USER_NAME, TEST_USER_PWD, TEST_HTTPS_SERVER_URL);

        Robolectric.clickOn(login);

        // After a successful login, app should redirect to the Home Activity
        ShadowActivity sActivity = shadowOf(activity);
        Intent homeIntent = sActivity.getNextStartedActivity();
        ShadowIntent sIntent = shadowOf(homeIntent);

        assertThat(sIntent.getComponent().getClassName(), equalTo(HomeActivity.class.getName()));

    }

    @Test
    public void shouldAcceptURLWithNoProtocol() {
        createAndExpectHTTPRequests();

        typeValues(TEST_USER_NAME, TEST_USER_PWD, TEST_SERVER_NAME + ".com"); // URL
                                                                              // should
                                                                              // be
                                                                              // testserver.com

        Robolectric.clickOn(login);

        // After a successful login, app should redirect to the Home Activity
        ShadowActivity sActivity = shadowOf(activity);
        Intent homeIntent = sActivity.getNextStartedActivity();
        ShadowIntent sIntent = shadowOf(homeIntent);

        assertThat(sIntent.getComponent().getClassName(), equalTo(HomeActivity.class.getName()));

    }

}
