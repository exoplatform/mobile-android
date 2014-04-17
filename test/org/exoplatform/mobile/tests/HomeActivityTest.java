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

import org.exoplatform.R;
import org.exoplatform.ui.HomeActivity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Apr 16, 2014  
 */
@RunWith(RobolectricTestRunner.class)
public class HomeActivityTest extends ExoActivityTestUtils<HomeActivity> {

  ViewFlipper flipper;
  TextView activities, documents, apps;
  
  private void init() {
    flipper = (ViewFlipper)activity.findViewById(R.id.home_social_flipper);
    activities = (TextView)activity.findViewById(R.id.home_btn_activity);
    documents = (TextView)activity.findViewById(R.id.home_btn_document);
    apps = (TextView)activity.findViewById(R.id.home_btn_apps);
  }
  
  @Override
  @Before
  public void setup() {
    controller = Robolectric.buildActivity(HomeActivity.class);
    
    // mock response for HTTP GET /rest/api/social/version/latest.json
    Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_VERSION_LATEST), getResponseOKForRequest(REQ_SOCIAL_VERSION_LATEST));
    // mock response for HTTP GET /rest/private/api/social/v1-alpha3/portal/identity/organization/{testuser}.json
    Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_IDENTITY), getResponseOKForRequest(REQ_SOCIAL_IDENTITY));
    // mock response for HTTP GET /rest/private/api/social/v1-alpha3/portal/identity/{testidentity}.json
    Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_IDENTITY_2), getResponseOKForRequest(REQ_SOCIAL_IDENTITY_2));
    // mock response for HTTP GET /rest/private/api/social/v1-alpha3/portal/activity_stream/feed.json?limit=10
    Robolectric.addHttpResponseRule(getMatcherForRequest(REQ_SOCIAL_NEWS), getResponseOKForRequest(REQ_SOCIAL_NEWS));
  }
  
  @Override
  @After
  public void teardown() {
    // empty to avoid call to controller.destroy()
  }
  
  @Test
  public void verifyDefaultLayout() {
    
    create();
    init();
    
    assertThat(flipper).hasChildCount(1); // should have only 1 activity in the flipper since RESP_SOCIAL_NEWS contains just 1 activity
    
    assertThat(activities).containsText(R.string.ActivityStream);
    assertThat(documents).containsText(R.string.Documents);
    assertThat(apps).containsText(R.string.Dashboard);
    
  }

}
