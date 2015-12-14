/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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

import static org.junit.Assert.assertEquals;

import org.exoplatform.BuildConfig;
import org.exoplatform.model.ExoFile;
import org.robolectric.annotation.Config;

import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com Apr 17, 2015
 */
// API LEVEL 21
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
@RunWith(RobolectricTestRunner.class)
public class ExoDocumentUtilsTest {

  @Before
  public void setup() {
  }

  @After
  public void teardown() {
  }

  @Test
  public void testConvertTechnicalToNaturalNames() {

    final String[] technicalNames = { ".organization.employees", ".spaces.exo_fr", ".spaces.exo_mobile",
        ".platform.web-contributors", ".platform.users.managers", ".spaces.thu_muc_tieng_viet_100", ".spaces.aađoiec",
        "Âã-đô-ịẻç", ".élus.députés", ".spaces.my-spaces", ".spaces.spaces-for-admins", "日本語", "عربي عربى", "指事字", "λάμβδα",
        "אותיות השימוש" };

    final String[] naturalNames = { "Organization Employees", "eXo Fr", "eXo Mobile", "Platform Web Contributors",
        "Platform Users Managers", "Thu Muc Tieng Viet 100", "Aađoiec", "Âã Đô Ịẻç", "Élus Députés", "My Spaces",
        "Spaces For Admins", "日本語", "عربي عربى", "指事字", "Λάμβδα", "אותיות השימוש" };

    for (int i = 0; i < technicalNames.length; i++) {
      String name = technicalNames[i];
      ExoFile file = new ExoFile();
      file.name = name;
      file.createNaturalName();
      assertEquals("Incorrect technical -> natural name conversion.", naturalNames[i], file.getName());
    }

  }

  public void testGetDrivesFromHttpResponse() {
    // TODO
    // Test ExoDocumentUtils.getDrives(HttpResponse, boolean)
  }
}
