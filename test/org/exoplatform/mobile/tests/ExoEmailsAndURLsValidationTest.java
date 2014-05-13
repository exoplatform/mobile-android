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

import org.exoplatform.utils.ExoConnectionUtils;
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
public class ExoEmailsAndURLsValidationTest {
  
  final String[]    CLOUD_URLS_FORBIDDEN = {
      "http://exoplatform.net", "http://wks-acc.exoplatform.org", "http://netstg.exoplatform.org" };
  
  final String[] EMAILS_OK = {
      "test@example.com",
      "test.test@example.com",
      "test-test@example.com",
      "test_test@example.com",
      "test+test@example.com",
      "test@test.example.com",
      "test@test-example.com",
      "test@test_example.com"
      // "test@example" disabled although it should be valid (TODO)
    };
  final String[] EMAILS_INCORRECT = {
      "example.com",
      "@example.com",
      "test",
      "test@"
      // "test@.com" disabled although it should be incorrect (TODO)
    };
  
  final String[] URLS_OK = {
      "test.com",
      "test.example.com",
      "test-example.com",
      "test.fr",
      "test.info",
      "http://test.com",
      "https://test.com",
      "t.e.s.t.com",
      "10.100.10.1",
      "test.com:80",
      "test123.com",
      "www.test.com/some/path"
      // "test"
      // "test_example.com"
    };
  final String[] URLS_INCORRECT = {
      "test.",
      ".com",
      "test{}.com",
      "test().com",
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
    };

  @Before
  public void setup() {
  }
  
  @After
  public void teardown() {
  }
  
  @Test
  public void testValidateEmail_ExoConnectionUtils() {
    
    for (String string : EMAILS_OK) {
      assertTrue(string+" should be valid", ExoConnectionUtils.validateEmail(string));
    }
    
    for (String string : EMAILS_INCORRECT) {
      assertFalse(string+" should be incorrect", ExoConnectionUtils.validateEmail(string));
    }
    
  }
  
  @Test
  public void testValidateURL_ExoConnectionUtils() {
    
    for (String string : URLS_OK) {
      assertTrue(string+" should be valid", ExoConnectionUtils.validateUrl(string));
    }
    
    for (String string : URLS_INCORRECT) {
      assertFalse(string+" should be incorrect", ExoConnectionUtils.validateUrl(string));
    }
    
  }
  
  @Test
  public void testForbiddenTenantNames_ExoConnectionUtils() {
    
    for (String string : CLOUD_URLS_FORBIDDEN) {
      assertTrue(string+" should be forbidden", ExoConnectionUtils.urlHasWrongTenant(string));
    }
    
  }
  
//  TODO
//  @Test
//  public void testValidateURL_URLAnalyzer() {
//    
//    URLAnalyzer a = new URLAnalyzer();
//    
//    for (String string : URLS_OK) {
//      String newUrl = a.parserURL(string);
//      assertTrue(newUrl+" should be valid", URLAnalyzer.isValidUrl(newUrl));
//    }
//    
//    for (String string : URLS_INCORRECT) {
//      String newUrl = a.parserURL(string);
//      assertFalse(newUrl+" should be incorrect", URLAnalyzer.isValidUrl(newUrl));
//    }
//    
//  }
  
  
}
