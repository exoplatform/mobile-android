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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Robolectric.shadowOf;

import java.util.Locale;

import org.exoplatform.model.ExoAccount;
import org.exoplatform.ui.LaunchActivity;
import org.exoplatform.ui.WelcomeActivity;
import org.exoplatform.ui.login.LoginActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import android.content.Context;
import android.content.Intent;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Apr 8, 2014  
 */
@RunWith(ExoRobolectricTestRunner.class)
public class LaunchActivityTest extends ExoActivityTestUtils<LaunchActivity> {
  
  Context ctx;

  @Override
  @Before
  public void setup() {
    controller = Robolectric.buildActivity(LaunchActivity.class);
    ctx = Robolectric.application.getApplicationContext();
  }

  @Test
  /**
   * Tests that on a clean launch, the app is redirected to the Welcome Activity (sign-up assistant)
   * @throws Exception
   */
  public void shouldRedirectToWelcomeScreen() throws Exception 
  {
     create();
     
      ShadowActivity sActivity = shadowOf(activity);
      Intent welcomeIntent = sActivity.getNextStartedActivity();
      ShadowIntent sIntent = shadowOf(welcomeIntent);
      
      assertThat(sIntent.getComponent().getClassName(), equalTo(WelcomeActivity.class.getName()));
      
  }
  
  @Test
  /*
   * Test rule  AUTO_DETECT_LANGUAGE_01 with French
   */
  public void shouldDetectAndConfigureAppInFrench() {
    
    final String expectedLang = "fr";
    Locale.setDefault(new Locale(expectedLang)); // set device language to French
    setLanguageInPreferences(ctx, ""); // empty the language preference in the app
        
    create();
    
    String lang = ctx.getSharedPreferences("exo_preference", 0).getString("exo_prf_localize", "");
    // French should be saved
    assertTrue("Saved language should be French", lang.equals(expectedLang));
  }
  
  @Test
  /*
   * Test rule  AUTO_DETECT_LANGUAGE_01 with Spanish
   */
  public void shouldDetectAndConfigureAppInSpanish() {
    
    final String expectedLang = "es";
    Locale.setDefault(new Locale(expectedLang)); // set device language to Spanish
    setLanguageInPreferences(ctx, ""); // empty the language preference in the app
    
    create();
    
    String lang = ctx.getSharedPreferences("exo_preference", 0).getString("exo_prf_localize", "");
    // Spanish should be saved
    assertTrue("Saved language should be Spanish", lang.equals(expectedLang));
  }
  
  @Test
  /*
   * Test rule  AUTO_DETECT_LANGUAGE_01 with German
   */
  public void shouldDetectAndConfigureAppInGerman() {
    
    final String expectedLang = "de";
    Locale.setDefault(new Locale(expectedLang)); // set device language to German
    setLanguageInPreferences(ctx, ""); // empty the language preference in the app
    
    create();
    
    String lang = ctx.getSharedPreferences("exo_preference", 0).getString("exo_prf_localize", "");
    // German should be saved
    assertTrue("Saved language should be German", lang.equals(expectedLang));
  }
  
  @Test
  /*
   * Test rule  AUTO_DETECT_LANGUAGE_01 with English
   */
  public void shouldDetectAndConfigureAppInEnglish() {
    
    final String expectedLang = "en";
    Locale.setDefault(new Locale(expectedLang)); // set device language to English
    setLanguageInPreferences(ctx, ""); // empty the language preference in the app
    
    create();
   
    String lang = ctx.getSharedPreferences("exo_preference", 0).getString("exo_prf_localize", "");
    // English should be saved
    assertTrue("Saved language should be English", lang.equals(expectedLang));
  }
  
  @Test
  /*
   *  Test rule AUTO_DETECT_LANGUAGE_03 
   */
  public void shouldDetectNotSupportedLanguageAndConfigureAppInEnglish() {
    
    final String deviceLang = "ja";
    Locale.setDefault(new Locale(deviceLang)); // set device language to Japanese
    setLanguageInPreferences(ctx, ""); // empty the language preference in the app
    
    create();
   
    String lang = ctx.getSharedPreferences("exo_preference", 0).getString("exo_prf_localize", "");
    // English should be configured since Japanese is not supported
    assertTrue("Saved language should be English", lang.equals("en"));
  }
  
  @Test
  /*
   * Test rule  AUTO_DETECT_LANGUAGE_02 
   */
  public void shouldKeepSavedLanguage() {
    
    final String expectedLang = "en";
    Locale.setDefault(new Locale("fr")); // set device language to French
    setLanguageInPreferences(ctx, expectedLang); // set language preference to English
    
    create();
   
    String lang = ctx.getSharedPreferences("exo_preference", 0).getString("exo_prf_localize", "");
    // English should still be configured and not replaced by French
    assertTrue("Saved language should be English", lang.equals(expectedLang));
  }
  
  @Test
  /*
   * Test rule  AUTO_DETECT_LANGUAGE_04 
   */
  public void shouldSetLanguagePreferenceInAnyCase() {
    String[] langs = {"en", "fr", "de", "es", "ja", "fr_FR", "fr_BE", ""};
    setLanguageInPreferences(ctx, ""); // empty the language preference in the app
    
    for (String lang : langs) {
      Locale.setDefault(new Locale(lang));
      create();
      String prefLang = ctx.getSharedPreferences("exo_preference", 0).getString("exo_prf_localize", "");
      // Saved language should not be empty
      assertFalse("Saved language should not be empty", prefLang.equals(""));
      
      setLanguageInPreferences(ctx, ""); // empty again before the next iteration
    }
  }
  
  @Test
  public void shouldRedirectToLoginScreen() throws Exception 
  {
	  
	  ExoAccount srv = getServerWithDefaultValues();
      srv.isAutoLoginEnabled = false;
      srv.isRememberEnabled = false;
	  
	  setDefaultServerInPreferences(ctx, srv);
	  
	  create();
	  
      ShadowActivity sActivity = shadowOf(activity);
      Intent loginIntent = sActivity.getNextStartedActivity();
      ShadowIntent sIntent = shadowOf(loginIntent);
      
      assertThat(sIntent.getComponent().getClassName(), equalTo(LoginActivity.class.getName()));
      
  }
  
}
