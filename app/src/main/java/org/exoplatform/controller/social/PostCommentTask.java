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
package org.exoplatform.controller.social;

import android.os.AsyncTask;

import org.exoplatform.R;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestComment;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.ui.social.AllUpdatesFragment;
import org.exoplatform.ui.social.ComposeMessageActivity;
import org.exoplatform.ui.social.MyConnectionsFragment;
import org.exoplatform.ui.social.MySpacesFragment;
import org.exoplatform.ui.social.MyStatusFragment;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.ui.social.SocialTabsActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.Log;
import org.exoplatform.widget.PostWaitingDialog;
import org.exoplatform.widget.WarningDialog;

/**
 * Async Task to post a comment on an activity. The activity is retrieved via
 * SocialDetailHelper.getInstance().getActivityId(); Created by paristote on
 * 12/18/15.
 */
public class PostCommentTask extends AsyncTask<String, Void, Boolean> {

  private int                      currentPosition;

  private ComposeMessageActivity   mComposeActivity;

  private ComposeMessageController mComposeMessageController;

  private PostWaitingDialog        mProgressDialog;

  public PostCommentTask(ComposeMessageActivity activity,
                         ComposeMessageController controller,
                         PostWaitingDialog dialog,
                         int position) {
    mComposeActivity = activity;
    mComposeMessageController = controller;
    mProgressDialog = dialog;
    currentPosition = position;
  }

  @Override
  public void onPreExecute() {
    String loadingLabel = mComposeActivity.getString(R.string.SendingData);
    mProgressDialog = new PostWaitingDialog(mComposeActivity, mComposeMessageController, null, loadingLabel);
    mProgressDialog.show();
  }

  @Override
  protected Boolean doInBackground(String... params) {
    try {
      String composeMessage = params[0];
      RestComment comment = new RestComment();
      comment.setText(composeMessage);
      String selectedId = SocialDetailHelper.getInstance().getActivityId();
      ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance().activityService;
      RestActivity restActivity = activityService.get(selectedId);

      activityService.createComment(restActivity, comment);
      return true;
    } catch (SocialClientLibException e) {
      Log.d(getClass().getSimpleName(), e.getMessage(), Log.getStackTraceString(e));
      return false;
    }

  }

  @Override
  protected void onPostExecute(Boolean result) {
    if (result) {
      mComposeActivity.finish();

      if (SocialDetailActivity.socialDetailActivity != null) {
        SocialDetailActivity.socialDetailActivity.onLoad();
      }

      if (SocialTabsActivity.instance != null) {
        int tabId = SocialTabsActivity.instance.mPager.getCurrentItem();
        switch (tabId) {
        case SocialTabsActivity.ALL_UPDATES:
          AllUpdatesFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, currentPosition);
          break;
        case SocialTabsActivity.MY_CONNECTIONS:
          MyConnectionsFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, currentPosition);
          break;
        case SocialTabsActivity.MY_SPACES:
          MySpacesFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, currentPosition);
          break;
        case SocialTabsActivity.MY_STATUS:
          MyStatusFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, currentPosition);
          break;
        }
      }
    } else {
      String titleString = mComposeActivity.getString(R.string.Warning);
      String contentString = mComposeActivity.getString(R.string.ErrorOnComment);
      String okString = mComposeActivity.getString(R.string.OK);
      WarningDialog dialog = new WarningDialog(mComposeActivity, titleString, contentString, okString);
      dialog.show();
    }
    mProgressDialog.dismiss();
  }
}
