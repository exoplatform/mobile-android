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
package org.exoplatform.controller.document;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.DocumentActionDescription;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.ui.DocumentActionDialog;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.widget.AddPhotoDialog;
import org.exoplatform.widget.DocumentExtendDialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DocumentActionAdapter extends BaseAdapter {

  public ExoFile                               _selectedFile;

  private DocumentActivity                     mDocumentActivity;

  private DocumentActionDialog                 _delegate;

  private DocumentActionDescription[]          fileActions = null;

  private ArrayList<DocumentActionDescription> fileActionList;

  private DocumentExtendDialog                 extendDialog;

  public DocumentActionAdapter(DocumentActivity context, DocumentActionDialog parent, ExoFile file, boolean isActionBar) {

    mDocumentActivity = context;
    _delegate = parent;
    _selectedFile = file;
    changeLanguage(isActionBar);

  }

  public void setSelectedFile(ExoFile file) {
    _selectedFile = file;
  }

  public void initCamera() {
    mDocumentActivity.takePicture();
  }

  public View getView(int position, View convertView, ViewGroup parent) {

    final int pos = position;
    LayoutInflater inflater = (LayoutInflater) mDocumentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    // TODO use ViewHolder pattern
    View rowView = inflater.inflate(R.layout.fileactionitem, parent, false);
    rowView.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        _delegate.dismiss();

        switch (pos) {
        case DocumentActivity.ACTION_ADD_PHOTO:
          new AddPhotoDialog(mDocumentActivity, _delegate).show();
          break;
        case DocumentActivity.ACTION_COPY:
          DocumentHelper.getInstance()._fileCopied = _selectedFile;
          DocumentHelper.getInstance()._fileMoved = new ExoFile();
          break;

        case DocumentActivity.ACTION_MOVE:
          DocumentHelper.getInstance()._fileMoved = _selectedFile;
          DocumentHelper.getInstance()._fileCopied = new ExoFile();
          break;

        case DocumentActivity.ACTION_PASTE:
          ExoFile _fileCopied = DocumentHelper.getInstance()._fileCopied;
          if (!"".equals(_fileCopied.path)) {
            String lastPathComponent = ExoDocumentUtils.getLastPathComponent(_fileCopied.path);
            String destinationUrl = _selectedFile.path + "/" + lastPathComponent;
            DocumentActivity._documentActivityInstance.pasteFile(_fileCopied, destinationUrl, DocumentActivity.ACTION_COPY);

          }
          ExoFile _fileMoved = DocumentHelper.getInstance()._fileMoved;
          if (!"".equals(_fileMoved.path)) {
            String lastPathComponent = ExoDocumentUtils.getLastPathComponent(_fileMoved.path);
            String destinationUrl = _selectedFile.path + "/" + lastPathComponent;
            DocumentActivity._documentActivityInstance.pasteFile(_fileMoved, destinationUrl, DocumentActivity.ACTION_MOVE);
          }
          DocumentHelper.getInstance()._fileCopied = new ExoFile();
          DocumentHelper.getInstance()._fileMoved = new ExoFile();

          break;
        case DocumentActivity.ACTION_DELETE:
          Context ctx = v.getContext();
          AlertDialog.Builder bld = new AlertDialog.Builder(ctx);
          int selectedTypeStrId = R.string.File;
          if (_selectedFile.isFolder) {
            bld.setTitle(R.string.DeleteConfirmFolderTitle);
            selectedTypeStrId = R.string.Folder;
          } else {
            bld.setTitle(R.string.DeleteConfirmFileTitle);
          }
          bld.setMessage(ctx.getString(R.string.DeleteConfirmMessage, ctx.getString(selectedTypeStrId), _selectedFile.name));
          bld.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
              String currentFolder = DocumentActivity._documentActivityInstance._fileForCurrentActionBar.currentFolder;
              if (currentFolder.equalsIgnoreCase(_selectedFile.currentFolder) && _selectedFile.isFolder) {
                DocumentActivity._documentActivityInstance._fileForCurrentActionBar = 
                    DocumentHelper.getInstance().folderToParentMap.getParcelable(DocumentActivity._documentActivityInstance._fileForCurrentActionBar.path);
              }

              DocumentActivity._documentActivityInstance.deleteFile(_selectedFile);
            }
          });
          bld.setNegativeButton(android.R.string.no, null);
          bld.show();

          break;
        case DocumentActivity.ACTION_RENAME:
          extendDialog = new DocumentExtendDialog(mDocumentActivity, _selectedFile, DocumentActivity.ACTION_RENAME);
          extendDialog.show();

          break;
        case DocumentActivity.ACTION_CREATE:
          extendDialog = new DocumentExtendDialog(mDocumentActivity, _selectedFile, DocumentActivity.ACTION_CREATE);

          extendDialog.show();
          break;
        case DocumentActivity.ACTION_OPEN_IN:
          ExoDocumentUtils.fileOpen(mDocumentActivity, _selectedFile.nodeType, _selectedFile.path, _selectedFile.name);
          break;

        }

      }

    });

    bindView(rowView, fileActionList.get(position), position);
    return (rowView);

  }

  private void bindView(View view, DocumentActionDescription fileAction, int position) {
    TextView label = (TextView) view.findViewById(R.id.label);
    label.setText(fileAction.actionName);
    ImageView icon = (ImageView) view.findViewById(R.id.icon);
    icon.setImageResource(fileAction.imageID);
    /*
     * Disable action view if it can not be removed || position ==
     * DocumentActivity.ACTION_COPY
     */
    if (!_selectedFile.canRemove && (position == DocumentActivity.ACTION_MOVE || position == DocumentActivity.ACTION_DELETE
        || position == DocumentActivity.ACTION_RENAME
        || (position == DocumentActivity.ACTION_PASTE && ("".equals(DocumentHelper.getInstance()._fileCopied.path)
            && "".equals(DocumentHelper.getInstance()._fileMoved.path))))) {
      label.setTextColor(android.graphics.Color.GRAY);
      view.setEnabled(false);
      return;
    }

    if (_selectedFile.isFolder) {
      if (position == DocumentActivity.ACTION_OPEN_IN
          || (position == DocumentActivity.ACTION_PASTE && ("".equals(DocumentHelper.getInstance()._fileCopied.path)
              && "".equals(DocumentHelper.getInstance()._fileMoved.path)))) {

        label.setTextColor(android.graphics.Color.GRAY);
        view.setEnabled(false);
      }
    } else {
      if (position == DocumentActivity.ACTION_ADD_PHOTO || position == DocumentActivity.ACTION_PASTE
          || position == DocumentActivity.ACTION_RENAME || position == DocumentActivity.ACTION_CREATE) {

        label.setTextColor(android.graphics.Color.GRAY);
        view.setEnabled(false);
      }

    }

  }

  public long getItemId(int position) {

    return position;
  }

  public Object getItem(int position) {

    return position;
  }

  public int getCount() {

    return fileActionList.size();
  }

  // Set language
  public void changeLanguage(boolean isAct) {
    Resources resource = mDocumentActivity.getResources();

    String strAddAPhoto = resource.getString(R.string.AddAPhoto);
    String strCopy = resource.getString(R.string.Copy);
    String strMove = resource.getString(R.string.Move);
    String strDelete = resource.getString(R.string.Delete);
    String strRename = resource.getString(R.string.Rename);
    String strPaste = resource.getString(R.string.Paste);
    String strCreateFolder = resource.getString(R.string.CreateFolder);
    String strOpenIn = resource.getString(R.string.OpenIn);

    fileActions = new DocumentActionDescription[] {
        new DocumentActionDescription(strAddAPhoto, R.drawable.documentactionpopupphotoicon),
        new DocumentActionDescription(strCopy, R.drawable.documentactionpopupcopyicon),
        new DocumentActionDescription(strMove, R.drawable.documentactionpopupcuticon),
        new DocumentActionDescription(strPaste, R.drawable.documentactionpopuppasteicon),
        new DocumentActionDescription(strDelete, R.drawable.documentactionpopupdeleteicon),
        new DocumentActionDescription(strRename, R.drawable.documentactionpopuprenameicon),
        new DocumentActionDescription(strCreateFolder, R.drawable.documentactionpopupaddfoldericon),
        new DocumentActionDescription(strOpenIn, R.drawable.documenticonforfolder) };
    int size = fileActions.length;
    int maxChildren = 0;
    if (isAct) {
      maxChildren = size - 1;
    } else {
      if (_selectedFile.isFolder) {
        maxChildren = size - 1;
      } else
        maxChildren = size;
    }
    fileActionList = new ArrayList<DocumentActionDescription>();
    for (int i = 0; i < maxChildren; i++) {
      fileActionList.add(fileActions[i]);
    }

  }
}
