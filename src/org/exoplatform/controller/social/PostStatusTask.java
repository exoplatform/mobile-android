package org.exoplatform.controller.social;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.ui.social.SocialActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.utils.WebdavMethod;
import org.exoplatform.widget.WaitingDialog;
import org.exoplatform.widget.WarningDialog;

import android.app.Activity;
import android.content.Context;

public class PostStatusTask extends UserTask<Void, Void, Integer> {
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
                        ComposeMessageController controller) {
    mContext = context;
    messageController = controller;
    sdcard_temp_dir = dir;
    composeMessage = content;
    changeLanguage();
  }

  @Override
  public void onPreExecute() {
    _progressDialog = new PostWaitingDialog(mContext, null, sendingData);
    _progressDialog.show();
  }

  @Override
  public Integer doInBackground(Void... params) {

    try {
      RestActivity activityImlp = new RestActivity();
      if (sdcard_temp_dir != null) {
        createFolder();

        File file = new File(sdcard_temp_dir);
        String imageDir = uploadUrl + "/" + file.getName();
        if (file != null) {
          ExoDocumentUtils.putFileToServerFromLocal(imageDir, file, ExoConstants.IMAGE_TYPE);
          Map<String, String> templateParams = new HashMap<String, String>();

          activityImlp.setType("DOC_ACTIVITY");
          String pathExtension = "jcr/repository/collaboration";
          int indexOfDocLink = imageDir.indexOf(pathExtension);
          StringBuffer docBuffer = new StringBuffer("/portal/rest/");
          docBuffer.append(imageDir.substring(indexOfDocLink));
          String docPath = imageDir.substring(indexOfDocLink + pathExtension.length());
          templateParams.put("DOCPATH", docPath);
          templateParams.put("MESSAGE", "");
          templateParams.put("DOCLINK", docBuffer.toString());
          templateParams.put("WORKSPACE", "collaboration");
          templateParams.put("REPOSITORY", "repository");
          templateParams.put("DOCNAME", file.getName());
          activityImlp.setTemplateParams(templateParams);

        }

      }

      activityImlp.setTitle(composeMessage);
      SocialServiceHelper.getInstance().getActivityService().create(activityImlp);
      return 1;
    } catch (SocialClientLibException e) {
      return 0;
    }

  }

  @Override
  public void onPostExecute(Integer result) {
    if (result == 1) {
      ((Activity) mContext).finish();
      SocialActivity.socialActivity.reloadActivity();
    } else {
      new WarningDialog(mContext, warningTitle, errorString, okString).show();
    }
    _progressDialog.dismiss();

  }

  private boolean createFolder() {

    uploadUrl = ExoDocumentUtils.repositoryHomeURL + "/Public/Mobile";

    HttpResponse response;
    try {
      // Re authenticate when we upload file from social
      WebdavMethod copy = new WebdavMethod("HEAD", uploadUrl);
      response = ExoConnectionUtils.httpClient.execute(copy);
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        return true;

      } else {
        copy = new WebdavMethod("MKCOL", uploadUrl);
        response = ExoConnectionUtils.httpClient.execute(copy);
        status = response.getStatusLine().getStatusCode();

        if (status >= 200 && status < 300) {
          return true;
        }

        return false;
      }

    } catch (IOException e) {
      return false;
    }
  }

  private void changeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    sendingData = bundle.getString("SendingData");
    okString = bundle.getString("OK");
    errorString = bundle.getString("PostError");
    warningTitle = bundle.getString("Warning");
  }

  private class PostWaitingDialog extends WaitingDialog {

    public PostWaitingDialog(Context context, String titleString, String contentString) {
      super(context, titleString, contentString);
    }

    @Override
    public void onBackPressed() {
      super.onBackPressed();
      messageController.onCancelPostTask();
    }

  }
}
