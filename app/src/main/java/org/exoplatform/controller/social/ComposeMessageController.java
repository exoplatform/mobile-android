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

import org.exoplatform.R;
import org.exoplatform.model.SocialSpaceInfo;
import org.exoplatform.ui.social.ComposeMessageActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.PostWaitingDialog;

import android.content.res.Resources;
import android.view.Gravity;
import android.widget.Toast;

public class ComposeMessageController {

  private PostWaitingDialog      mProgressDialog;

  private int                    composeType;

  private ComposeMessageActivity mComposeActivity;

  private PostStatusTask         mPostTask;

  private PostCommentTask        mCommentTask;

  private String                 sdcard_temp_dir = null;

  private String                 inputTextWarning;

  private String                 okString;

  private String                 titleString;

  private String                 contentString;

  private String                 sendingData;

  /**
   * Either null (public) or the space name
   */
  private SocialSpaceInfo        postDestination;

  public ComposeMessageController(ComposeMessageActivity activity, int type, PostWaitingDialog dialog) {
    mComposeActivity = activity;
    composeType = type;
    postDestination = null;
    mProgressDialog = dialog;
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
    sdcard_temp_dir = PhotoUtils.startImageCapture(mComposeActivity);
  }

  public void onSendMessage(String composeMessage, String sdcard, int position) {
    if ((composeMessage != null) && (composeMessage.length() > 0)) {
      if (ExoConnectionUtils.isNetworkAvailableExt(mComposeActivity)) {
        if (composeType == ExoConstants.COMPOSE_POST_TYPE) {
          onPostTask(composeMessage, sdcard);
        } else {
          onCommentTask(composeMessage, position);
        }
      } else {
        new ConnectionErrorDialog(mComposeActivity).show();
      }
    } else {
      Toast toast = Toast.makeText(mComposeActivity, inputTextWarning, Toast.LENGTH_LONG);
      toast.setGravity(Gravity.BOTTOM, 0, 0);
      toast.show();
    }
  }

  private void onPostTask(String composeMessage, String sdcard) {
    if (mPostTask == null || mPostTask.getStatus() == PostStatusTask.Status.FINISHED) {
      mPostTask = new PostStatusTask(mComposeActivity, sdcard, composeMessage, this, mProgressDialog);
      mPostTask.execute();
    }
  }

  public void onCancelPostTask() {
    if (mPostTask != null && mPostTask.getStatus() == PostStatusTask.Status.RUNNING) {
      mPostTask.cancel(true);
      mPostTask = null;
    }
  }

  private void onCommentTask(String composeMessage, int position) {
    if (mCommentTask == null || mCommentTask.getStatus() == PostCommentTask.Status.FINISHED) {
      mCommentTask = new PostCommentTask(mComposeActivity, this, mProgressDialog, position);
      mCommentTask.execute(composeMessage);
    }
  }

  public String getSdCardTempDir() {
    return sdcard_temp_dir;
  }

  private void changeLanguage() {
    Resources resource = mComposeActivity.getResources();
    inputTextWarning = resource.getString(R.string.InputTextWarning);
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    contentString = resource.getString(R.string.ErrorOnComment);
    sendingData = resource.getString(R.string.SendingData);
  }
}
