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
import android.content.Context;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Apr 15, 2014  
 */
public abstract class ExoActivityTestUtils<A extends Activity> {
  
  final String TEST_SERVER_NAME = "testserver";
  final String TEST_SERVER_URL = "http://www.test.com";
  final String TEST_HTTPS_SERVER_URL = "https://www.test.com";
  final String TEST_WRONG_SERVER_URL = "test&%$'{}";
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
  final String RESP_SOCIAL_NEWS = // 1 activity
      "{\"activities\":[{\"body\":\"\", \"appId\":\"\", \"identityId\":\"7ba7ff1a0a2106c63a820dfdda487a4a\", \"totalNumberOfComments\":1, \"templateParams\":{\"author\":\"fdrouet\", \"contenLink\":\"repository/collaboration/sites/intranet/web contents/Contributions/jmxtrans-addon\", \"contentName\":\"jmxtrans-addon\", \"dateCreated\":\"2014-04-17T11:57:18\", \"docSummary\":\"\", \"docTitle\":\"JMXTrans addon\", \"docTypeLabel\":\"Add-on\", \"docVersion\":\"0\", \"id\":\"6f1e77f8c06313bc4af5b62c1775b63d\", \"imagePath\":\"\", \"isSystemComment\":\"true\", \"lastModified\":\"\", \"message\":\"SocialIntegration.messages.emptyContent\", \"mimeType\":\"\", \"repository\":\"repository\", \"state\":\"draft\", \"systemComment\":\"\", \"workspace\":\"collaboration\"}, \"liked\":false, \"lastUpdated\":1397728639490, \"postedTime\":1397728639167, \"type\":\"contents:spaces\", \"posterIdentity\":{\"id\":\"7ba7ff1a0a2106c63a820dfdda487a4a\", \"providerId\":\"organization\", \"remoteId\":\"fdrouet\", \"profile\":{\"avatarUrl\":\"http://community.exoplatform.com:80/rest/jcr/repository/social/production/soc%3Aproviders/soc%3Aorganization/soc%3Afdrouet/soc%3Aprofile/soc%3Aavatar/?upd=1372169292623\", \"fullName\":\"Frederic DROUET\"} }, \"activityStream\":{\"title\":\"\", \"permaLink\":\"http://community.exoplatform.com:80/portal/intranet/activities/fdrouet\", \"prettyId\":\"fdrouet\", \"faviconUrl\":\"\", \"fullName\":\"Frederic DROUET\", \"type\":\"organization\"}, \"id\":\"6f1e78dac06313bc6eb98680ce104064\", \"title\":\"JMXTrans addon\", \"priority\":0.0, \"createdAt\":\"Thu Apr 17 11:57:19 +0200 2014\", \"likedByIdentities\":[], \"totalNumberOfLikes\":0, \"titleId\":\"\", \"comments\":[] } ] }";
  
  final int REQ_SOCIAL_MY_CONNECTIONS = 4;
  final String RESP_SOCIAL_MY_CONNECTIONS = // 1 activity
      "{\"activities\":[{\"body\":\"\",\"appId\":\"\",\"identityId\":\"51998d9b0a2106c60330eb14726dc376\",\"totalNumberOfComments\":1,\"templateParams\":{},\"liked\":false,\"lastUpdated\":1396514460377,\"postedTime\":1396514164727,\"type\":\"DEFAULT_ACTIVITY\",\"posterIdentity\":{\"id\":\"51998d9b0a2106c60330eb14726dc376\",\"providerId\":\"organization\",\"remoteId\":\"patrice_lamarque\",\"profile\":{\"avatarUrl\":\"http://community.exoplatform.com:80/rest/jcr/repository/social/production/soc%3Aproviders/soc%3Aorganization/soc%3Apatrice_lamarque/soc%3Aprofile/soc%3Aavatar/?upd=1371464305999\",\"fullName\":\"Patrice Lamarque\"}},\"activityStream\":{\"title\":\"\",\"permaLink\":\"http://community.exoplatform.com:80/portal/intranet/activities/patrice_lamarque\",\"prettyId\":\"patrice_lamarque\",\"faviconUrl\":\"\",\"fullName\":\"Patrice Lamarque\",\"type\":\"organization\"},\"id\":\"26bb0ff9c06313bc307d3e126720561e\",\"title\":\"2 very cool add-ons have been added to the add-ons center today : <ul><li><a href=\"http://community.exoplatform.com/portal/intranet/addon-detail?content-id=/repository/collaboration/sites/intranet/web%20contents/Contributions/exo-atemis-extension\">Atemis</a></li><li><a href=\"http://community.exoplatform.com/portal/intranet/addon-detail?content-id=/repository/collaboration/sites/intranet/web%20contents/Contributions/video-wiki-macro\">Video Wiki Macro</a></li></ul>Try them out!\",\"priority\":0.0,\"createdAt\":\"Thu Apr 3 10:36:04 +0200 2014\",\"likedByIdentities\":[],\"totalNumberOfLikes\":8,\"titleId\":\"\",\"comments\":[]}]}";

  final int REQ_SOCIAL_ALL_UPDATES = 5;
  final String RESP_SOCIAL_ALL_UPDATES = // 2 activities
      "{\"activities\":[{\"body\":\"\",\"appId\":\"\",\"identityId\":\"f07184510a2106c6201ac1956140d95f\",\"totalNumberOfComments\":2,\"templateParams\":{},\"liked\":false,\"lastUpdated\":1398099158541,\"postedTime\":1398013281970,\"type\":\"DEFAULT_ACTIVITY\",\"posterIdentity\":{\"id\":\"7fe04a04c06313bc6121d1235b281f23\",\"providerId\":\"organization\",\"remoteId\":\"ilkay_aydemir10\",\"profile\":{\"avatarUrl\":\"http://community.exoplatform.com:80/rest/jcr/repository/social/production/soc%3Aproviders/soc%3Aorganization/soc%3Ailkay_aydemir10/soc%3Aprofile/soc%3Aavatar/?upd=1398009779213\",\"fullName\":\"ilkay Aydemir\"}},\"activityStream\":{\"title\":\"\",\"permaLink\":\"http://community.exoplatform.com:80/portal/intranet/activities/jmazziotta\",\"prettyId\":\"jmazziotta\",\"faviconUrl\":\"\",\"fullName\":\"Julie Mazziotta\",\"type\":\"organization\"},\"id\":\"8015c6b9c06313bc7bddbab957acc8b0\",\"title\":\"Are you the admin site?\",\"priority\":0.0,\"createdAt\":\"Sun Apr 20 19:01:21 +0200 2014\",\"likedByIdentities\":[],\"totalNumberOfLikes\":0,\"titleId\":\"\",\"comments\":[]},{\"body\":\"\",\"appId\":\"\",\"identityId\":\"d3c28a300a2106c658573c3c030bf9da\",\"totalNumberOfComments\":2,\"templateParams\":{},\"liked\":false,\"lastUpdated\":1398080445273,\"postedTime\":1397828057340,\"type\":\"DEFAULT_ACTIVITY\",\"posterIdentity\":{\"id\":\"51998d9b0a2106c60330eb14726dc376\",\"providerId\":\"organization\",\"remoteId\":\"patrice_lamarque\",\"profile\":{\"avatarUrl\":\"http://community.exoplatform.com:80/rest/jcr/repository/social/production/soc%3Aproviders/soc%3Aorganization/soc%3Apatrice_lamarque/soc%3Aprofile/soc%3Aavatar/?upd=1371464305999\",\"fullName\":\"Patrice Lamarque\"}},\"activityStream\":{\"title\":\"\",\"permaLink\":\"http://community.exoplatform.com:80/portal/g/:spaces:translations/translations\",\"prettyId\":\"translations\",\"faviconUrl\":\"\",\"fullName\":\"Translations\",\"type\":\"space\"},\"id\":\"750b7901c06313bc6477591b2d591ed4\",\"title\":\"<a href='/portal/intranet/profile/nguyenbaoan'>An Bao Nguyen</a> <a href='/portal/intranet/profile/tglenat'>Tristan Glenat</a> Shouldn't we move eXo Mobile Translations to <a href='http://translate.exoplatform.org/project/exo-platform/' target='_blank'>http://translate.exoplatform.org/project/exo-platform/</a> now ? It will make them easier to find for potential contributors.\",\"priority\":0.0,\"createdAt\":\"Fri Apr 18 15:34:17 +0200 2014\",\"likedByIdentities\":[],\"totalNumberOfLikes\":3,\"titleId\":\"\",\"comments\":[]}]}";
  
  final int REQ_PLATFORM_INFO = 6;
  final String RESP_PLATFORM_INFO = 
      "{\"duration\":\"UNLIMITED\",\"platformEdition\":\"ENTERPRISE\",\"buildNumber\":null,\"productCode\":\"CWI-team-09LC0xLDA2L\",\"unlockKey\":\"aaabbbccc\",\"nbUsers\":null,\"dateOfKeyGeneration\":null,\"platformVersion\":\"4.0.4\",\"isMobileCompliant\":\"true\",\"platformBuildNumber\":\"20131225\",\"platformRevision\":\"aaabbbccc\",\"userHomeNodePath\":\"/Users/p___/ph___/phi___/philippe\",\"runningProfile\":\"all\",\"currentRepoName\":\"repository\",\"defaultWorkSpaceName\":\"collaboration\"}";
  
  final int REQ_JCR_USER = 7;
  final String RESP_JCR_USER = "{}";
  
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
  
  public void createAndAttach() {
    activity = controller
        .attach()
        .create()
        .start()
        .resume()
        .visible()
        .get();
  }
  
  public void createWithContext(Context ctx) {
	 activity = controller
	   .withBaseContext(ctx)
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
      
    case REQ_SOCIAL_ALL_UPDATES:
      m.path("rest/private/api/social/v1-alpha3/portal/activity_stream/feed.json");
      m.param("limit", "50");
      break;
      
    case REQ_SOCIAL_MY_CONNECTIONS:
      m.path("rest/private/api/social/v1-alpha3/portal/activity_stream/connections.json");
      break;

    case REQ_PLATFORM_INFO:
      m.path("rest/private/platform/info");
      break;
      
    case REQ_JCR_USER:
      m.path("rest/private/jcr/repository/collaboration/Users/t___/te___/tes___/testuser");
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
        
      case REQ_SOCIAL_ALL_UPDATES:
        resp.setEntity(new StringEntity(RESP_SOCIAL_ALL_UPDATES));
        break;
       
      case REQ_SOCIAL_MY_CONNECTIONS:
        resp.setEntity(new StringEntity(RESP_SOCIAL_MY_CONNECTIONS));
        break;

      case REQ_PLATFORM_INFO:
        resp.setEntity(new StringEntity(RESP_PLATFORM_INFO));
        break;
        
      case REQ_JCR_USER:
        resp.setEntity(new StringEntity(RESP_JCR_USER));
        break;
      }
      
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
    
    return resp;
  }
  
}
