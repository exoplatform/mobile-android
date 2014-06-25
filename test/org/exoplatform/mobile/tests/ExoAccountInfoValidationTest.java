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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.utils.ExoUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * May 13, 2014  
 */
@RunWith(RobolectricTestRunner.class)
public class ExoAccountInfoValidationTest {
  
  @Before
  public void setup() {
  }
  
  @After
  public void teardown() {
  }
  
  
  @Test
  public void testForbiddenTenantNames() {
    
    final String[]    CLOUD_URLS_FORBIDDEN = {
        "http://exoplatform.net", "http://wks-acc.exoplatform.org", "http://netstg.exoplatform.org" };
    
    for (String string : CLOUD_URLS_FORBIDDEN) {
      assertTrue(string+" should be forbidden", ExoUtils.urlHasWrongTenant(string));
    }
    
  }
  
  @Test
  public void testShouldValidateCorrectEmails() {
    
    final String[] EMAILS_OK = {
        "test@example.com",
        "test.test@example.com",
        "test-test@example.com",
        "test_test@example.com",
        "test+test@example.com",
        "test@test.example.com",
        "test@test-example.com"
      };
    
    for (String string : EMAILS_OK) {
      assertTrue(string+" should be valid", ExoUtils.isEmailValid(string));
    }
    
  }
  
  @Test
  public void testShouldNotValidateIncorrectEmailsAndNull() {
    
    final String[] EMAILS_INCORRECT = {
        "example.com",
        "@example.com",
        "test",
        "test@",
        "test@.com" 
      };
    
    for (String string : EMAILS_INCORRECT) {
      assertFalse(string+" should not be validated", ExoUtils.isEmailValid(string));
    }
    
    assertFalse("null should not be validated", ExoUtils.isEmailValid(null));
  }
  
  @Test
  public void testShouldValidateCorrectURLs() {
    
    final String[] URLS_OK = {
        "test.com",
        "test.example.com",
        "test-example.com",
        "test.fr",
        "test.info",
        "test.org",
        "test.net",
        "test.co.uk",
        "http://test.com",
        "https://test.com",
        "t.e.s.t.com",
        "10.100.10.1",
        "test.com:80",
        "test123.com",
        "www.test.com/some/path"
      };
    
    for (String string : URLS_OK) {
      assertTrue(string+" should be valid", ExoUtils.isUrlValid(string));
    }
    
  }
  
  @Test
  public void testShouldNotValidateIncorrectURLsAndNull() {
    
    final String[] URLS_INCORRECT = {
        "test.",
        ".com",
        "http://test{}.com",
        "https://test().com",
        "test[].com",
        "test!.com",
        "test@.com",
        "test#.com",
        "test$.com",
        "test&.com",
        "test*.com",
        "test|.com",
        "test~.com",
        "test example.com",
        "test_example.com",
        "http:test-example.org",
        "http//test-example.org",
        "0.0.0.0",
        "300.10.100.200",
        "test.10.20.30"
      };
    
    for (String string : URLS_INCORRECT) {
      assertFalse(string+" should not be validated", ExoUtils.isUrlValid(string));
    }
    
    assertFalse("null should not be validated", ExoUtils.isUrlValid(null));
  }
  
  
  @Test 
  public void testShouldStripCorrectURLs() {
    
    String expectedUrl = "http://test.com";
    String[] HTTP_URLS = {
      "test.com",
      "http://test.com",
      "test.com/some/long/path/",
      "http://test.com/shortpath/",
      "http://test.com/index.html"
    }; 
    
    for (String string : HTTP_URLS) {
      String url = ExoUtils.stripUrl(string);
      assertThat("URL '"+string+"' should have been stripped to "+expectedUrl, url, equalTo(expectedUrl));
    }
    
    expectedUrl = "https://test.com";
    String[] HTTPS_URLS = {
      "https://test.com",
      "https://test.com/some/long/path/",
      "https://test.com/shortpath/",
      "https://test.com/index.html"
    };
    
    for (String string : HTTPS_URLS) {
      String url = ExoUtils.stripUrl(string);
      assertThat("URL '"+string+"' should have been stripped to "+expectedUrl, url, equalTo(expectedUrl));
    }
     
    // Other URLs with ports
   String myURL = "http://test.fr:8080";
   expectedUrl = "http://test.fr:8080";
   String url = ExoUtils.stripUrl(myURL);
   assertThat("URL '"+myURL+"' should have been stripped to "+expectedUrl, url, equalTo(expectedUrl));
   
   myURL = "https://test.fr:443";
   expectedUrl = "https://test.fr:443";
   url = ExoUtils.stripUrl(myURL);
   assertThat("URL '"+myURL+"' should have been stripped to "+expectedUrl, url, equalTo(expectedUrl));
   
   myURL = "http://test.fr:80";
   expectedUrl = "http://test.fr:80";
   url = ExoUtils.stripUrl(myURL);
   assertThat("URL '"+myURL+"' should have been stripped to "+expectedUrl, url, equalTo(expectedUrl));
    
  }
  
  @Test
  public void testStripShouldFailWithIncorrectURLsAndNull() {
    
    
    
  }
  
  @Test
  public void testAccountsAreEqual() {
    
    ServerObjInfo account1 = new ServerObjInfo();
    ServerObjInfo account2 = new ServerObjInfo();
    
    // account 1
    account1.serverName = "test server";
    account1.serverUrl = "http://test-example.com";
    account1.username = "testuser";
    // account 2
    account2.serverName = "test server";
    account2.serverUrl = "http://test-example.com";
    account2.username = "testuser";
    // test
    assertTrue("Account 1 and 2 should be equal", account1.equals(account2));
    
    // empty usernames
    account1.username = "";
    account2.username = "";
    assertTrue("Account 1 and 2 should be equal with empty usernames", account1.equals(account2));
    
    // different passwords
    account1.password = "password";
    account2.password = "other-password";
    assertTrue("Account 1 and 2 should be equal with different passwords", account1.equals(account2));
    
    // different auto login option
    account1.isAutoLoginEnabled = true;
    account2.isAutoLoginEnabled = false;
    assertTrue("Account 1 and 2 should be equal with different auto login option", account1.equals(account2));
    
    // different remember me option
    account1.isRememberEnabled = true;
    account2.isRememberEnabled = false;
    assertTrue("Account 1 and 2 should be equal with different remember me option", account1.equals(account2));
    
  }
  
  @Test
  public void testAccountsAreNotEqual() {
    
    ServerObjInfo account1 = new ServerObjInfo();
    ServerObjInfo account2 = new ServerObjInfo();
    
    // different server name
    account1.serverName = "test server";
    account2.serverName = "another test server";
    account1.serverUrl  = "http://test-example.com";
    account2.serverUrl  = "http://test-example.com";
    account1.username   = "testuser";
    account2.username   = "testuser";
    assertFalse("Account 1 and 2 should be different, server names are not equal", account1.equals(account2));
    
    // different server URL
    account1.serverName = "test server";
    account2.serverName = "test server";
    account1.serverUrl  = "http://test-example.com";
    account2.serverUrl  = "http://another-test-example.com";
    account1.username   = "testuser";
    account2.username   = "testuser";
    assertFalse("Account 1 and 2 should be different, URLs are not equal", account1.equals(account2));
    
    // different username
    account1.serverName = "test server";
    account2.serverName = "test server";
    account1.serverUrl  = "http://test-example.com";
    account2.serverUrl  = "http://test-example.com";
    account1.username   = "testuser";
    account2.username   = "othertestuser";
    assertFalse("Account 1 and 2 should be different, usernames are not equal", account1.equals(account2));
    
  }
  
  
  
}
