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

import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

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
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.utils.TitleExtractor;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

/**
 * Created by The eXo Platform SAS.<br/>
 * 
 * @author Philippe Aristote paristote@exoplatform.com
 * @since Jun 4, 2015
 */
public class ShareService extends IntentService {

  public static final String LOG_TAG     = "____eXo____ShareService____";

  public static final String POST_INFO   = "postInfo";

  private int                notifId     = 1;

  private SocialPostInfo     postInfo;

  // private UploadInfo uploadInfo;

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
   * On Platform 4.1-M2, the upload service renames the uploaded file. Therefore
   * the link to this file in the activity becomes incorrect. To fix this, we
   * rename the file before upload so the same name is used in the activity.
   */
  private void cleanupFilename(UploadInfo uploadInfo) {
    final String TILDE_HYPHENS_COLONS_SPACES = "[~_:\\s]";
    final String MULTIPLE_HYPHENS = "-{2,}";
    final String FORBIDDEN_CHARS = "[`!@#\\$%\\^&\\*\\|;\"'<>/\\\\\\[\\]\\{\\}\\(\\)\\?,=\\+\\.]+";
    String name = uploadInfo.fileToUpload.documentName;
    String ext = "";
    int lastDot = name.lastIndexOf('.');
    if (lastDot > 0 && lastDot < name.length()) {
      ext = name.substring(lastDot); // the ext with the dot
      name = name.substring(0, lastDot); // the name before the ext
    }
    // [~_:\s] Replaces ~ _ : and spaces by -
    name = Pattern.compile(TILDE_HYPHENS_COLONS_SPACES).matcher(name).replaceAll("-");
    // [`!@#\$%\^&\*\|;"'<>/\\\[\]\{\}\(\)\?,=\+\.]+ Deletes forbidden chars
    name = Pattern.compile(FORBIDDEN_CHARS).matcher(name).replaceAll("");
    // Converts accents to regular letters
    name = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    // Replaces upper case characters by lower case
    Locale loc = new Locale(SettingUtils.getPrefsLanguage(getApplicationContext()));
    name = name.toLowerCase(loc == null ? Locale.getDefault() : loc);
    // Remove consecutive -
    name = Pattern.compile(MULTIPLE_HYPHENS).matcher(name).replaceAll("-");
    // Save
    uploadInfo.fileToUpload.documentName = name + ext;
  }

  /**
   * Create the resources needed to create the upload destination folder and
   * upload the file
   */
  private UploadInfo initUpload() {
    postInfo.activityType = SocialPostInfo.TYPE_DOC;

    UploadInfo uploadInfo = new UploadInfo();
    uploadInfo.uploadId = Long.toHexString(System.currentTimeMillis());
    uploadInfo.repository = DocumentHelper.getInstance().repository;
    uploadInfo.workspace = DocumentHelper.getInstance().workspace;

    if (postInfo.isPublic()) {
      // File will be uploaded in the Public folder of the user's drive
      // e.g. /Users/u___/us___/use___/user/Public/Mobile
      uploadInfo.drive = ExoConstants.DOCUMENT_PERSONAL_DRIVE_NAME;
      uploadInfo.folder = "Public/Mobile";
      uploadInfo.jcrUrl = DocumentHelper.getInstance().getRepositoryHomeUrl();
    } else {
      // File will be uploaded in the Documents folder of the space's drive
      // e.g. /Groups/spaces/the_space/Documents/Mobile
      uploadInfo.drive = ".spaces." + postInfo.destinationSpace.getOriginalName();
      uploadInfo.folder = "Mobile";
      StringBuffer url = new StringBuffer(postInfo.ownerAccount.serverUrl).append(ExoConstants.DOCUMENT_JCR_PATH)
                                                                          .append("/")
                                                                          .append(uploadInfo.repository)
                                                                          .append("/")
                                                                          .append(uploadInfo.workspace)
                                                                          .append("/Groups/spaces/")
                                                                          .append(postInfo.destinationSpace.getOriginalName())
                                                                          .append("/Documents");
      uploadInfo.jcrUrl = url.toString();
    }
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
    for (int i = 0; i < postInfo.postAttachmentUri.size(); i++) {
      // close the current open input stream
      if (uploadInfo != null && uploadInfo.fileToUpload != null)
        uploadInfo.fileToUpload.closeDocStream();
      // Retrieve details of the document to upload
      if (i != 0) {
        uploadInfo = new UploadInfo(uploadInfo);
      }
      String uriString = postInfo.postAttachmentUri.get(i);
      Uri uri = Uri.parse(uriString);
      uploadInfo.fileToUpload = ExoDocumentUtils.documentInfoFromUri(uri, getBaseContext());

      if (uploadInfo.fileToUpload == null) {
        notifyResult(ShareResult.ERROR_INCORRECT_CONTENT_URI);
        return false;
      } else {
        cleanupFilename(uploadInfo);
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
          Log.d(LOG_TAG, "doUpload failed when upload attach ", i, " uri=", uriString);
        break;
      }
      if (uploadedAll) {
        uploadInfo.uploadedUrl = getDocUrl(uploadInfo);
        if (Log.LOGD)
          Log.d(LOG_TAG, "doUpload uploaded attach ", i, " uri=", uriString);
        if (i == 0)
          postInfo.templateParams = docParams(uploadInfo);
        else {
          uploadedMap.add(uploadInfo);
        }
      }
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
        Log.d(LOG_TAG, "doPost post commplete");
      for (UploadInfo commentInfo : uploadedMap) {
        ret = doComment(createdAct, commentInfo);
        if (!ret)
          break;
        if (Log.LOGD)
          Log.d(LOG_TAG, "doPost comment success");
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

  private boolean doComment(RestActivity restAct, UploadInfo commentInfo) {
    boolean ret = false;
    String mimeType = (commentInfo == null ? null
                                           : (commentInfo.fileToUpload == null ? null
                                                                               : commentInfo.fileToUpload.documentMimeType));
    StringBuilder bld = new StringBuilder();
    // append link
    bld.append("<a href=\"")
       .append(commentInfo.uploadedUrl)
       .append("\" >")
       .append(commentInfo.fileToUpload.documentName)
       .append("</a>");
    if (mimeType != null && mimeType.startsWith("image/")) {
      String src = commentInfo.uploadedUrl.replace("/jcr/", "/thumbnailImage/medium/");
      bld.append("<br/><img src=\"").append(src).append("\" />");
    }

    ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance().activityService;
    RestComment restComment = new RestComment();
    restComment.setText(bld.toString());
    try {
      ret = activityService.createComment(restAct, restComment) != null;
    } catch (SocialClientLibException e) {
      Log.d(LOG_TAG, Log.getStackTraceString(e));
    }
    return ret;
  }

  private String getDocUrl(UploadInfo pUploadInfo) {
    return pUploadInfo.jcrUrl + "/" + pUploadInfo.folder + "/" + pUploadInfo.fileToUpload.documentName;
  }

  private Map<String, String> docParams(UploadInfo pUploadInfo) {
    // Create and return TemplateParams for a DOC_ACTIVITY
    String docUrl = pUploadInfo.uploadedUrl;
    Map<String, String> templateParams = new HashMap<String, String>();
    templateParams.put("WORKSPACE", pUploadInfo.workspace);
    templateParams.put("REPOSITORY", pUploadInfo.repository);
    String docLink = docUrl.substring(postInfo.ownerAccount.serverUrl.length());
    templateParams.put("DOCLINK", docLink);
    StringBuffer beginPath = new StringBuffer(ExoConstants.DOCUMENT_JCR_PATH).append("/")
                                                                             .append(pUploadInfo.repository)
                                                                             .append("/")
                                                                             .append(pUploadInfo.workspace);
    String docPath = docLink.substring(beginPath.length());
    templateParams.put("DOCPATH", docPath);
    templateParams.put("DOCNAME", pUploadInfo.fileToUpload.documentName);

    if (!postInfo.isPublic()) {
      templateParams.put("mimeType", pUploadInfo.fileToUpload.documentMimeType);
    }

    return templateParams;
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

  private void notifyBegin() {
    notifId = (int) System.currentTimeMillis();
    String title = postInfo.hasAttachment() ? getString(R.string.ShareDocumentTitle) : getString(R.string.ShareMessageTitle);
    String text = postInfo.hasAttachment() ? getString(R.string.ShareDocumentText) : getString(R.string.ShareMessageText);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
    builder.setSmallIcon(R.drawable.application_icon);
    builder.setContentTitle(title);
    builder.setContentText(text);
    builder.setAutoCancel(true);
    builder.setProgress(0, 0, true);
    NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    manager.notify(notifId, builder.build());
  }

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
    builder.setSmallIcon(R.drawable.application_icon);
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

    public String       uploadedUrl;

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

  }
}
