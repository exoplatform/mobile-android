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
import java.util.HashMap;
import java.util.Map;

import org.exoplatform.R;
import org.exoplatform.shareextension.service.ShareService.UploadInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.service.QueryParams;
import org.exoplatform.social.client.api.service.QueryParams.QueryParamOption;
import org.exoplatform.social.client.core.service.QueryParamsImpl;
import org.exoplatform.ui.social.AllUpdatesFragment;
import org.exoplatform.ui.social.MyStatusFragment;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.widget.PostWaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.webkit.MimeTypeMap;

// TODO replace by ShareService
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

  public PostStatusTask(Context context, String dir, String content, ComposeMessageController controller, PostWaitingDialog dialog) {
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
      boolean isInSpace = messageController.getPostDestination() != null;
      String thisServerUrl = AccountSetting.getInstance().getDomainName();
      RestActivity activityImpl = new RestActivity();
      activityImpl.setTitle(composeMessage);
      // If the message is posted in a space
      if (isInSpace)
        activityImpl.setType(RestActivity.SPACE_DEFAULT_ACTIVITY_TYPE);
      else
        activityImpl.setType(RestActivity.DEFAULT_ACTIVITY_TYPE);

      // If the post contains an attached image
      if (sdcard_temp_dir != null) {

        UploadInfo uploadInfo = new UploadInfo();
        uploadInfo.repository = DocumentHelper.getInstance().repository;
        uploadInfo.workspace = DocumentHelper.getInstance().workspace;
        if (isInSpace) {
          String spaceOriginalName = messageController.getPostDestination().getOriginalName();
          uploadInfo.folder = "Mobile";
          StringBuffer url = new StringBuffer(thisServerUrl).append(ExoConstants.DOCUMENT_JCR_PATH)
                                                            .append("/")
                                                            .append(uploadInfo.repository)
                                                            .append("/")
                                                            .append(uploadInfo.workspace)
                                                            .append("/Groups/spaces/")
                                                            .append(spaceOriginalName)
                                                            .append("/Documents");
          uploadInfo.jcrUrl = url.toString();
        } else {
          uploadInfo.folder = "Public/Mobile";
          uploadInfo.jcrUrl = DocumentHelper.getInstance().getRepositoryHomeUrl();
        }
        uploadUrl = uploadInfo.jcrUrl + "/" + uploadInfo.folder;

        // Create destination folder
        if (ExoDocumentUtils.createFolder(uploadUrl)) {
          // Upload file
          File file = new File(sdcard_temp_dir);
          String imageDir = uploadUrl + "/" + file.getName();
          if (file != null) {
            File tempFile = PhotoUtils.reziseFileImage(file);
            if (tempFile != null) {
              String mime = ExoConstants.IMAGE_TYPE;
              String ext = MimeTypeMap.getFileExtensionFromUrl(tempFile.getName());
              if (ext != null && !"".equals(ext))
                mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
              ExoDocumentUtils.putFileToServerFromLocal(imageDir, tempFile, mime);
            }
            // Activity Type
            if (isInSpace)
              activityImpl.setType(RestActivity.SPACE_DOC_ACTIVITY_TYPE);
            else
              activityImpl.setType(RestActivity.DOC_ACTIVITY_TYPE);
            // Template Params
            Map<String, String> templateParams = new HashMap<String, String>();
            String docLink = imageDir.substring(thisServerUrl.length());
            StringBuffer beginPath = new StringBuffer(ExoConstants.DOCUMENT_JCR_PATH).append("/")
                                                                                     .append(uploadInfo.repository)
                                                                                     .append("/")
                                                                                     .append(uploadInfo.workspace);
            String docPath = docLink.substring(beginPath.length());
            templateParams.put("DOCPATH", docPath);
            templateParams.put("MESSAGE", composeMessage);
            templateParams.put("DOCLINK", docLink);
            templateParams.put("WORKSPACE", uploadInfo.workspace);
            templateParams.put("REPOSITORY", uploadInfo.repository);
            templateParams.put("DOCNAME", file.getName());
            activityImpl.setTemplateParams(templateParams);
          }
        }
      }

      // Publish
      if (isInSpace) {
        String spaceName = messageController.getPostDestination().name;
        RestIdentity spaceIdentity = SocialServiceHelper.getInstance().identityService.getIdentity("space", spaceName);
        if (spaceIdentity == null)
          return 0; // Failed to get the space identity
        activityImpl.setIdentityId(spaceIdentity.getId());
        QueryParamOption paramSpaceId = QueryParams.IDENTITY_ID_PARAM;
        paramSpaceId.setValue(spaceIdentity.getId());
        QueryParams qparams = new QueryParamsImpl();
        qparams.append(paramSpaceId);
        SocialServiceHelper.getInstance().activityService.create(activityImpl, qparams);
      } else {
        SocialServiceHelper.getInstance().activityService.create(activityImpl);
      }
      return 1;
    } catch (SocialClientLibException e) {
      return 0;
    } catch (RuntimeException e) {
      // XXX cannot replace because SocialClientLib can throw exceptions like ServerException, UnsupportMethod ,..
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
