package exo.exoplatform.social;

import java.io.File;

import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.core.model.RestActivityImpl;
import org.exoplatform.social.client.core.model.RestCommentImpl;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.cyrilmottier.android.greendroid.R;

import exo.exoplatform.utils.PhotoUltils;
import exo.exoplatform.utils.ExoConstants;
import exo.exoplatform.widget.MyActionBar;
import greendroid.widget.ActionBarItem;

public class ComposeMessageActivity extends MyActionBar implements OnClickListener {

  private int      composeType;

  private EditText composeEditText;

  // private LinearLayout imageLayoutWrap;

  private Button   sendButton;

  private Button   cancelButton;

  private String   composeMessage;

  private String   sdcard_temp_dir;

  // private InputMethodManager inputManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo_Back);
    setActionBarContentView(R.layout.compose_message_layout);
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    composeType = getIntent().getIntExtra(ExoConstants.COMPOSE_TYPE, composeType);
    if (composeType == 0) {
      setTitle("Status Update");

      addActionBarItem();
      getActionBar().getItem(0).setDrawable(R.drawable.gd_action_bar_take_photo);
      // addActionBarItem(Type.TakePhoto, R.drawable.gd_action_bar_take_photo);
    } else {
      setTitle("Comment");
    }
    initComponents();

  }

  private void initComponents() {
    composeEditText = (EditText) findViewById(R.id.compose_text_view);
    // composeEditText.requestFocus();
    composeMessage = composeEditText.getText().toString();
    // imageLayoutWrap = (LinearLayout) findViewById(R.id.compose_image_wrap);

    sendButton = (Button) findViewById(R.id.compose_send_button);
    sendButton.setOnClickListener(this);

    cancelButton = (Button) findViewById(R.id.compose_cancel_button);
    cancelButton.setOnClickListener(this);

  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    // TODO Auto-generated method stub
    switch (position) {
    case -1:
      destroy();
      break;

    case 0:
      // new AddPhotoDialog(this).show();
      break;
    }

    // return super.onHandleActionBarItemClick(item, position);
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
        try {
          Thread.sleep(1000);
          destroy();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

      }

    }

    if (view == cancelButton) {

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
    RestCommentImpl comment = new RestCommentImpl();
    comment.setText(composeMessage);
    RestActivity restActivity = (RestActivity) SocialActivity.activityService.get(ActivityStreamDisplay.selectedRestActivity.getId());

    SocialActivity.activityService.createComment(restActivity, comment);

  }

  private void onPostTask() {
    RestActivityImpl activityImlp = new RestActivityImpl();
    activityImlp.setTitle(composeMessage);
    SocialActivity.activityService.create(activityImlp);

  }

  private void initCamera() {
    sdcard_temp_dir = Environment.getExternalStorageDirectory() + File.separator
        + PhotoUltils.getDateFormat() + ".jpg";
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

      }
    }

  }

  private class AddPhotoDialog extends Dialog implements android.view.View.OnClickListener {

    Button takePhotoButton;

    Button libraryButton;

    Button cancelButton;

    public AddPhotoDialog(Context context) {
      super(context);
      requestWindowFeature(Window.FEATURE_NO_TITLE);
      setContentView(R.layout.add_photo_dialog_layout);
      takePhotoButton = (Button) findViewById(R.id.add_photo_take_button);
      takePhotoButton.setOnClickListener(this);
      libraryButton = (Button) findViewById(R.id.add_photo_library_button);
      libraryButton.setOnClickListener(this);
      cancelButton = (Button) findViewById(R.id.add_photo_cancel_button);
      cancelButton.setOnClickListener(this);
    }

    public void onClick(View view) {
      if (view == takePhotoButton) {
        dismiss();
        initCamera();
      }
      if (view == libraryButton) {
        dismiss();
      }
      if (view == cancelButton) {
        dismiss();
      }
    }
  }

}