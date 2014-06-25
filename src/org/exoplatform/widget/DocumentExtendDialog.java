/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.widget;

import org.exoplatform.R;
import org.exoplatform.model.ExoFile;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoUtils;
import org.exoplatform.utils.ExoDocumentUtils;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
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

  private String   inputNameURLInvalid;

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
    if (actionId == DocumentActivity.ACTION_RENAME) {
      titleTextView.setText(renameTitleStr);
      actionTitleView.setText(renameActionTitle);
    } else if (actionId == DocumentActivity.ACTION_CREATE) {
      titleTextView.setText(createTitleStr);
      actionTitleView.setText(createActionTitle);
    }

  }

  public void onClick(View view) {
    if (view.equals(okButton)) {
      folderName = actionEditText.getText().toString();

      if ((folderName != null) && (folderName.length() > 0)) {

        if (actionId == DocumentActivity.ACTION_RENAME) {
          if (DocumentActivity._documentActivityInstance._fileForCurrentActionBar != null) {
            String currentFolder = DocumentActivity._documentActivityInstance._fileForCurrentActionBar.currentFolder;
            String destinationUrl = ExoDocumentUtils.getParentUrl(selectedFile.path) + "/"
                + folderName;
            if (currentFolder.equalsIgnoreCase(selectedFile.currentFolder)) {
              DocumentActivity._documentActivityInstance._fileForCurrentActionBar.name = folderName;
              currentFolder = ExoDocumentUtils.getParentUrl(currentFolder) + "/" + folderName;
              DocumentActivity._documentActivityInstance._fileForCurrentActionBar.currentFolder = currentFolder;
            }
            if (ExoUtils.isDocumentUrlValid(destinationUrl)) {
              DocumentActivity._documentActivityInstance.onLoad(selectedFile.path,
                                                                destinationUrl,
                                                                DocumentActivity.ACTION_RENAME);
            } else {
              Toast toast = Toast.makeText(mContext, inputNameURLInvalid, Toast.LENGTH_SHORT);
              toast.setGravity(Gravity.CENTER, 0, 0);
              toast.show();
            }

          }

        } else {
          String desUrl = selectedFile.path + "/" + folderName;
          if (ExoUtils.isDocumentUrlValid(desUrl)) {
            DocumentActivity._documentActivityInstance.onLoad(selectedFile.path,
                                                              desUrl,
                                                              DocumentActivity.ACTION_CREATE);
          } else {
            Toast toast = Toast.makeText(mContext, inputNameURLInvalid, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
          }

        }
        dismiss();
      } else {
        Toast toast = Toast.makeText(mContext, inputTextWarning, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.BOTTOM, 0, 0);
        toast.show();
      }
    }
    if (view.equals(cancelButton)) {
      dismiss();
    }

  }

  private void onChangeLanguage() {
    Resources res = mContext.getResources();
    renameTitleStr = res.getString(R.string.DocumentRenameTitle);
    renameActionTitle = res.getString(R.string.DocumentRenameContent);
    createTitleStr = res.getString(R.string.DocumentCreateTitle);
    createActionTitle = res.getString(R.string.DocumentCreateContent);
    okStr = res.getString(R.string.OK);
    cancelStr = res.getString(R.string.Cancel);
    inputTextWarning = res.getString(R.string.DocumentFolderNameEmpty);
    inputNameURLInvalid = res.getString(R.string.SpecialCharacters);

  }

}
