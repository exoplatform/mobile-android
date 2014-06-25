package org.exoplatform.mobile.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Arnaud Héritier <aheritier@exoplatform.com>
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({WelcomeActivityTest.class, LoginActivityTest.class, SignInOnPremiseActivityTest.class,
                        LaunchActivityTest.class, ExoAccountInfoValidationTest.class, SettingsActivityTest.class,
                        HomeActivityTest.class})
public class TestSuite {
}
