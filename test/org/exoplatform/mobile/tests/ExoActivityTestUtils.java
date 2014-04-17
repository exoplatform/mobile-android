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

import java.io.UnsupportedEncodingException;

import org.apache.http.ProtocolVersion;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpResponse;
import org.exoplatform.model.ServerObjInfo;
import org.junit.After;
import org.junit.Before;
import org.robolectric.tester.org.apache.http.FakeHttpLayer.RequestMatcherBuilder;
import org.robolectric.tester.org.apache.http.RequestMatcher;
import org.robolectric.util.ActivityController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Apr 15, 2014  
 */
public abstract class ExoActivityTestUtils<A extends Activity> {
  
  final String TEST_SERVER_NAME = "testserver";
  final String TEST_SERVER_URL = "http://www.test.com";
  final String TEST_USER_NAME = "testuser";
  final String TEST_USER_PWD = "testpwd";
  final String TEST_USER_IDENTITY = "testidentityid";
  
  final int REQ_SOCIAL_VERSION_LATEST = 0;
  final String RESP_SOCIAL_VERSION_LATEST = "{\"version\":\"v1-alpha3\"}";
  final int REQ_SOCIAL_IDENTITY = 1;
  final int REQ_SOCIAL_IDENTITY_2 = 2;
  final String RESP_SOCIAL_IDENTITY = "{" +
  		"\"id\":\""+TEST_USER_IDENTITY+"\"," +
  		"\"providerId\":\"organization\"," +
  		"\"remoteId\":\""+TEST_USER_NAME+"\"," +
  		"\"profile\":{\"avatarUrl\":\"\"," +
  		"\"fullName\":\""+TEST_USER_NAME+"\"}" +
  "}";
  final int REQ_SOCIAL_NEWS = 3;
  final String RESP_SOCIAL_NEWS = 
      "{\"activities\":[{\"body\":\"\", \"appId\":\"\", \"identityId\":\"7ba7ff1a0a2106c63a820dfdda487a4a\", \"totalNumberOfComments\":1, \"templateParams\":{\"author\":\"fdrouet\", \"contenLink\":\"repository/collaboration/sites/intranet/web contents/Contributions/jmxtrans-addon\", \"contentName\":\"jmxtrans-addon\", \"dateCreated\":\"2014-04-17T11:57:18\", \"docSummary\":\"\", \"docTitle\":\"JMXTrans addon\", \"docTypeLabel\":\"Add-on\", \"docVersion\":\"0\", \"id\":\"6f1e77f8c06313bc4af5b62c1775b63d\", \"imagePath\":\"\", \"isSystemComment\":\"true\", \"lastModified\":\"\", \"message\":\"SocialIntegration.messages.emptyContent\", \"mimeType\":\"\", \"repository\":\"repository\", \"state\":\"draft\", \"systemComment\":\"\", \"workspace\":\"collaboration\"}, \"liked\":false, \"lastUpdated\":1397728639490, \"postedTime\":1397728639167, \"type\":\"contents:spaces\", \"posterIdentity\":{\"id\":\"7ba7ff1a0a2106c63a820dfdda487a4a\", \"providerId\":\"organization\", \"remoteId\":\"fdrouet\", \"profile\":{\"avatarUrl\":\"http://community.exoplatform.com:80/rest/jcr/repository/social/production/soc%3Aproviders/soc%3Aorganization/soc%3Afdrouet/soc%3Aprofile/soc%3Aavatar/?upd=1372169292623\", \"fullName\":\"Frédéric DROUET\"} }, \"activityStream\":{\"title\":\"\", \"permaLink\":\"http://community.exoplatform.com:80/portal/intranet/activities/fdrouet\", \"prettyId\":\"fdrouet\", \"faviconUrl\":\"\", \"fullName\":\"Frédéric DROUET\", \"type\":\"organization\"}, \"id\":\"6f1e78dac06313bc6eb98680ce104064\", \"title\":\"JMXTrans addon\", \"priority\":0.0, \"createdAt\":\"Thu Apr 17 11:57:19 +0200 2014\", \"likedByIdentities\":[], \"totalNumberOfLikes\":0, \"titleId\":\"\", \"comments\":[] } ] }";
  
  
  ActivityController<A> controller;
  A activity;
  
  @Before
  public abstract void setup();
  
  @After
  public void teardown() {
    controller.destroy();
  }
  
  public void create() {
    activity = controller
        .create()
        .start()
        .resume()
        .visible()
        .get();
  }
  
  public void createWithBundle(Bundle b) {
    activity = controller
        .create(b) // passing the bundle here, no need to call controller.restoreInstanceState(b)
        .start()
        .resume()
        .visible()
        .get();
  }
  
  public void createWithIntent(Intent i) {
     activity = controller
       .withIntent(i) // passing the intent here
       .create()
       .start()
       .resume()
       .visible()
       .get();
  }
  
  // UTILS
  
  /**
   * Creates a Server Object with the default name, URL, username and password
   * @return a ServerObjInfo object
   */
  public ServerObjInfo getServerWithDefaultValues() {
    ServerObjInfo srv = new ServerObjInfo();
    srv.serverName = TEST_SERVER_NAME;
    srv.serverUrl = TEST_SERVER_URL;
    srv.username = TEST_USER_NAME;
    srv.password = TEST_USER_PWD;
    return srv;
  }
  
  /**
   * Creates a RequestMatcher to use in Robolectric.addHttpResponseRule() for the specified request
   * @param req the id of the request, e.g REQ_SOCIAL_IDENTITY
   * @return a RequestMatcher object that matches the request id
   */
  public RequestMatcher getMatcherForRequest(int req) {
    RequestMatcherBuilder m = new RequestMatcherBuilder();
    switch (req) {
    case REQ_SOCIAL_VERSION_LATEST:
      m.path("rest/api/social/version/latest.json");
      break;
   
    case REQ_SOCIAL_IDENTITY:
      m.path("rest/private/api/social/v1-alpha3/portal/identity/organization/"+TEST_USER_NAME+".json");
      break;
      
    case REQ_SOCIAL_IDENTITY_2:
      m.path("rest/private/api/social/v1-alpha3/portal/identity/"+TEST_USER_IDENTITY+".json");
      break;
      
    case REQ_SOCIAL_NEWS:
      m.path("rest/private/api/social/v1-alpha3/portal/activity_stream/feed.json");
      m.param("limit", "10");
      break;
      
    default:
      break;
    }
    return m;
  }
  
  /**
   * Creates a BasicHttpResponse to use in Robolectric.addHttpResponseRule() for the specified request.<br/>
   * Response details:<br/>
   * - HTTP 1.1 <br/>
   * - 200 OK <br/>
   * - a content (entity) corresponding to the specified request, e.g. REQ_SOCIAL_VERSION_LATEST -> {"version":"v1-alpha3"}
   * @param req the id of the request, e.g REQ_SOCIAL_VERSION_LATEST
   * @return the response that matches the request id
   */
  public BasicHttpResponse getResponseOKForRequest(int req) {
    BasicHttpResponse resp = new BasicHttpResponse(new ProtocolVersion("HTTP", 1, 1), 200, "OK");
    try {
      
      switch (req) {
      case REQ_SOCIAL_VERSION_LATEST:
        resp.setEntity(new StringEntity(RESP_SOCIAL_VERSION_LATEST));
        break;
        
      case REQ_SOCIAL_IDENTITY:
      case REQ_SOCIAL_IDENTITY_2:
        resp.setEntity(new StringEntity(RESP_SOCIAL_IDENTITY));
        break;
      
      case REQ_SOCIAL_NEWS:
        resp.setEntity(new StringEntity(RESP_SOCIAL_NEWS));
        break;
       
      default:
        break;
      }
      
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    
    return resp;
  }
  
}
