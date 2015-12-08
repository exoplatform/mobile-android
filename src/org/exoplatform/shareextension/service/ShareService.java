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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.exoplatform.R;
import org.exoplatform.model.SocialPostInfo;
import org.exoplatform.shareextension.service.Action.ActionListener;
import org.exoplatform.shareextension.service.PostAction.PostActionListener;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestComment;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.ExoDocumentUtils.DocumentInfo;
import org.exoplatform.utils.Log;
import org.exoplatform.utils.TitleExtractor;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

/**
 * Created by The eXo Platform SAS.<br/>
 * IntentService that publishes a post with optional attachments. If multiple
 * files are attached, the first one is part of the activity, the other are
 * added in comments on the activity.
 * 
 * @author Philippe Aristote paristote@exoplatform.com
 * @since Jun 4, 2015
 */
public class ShareService extends IntentService {

  public static final String LOG_TAG     = "____eXo____ShareService____";

  public static final String POST_INFO   = "postInfo";

  private int                notifId     = 1;

  private SocialPostInfo     postInfo;

  // key is uri in device, value is url on server
  private List<UploadInfo>   uploadedMap = new ArrayList<UploadInfo>();

  private enum ShareResult {
    SUCCESS, ERROR_INCORRECT_CONTENT_URI, ERROR_INCORRECT_ACCOUNT, ERROR_CREATE_FOLDER, ERROR_UPLOAD_FAILED, ERROR_POST_FAILED, ERROR_COMMENT_FAILED
  }

  public ShareService() {
    super("eXo_Share_Service");
  }

  /*
   * We start here when the service is called
   */
  @Override
  protected void onHandleIntent(Intent intent) {
    // Retrieve the content of the post from the intent
    postInfo = (SocialPostInfo) intent.getParcelableExtra(POST_INFO);

    // Notify the user that the share has started
    notifyBegin();

    if (postInfo.ownerAccount == null) {
      notifyResult(ShareResult.ERROR_INCORRECT_ACCOUNT);
      return;
    }

    if (postInfo.hasAttachment()) {

      UploadInfo initUploadInfo = initUpload();
      boolean uploadStarted = startUpload(initUploadInfo);
      if (uploadStarted) {
        boolean uploadedAll = doUpload(initUploadInfo);
        if (uploadedAll) {
          // already set templateParam when first doc upload completed
          doPost();
        }
      }
      ;
    } else {
      // We don't have an attachment, maybe a link
      // TODO move as a separate Action - MOB-1866
      String link = null;
      link = extractLinkFromText();
      if (link != null) {
        postInfo.activityType = SocialPostInfo.TYPE_LINK;
        postInfo.postMessage = postInfo.postMessage.replace(link, String.format(Locale.US, "<a href=\"%s\">%s</a>", link, link));
      }
      postInfo.templateParams = linkParams(link);
      doPost();
    }

  }

  /**
   * Create the resources needed to create the upload destination folder and
   * upload the file
   */
  private UploadInfo initUpload() {
    postInfo.activityType = SocialPostInfo.TYPE_DOC;
    UploadInfo uploadInfo = new UploadInfo();
    uploadInfo.init(postInfo);

    return uploadInfo;

  }

  /**
   * Create the directory where the files are stored on the server, if it does
   * not already exist.
   */
  private boolean startUpload(UploadInfo uploadInfo) {
    return CreateFolderAction.execute(postInfo, uploadInfo, new ActionListener() {

      @Override
      public boolean onSuccess(String message) {
        return true;
      }

      @Override
      public boolean onError(String error) {
        notifyResult(ShareResult.ERROR_CREATE_FOLDER);
        return false;
      }
    });
  }

  /**
   * Upload the file
   */
  private boolean doUpload(UploadInfo initUploadInfo) {
    boolean uploadedAll = false;
    uploadedMap.clear();
    UploadInfo uploadInfo = initUploadInfo;
    final int numberOfFiles = postInfo.postAttachedFiles.size();
    for (int i = 0; i < numberOfFiles; i++) {
      // notify the start of the upload i / total
      notifyProgress(i + 1, numberOfFiles);
      // close the current open input stream
      if (uploadInfo != null && uploadInfo.fileToUpload != null)
        uploadInfo.fileToUpload.closeDocStream();
      // Retrieve details of the document to upload
      if (i != 0) {
        uploadInfo = new UploadInfo(uploadInfo);
      }

      String fileUri = "file://" + postInfo.postAttachedFiles.get(i);
      Uri uri = Uri.parse(fileUri);
      uploadInfo.fileToUpload = ExoDocumentUtils.documentInfoFromUri(uri, getBaseContext());

      if (uploadInfo.fileToUpload == null) {
        notifyResult(ShareResult.ERROR_INCORRECT_CONTENT_URI);
        return false;
      } else {
        uploadInfo.fileToUpload.documentName = ExoDocumentUtils.cleanupFilename(uploadInfo.fileToUpload.documentName);
      }
      uploadedAll = UploadAction.execute(postInfo, uploadInfo, new ActionListener() {

        @Override
        public boolean onSuccess(String message) {
          return true;
        }

        @Override
        public boolean onError(String error) {
          notifyResult(ShareResult.ERROR_UPLOAD_FAILED);
          return false;
        }
      });
      if (uploadInfo != null && uploadInfo.fileToUpload != null)
        uploadInfo.fileToUpload.closeDocStream();
      if (!uploadedAll) {
        if (Log.LOGD)
          Log.e(LOG_TAG, String.format("Failed to upload file %d/%d : %s (doUpload)", i + 1, numberOfFiles, fileUri));
        break;
      }
      if (uploadedAll) {
        if (Log.LOGD)
          Log.d(LOG_TAG, String.format("Uploaded file %d/%d OK %s (doUpload)", i + 1, numberOfFiles, fileUri));
        if (i == 0)
          postInfo.buildTemplateParams(uploadInfo);
        else {
          uploadedMap.add(uploadInfo);
        }
      }
      // Delete file after upload
      File f = new File(postInfo.postAttachedFiles.get(i));
      if (Log.LOGD)
        Log.d(LOG_TAG, "File " + f.getName() + " deleted: " + (f.delete() ? "YES" : "NO"));
    }
    return uploadedAll;

  }

  /**
   * Post the message
   */
  private boolean doPost() {
    RestActivity createdAct = PostAction.execute(postInfo, new PostActionListener());
    boolean ret = createdAct != null;
    if (ret) {
      if (Log.LOGD)
        Log.d(LOG_TAG, "Post activity done");
      for (UploadInfo commentInfo : uploadedMap) {
        ret = doComment(createdAct, commentInfo);
        if (!ret)
          break;
        if (Log.LOGD)
          Log.d(LOG_TAG, "Comment activity done");
      }
      // Share finished successfully
      // Needed to avoid some problems when reopening the app
      if (ret) {
        ExoConnectionUtils.loggingOut();
        // Notify
        notifyResult(ShareResult.SUCCESS);
      } else
        notifyResult(ShareResult.ERROR_COMMENT_FAILED);
    } else
      notifyResult(ShareResult.ERROR_POST_FAILED);
    return ret;
  }

  /**
   * Post a comment on an activity
   * 
   * @param restAct the activity to comment
   * @param commentInfo the info to put in the comment
   * @return
   */
  private boolean doComment(RestActivity restAct, UploadInfo commentInfo) {
    // TODO create a Comment Action to delegate the operation
    boolean ret = false;
    String mimeType = (commentInfo == null ? null
                                           : (commentInfo.fileToUpload == null ? null
                                                                               : commentInfo.fileToUpload.documentMimeType));
    String urlWithoutServer = null;
    try {
      URL url = new URL(commentInfo.getUploadedUrl());
      urlWithoutServer = url.getPath();
      if (urlWithoutServer != null && !urlWithoutServer.startsWith("/"))
        urlWithoutServer = "/" + urlWithoutServer;
    } catch (MalformedURLException e) {
      if (Log.LOGW)
        Log.w(LOG_TAG, e.getMessage());
      return false;
    }
    StringBuilder bld = new StringBuilder();
    // append link
    bld.append("<a href=\"")
       .append(urlWithoutServer)
       .append("\">")
       .append(commentInfo.fileToUpload.documentName)
       .append("</a>");
    // add image in the comment's body
    if (mimeType != null && mimeType.startsWith("image/")) {
      String thumbnailUrl = urlWithoutServer.replace("/jcr/", "/thumbnailImage/large/");
      bld.append("<br/><a href=\"").append(urlWithoutServer).append("\"><img src=\"").append(thumbnailUrl).append("\" /></a>");
    }

    ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance().activityService;
    RestComment restComment = new RestComment();
    restComment.setText(bld.toString());
    try {
      ret = activityService.createComment(restAct, restComment) != null;
    } catch (SocialClientLibException e) {
      if (Log.LOGD)
        Log.e(LOG_TAG, Log.getStackTraceString(e));
    }
    return ret;
  }

  private Map<String, String> linkParams(String link) {
    // Create and return TemplateParams for a LINK_ACTIVITY
    // Return null if there is no link
    if (link == null)
      return null;
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put("comment", postInfo.postMessage);
    templateParams.put("link", link);
    templateParams.put("description", "");
    templateParams.put("image", "");
    try {
      templateParams.put("title", TitleExtractor.getPageTitle(link));
    } catch (IOException e) {
      Log.e(LOG_TAG, "Cannot retrieve link title", e);
      templateParams.put("title", link);
    }
    return templateParams;
  }

  private String extractLinkFromText() {
    String text = postInfo.postMessage;
    // Find an occurrence of http:// or https://
    // And return the corresponding URL if any
    int posHttp = text.indexOf("http://");
    int posHttps = text.indexOf("https://");
    int startOfLink = -1;
    if (posHttps > -1)
      startOfLink = posHttps;
    else if (posHttp > -1)
      startOfLink = posHttp;
    if (startOfLink > -1) {
      int endOfLink = text.indexOf(' ', startOfLink);
      if (endOfLink == -1)
        return text.substring(startOfLink);
      else
        return text.substring(startOfLink, endOfLink);
    } else {
      return null;
    }
  }

  /**
   * Send a local notification to inform that the share has started
   */
  private void notifyBegin() {
    notifId = (int) System.currentTimeMillis();
    String title = postInfo.hasAttachment() ? getString(R.string.ShareDocumentTitle) : getString(R.string.ShareMessageTitle);
    String text = postInfo.hasAttachment() ? getString(R.string.ShareDocumentText) : getString(R.string.ShareMessageText);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
    builder.setSmallIcon(R.drawable.icon_share_notif);
    builder.setContentTitle(title);
    builder.setContentText(text);
    builder.setAutoCancel(true);
    builder.setProgress(0, 0, true);
    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    manager.notify(notifId, builder.build());
  }

  /**
   * Send a local notification to inform of the progress. Only called if the
   * share contains 1 or more attachments.
   * 
   * @param current the index of the current file being uploaded
   * @param total the total number of files to upload
   */
  private void notifyProgress(int current, int total) {
    String text = String.format(Locale.US, "%s (%d/%d)", getString(R.string.ShareDocumentText), current, total);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
    builder.setSmallIcon(R.drawable.icon_share_notif);
    builder.setContentTitle(getString(R.string.ShareDocumentTitle));
    builder.setContentText(text);
    builder.setAutoCancel(true);
    builder.setProgress(0, 0, true);
    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    manager.notify(notifId, builder.build());
  }

  /**
   * Notify the end of the sharing. The message depends on the given result.
   * 
   * @param result one of {@link ShareResult} values
   */
  private void notifyResult(ShareResult result) {
    String text = "";
    switch (result) {
    case ERROR_CREATE_FOLDER:
      text = getString(R.string.ShareErrorUploadFolderFailed);
      break;
    case ERROR_INCORRECT_ACCOUNT:
      text = getString(R.string.ShareErrorIncorrectAccount);
      break;
    case ERROR_INCORRECT_CONTENT_URI:
      text = getString(R.string.ShareErrorCannotReadDoc);
      break;
    case ERROR_POST_FAILED:
      text = getString(R.string.ShareErrorPostFailed);
      break;
    case ERROR_COMMENT_FAILED:
      text = getString(R.string.ShareErrorCommentFailed);
      break;
    case ERROR_UPLOAD_FAILED:
      text = getString(R.string.ShareErrorUploadFailed);
      break;
    case SUCCESS:
      text = getString(R.string.ShareOperationSuccess);
      break;
    default:
      break;
    }
    String title = postInfo.hasAttachment() ? getString(R.string.ShareDocumentTitle) : getString(R.string.ShareMessageTitle);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
    builder.setSmallIcon(R.drawable.icon_share_notif);
    builder.setContentTitle(title);
    builder.setContentText(text);
    builder.setAutoCancel(true);
    builder.setProgress(0, 0, false);
    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    manager.notify(notifId, builder.build());
  }

  public static class UploadInfo {

    public String       uploadId;

    public DocumentInfo fileToUpload;

    public String       repository;

    public String       workspace;

    public String       drive;

    public String       folder;

    public String       jcrUrl;

    public UploadInfo() {
      super();
    }

    public UploadInfo(UploadInfo another) {
      uploadId = Long.toHexString(System.currentTimeMillis());
      this.repository = another.repository;
      this.workspace = another.workspace;
      this.drive = another.drive;
      this.folder = another.folder;
      this.jcrUrl = another.jcrUrl;
    }

    public void init(SocialPostInfo postInfo) {

      uploadId = Long.toHexString(System.currentTimeMillis());
      repository = DocumentHelper.getInstance().repository;
      workspace = DocumentHelper.getInstance().workspace;

      if (postInfo.isPublic()) {
        // File will be uploaded in the Public folder of the user's drive
        // e.g. /Users/u___/us___/use___/user/Public/Mobile
        drive = ExoConstants.DOCUMENT_PERSONAL_DRIVE_NAME;
        folder = "Public/Mobile";
        jcrUrl = DocumentHelper.getInstance().getRepositoryHomeUrl();
      } else {
        // File will be uploaded in the Documents folder of the space's drive
        // e.g. /Groups/spaces/the_space/Documents/Mobile
        drive = ".spaces." + postInfo.destinationSpace.getOriginalName();
        folder = "Mobile";
        StringBuffer url = new StringBuffer(postInfo.ownerAccount.serverUrl).append(ExoConstants.DOCUMENT_JCR_PATH)
                                                                            .append("/")
                                                                            .append(repository)
                                                                            .append("/")
                                                                            .append(workspace)
                                                                            .append("/Groups/spaces/")
                                                                            .append(postInfo.destinationSpace.getOriginalName())
                                                                            .append("/Documents");
        jcrUrl = url.toString();
      }
    }
    
    public String getUploadedUrl() {
      return new StringBuffer(jcrUrl).append("/").append(folder).append("/").append(fileToUpload.documentName).toString();
    }
  }
}
