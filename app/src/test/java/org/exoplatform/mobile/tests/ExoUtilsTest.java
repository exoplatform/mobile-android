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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.exoplatform.BuildConfig;
import org.exoplatform.utils.ExoUtils;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com Oct 28, 2014
 */

// API LEVEL 21
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricTestRunner.class)
public class ExoUtilsTest {

  @Before
  public void setUp() throws Exception {
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testUrlValidationFailed() {
    String[] validUrls = { "http://www.mycompany.", "shttps://safe.company.com", "www.mycompany.com!",
        "http://int.my.company_com.vn", "http://1192.168.4.42" };

    for (String url : validUrls) {
      assertFalse(url + " should NOT be valid.", ExoUtils.isUrlValid(url));
    }
  }

  @Test
  public void testUrlValidationPassed() {
    String[] validUrls = { "http://www.mycompany.com", "https://safe.company.com", "www.mycompany.com",
        "http://int.my.company.com.vn", "http://192.168.4.42", "http://192.168.4.42:8080", "http://my-company.com" };

    for (String url : validUrls) {
      assertTrue(url + " should be valid.", ExoUtils.isUrlValid(url));
    }
  }

  // @Test
  public void testDocumentUrlValidationFailed() {

  }

  // @Test
  public void testDocumentUrlValidationPassed() {

  }

  // @Test
  public void testDocumentUrlEncoding() {

  }

  @Test
  public void testStripUrl() {
    String url1 = "http://www.mycompany.com";
    String url2 = "https://www.mycompany.com";
    String url3 = "http://www.mycompany.com:8080";
    String url4 = "http://www.mycompany.com/some/path";
    String url5 = "http://www.mycompany.com?foo=bar";
    String url6 = "www.mycompany.com";

    assertEquals("http://www.mycompany.com", ExoUtils.stripUrl(url1));
    assertEquals("https://www.mycompany.com", ExoUtils.stripUrl(url2));
    assertEquals("http://www.mycompany.com:8080", ExoUtils.stripUrl(url3));
    assertEquals("http://www.mycompany.com", ExoUtils.stripUrl(url4));
    assertEquals("http://www.mycompany.com", ExoUtils.stripUrl(url5));
    assertEquals("http://www.mycompany.com", ExoUtils.stripUrl(url6));
  }

  @Test
  public void testAccountNameValidationFailed() {
    String incorrectChars = "~ ` ! @ # $ % ^ & * ( ) = { } [ ] | \\ : ; \" ' , < > ? / - _ .";
    String[] chars = incorrectChars.split(" ");
    for (String c : chars) {
      String incorrectName = "Account " + c;
      assertFalse(incorrectName + " should be an invalid account name", ExoUtils.isServerNameValid(incorrectName));
    }
    assertFalse("null should be invalid", ExoUtils.isServerNameValid(null));
  }

  @Test
  public void testAccountNameValidationPassed() {
    String[] correctNames = { "Account", "Account 123", "My Account", "my long account name" };
    for (String name : correctNames) {
      assertTrue(name + " should be a valid account name", ExoUtils.isServerNameValid(name));
    }
  }

  @Test
  public void testAccountUsernameValidationFailed() {
    // list all forbidden characters here, separated by a space
    String incorrectChars = "~ ` ! @ # $ % ^ & * ( ) = { } [ ] | \\ : ; \" ' , < > ? /";
    String[] chars = incorrectChars.split(" ");
    for (String c : chars) {
      String wrongUsername = "john" + c + "doe";
      assertFalse("Username '" + wrongUsername + "' should not have been validated", ExoUtils.isUsernameValid(wrongUsername));
    }
    // test username that contains a space
    assertFalse("Username 'john doe' should not have been validated", ExoUtils.isUsernameValid("john doe"));
  }

  @Test
  public void testAccountUsernameValidationPassed() {
    String[] testUsernames = { "johndoe", "john.doe", "john-doe", "john_doe", "john+doe", "JohnDoe", "johndoe1234" };
    for (String username : testUsernames) {
      assertTrue("Username '" + username + "' should have been validated", ExoUtils.isUsernameValid(username));
    }
  }

  @Test
  public void testEmailValidationFailed() {
    String[] invalidEmails = { "test@@example.com", "test@example@example.com", "test@example.", "test@example+com" };

    for (String email : invalidEmails) {
      assertFalse(email + " should be an invalid email address", ExoUtils.isEmailValid(email));
    }
  }

  @Test
  public void testEmailValidationPassed() {
    String[] validEmails = { "test@example.com", "test@example.com.vn", "test.foo@example.com" };
    for (String email : validEmails) {
      assertTrue(email + " should be a valid email address", ExoUtils.isEmailValid(email));
    }
  }

  @Test
  public void testForbiddenUrls() {
    String[] forbiddenUrls = { "http://exoplatform.net", "http://wks-acc.exoplatform.org", "http://netstg.exoplatform.org" };
    for (String url : forbiddenUrls) {
      assertTrue(url + " should be forbidden", ExoUtils.isURLForbidden(url));
    }
  }

  @Test
  public void testAccountNameFromUrlExtraction() {
    String url1 = "http://mycompany.com";
    assertEquals("Mycompany", ExoUtils.getAccountNameFromURL(url1, ""));

    String url2 = "http://int.mycompany.com";
    assertEquals("Int", ExoUtils.getAccountNameFromURL(url2, ""));

    String url3 = "http://intranet.secure.mycompany.com.vn";
    assertEquals("Intranet", ExoUtils.getAccountNameFromURL(url3, ""));

    String url4 = "https://mycompany.com"; // HTTPS
    assertEquals("Mycompany", ExoUtils.getAccountNameFromURL(url4, ""));

    String url5 = "http://mycompany.com:8080"; // Port 8080
    assertEquals("Mycompany", ExoUtils.getAccountNameFromURL(url5, ""));

    String url6 = "http://localhost";
    assertEquals("Localhost", ExoUtils.getAccountNameFromURL(url6, ""));

    String url7 = "http://192.168.4.42";
    assertEquals("192.168.4.42", ExoUtils.getAccountNameFromURL(url7, ""));
  }

  @Test
  public void testIpAddressCheck() {
    String[] correctIPs = { "192.168.4.42", "1.1.1.1", "255.255.255.255" };
    String[] incorrectIPs = { "z.a.b.c", "0123.45.67.89", "192.168.4.42:8080" };
    for (String ip : correctIPs) {
      assertTrue("IP " + ip + " should be correct", ExoUtils.isCorrectIPAddress(ip));
    }
    for (String ip : incorrectIPs) {
      assertFalse("IP " + ip + " should be incorrect", ExoUtils.isCorrectIPAddress(ip));
    }
  }

  @Test
  public void testCapitalizeString() {
    String string = "word";
    assertEquals("Word", ExoUtils.capitalize(string));

    string = "two words";
    assertEquals("Two words", ExoUtils.capitalize(string));

    string = "w";
    assertEquals("W", ExoUtils.capitalize(string));

    string = "two Words";
    assertEquals("Two Words", ExoUtils.capitalize(string));

    string = "Two words";
    assertEquals("Two words", ExoUtils.capitalize(string));
  }

}
