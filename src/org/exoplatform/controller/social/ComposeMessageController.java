package org.exoplatform.controller.social;

import java.io.File;

import org.exoplatform.R;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestComment;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.ui.social.SocialActivity;
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
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Toast;

public class ComposeMessageController {

  private PostWaitingDialog _progressDialog;

  private int               composeType;

  private Context           mContext;

  private PostStatusTask    mPostTask;

  private String            sdcard_temp_dir = null;

  private String            inputTextWarning;

  private String            okString;

  private String            titleString;

  private String            contentString;

  public ComposeMessageController(Context context, int type, PostWaitingDialog dialog) {
    mContext = context;
    composeType = type;
    _progressDialog = dialog;
    changeLanguage();

  }

  public void initCamera() {
    String parentPath = Environment.getExternalStorageDirectory() + "/eXo/";
    sdcard_temp_dir = parentPath + PhotoUtils.getImageFileName();

    Intent takePictureFromCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    takePictureFromCameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                         Uri.fromFile(new File(sdcard_temp_dir)));
    ((Activity) mContext).startActivityForResult(takePictureFromCameraIntent,
                                                 ExoConstants.TAKE_PICTURE_WITH_CAMERA);
  }

  public void onSendMessage(String composeMessage, String sdcard) {
    if ((composeMessage != null) && (composeMessage.length() > 0)) {
      if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
        if (composeType == 0) {
          onPostTask(composeMessage, sdcard);
        } else {
          onCommentTask(composeMessage);
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
      mPostTask = (PostStatusTask) new PostStatusTask(mContext,
                                                      sdcard,
                                                      composeMessage,
                                                      this,
                                                      _progressDialog).execute();
    }
  }

  public void onCancelPostTask() {
    if (mPostTask != null && mPostTask.getStatus() == PostStatusTask.Status.RUNNING) {
      mPostTask.cancel(true);
      mPostTask = null;
    }
  }

  private void onCommentTask(String composeMessage) {
    try {
      RestComment comment = new RestComment();
      comment.setText(composeMessage);
      String selectedId = SocialDetailHelper.getInstance().getActivityId();
      ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance()
                                                                         .getActivityService();
      RestActivity restActivity = (RestActivity) activityService.get(selectedId);

      activityService.createComment(restActivity, comment);

      ((Activity) mContext).finish();

      SocialActivity.socialActivity.loadActivity();

    } catch (SocialClientLibException e) {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
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

}
