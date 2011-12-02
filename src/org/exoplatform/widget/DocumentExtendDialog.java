package org.exoplatform.widget;


import org.exoplatform.R;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.DocumentActivity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class DocumentExtendDialog extends Dialog implements android.view.View.OnClickListener {

  private String   renameTitleStr;

  private String   createTitleStr;

  private String   renameActionTitle;

  private String   createActionTitle;

  private String   okStr;

  private String   cancelStr;

  private String   inputTextWarning;

  private String   folderNameConflict;

  private int      actionId;

  private TextView titleTextView;

  private TextView actionTitleView;

  private EditText actionEditText;

  private String   folderName;

  private Button   okButton;

  private Button   cancelButton;

  private ExoFile  selectedFile;

  private Context  mContext;

  public DocumentExtendDialog(Context context, ExoFile file, int id) {
    super(context);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.document_extend_dialog_layout);
    mContext = context;
    actionId = id;
    selectedFile = file;
    onChangeLanguage();
    titleTextView = (TextView) findViewById(R.id.document_extend_dialog_title);
    actionTitleView = (TextView) findViewById(R.id.document_extend_textview);
    actionEditText = (EditText) findViewById(R.id.document_extend_edittext);
    okButton = (Button) findViewById(R.id.document_extend_ok);
    okButton.setText(okStr);
    okButton.setOnClickListener(this);
    cancelButton = (Button) findViewById(R.id.document_extend_cancel);
    cancelButton.setText(cancelStr);
    cancelButton.setOnClickListener(this);
    initInfomation();
  }

  private void initInfomation() {
    if (actionId == 5) {
      titleTextView.setText(renameTitleStr);
      actionTitleView.setText(renameActionTitle);
    } else if (actionId == 6) {
      titleTextView.setText(createTitleStr);
      actionTitleView.setText(createActionTitle);
    }

  }

  // @Override
  public void onClick(View view) {
    if (view == okButton) {
      folderName = actionEditText.getText().toString();
      if ((folderName != null) && (folderName.length() > 0)) {
        int index = selectedFile.urlStr.lastIndexOf("/");
        String lastPathComponent = selectedFile.urlStr.substring(0, index + 1);
        String currentFolderName = selectedFile.urlStr.substring(index + 1);
        if (folderName.equalsIgnoreCase(currentFolderName)) {
          Toast toast = Toast.makeText(mContext, folderNameConflict, Toast.LENGTH_SHORT);
          toast.setGravity(Gravity.CENTER, 0, 0);
          toast.show();
        } else {
          if (actionId == 5) {

            String destinationUrl = lastPathComponent.concat(folderName);
            DocumentActivity._documentActivityInstance.onLoad(selectedFile.urlStr,
                                                              destinationUrl,
                                                              5);

          } else {
            StringBuffer buffer = new StringBuffer();
            buffer.append(selectedFile.urlStr);
            buffer.append("/");
            buffer.append(folderName);
            String desUrl = buffer.toString();

            DocumentActivity._documentActivityInstance.onLoad(selectedFile.urlStr, desUrl, 6);

          }
          dismiss();
        }

      } else {
        Toast toast = Toast.makeText(mContext, inputTextWarning, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
      }

    }
    if (view == cancelButton) {
      dismiss();
    }

  }

  private void onChangeLanguage() {
    LocalizationHelper local = LocalizationHelper.getInstance();
    renameTitleStr = local.getString("DocumentRenameTitle");
    renameActionTitle = local.getString("DocumentRenameContent");
    createTitleStr = local.getString("DocumentCreateTitle");
    createActionTitle = local.getString("DocumentCreateContent");
    okStr = local.getString("OK");
    cancelStr = local.getString("Cancel");
    inputTextWarning = local.getString("DocumentFolderNameEmpty");
    folderNameConflict = local.getString("DocumentFolderNameConflict");

  }

}
