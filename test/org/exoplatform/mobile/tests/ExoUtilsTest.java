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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.exoplatform.utils.ExoUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com Oct 28, 2014
 */
@RunWith(ExoRobolectricTestRunner.class)
public class ExoUtilsTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    // @Test
    public void testUrlValidationFailed() {

    }

    // @Test
    public void testUrlValidationPassed() {

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

    // @Test
    public void testStripUrl() {

    }

    // @Test
    public void testAccountNameValidationFailed() {

    }

    // @Test
    public void testAccountNameValidationPassed() {

    }

    @Test
    public void testAccountUsernameValidationFailed() {
        // list all forbidden characters here, separated by a space
        String incorrectChars = "~ ` ! @ # $ % ^ & * ( ) = { } [ ] | \\ : ; \" ' , < > ? /";
        String[] chars = incorrectChars.split(" ");
        for (String c : chars) {
            String wrongUsername = "john" + c + "doe";
            assertFalse("Username '" + wrongUsername + "' should not have been validated",
                        ExoUtils.isUsernameValid(wrongUsername));
        }
        // test username that contains a space
        assertFalse("Username 'john doe' should not have been validated",
                    ExoUtils.isUsernameValid("john doe"));
    }

    @Test
    public void testAccountUsernameValidationPassed() {
        String[] testUsernames = { "johndoe", "john.doe", "john-doe", "john_doe", "john+doe",
                "JohnDoe", "johndoe1234" };
        for (String username : testUsernames) {
            assertTrue("Username '" + username + "' should have been validated",
                       ExoUtils.isUsernameValid(username));
        }
    }

    // @Test
    public void testEmailValidationFailed() {

    }

    // @Test
    public void testEmailValidationPassed() {

    }

    // @Test
    public void testForbiddenUrls() {

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
