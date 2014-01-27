package org.exoplatform.controller.document;

import java.util.ArrayList;

import org.exoplatform.poc.tabletversion.R;
import org.exoplatform.model.DocumentActionDescription;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.ui.DocumentActionDialog;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.widget.AddPhotoDialog;
import org.exoplatform.widget.DocumentExtendDialog;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter for menu action of file
 */
public class DocumentActionAdapter extends BaseAdapter {

  public  ExoFile                               _selectedFile;

  private DocumentActivity                     mContext;

  private DocumentActionDialog                 mParentDialog;

  private ArrayList<DocumentActionDescription> mFileActionList;

  private DocumentExtendDialog                 extendDialog;

  public  DocumentActionAdapter(DocumentActivity context, DocumentActionDialog parent,
                               ExoFile file, boolean isActionBar) {

    mContext = context;
    mParentDialog = parent;
    _selectedFile = file;
    mFileActionList = new ArrayList<DocumentActionDescription>();
    //changeLanguage(isActionBar);

    initComponents(isActionBar);
  }

  public void setSelectedFile(ExoFile file) {
    _selectedFile = file;
  }

  public void initCamera() {
    mContext.takePicture();
  }

  public View getView(int position, View convertView, ViewGroup parent) {

    final int actionId = mFileActionList.get(position).actionId;
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.file_menu_item, parent, false);
    rowView.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        mParentDialog.dismiss();

        switch (actionId) {

          case DocumentActivity.ACTION_ADD_PHOTO:
            new AddPhotoDialog(mContext, mParentDialog).show();
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

              mContext.startLoadingDocuments(_fileCopied.path, destinationUrl, DocumentActivity.ACTION_COPY);
            }

            ExoFile _fileMoved = DocumentHelper.getInstance()._fileMoved;
            if (!"".equals(_fileMoved.path)) {
              String lastPathComponent = ExoDocumentUtils.getLastPathComponent(_fileMoved.path);
              String destinationUrl = _selectedFile.path + "/" + lastPathComponent;

              mContext.startLoadingDocuments(_fileMoved.path, destinationUrl, DocumentActivity.ACTION_MOVE);
            }

            DocumentHelper.getInstance()._fileCopied = new ExoFile();
            DocumentHelper.getInstance()._fileMoved = new ExoFile();
            break;

          case DocumentActivity.ACTION_DELETE:
            String currentFolder = mContext._fileForCurrentActionBar.currentFolder;

            if (currentFolder.equalsIgnoreCase(_selectedFile.currentFolder) && _selectedFile.isFolder) {
              mContext._fileForCurrentActionBar = DocumentHelper.getInstance().currentFileMap.getParcelable(mContext._fileForCurrentActionBar.path);
            }

            mContext.startLoadingDocuments(_selectedFile.path, _selectedFile.path,
                DocumentActivity.ACTION_DELETE);
            break;

          case DocumentActivity.ACTION_RENAME:
            extendDialog = new DocumentExtendDialog(mContext, _selectedFile, DocumentActivity.ACTION_RENAME);
            extendDialog.show();
            break;

          case DocumentActivity.ACTION_CREATE:
            extendDialog = new DocumentExtendDialog(mContext, _selectedFile, DocumentActivity.ACTION_CREATE);
            extendDialog.show();
            break;

          case DocumentActivity.ACTION_OPEN_IN:
            ExoDocumentUtils.fileOpen(mContext, _selectedFile.nodeType,
                _selectedFile.path, _selectedFile.name);
            break;

        }

      }

    });

    bindView(rowView, mFileActionList.get(position), actionId);
    return (rowView);
  }

  private void bindView(View view, DocumentActionDescription fileAction, int actionId) {
    TextView label = (TextView) view.findViewById(R.id.label);
    label.setText(fileAction.actionName);
    ImageView icon = (ImageView) view.findViewById(R.id.icon);
    icon.setImageResource(fileAction.imageID);

    /**
     * Disable action view if it can not be removed || actionId == DocumentActivity.ACTION_COPY
     */
    if (!_selectedFile.canRemove &&
        (actionId == DocumentActivity.ACTION_MOVE || actionId == DocumentActivity.ACTION_DELETE
          || actionId == DocumentActivity.ACTION_RENAME
          || (actionId == DocumentActivity.ACTION_PASTE
              && ("".equals(DocumentHelper.getInstance()._fileCopied.path)
                && "".equals(DocumentHelper.getInstance()._fileMoved.path))
             )
        )
      ) {
      label.setTextColor(android.graphics.Color.GRAY);
      view.setEnabled(false);
      return;
    }

    if (_selectedFile.isFolder) {
      if (actionId == DocumentActivity.ACTION_OPEN_IN
          || (actionId == DocumentActivity.ACTION_PASTE
                && ("".equals(DocumentHelper.getInstance()._fileCopied.path) && "".equals(DocumentHelper.getInstance()._fileMoved.path))
             )
          ) {

        label.setTextColor(android.graphics.Color.GRAY);
        view.setEnabled(false);
      }
    } else {
      if (actionId == DocumentActivity.ACTION_ADD_PHOTO
          || actionId == DocumentActivity.ACTION_PASTE
          || actionId == DocumentActivity.ACTION_RENAME
          || actionId == DocumentActivity.ACTION_CREATE) {

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
    return mFileActionList.size();
  }


  private void initComponents(boolean isActionBar) {
    Resources resource = mContext.getResources();

    DocumentActionDescription[] addNewActions = new DocumentActionDescription[] {
        new DocumentActionDescription(DocumentActivity.ACTION_ADD_PHOTO, resource.getString(R.string.AddAPhoto), R.drawable.ic_action_camera),
        new DocumentActionDescription(DocumentActivity.ACTION_CREATE,    resource.getString(R.string.CreateFolder), R.drawable.documentactionpopupaddfoldericon)
    };

    DocumentActionDescription[] normalActions = new DocumentActionDescription[] {
        new DocumentActionDescription(DocumentActivity.ACTION_COPY,    resource.getString(R.string.Copy),   R.drawable.ic_action_copy),
        new DocumentActionDescription(DocumentActivity.ACTION_MOVE,    resource.getString(R.string.Move),   R.drawable.ic_action_cut),
        new DocumentActionDescription(DocumentActivity.ACTION_PASTE,   resource.getString(R.string.Paste),  R.drawable.ic_action_paste),
        new DocumentActionDescription(DocumentActivity.ACTION_DELETE,  resource.getString(R.string.Delete), R.drawable.ic_action_remove),
        new DocumentActionDescription(DocumentActivity.ACTION_RENAME,  resource.getString(R.string.Rename), R.drawable.ic_action_edit),       // old icon : documentactionpopuprenameicon
        new DocumentActionDescription(DocumentActivity.ACTION_OPEN_IN, resource.getString(R.string.OpenIn), R.drawable.ic_action_collection)  // old icon: documenticonforfolder
    };

    DocumentActionDescription[] fileActions = isActionBar ? addNewActions : normalActions;

    for (int i = 0; i < fileActions.length; i++) {
      mFileActionList.add(fileActions[i]);
    }

    /** Remove open in action in case of folder */
    if (!isActionBar && _selectedFile.isFolder) mFileActionList.remove(5);
  }

}
