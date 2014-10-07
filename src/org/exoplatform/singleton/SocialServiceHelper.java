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
package org.exoplatform.singleton;

import java.util.ArrayList;

import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.IdentityService;
import org.exoplatform.ui.social.SocialTabsActivity;

/*
 * The singleton for management the activity and identity service, which are used while
 *  retrieve and commit data in Social Activity Stream.  
 */
public class SocialServiceHelper {

    public ActivityService<RestActivity> activityService;

    public IdentityService<RestIdentity> identityService;

    public String                        userIdentity;

    /*
     * Social List
     */
    // public ArrayList<SocialActivityInfo> allUpdatesList;

    public ArrayList<SocialActivityInfo> myConnectionsList;

    public ArrayList<SocialActivityInfo> mySpacesList;

    public ArrayList<SocialActivityInfo> myStatusList;

    public ArrayList<SocialActivityInfo> socialInfoList;

    public String[]                      userProfile;

    private static SocialServiceHelper   serviceHelper = new SocialServiceHelper();

    private SocialServiceHelper() {

    }

    public static SocialServiceHelper getInstance() {
        return serviceHelper;
    }

    public ArrayList<SocialActivityInfo> getSocialListForTab(int tabId) {
        switch (tabId) {
        case SocialTabsActivity.ALL_UPDATES:
            return socialInfoList;

        case SocialTabsActivity.MY_CONNECTIONS:
            return myConnectionsList;

        case SocialTabsActivity.MY_SPACES:
            return mySpacesList;

        case SocialTabsActivity.MY_STATUS:
            return myStatusList;

        default:
            return null;
        }
    }

    public void clearData() {
        userIdentity = null;
        activityService = null;
        identityService = null;
        socialInfoList = null;
        userProfile = null;
        myConnectionsList = null;
        mySpacesList = null;
        myStatusList = null;
    }

}
