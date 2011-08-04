package org.exoplatform.mocks;

import java.util.ArrayList;



public class ActivitiesMocks {

  public ArrayList<MockSocialActivity> arrayOfActivities;

  public ActivitiesMocks() {

    MockSocialActivity act_01 = new MockSocialActivity("32D52", "This is a short message", "", 3600, 1, 1);

    MockSocialActivity act_02 = new MockSocialActivity("32D52",
                                               "This is a normal message, with some content. And a second sentence.",
                                               "",
                                               3600,
                                               1,
                                               1);

    MockSocialActivity act_03 = new MockSocialActivity("32D52",
                                               "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor incididunt "
                                                   + "ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation "
                                                   + "ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                                               "",
                                               3600,
                                               1,
                                               1);

    MockSocialActivity act_04 = new MockSocialActivity("32D52",
                                               "Lorem ipsum dolor sit amet, consectetur adipisicing elit, sed do eiusmod tempor "
                                                   + "incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud "
                                                   + "exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure "
                                                   + "dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. "
                                                   + "Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit "
                                                   + "anim id est laborum.",
                                               "",
                                               3600,
                                               1,
                                               1);

    MockSocialActivity act_05 = new MockSocialActivity("33D52", "This is a short message", "", 3600, 1, 1);

    MockSocialActivity act_06 = new MockSocialActivity("1D52", "This is a short message", "", 3600, 1, 1);

    MockSocialActivity act_07 = new MockSocialActivity("33D52", "This is a short message", "", 3600, 0, 0);

    MockSocialActivity act_08 = new MockSocialActivity("33D52", "This is a short message", "", 3600, 2, 0);

    MockSocialActivity act_09 = new MockSocialActivity("33D52", "This is a short message", "", 3600, 20, 0);

    MockSocialActivity act_10 = new MockSocialActivity("33D52", "This is a short message", "", 3600, 200, 0);

    MockSocialActivity act_11 = new MockSocialActivity("33D52",
                                               "This is a short message",
                                               "",
                                               3600,
                                               2000,
                                               0);

    MockSocialActivity act_12 = new MockSocialActivity("33D52", "This is a short message", "", 3600, 0, 0);

    MockSocialActivity act_13 = new MockSocialActivity("33D52", "This is a short message", "", 3600, 0, 2);

    MockSocialActivity act_14 = new MockSocialActivity("33D52", "This is a short message", "", 3600, 0, 20);

    MockSocialActivity act_15 = new MockSocialActivity("33D52", "This is a short message", "", 3600, 0, 200);

    MockSocialActivity act_16 = new MockSocialActivity("33D52",
                                               "This is a short message",
                                               "",
                                               3600,
                                               0,
                                               2000);

    MockSocialActivity act_17 = new MockSocialActivity("33D52", "This is a short message", "", 0, 1, 1);

    MockSocialActivity act_18 = new MockSocialActivity("33D52", "This is a short message", "", 30, 1, 1);

    MockSocialActivity act_19 = new MockSocialActivity("33D52", "This is a short message", "", 60, 1, 1);

    MockSocialActivity act_20 = new MockSocialActivity("33D52", "This is a short message", "", 600, 1, 1);

    MockSocialActivity act_21 = new MockSocialActivity("33D52", "This is a short message", "", 3600, 1, 1);

    MockSocialActivity act_22 = new MockSocialActivity("33D52", "This is a short message", "", 7200, 1, 1);

    MockSocialActivity act_23 = new MockSocialActivity("33D52", "This is a short message", "", 86400, 1, 1);

    MockSocialActivity act_24 = new MockSocialActivity("33D52", "This is a short message", "", 172800, 1, 1);

    MockSocialActivity act_25 = new MockSocialActivity("33D52", "This is a short message", "", 864000, 1, 1);

    MockSocialActivity act_26 = new MockSocialActivity("33D52",
                                               "This is a short message",
                                               "",
                                               2592000,
                                               1,
                                               1);

    MockSocialActivity act_27 = new MockSocialActivity("33D52",
                                               "This is a short message",
                                               "",
                                               5184000,
                                               1,
                                               1);

    arrayOfActivities = new ArrayList<MockSocialActivity>();

    arrayOfActivities.add(act_01);
    arrayOfActivities.add(act_02);
    arrayOfActivities.add(act_03);
    arrayOfActivities.add(act_04);
    arrayOfActivities.add(act_05);
    arrayOfActivities.add(act_06);
    arrayOfActivities.add(act_07);
    arrayOfActivities.add(act_08);
    arrayOfActivities.add(act_09);
    arrayOfActivities.add(act_10);
    arrayOfActivities.add(act_11);
    arrayOfActivities.add(act_12);
    arrayOfActivities.add(act_13);
    arrayOfActivities.add(act_14);
    arrayOfActivities.add(act_15);
    arrayOfActivities.add(act_16);
    arrayOfActivities.add(act_17);
    arrayOfActivities.add(act_18);
    arrayOfActivities.add(act_19);
    arrayOfActivities.add(act_20);
    arrayOfActivities.add(act_21);
    arrayOfActivities.add(act_22);
    arrayOfActivities.add(act_23);
    arrayOfActivities.add(act_24);
    arrayOfActivities.add(act_25);
    arrayOfActivities.add(act_26);
    arrayOfActivities.add(act_27);

  }

}
