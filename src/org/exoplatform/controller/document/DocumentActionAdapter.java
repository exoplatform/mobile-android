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

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DocumentActionAdapter extends BaseAdapter {

  public ExoFile                               _selectedFile;

  private DocumentActivity                     _mContext;

  private DocumentActionDialog                 _delegate;

  // Localization string

  private String                               strAddAPhoto = "";

  private String                               strCopy      = "";

  private String                               strMove      = "";

  private String                               strDelete    = "";

  private String                               strRename    = "";

  private String                               strPaste     = "";

  private String                               strOpenIn    = "";

  private String                               strCreateFolder;

  private DocumentActionDescription[]          fileActions  = null;

  private ArrayList<DocumentActionDescription> fileActionList;

 
  private final static int ADD_PHOTO = 0;
  private final static int COPY_FILE = 1;
  private final static int MOVE_FILE = 2;
  private final static int PASTE = 3;
  private final static int DELETE = 4;
  private final static int RENAME = 5;
  private final static int CREATE_FOLDER = 6;
  

  private DocumentExtendDialog                 extendDialog;

  public DocumentActionAdapter(DocumentActivity context,
                               DocumentActionDialog parent,
                               ExoFile file,
                               boolean isActionBar) {
 
    _mContext = context;
    _delegate = parent;
    _selectedFile = file;
    changeLanguage(isActionBar);

  }

  public void setSelectedFile(ExoFile file) {

    _selectedFile = file;
  }

  public void initCamera() {
    _mContext.takePicture();
  }

  public View getView(int position, View convertView, ViewGroup parent) {

    final int pos = position;
    LayoutInflater inflater = (LayoutInflater) _mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.fileactionitem, parent, false);
    rowView.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        _delegate.dismiss();
        if (pos == ADD_PHOTO)// Add Photo
        {
          new AddPhotoDialog(_mContext, _delegate).show();

        } else if (pos == COPY_FILE)// Copy file
        {
          DocumentHelper.getInstance()._fileCopied = _selectedFile;         
          DocumentHelper.getInstance()._fileMoved = null;
        } else if (pos == MOVE_FILE)// move file
        {
          DocumentHelper.getInstance()._fileMoved = _selectedFile;
          DocumentHelper.getInstance()._fileCopied = null;
        } else if (pos == PASTE || pos == DELETE) {

          if (pos == DELETE)// Delete file, folder
          {
            String currentFolder = DocumentActivity._documentActivityInstance._fileForCurrentActionBar.currentFolder;

            if (currentFolder.equalsIgnoreCase(_selectedFile.currentFolder)) {
              DocumentActivity._documentActivityInstance._fileForCurrentActionBar = DocumentHelper.getInstance().currentFileMap.getParcelable(DocumentActivity._documentActivityInstance._fileForCurrentActionBar.path);
            }

            DocumentActivity._documentActivityInstance.onLoad(_selectedFile.path,
                                                              _selectedFile.path,
                                                              DocumentLoadTask.ACTION_DELETE);
          } else {
            // Paste file
            ExoFile _fileCopied = DocumentHelper.getInstance()._fileCopied;
            if (_fileCopied.path != "") {
              String lastPathComponent = ExoDocumentUtils.getLastPathComponent(_fileCopied.path);
              String destinationUrl = _selectedFile.path + "/" + lastPathComponent;

              DocumentActivity._documentActivityInstance.onLoad(_fileCopied.path, destinationUrl, DocumentLoadTask.ACTION_COPY);

            }
            ExoFile _fileMoved = DocumentHelper.getInstance()._fileMoved;
            if (_fileMoved.path != "") {
              if (!_fileMoved.path.equalsIgnoreCase(_selectedFile.path)) {
                String lastPathComponent = ExoDocumentUtils.getLastPathComponent(_fileMoved.path);
                String destinationUrl = _selectedFile.path + "/" + lastPathComponent;

                DocumentActivity._documentActivityInstance.onLoad(_fileMoved.path,
                                                                  destinationUrl,
                                                                  DocumentLoadTask.ACTION_MOVE);
              }

            }
            DocumentHelper.getInstance()._fileCopied = new ExoFile();
            DocumentHelper.getInstance()._fileMoved = new ExoFile();
          }

        } else if (pos == RENAME)// Rename file
        {
          extendDialog = new DocumentExtendDialog(_mContext, _selectedFile, RENAME);
          extendDialog.show();

        } else if (pos == CREATE_FOLDER) { // Create folder
          extendDialog = new DocumentExtendDialog(_mContext, _selectedFile, CREATE_FOLDER);
          extendDialog.show();
        } else if (pos == 7) { // Open file in
          ExoDocumentUtils.fileOpen(_mContext,
                                    _selectedFile.nodeType,
                                    _selectedFile.path,
                                    _selectedFile.name);
        }

      }

    });

    bindView(rowView, fileActionList.get(position));
    return (rowView);

  }

  private void bindView(View view, DocumentActionDescription fileAction) {
    TextView label = (TextView) view.findViewById(R.id.label);
    label.setText(fileAction.actionName);
    ImageView icon = (ImageView) view.findViewById(R.id.icon);
    icon.setImageResource(fileAction.imageID);

    if (_selectedFile.isFolder) {
      if (fileAction.actionName.equalsIgnoreCase(strOpenIn)
          || (fileAction.actionName.equalsIgnoreCase(strPaste) && (DocumentHelper.getInstance()._fileCopied.path == "" && DocumentHelper.getInstance()._fileMoved.path == ""))) {

        label.setTextColor(android.graphics.Color.GRAY);
        view.setEnabled(false);
      }
    } else {
      if (fileAction.actionName.equalsIgnoreCase(strAddAPhoto)
          || fileAction.actionName.equalsIgnoreCase(strPaste)
          || fileAction.actionName.equalsIgnoreCase(strRename)
          || fileAction.actionName.equalsIgnoreCase(strCreateFolder)) {

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
    Resources resource = _mContext.getResources();

    strAddAPhoto = resource.getString(R.string.AddAPhoto);
    strCopy = resource.getString(R.string.Copy);
    strMove = resource.getString(R.string.Move);
    strDelete = resource.getString(R.string.Delete);
    strRename = resource.getString(R.string.Rename);
    strPaste = resource.getString(R.string.Paste);
    strCreateFolder = resource.getString(R.string.CreateFolder);
    strOpenIn = resource.getString(R.string.OpenIn);

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
