package org.exoplatform.social;

import greendroid.widget.ActionBarItem;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.apache.http.HttpResponse;
import org.exoplatform.controller.AppController;
import org.exoplatform.controller.ExoApplicationsController2;
import org.exoplatform.controller.social.PostStatusTask;
import org.exoplatform.document.ExoDocumentUtils;
import org.exoplatform.proxy.WebdavMethod;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.core.model.RestActivityImpl;
import org.exoplatform.social.client.core.model.RestCommentImpl;
import org.exoplatform.social.image.SelectedImageActivity;
import org.exoplatform.social.image.SocialPhotoAlbums;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUltils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.AddPhotoDialog;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.WarningDialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.cyrilmottier.android.greendroid.R;

public class ComposeMessageActivity extends MyActionBar implements OnClickListener {

  private int                          composeType;

  private EditText                     composeEditText;

  private static LinearLayout          fileAttachWrap;

  private Button                       sendButton;

  private Button                       cancelButton;

  private PostStatusTask               mPostTask;

  private String                       composeMessage;

  private String                       sdcard_temp_dir = null;

  private String                       comment;

  private String                       statusUpdate;

  private String                       sendText;

  private String                       cancelText;

  private String                       inputTextWarning;

  private String                       okString;

  private String                       titleString;

  private String                       contentString;

  public static ComposeMessageActivity composeMessageActivity;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.compose_message_layout);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    onChangeLanguage(AppController.bundle);
    composeType = getIntent().getIntExtra(ExoConstants.COMPOSE_TYPE, composeType);
    if (composeType == 0) {
      setTitle(statusUpdate);
      addActionBarItem();
      getActionBar().getItem(0).setDrawable(R.drawable.gd_action_bar_take_photo);
    } else {
      setTitle(comment);
    }
    composeMessageActivity = this;
    initComponents();

  }

  private void initComponents() {
    composeEditText = (EditText) findViewById(R.id.compose_text_view);
    composeMessage = composeEditText.getText().toString();
    fileAttachWrap = (LinearLayout) findViewById(R.id.compose_attach_file_wrap);
    sendButton = (Button) findViewById(R.id.compose_send_button);
    sendButton.setText(sendText);
    sendButton.setOnClickListener(this);
    cancelButton = (Button) findViewById(R.id.compose_cancel_button);
    cancelButton.setText(cancelText);
    cancelButton.setOnClickListener(this);
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      destroy();
      break;

    case 0:
      // new AddPhotoDialog(this).show();
      break;
    }

    return true;
  }

  public void onClick(View view) {
    if (view == sendButton) {
      composeMessage = composeEditText.getText().toString();
      if ((composeMessage != null) && (composeMessage.length() > 0)) {
        if (composeType == 0) {
          onPostTask();
        } else {
          onCommentTask();
        }
      } else {
        Toast toast = Toast.makeText(ComposeMessageActivity.this,
                                     inputTextWarning,
                                     Toast.LENGTH_LONG);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
      }

    }

    if (view == cancelButton) {
      destroy();

    }
  }

  private void destroy() {
    super.onDestroy();
    finish();
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    destroy();

  }

  private void onCommentTask() {
    try {
      RestCommentImpl comment = new RestCommentImpl();
      comment.setText(composeMessage);
      RestActivity restActivity = (RestActivity) ExoApplicationsController2.activityService.get(ActivityStreamDisplay.selectedRestActivity.getId());

      ExoApplicationsController2.activityService.createComment(restActivity, comment);
      destroy();
    } catch (RuntimeException e) {
      WarningDialog dialog = new WarningDialog(ComposeMessageActivity.this,
                                               titleString,
                                               contentString,
                                               okString);
      dialog.show();
    }
  }

  private void onPostTask() {
    if (mPostTask == null || mPostTask.getStatus() == PostStatusTask.Status.FINISHED) {
      mPostTask = (PostStatusTask) new PostStatusTask(this, sdcard_temp_dir, composeMessage).execute();
    }
  }

  private void initCamera() {
    String parentPath = Environment.getExternalStorageDirectory() + "/eXo/";
    sdcard_temp_dir = parentPath + PhotoUltils.getImageFileName();

    Intent takePictureFromCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    takePictureFromCameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                         Uri.fromFile(new File(sdcard_temp_dir)));
    startActivityForResult(takePictureFromCameraIntent, ExoConstants.TAKE_PICTURE_WITH_CAMERA);
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == ExoConstants.TAKE_PICTURE_WITH_CAMERA) {
      File file = new File(sdcard_temp_dir);

      if (resultCode == Activity.RESULT_OK) {
        addImageToMessage(file);

      } else {
        file.delete();
      }
    }

  }

  public static void addImageToMessage(File file) {
    try {
      final String filePath = file.getAbsolutePath();
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inSampleSize = 8;
      FileInputStream fis = new FileInputStream(file);
      Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);
      fis.close();
      ImageView image = new ImageView(composeMessageActivity);
      image.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      image.setImageBitmap(bitmap);
      image.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          Intent intent = new Intent(composeMessageActivity, SelectedImageActivity.class);
          intent.putExtra(ExoConstants.SELECTED_IMAGE_EXTRA, filePath);
          composeMessageActivity.startActivity(intent);
        }
      });
      LayoutParams params = new LayoutParams(50, 50);
      params.setMargins(2, 2, 2, 2);
      fileAttachWrap.removeAllViews();
      fileAttachWrap.addView(image, params);
    } catch (Exception e) {

    }

  }

  private void onChangeLanguage(ResourceBundle resourceBundle) {
    comment = resourceBundle.getString("Comment");
    statusUpdate = resourceBundle.getString("StatusUpdate");
    sendText = resourceBundle.getString("Send");
    cancelText = resourceBundle.getString("Cancel");
    inputTextWarning = resourceBundle.getString("InputTextWarning");
    okString = resourceBundle.getString("OK");
    titleString = resourceBundle.getString("Warning");
    contentString = resourceBundle.getString("ConnectionError");

  }

}
