package org.exoplatform.widget;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoDocumentUtils;

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
    if (view.equals(okButton)) {
      folderName = actionEditText.getText().toString();

      ArrayList<ExoFile> files = DocumentActivity._documentActivityInstance._documentAdapter._documentList;
      boolean fileExisted = false;
      if (files != null) {
        for (ExoFile file : files) {
          if (file != null) {
            if (file.name.equalsIgnoreCase(folderName)) {
              fileExisted = true;
              break;
            }
          }

        }
      }
      if (fileExisted) {
        Toast toast = Toast.makeText(mContext, folderNameConflict, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        return;
      }

      if ((folderName != null) && (folderName.length() > 0)) {

        if (actionId == 5) {
          if (DocumentActivity._documentActivityInstance._fileForCurrentActionBar != null) {
            String currentFolder = DocumentActivity._documentActivityInstance._fileForCurrentActionBar.currentFolder;
            String destinationUrl = ExoDocumentUtils.getParentUrl(selectedFile.path) + "/"
                + folderName;
            if (currentFolder.equalsIgnoreCase(selectedFile.currentFolder)) {
              DocumentActivity._documentActivityInstance._fileForCurrentActionBar.name = folderName;
              currentFolder = ExoDocumentUtils.getParentUrl(currentFolder) + "/" + folderName;
              DocumentActivity._documentActivityInstance._fileForCurrentActionBar.currentFolder = currentFolder;
            }
            DocumentActivity._documentActivityInstance.onLoad(selectedFile.path, destinationUrl, 5);
          }

        } else {
          String desUrl = selectedFile.path + "/" + folderName;
          DocumentActivity._documentActivityInstance.onLoad(selectedFile.path, desUrl, 6);
        }
        dismiss();
      }

    } else {
      Toast toast = Toast.makeText(mContext, inputTextWarning, Toast.LENGTH_SHORT);
      toast.setGravity(Gravity.CENTER, 0, 0);
      toast.show();
    }
    if (view.equals(cancelButton)) {
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
