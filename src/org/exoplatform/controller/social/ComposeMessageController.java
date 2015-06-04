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
package org.exoplatform.controller.social;

import java.io.File;

import org.exoplatform.R;
import org.exoplatform.model.SocialSpaceInfo;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestComment;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.ui.social.AllUpdatesFragment;
import org.exoplatform.ui.social.MyConnectionsFragment;
import org.exoplatform.ui.social.MySpacesFragment;
import org.exoplatform.ui.social.MyStatusFragment;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.ui.social.SocialTabsActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.PostWaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Toast;

public class ComposeMessageController {

  private PostWaitingDialog _progressDialog;

  private int               composeType;

  private Context           mContext;

  private PostStatusTask    mPostTask;

  private CommentTask       mCommetnTask;

  private String            sdcard_temp_dir = null;

  private String            inputTextWarning;

  private String            okString;

  private String            titleString;

  private String            contentString;

  /**
   * Either null (public) or the space name
   */
  private SocialSpaceInfo   postDestination;

  public ComposeMessageController(Context context, int type, PostWaitingDialog dialog) {
    mContext = context;
    composeType = type;
    postDestination = null;
    _progressDialog = dialog;
    changeLanguage();

  }

  /**
   * Set this post's destination.
   * 
   * @param destination null (public) or the SocialSpaceInfo object
   */
  public void setPostDestination(SocialSpaceInfo destination) {
    this.postDestination = destination;
  }

  /**
   * @return This post's destination:<br/>
   *         - null: public<br/>
   *         - SocialSpaceInfo: the destination space
   */
  public SocialSpaceInfo getPostDestination() {
    return this.postDestination;
  }

  /*
   * Take a photo and store it into /sdcard/eXo/DocumentCache
   */

  public void initCamera() {
    String parentPath = PhotoUtils.getParentImagePath(mContext);
    sdcard_temp_dir = parentPath + "/" + PhotoUtils.getImageFileName();

    Intent takePictureFromCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    takePictureFromCameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(sdcard_temp_dir)));
    ((Activity) mContext).startActivityForResult(takePictureFromCameraIntent, ExoConstants.TAKE_PICTURE_WITH_CAMERA);
  }

  public void onSendMessage(String composeMessage, String sdcard, int position) {
    if ((composeMessage != null) && (composeMessage.length() > 0)) {
      if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
        if (composeType == 0) {
          onPostTask(composeMessage, sdcard);
        } else {
          onCommentTask(composeMessage, position);
        }
      } else {
        new ConnectionErrorDialog(mContext).show();
      }
    } else {
      Toast toast = Toast.makeText(mContext, inputTextWarning, Toast.LENGTH_LONG);
      toast.setGravity(Gravity.BOTTOM, 0, 0);
      toast.show();
    }
  }

  private void onPostTask(String composeMessage, String sdcard) {
    if (mPostTask == null || mPostTask.getStatus() == PostStatusTask.Status.FINISHED) {
      mPostTask = (PostStatusTask) new PostStatusTask(mContext, sdcard, composeMessage, this, _progressDialog).execute();
    }
  }

  public void onCancelPostTask() {
    if (mPostTask != null && mPostTask.getStatus() == PostStatusTask.Status.RUNNING) {
      mPostTask.cancel(true);
      mPostTask = null;
    }
  }

  private void onCommentTask(String composeMessage, int position) {
    if (mCommetnTask == null || mCommetnTask.getStatus() == CommentTask.Status.FINISHED) {
      mCommetnTask = (CommentTask) new CommentTask(position).execute(composeMessage);
    }
  }

  public String getSdCardTempDir() {
    return sdcard_temp_dir;
  }

  private void changeLanguage() {
    Resources resource = mContext.getResources();
    inputTextWarning = resource.getString(R.string.InputTextWarning);
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    contentString = resource.getString(R.string.ErrorOnComment);
  }

  private class CommentTask extends AsyncTask<String, Void, Boolean> {

    private int currentPosition;

    public CommentTask(int position) {
      currentPosition = position;
    }

    @Override
    protected Boolean doInBackground(String... params) {
      try {
        String composeMessage = params[0];
        RestComment comment = new RestComment();
        comment.setText(composeMessage);
        String selectedId = SocialDetailHelper.getInstance().getActivityId();
        ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance().activityService;
        RestActivity restActivity = (RestActivity) activityService.get(selectedId);

        activityService.createComment(restActivity, comment);
        return true;
      } catch (SocialClientLibException e) {
        return false;
      }

    }

    @Override
    protected void onPostExecute(Boolean result) {
      if (result) {
        ((Activity) mContext).finish();
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
        WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
        dialog.show();
      }
    }

  }

}
