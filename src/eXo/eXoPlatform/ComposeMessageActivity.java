package eXo.eXoPlatform;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

import java.io.File;

import com.cyrilmottier.android.greendroid.R;

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
import android.view.inputmethod.InputMethodManager;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

public class ComposeMessageActivity extends MyActionBar implements OnClickListener {

  private int                          composeType;

  private EditText                     composeEditText;

//  private LinearLayout                 imageLayoutWrap;

  private Button                       sendButton;

  private Button                       cancelButton;

  private String                       composeMessage;

  private String                       sdcard_temp_dir;

  public static ComposeMessageActivity composeMessageActivity;
  
  private InputMethodManager           inputManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setActionBarContentView(R.layout.compose_message_layout);

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
   
    composeMessageActivity = this;
    composeType = getIntent().getIntExtra(eXoConstants.COMPOSE_TYPE, composeType);
    if (composeType == 0) {
      setTitle("Status Update");
      
      addActionBarItem();
      getActionBar().getItem(0).setDrawable(R.drawable.gd_action_bar_take_photo);
//      addActionBarItem(Type.TakePhoto, R.drawable.gd_action_bar_take_photo);
    } else {
      setTitle("Comment");
    }
    initComponents();

  }

  private void initComponents() {
    composeEditText = (EditText) findViewById(R.id.compose_text_view);
    composeEditText.requestFocus();
    inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
    inputManager.showSoftInput(composeEditText, InputMethodManager.SHOW_FORCED);
    inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,
                                 InputMethodManager.HIDE_IMPLICIT_ONLY);
    
//    imageLayoutWrap = (LinearLayout) findViewById(R.id.compose_image_wrap);

    sendButton = (Button) findViewById(R.id.compose_send_button);
    sendButton.setOnClickListener(this);

    cancelButton = (Button) findViewById(R.id.compose_cancel_button);
    cancelButton.setOnClickListener(this);

  }
  
  private void hideKeyBoard() {
    inputManager.hideSoftInputFromInputMethod(composeEditText.getWindowToken(),
                                              InputMethodManager.HIDE_IMPLICIT_ONLY);
    inputManager.toggleSoftInput(0, 0);
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    // TODO Auto-generated method stub
    switch (position) {
    case -1:
      finish();
      break;
      
    case 0:
   // new AddPhotoDialog(this).show();
      break;
    }

//    return super.onHandleActionBarItemClick(item, position);
    return true;
  }

  public void onClick(View view) {
    if (view == sendButton) {
      if (composeType == 0) {

      } else {

      }

    }

    if (view == cancelButton) {
      hideKeyBoard();
      composeMessageActivity = null;
      finish();
    }
  }

  public void finishMe() {
//    GDActivity.TYPE = 1;
    hideKeyBoard();
    if (composeType == 0) {
      Intent intent = new Intent(composeMessageActivity, AsyncImageViewListActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
    } else {
      Intent intent = new Intent(composeMessageActivity, ActivityStreamDisplay.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
    }
    composeMessageActivity = null;
  }

  @Override
  public void onBackPressed() {
    // TODO Auto-generated method stub
    super.onBackPressed();
    composeMessageActivity = null;
    finish();

  }

  private void initCamera() {
    sdcard_temp_dir = Environment.getExternalStorageDirectory() + File.separator
        + PhotoUltils.getDateFormat() + ".jpg";
    Intent takePictureFromCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    takePictureFromCameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                                         Uri.fromFile(new File(sdcard_temp_dir)));
    startActivityForResult(takePictureFromCameraIntent, eXoConstants.TAKE_PICTURE_WITH_CAMERA);
  }

  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == eXoConstants.TAKE_PICTURE_WITH_CAMERA) {
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
