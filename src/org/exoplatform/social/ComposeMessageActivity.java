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
import org.exoplatform.document.ExoDocumentUtils;
import org.exoplatform.proxy.WebdavMethod;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.core.model.RestActivityImpl;
import org.exoplatform.social.client.core.model.RestCommentImpl;
import org.exoplatform.social.image.SelectedImageActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.PhotoUltils;
import org.exoplatform.utils.UserTask;
import org.exoplatform.widget.MyActionBar;

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

  private String                       composeMessage;

  private String                       sdcard_temp_dir = null;

  private String                       comment;

  private String                       statusUpdate;

  private String                       sendText;

  private String                       cancelText;

  private String                       inputTextWarning;

  private String                       noService;

  private String                       addPhotoTitle;

  private String                       takePhotoText;

  private String                       photoLibraryText;

  private String                       loadingData;

  private Intent                       intent;

  private PostStatusTask               mPostTask;

  private String                       uploadUrl;

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
      if (SocialActivity.socialActivity != null)
        SocialActivity.socialActivity.finish();
      if (ActivityStreamDisplay.activityStreamDisplay != null)
        ActivityStreamDisplay.activityStreamDisplay.finish();
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
        } else {
          onCommentTask();
          try {
            Thread.sleep(1000);
            destroy();
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
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
    } catch (RuntimeException e) {
      Toast toast = Toast.makeText(this, noService, Toast.LENGTH_LONG);
      toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
      toast.show();
    }
  }

  private void onPostTask() {
    if (mPostTask == null || mPostTask.getStatus() == PostStatusTask.Status.FINISHED) {
      mPostTask = (PostStatusTask) new PostStatusTask().execute();
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
    noService = resourceBundle.getString("NoService");
    addPhotoTitle = resourceBundle.getString("AddAPhoto");
    takePhotoText = resourceBundle.getString("TakeAPhoto");
    photoLibraryText = resourceBundle.getString("PhotoLibrary");
    loadingData = resourceBundle.getString("LoadingData");

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
        // Intent intent = new Intent(ComposeMessageActivity.this,
        // SocialImageLibrary.class);
        // startActivity(intent);

      }
      if (view == cancelButton) {
        dismiss();
      }
    }
  }

  private boolean createFolder() {
    String userName = AppController.sharedPreference.getString(AppController.EXO_PRF_USERNAME,
                                                               "exo_prf_username");
    String domain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN,
                                                             "exo_prf_domain");
    uploadUrl = ExoDocumentUtils.getDocumentUrl(userName, domain);
    uploadUrl += "/Public/Mobile";

    HttpResponse response;
    try {

      WebdavMethod copy = new WebdavMethod("MKCOL", uploadUrl);

      response = ExoConnectionUtils.httpClient.execute(copy);
      int status = response.getStatusLine().getStatusCode();
      if (status >= 200 && status < 300) {
        return true;
      } else
        return false;

    } catch (Exception e) {
      return false;
    }
  }

  private class PostStatusTask extends UserTask<Void, Void, Integer> {
    private ProgressDialog _progressDialog;

    @Override
    public void onPreExecute() {
      _progressDialog = ProgressDialog.show(ComposeMessageActivity.this, null, loadingData);
    }

    @Override
    public Integer doInBackground(Void... params) {

      try {
        RestActivityImpl activityImlp = new RestActivityImpl();
        if (sdcard_temp_dir != null) {
          createFolder();
          File file = new File(sdcard_temp_dir);
          String imageDir = uploadUrl + "/" + file.getName();
          ExoDocumentUtils.putFileToServerFromLocal(imageDir, file, "image/jpeg");
          Map<String, String> templateParams = new HashMap<String, String>();
          templateParams.put("image", imageDir);
          activityImlp.setTemplateParams(templateParams);
        }

        activityImlp.setTitle(composeMessage);
        ExoApplicationsController2.activityService.create(activityImlp);
        return 1;
      } catch (RuntimeException e) {
        return 0;
      }

    }

    @Override
    public void onPostExecute(Integer result) {
      if (result == 1) {
        Toast toast = Toast.makeText(ComposeMessageActivity.this,
                                     "post successfully",
                                     Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.show();
      }
      _progressDialog.dismiss();
      destroy();

    }

  }

}
