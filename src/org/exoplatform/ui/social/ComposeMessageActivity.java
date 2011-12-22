package org.exoplatform.ui.social;

import greendroid.widget.ActionBarItem;

import java.io.File;
import java.io.FileInputStream;

import org.exoplatform.controller.social.ComposeMessageController;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.AddPhotoDialog;
import org.exoplatform.widget.MyActionBar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

import com.cyrilmottier.android.greendroid.R;

public class ComposeMessageActivity extends MyActionBar implements View.OnClickListener {
  private int                          composeType;

  private ScrollView                   composeScrollView;

  private EditText                     composeEditText;

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

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);
    setActionBarContentView(R.layout.compose_message_layout);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    changeLanguage();
    composeMessageActivity = this;
    composeType = getIntent().getIntExtra(ExoConstants.COMPOSE_TYPE, composeType);
    if (composeType == 0) {
      setTitle(statusUpdate);
      addActionBarItem();
      getActionBar().getItem(0).setDrawable(R.drawable.action_bar_icon_photo);
    } else {
      setTitle(comment);
    }
    initComponents();

  }

  private void initComponents() {
    messageController = new ComposeMessageController(this, composeType);

    composeScrollView = (ScrollView) findViewById(R.id.compose_textfield_scroll);
    composeScrollView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.showSoftInput(composeEditText, InputMethodManager.SHOW_IMPLICIT);

        return true;
      }
    });

    composeEditText = (EditText) findViewById(R.id.compose_text_view);
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
      if (SocialActivity.socialActivity != null) {
        SocialActivity.socialActivity.finish();
      }
      if (SocialDetailActivity.socialDetailActivity != null) {
        SocialDetailActivity.socialDetailActivity.finish();
      }
      destroy();
      break;

    case 0:
      new AddPhotoDialog(this, messageController).show();
      break;
    }

    return true;
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == ExoConstants.TAKE_PICTURE_WITH_CAMERA) {
      String sdcard_dir = messageController.getSdCardTempDir();
      File file = new File(sdcard_dir);
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
      composeMessageActivity.sdcard_temp_dir = filePath;
      BitmapFactory.Options options = new BitmapFactory.Options();
      options.inSampleSize = 8;
      FileInputStream fis = new FileInputStream(file);
      Bitmap bitmap = BitmapFactory.decodeStream(fis, null, options);
      fis.close();
      ImageView image = new ImageView(composeMessageActivity);
      image.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      image.setScaleType(ScaleType.FIT_XY);
      image.setImageBitmap(bitmap);
      image.setOnClickListener(new OnClickListener() {

        public void onClick(View v) {
          Intent intent = new Intent(composeMessageActivity, SelectedImageActivity.class);
          intent.putExtra(ExoConstants.SELECTED_IMAGE_EXTRA, filePath);
          composeMessageActivity.startActivity(intent);
        }
      });
      LayoutParams params = new LayoutParams(70, 50);
      params.setMargins(2, 2, 2, 2);
      fileAttachWrap.removeAllViews();
      fileAttachWrap.addView(image, params);
    } catch (Exception e) {

    }

  }

  private void destroy() {
    super.onDestroy();
    finish();
  }

  @Override
  public void onBackPressed() {
    destroy();
  }

  // @Override
  public void onClick(View view) {

    if (view.equals(sendButton)) {
      composeMessage = composeEditText.getText().toString();
      messageController.onSendMessage(composeMessage, sdcard_temp_dir);
    }
    if (view.equals(cancelButton)) {
      destroy();
    }
  }

  private void changeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    comment = bundle.getString("Comment");
    statusUpdate = bundle.getString("StatusUpdate");
    sendText = bundle.getString("Send");
    cancelText = bundle.getString("Cancel");
  }

}
