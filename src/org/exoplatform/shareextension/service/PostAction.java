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
package org.exoplatform.shareextension.service;

import org.exoplatform.model.SocialPostInfo;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.service.QueryParams;
import org.exoplatform.social.client.api.service.QueryParams.QueryParamOption;
import org.exoplatform.social.client.core.service.QueryParamsImpl;

import android.util.Log;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Philippe Aristote paristote@exoplatform.com
 * @since Jun 17, 2015
 */
public class PostAction extends Action {

  public static void execute(SocialPostInfo post, ActionListener listener) {

    PostAction action = new PostAction();
    action.postInfo = post;
    action.listener = listener;
    action.execute();

  }

  @Override
  protected void doExecute() {

    boolean postResult = false;
    if (SocialPostInfo.TYPE_DOC.equals(postInfo.activityType))
      postResult = postDocActivity();
    else if (SocialPostInfo.TYPE_LINK.equals(postInfo.activityType))
      postResult = postLinkActivity();
    else {
      postResult = postTextActivity();
    }

    if (postResult) {
      listener.onSuccess("Message posted successfully");
    } else {
      listener.onError("Could not post the message");
    }
  }

  private boolean postDocActivity() {
    // Post the DOC activity
    RestActivity activity = new RestActivity();
    activity.setTitle(postInfo.postMessage);
    // These types are actually the same (DOC_ACTIVITY).
    // If they change later on PLF, we'll have to update in SCL only
    if (postInfo.isPublic())
      activity.setType(RestActivity.DOC_ACTIVITY_TYPE);
    else
      activity.setType(RestActivity.SPACE_DOC_ACTIVITY_TYPE);
    postInfo.addTemplateParam("MESSAGE", postInfo.postMessage);
    activity.setTemplateParams(postInfo.templateParams);

    return postActivity(activity);
  }

  private boolean postLinkActivity() {
    // Post the LINK activity
    RestActivity activity = new RestActivity();
    activity.setTitle(postInfo.postMessage);
    activity.setTemplateParams(postInfo.templateParams);
    // These types are actually the same (LINK_ACTIVITY).
    // If they change later on PLF, we'll have to update in SCL only
    if (postInfo.isPublic())
      activity.setType(RestActivity.LINK_ACTIVITY_TYPE);
    else
      activity.setType(RestActivity.SPACE_LINK_ACTIVITY_TYPE);

    return postActivity(activity);
  }

  private boolean postTextActivity() {
    // Post the DEFAULT activity
    RestActivity activity = new RestActivity();
    activity.setTitle(postInfo.postMessage);
    if (postInfo.isPublic())
      activity.setType(RestActivity.DEFAULT_ACTIVITY_TYPE);
    else
      activity.setType(RestActivity.SPACE_DEFAULT_ACTIVITY_TYPE);

    return postActivity(activity);
  }

  private boolean postActivity(RestActivity activity) {
    // Perform the actual Post using the Social Activity service
    try {
      if (postInfo.isPublic()) {
        return (SocialServiceHelper.getInstance().activityService.create(activity) != null);
      } else {
        String spaceId = retrieveSpaceId(postInfo.destinationSpace.name);
        if (spaceId != null) {
          QueryParamOption paramSpaceId = QueryParams.IDENTITY_ID_PARAM;
          paramSpaceId.setValue(spaceId);
          QueryParams params = new QueryParamsImpl();
          params.append(paramSpaceId);
          activity.setIdentityId(spaceId);
          return (SocialServiceHelper.getInstance().activityService.create(activity, params) != null);
        } else {
          Log.e(LOG_TAG, "Post message failed: could not get space ID for space " + postInfo.destinationSpace);
        }
      }
    } catch (Exception e) {
      Log.e(LOG_TAG, "Post message failed", e);
    }
    return false;
  }

  private String retrieveSpaceId(String spaceName) {
    // Retrieve the space identity from the Social Identity service
    try {
      RestIdentity spaceIdentity = SocialServiceHelper.getInstance().identityService.getIdentity("space", spaceName);
      return spaceIdentity.getId();
    } catch (SocialClientLibException e) {
      Log.e(LOG_TAG, "Could not retrieve the space ID of " + spaceName, e);
    }
    return null;
  }

}
