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
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.exoplatform.R;
import org.exoplatform.model.SocialPostInfo;
import org.exoplatform.shareextension.service.Action.ActionListener;
import org.exoplatform.shareextension.service.PostAction;
import org.exoplatform.shareextension.service.PostAction.PostActionListener;
import org.exoplatform.shareextension.service.ShareService;
import org.exoplatform.shareextension.service.ShareService.UploadInfo;
import org.exoplatform.shareextension.service.UploadAction;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.ui.social.AllUpdatesFragment;
import org.exoplatform.ui.social.MyStatusFragment;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.Log;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.widget.PostWaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;

/**
 * A background task that publishes a status with optional attachment. Uses
 * {@link UploadAction} and {@link PostAction} to not have duplicate activities
 * (MOB-1384). <br/>
 * TODO An even better solution would be to use the {@link ShareService}
 * directly.
 */
public class PostStatusTask extends AsyncTask<Void, Void, Integer> {
  private PostWaitingDialog        _progressDialog;

  private Context                  mContext;

  private String                   sdcard_temp_dir;

  private String                   composeMessage;

  private String                   sendingData;

  private String                   okString;

  private String                   errorString;

  private String                   warningTitle;

  private String                   uploadUrl;

  private ComposeMessageController messageController;

  public PostStatusTask(Context context,
                        String dir,
                        String content,
                        ComposeMessageController controller,
                        PostWaitingDialog dialog) {
    mContext = context;
    messageController = controller;
    sdcard_temp_dir = dir;
    composeMessage = content;
    _progressDialog = dialog;
    changeLanguage();
  }

  @Override
  public void onPreExecute() {
    _progressDialog = new PostWaitingDialog(mContext, messageController, null, sendingData);
    _progressDialog.show();
  }

  @Override
  public Integer doInBackground(Void... params) {

    try {
      SocialPostInfo postInfo = new SocialPostInfo();
      postInfo.destinationSpace = messageController.getPostDestination();
      postInfo.postMessage = composeMessage;
      postInfo.ownerAccount = AccountSetting.getInstance().getCurrentAccount();

      // If the post contains an attached image
      if (sdcard_temp_dir != null) {
        postInfo.activityType = SocialPostInfo.TYPE_DOC;
        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.init(postInfo);
        uploadUrl = uploadInfo.jcrUrl + "/" + uploadInfo.folder;

        // Create destination folder
        if (ExoDocumentUtils.createFolder(uploadUrl)) {

          // Upload file
          File file = new File(sdcard_temp_dir);
          if (file != null) {
            File tempFile = PhotoUtils.reziseFileImage(file);
            String uploadedFileName = file.getName();
            if (tempFile != null) {
              uploadInfo.fileToUpload = ExoDocumentUtils.documentInfoFromUri(Uri.fromFile(tempFile),
                                                                             mContext.getApplicationContext());
              uploadInfo.uploadId = Long.toHexString(System.currentTimeMillis());
              if (uploadInfo.fileToUpload != null) {
                uploadInfo.fileToUpload.documentName = uploadedFileName;
                uploadInfo.fileToUpload.cleanupFilename(mContext);
                final AtomicBoolean uploaded = new AtomicBoolean(false);
                // UploadAction.excute is synchronize method
                UploadAction.execute(postInfo, uploadInfo, new ActionListener() {

                  @Override
                  public boolean onSuccess(String message) {
                    uploaded.set(true);
                    return true;
                  }

                  @Override
                  public boolean onError(String error) {
                    uploaded.set(false);
                    return false;
                  }
                });
                if (uploadInfo.fileToUpload.documentData != null) {
                  try {
                    uploadInfo.fileToUpload.documentData.close();
                  } catch (IOException e) {
                    Log.d(getClass().getSimpleName(), Log.getStackTraceString(e));
                  }
                }
                tempFile.delete();
                if (uploaded.get()) {
                  uploadedFileName = uploadInfo.fileToUpload.documentName;
                }
              }
            }
            // build post param
            postInfo.buildTemplateParams(uploadInfo);
          }
        }
      }
      final AtomicBoolean posted = new AtomicBoolean(false);
      // post action execute is synchronize
      PostAction.execute(postInfo, new PostActionListener() {

        @Override
        public boolean onSuccess(String message) {
          posted.set(true);
          return true;
        }

        @Override
        public boolean onError(String error) {
          posted.set(false);
          return false;
        }
      });
      if (posted.get())
        return 1;
      else
        return 0;
    } catch (RuntimeException e) {
      // Cannot replace because SocialClientLib can throw exceptions like ServerException, UnsupportMethod, etc
      if (Log.LOGD)
        Log.d(getClass().getSimpleName(), e.getMessage(), Log.getStackTraceString(e));
      return -2;
    }

  }

  @Override
  public void onPostExecute(Integer result) {
    if (result == 1) {
      ((Activity) mContext).finish();
      if (AllUpdatesFragment.instance != null)
        AllUpdatesFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, 0);
      if (MyStatusFragment.instance != null)
        MyStatusFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, 0);

    } else {
      new WarningDialog(mContext, warningTitle, errorString, okString).show();
    }
    _progressDialog.dismiss();

  }

  private void changeLanguage() {
    Resources resource = mContext.getResources();
    sendingData = resource.getString(R.string.SendingData);
    okString = resource.getString(R.string.OK);
    errorString = resource.getString(R.string.PostError);
    warningTitle = resource.getString(R.string.Warning);
  }

}
