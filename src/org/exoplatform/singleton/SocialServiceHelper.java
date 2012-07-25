package org.exoplatform.singleton;

import java.util.ArrayList;

import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.model.RestProfile;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.IdentityService;

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

  public void clearData() {
    userIdentity = null;
    activityService = null;
    identityService = null;
    socialInfoList = null;
    userProfile = null;
  }

}
