package org.exoplatform.controller.social;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.exoplatform.document.ExoDocumentUtils;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.core.model.RestActivityImpl;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.utils.WebdavMethod;
import org.exoplatform.widget.WarningDialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

public class PostStatusTask extends UserTask<Void, Void, Integer> {
  private ProgressDialog _progressDialog;

  private Context        mContext;

  private String         sdcard_temp_dir;

  private String         composeMessage;

  private String         loadingData;

  private String         okString;

  private String         errorString;

  private String         warningTitle;

  private String         uploadUrl;

  public PostStatusTask(Context context, String dir, String content) {
    mContext = context;
    sdcard_temp_dir = dir;
    composeMessage = content;
    changeLanguage();
  }

  @Override
  public void onPreExecute() {
    _progressDialog = ProgressDialog.show(mContext, null, loadingData);
  }

  @Override
  public Integer doInBackground(Void... params) {

    try {
      RestActivityImpl activityImlp = new RestActivityImpl();
      if (sdcard_temp_dir != null) {
        createFolder();
        File file = new File(sdcard_temp_dir);
        String imageDir = uploadUrl + "/" + file.getName();
        if (file != null) {
          ExoDocumentUtils.putFileToServerFromLocal(imageDir, file, "image/jpeg");
          Map<String, String> templateParams = new HashMap<String, String>();
          templateParams.put("image", imageDir);
          activityImlp.setTemplateParams(templateParams);

        }

      }

      activityImlp.setTitle(composeMessage);
      SocialServiceHelper.getInstance().getActivityService().create(activityImlp);
      return 1;
    } catch (RuntimeException e) {
      return 0;
    }

  }

  @Override
  public void onPostExecute(Integer result) {
    if (result == 1) {
      ((Activity) mContext).finish();
    } else {
      new WarningDialog(mContext, warningTitle, errorString, okString).show();
    }
    _progressDialog.dismiss();

  }

  private boolean createFolder() {
    String userName = AccountSetting.getInstance().getUsername();
    String domain = AccountSetting.getInstance().getPassword();
    uploadUrl = ExoDocumentUtils.getDocumentUrl(userName, domain);
    uploadUrl += "/Public/Mobile";

    HttpResponse response;
    try {

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

    } catch (Exception e) {
      return false;
    }
  }

  private void changeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    loadingData = bundle.getString("LoadingData");
    okString = bundle.getString("OK");
    errorString = bundle.getString("PostError");
    warningTitle = bundle.getString("Warning");
  }

}
