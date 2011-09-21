package org.exoplatform.controller.social;

import java.io.File;

import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.service.ActivityService;
import org.exoplatform.social.client.core.model.RestCommentImpl;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUltils;
import org.exoplatform.widget.WarningDialog;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.widget.Toast;

public class ComposeMessageController {
  private int            composeType;

  private Context        mContext;

  private PostStatusTask mPostTask;

  private String         sdcard_temp_dir = null;

  private String         inputTextWarning;

  private String         okString;

  private String         titleString;

  private String         contentString;

  public ComposeMessageController(Context context, int type) {
    mContext = context;
    composeType = type;
    changeLanguage();

  }

  public void initCamera() {
    String parentPath = Environment.getExternalStorageDirectory() + "/eXo/";
    sdcard_temp_dir = parentPath + PhotoUltils.getImageFileName();

    Intent takePictureFromCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    takePictureFromCameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                         Uri.fromFile(new File(sdcard_temp_dir)));
    ((Activity) mContext).startActivityForResult(takePictureFromCameraIntent,
                                                 ExoConstants.TAKE_PICTURE_WITH_CAMERA);
  }

  public void onSendMessage(String composeMessage, String sdcard) {
    if ((composeMessage != null) && (composeMessage.length() > 0)) {
      if (composeType == 0) {
        onPostTask(composeMessage, sdcard);
      } else {
        onCommentTask(composeMessage);
      }
    } else {
      Toast toast = Toast.makeText(mContext, inputTextWarning, Toast.LENGTH_LONG);
      toast.setGravity(Gravity.BOTTOM, 0, 0);
      toast.show();
    }
  }

  private void onPostTask(String composeMessage, String sdcard) {
    if (mPostTask == null || mPostTask.getStatus() == PostStatusTask.Status.FINISHED) {
      mPostTask = (PostStatusTask) new PostStatusTask(mContext, sdcard, composeMessage).execute();
    }
  }

  private void onCommentTask(String composeMessage) {
    try {
      RestCommentImpl comment = new RestCommentImpl();
      comment.setText(composeMessage);
      String selectedId = SocialDetailHelper.getInstance().getActivityId();
      ActivityService<RestActivity> activityService = SocialServiceHelper.getInstance()
                                                                         .getActivityService();
      RestActivity restActivity = (RestActivity) activityService.get(selectedId);

      activityService.createComment(restActivity, comment);
      ((Activity) mContext).finish();
    } catch (RuntimeException e) {
      WarningDialog dialog = new WarningDialog(mContext, titleString, contentString, okString);
      dialog.show();
    }
  }

  public String getSdCardTempDir() {
    return sdcard_temp_dir;
  }

  private void changeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    inputTextWarning = bundle.getString("InputTextWarning");
    okString = bundle.getString("OK");
    titleString = bundle.getString("Warning");
    contentString = bundle.getString("ConnectionError");
  }

}
