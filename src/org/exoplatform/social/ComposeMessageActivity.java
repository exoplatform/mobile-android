package org.exoplatform.social;

import greendroid.widget.ActionBarItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import org.exoplatform.controller.AppController;
import org.exoplatform.controller.ExoApplicationsController2;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.core.model.RestActivityImpl;
import org.exoplatform.social.client.core.model.RestCommentImpl;
import org.exoplatform.social.image.SocialImageLibrary;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUltils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.MyActionBar;

import android.app.Activity;
import android.app.Dialog;
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

  private String                       composeMessage;

  private String                       sdcard_temp_dir;

  private String                       comment;

  private String                       statusUpdate;

  private String                       sendText;

  private String                       cancelText;

  private String                       inputTextWarning;

  private String                       noService;

  private String                       addPhotoTitle;

  private String                       takePhotoText;

  private String                       photoLibraryText;

  private Intent                       intent;

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
      // Intent intent = new Intent(this, ExoApplicationsController2.class);
      // startActivity(intent);
      destroy();
      break;

    case 0:
      new AddPhotoDialog(this).show();
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
          intent = new Intent(this, SocialActivity.class);
          startActivity(intent);
        } else {
          onCommentTask();
          intent = new Intent(this, ActivityStreamDisplay.class);
          startActivity(intent);
        }
        try {
          Thread.sleep(1000);
          destroy();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

      } else {
        Toast toast = Toast.makeText(ComposeMessageActivity.this,
                                     inputTextWarning,
                                     Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
      }

    }

    if (view == cancelButton) {
      if (composeType == 0) {
        intent = new Intent(this, SocialActivity.class);
        startActivity(intent);
      } else {
        intent = new Intent(this, ActivityStreamDisplay.class);
        startActivity(intent);
      }
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
    if (composeType == 0) {
      intent = new Intent(this, SocialActivity.class);
      startActivity(intent);
    } else {
      intent = new Intent(this, ActivityStreamDisplay.class);
      startActivity(intent);
    }
    destroy();

  }

  private void onCommentTask() {
    try {
      RestCommentImpl comment = new RestCommentImpl();
      comment.setText(composeMessage);
      RestActivity restActivity = (RestActivity) ExoApplicationsController2.activityService.get(ActivityStreamDisplay.selectedRestActivity.getId());

      ExoApplicationsController2.activityService.createComment(restActivity, comment);
    } catch (RuntimeException e) {
      Toast toast = Toast.makeText(this, noService, Toast.LENGTH_LONG);
      toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
      toast.show();
    }
  }

  private void onPostTask() {
    try {
      RestActivityImpl activityImlp = new RestActivityImpl();
      activityImlp.setTitle(composeMessage);
      ExoApplicationsController2.activityService.create(activityImlp);
    } catch (RuntimeException e) {
      Toast toast = Toast.makeText(this, noService, Toast.LENGTH_LONG);
      toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
      toast.show();
    }
  }

  private void initCamera() {
    String parentPath = Environment.getExternalStorageDirectory() + "/eXo/";
    sdcard_temp_dir = parentPath + PhotoUltils.getDateFormat() + ".png";

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
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inSampleSize = 8;
      Bitmap bitmap;
      bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
      ImageView image = new ImageView(composeMessageActivity);
      image.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      image.setImageBitmap(bitmap);
      LayoutParams params = new LayoutParams(60, 60);
      params.setMargins(2, 2, 2, 2);
      fileAttachWrap.removeAllViews();
      fileAttachWrap.addView(image, params);
    } catch (FileNotFoundException e) {

    }

  }

  private void onChangeLanguage(ResourceBundle resourceBundle) {
    comment = resourceBundle.getString("Comment");
    statusUpdate = resourceBundle.getString("StatusUpdate");
    sendText = resourceBundle.getString("Send");
    cancelText = resourceBundle.getString("Cancel");
    inputTextWarning = resourceBundle.getString("InputTextWarning");
    noService = resourceBundle.getString("NoService");
    addPhotoTitle = resourceBundle.getString("AddAPhoto");
    takePhotoText = resourceBundle.getString("TakeAPhoto");
    photoLibraryText = resourceBundle.getString("PhotoLibrary");

  }

  private class AddPhotoDialog extends Dialog implements android.view.View.OnClickListener {

    Button takePhotoButton;

    Button libraryButton;

    Button cancelButton;

    public AddPhotoDialog(Context context) {
      super(context);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.add_photo_dialog_layout);
      TextView titleView = (TextView) findViewById(R.id.add_photo_title);
      titleView.setText(addPhotoTitle);
      takePhotoButton = (Button) findViewById(R.id.add_photo_take_button);
      takePhotoButton.setText(takePhotoText);
      takePhotoButton.setOnClickListener(this);
      libraryButton = (Button) findViewById(R.id.add_photo_library_button);
      libraryButton.setText(photoLibraryText);
      libraryButton.setOnClickListener(this);
      cancelButton = (Button) findViewById(R.id.add_photo_cancel_button);
      cancelButton.setText(cancelText);
      cancelButton.setOnClickListener(this);
    }

    public void onClick(View view) {
      if (view == takePhotoButton) {
        dismiss();
        initCamera();
      }
      if (view == libraryButton) {
        dismiss();
        Intent intent = new Intent(ComposeMessageActivity.this, SocialImageLibrary.class);
        startActivity(intent);

      }
      if (view == cancelButton) {
        dismiss();
      }
    }
  }

  private class PostStatusTask extends UserTask<Void, Void, Void> {

    @Override
    public Void doInBackground(Void... params) {
      // TODO Auto-generated method stub
      return null;
    }

  }

}
