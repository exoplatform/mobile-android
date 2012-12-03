package org.exoplatform.ui.social;

import greendroid.util.Config;
import greendroid.widget.ActionBarItem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.exoplatform.R;
import org.exoplatform.controller.social.ComposeMessageController;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUtils;
import org.exoplatform.utils.SettingUtils;
import org.exoplatform.widget.AddPhotoDialog;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.PostWaitingDialog;
import org.exoplatform.widget.RemoveAttachedPhotoDialog;
import org.exoplatform.widget.RetangleImageView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

public class ComposeMessageActivity extends MyActionBar implements View.OnClickListener {

  private PostWaitingDialog            _progressDialog;

  private int                          composeType;

  private EditText                     composeEditText;

  private ScrollView                   textFieldScrollView;

  private static LinearLayout          fileAttachWrap;

  private Button                       sendButton;

  private Button                       cancelButton;

  private String                       composeMessage;

  private String                       comment;

  private String                       statusUpdate;

  private String                       cancelText;

  private String                       sendText;

  private ComposeMessageController     messageController;

  public static ComposeMessageActivity composeMessageActivity;

  private String                       sdcard_temp_dir = null;

  private int                          currentPosition;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.compose_message_layout);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    changeLanguage();
    composeMessageActivity = this;
    if (savedInstanceState != null)
      finish();
    else {
      composeType = getIntent().getIntExtra(ExoConstants.COMPOSE_TYPE, composeType);
      if (composeType == 0) {
        setTitle(statusUpdate);
        addActionBarItem();
        getActionBar().getItem(0).setDrawable(R.drawable.action_bar_icon_photo);
      } else {
        currentPosition = getIntent().getIntExtra(ExoConstants.ACTIVITY_CURRENT_POSITION,
                                                  currentPosition);
        setTitle(comment);
      }
      initComponents();
    }
  }

  private void initComponents() {
    messageController = new ComposeMessageController(this, composeType, _progressDialog);
    composeEditText = (EditText) findViewById(R.id.compose_text_view);
    textFieldScrollView = (ScrollView) findViewById(R.id.compose_textfield_scroll);
    textFieldScrollView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
          InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
          mgr.showSoftInput(composeEditText, InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
        return false;
      }
    });

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

      if (SocialDetailActivity.socialDetailActivity != null) {
        SocialDetailActivity.socialDetailActivity.finish();
      }

      if (SocialTabsActivity.instance != null) {
        SocialTabsActivity.instance.finish();
      }
      finish();
      break;

    case 0:
      new AddPhotoDialog(this, messageController).show();
      break;
    }

    return true;
  }

  public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    super.onActivityResult(requestCode, resultCode, intent);
    if (resultCode == RESULT_OK) {
      switch (requestCode) {
      // Add image after capturing photo from camera
      case ExoConstants.TAKE_PICTURE_WITH_CAMERA:
        String sdcard_dir = messageController.getSdCardTempDir();
        File file = new File(sdcard_dir);
        addImageToMessage(file);
        break;
      // Get the pick image action result from native photo album and send
      // it to
      // SelectedImageActivity class
      case ExoConstants.REQUEST_ADD_PHOTO:
        Intent intent2 = new Intent(this, SelectedImageActivity.class);
        Uri uri = intent.getData();
        intent.putExtra(ExoConstants.SELECTED_IMAGE_MODE, 2);
        intent2.setData(uri);
        if (intent.getExtras() != null) {
          intent2.putExtras(intent.getExtras());
        }

        startActivity(intent2);
        break;
      }
    }
    /*
     * Set default language to our application setting language
     */
    SettingUtils.setDefaultLanguage(this);
  }

  public static void addImageToMessage(File file) {
    try {
      final String filePath = file.getAbsolutePath();
      composeMessageActivity.sdcard_temp_dir = filePath;
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inSampleSize = 4;
      FileInputStream fis = new FileInputStream(file);
      Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);
      fis.close();
      bitmap = PhotoUtils.resizeImageBitmap(composeMessageActivity, bitmap);
      RetangleImageView image = new RetangleImageView(composeMessageActivity);
      image.setPadding(1, 1, 1, 1);
      image.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      image.setImageBitmap(bitmap);
      image.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          Intent intent = new Intent(composeMessageActivity, SelectedImageActivity.class);
          intent.putExtra(ExoConstants.SELECTED_IMAGE_MODE, 1);
          intent.putExtra(ExoConstants.SELECTED_IMAGE_EXTRA, filePath);
          composeMessageActivity.startActivity(intent);
        }
      });
      image.setOnLongClickListener(new OnLongClickListener() {

        @Override
        public boolean onLongClick(View v) {
          new RemoveAttachedPhotoDialog(composeMessageActivity).show();
          return true;
        }
      });
      LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
      fileAttachWrap.removeAllViews();
      fileAttachWrap.addView(image, params);
    } catch (IOException e) {
      if (Config.GD_ERROR_LOGS_ENABLED)
        Log.e("Exception", "Error when adding image to message!");

    }

  }

  public static void removeImageFromMessage() {
    fileAttachWrap.removeAllViews();
    composeMessageActivity.sdcard_temp_dir = null;
  }

  @Override
  public void finish() {
    if (_progressDialog != null) {
      _progressDialog.dismiss();
    }
    super.finish();
  }

  @Override
  public void onBackPressed() {
    finish();
  }

  @Override
  public void onClick(View view) {

    if (view.equals(sendButton)) {
      composeMessage = composeEditText.getText().toString();
      messageController.onSendMessage(composeMessage, sdcard_temp_dir, currentPosition);
    }
    if (view.equals(cancelButton)) {
      finish();
    }
  }

  private void changeLanguage() {
    Resources resource = getResources();
    comment = resource.getString(R.string.Comment);
    statusUpdate = resource.getString(R.string.StatusUpdate);
    sendText = resource.getString(R.string.Send);
    cancelText = resource.getString(R.string.Cancel);
  }

}
