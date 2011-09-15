package org.exoplatform.singleton;

import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.api.service.IdentityService;
/*
 * The singleton for management the activity and identity service, which are used while
 *  retrieve and commit data in Social Activity Stream.  
 */
public class SocialServiceHelper {

  private ActivityService<RestActivity> activityService; 

  private IdentityService<?>            identityService;

  private String                        userIdentity;  

  private static SocialServiceHelper    serviceHelper = new SocialServiceHelper();

  private SocialServiceHelper() {

  }

  public static SocialServiceHelper getInstance() {
    return serviceHelper;
  }

  public void setActivityService(ActivityService<RestActivity> actService) {
    activityService = actService;
  }

  public ActivityService<RestActivity> getActivityService() {
    return activityService;
  }

  public void setIdentityService(IdentityService<?> idService) {
    identityService = idService;
  }

  public IdentityService<?> getIdentityService() {
    return identityService;
  }

  public void setUserId(String id) {
    userIdentity = id;
  }

  public String getUserId() {
    return userIdentity;
  }
}
