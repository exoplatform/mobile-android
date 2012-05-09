package org.exoplatform.controller.document;

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

  public ExoFile                      _selectedFile;

  private DocumentActivity            _mContext;

  private DocumentActionDialog        _delegate;

  // Localization string

  private String                      strAddAPhoto   = "";

  private String                      strCopy        = "";

  private String                      strMove        = "";

  private String                      strDelete      = "";

  private String                      strRename      = "";

  private String                      strPaste       = "";

  private String                      strCreateFolder;

  private DocumentActionDescription[] fileActionList = null;

  private DocumentExtendDialog        extendDialog;

  public DocumentActionAdapter(DocumentActivity context, DocumentActionDialog parent, ExoFile file) {
    _mContext = context;
    _delegate = parent;
    _selectedFile = file;

    changeLanguage();

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
        if (pos == 0)// Add Photo
        {
          new AddPhotoDialog(_mContext, _delegate).show();

        } else if (pos == 1)// Copy file
        {
          DocumentHelper.getInstance().setFileCopy(_selectedFile);
          DocumentHelper.getInstance().setFileMove(null);
        } else if (pos == 2)// move file
        {
          DocumentHelper.getInstance().setFileMove(_selectedFile);
          DocumentHelper.getInstance().setFileCopy(null);
        } else if (pos == 3 || pos == 4) {

          if (pos == 4)// Delete file, folder
          {
            String currentFolder = DocumentActivity._documentActivityInstance._fileForCurrentActionBar.currentFolder;

            if (currentFolder.equalsIgnoreCase(_selectedFile.currentFolder)) {
              DocumentActivity._documentActivityInstance._fileForCurrentActionBar = DocumentHelper.getInstance().currentFileMap.getParcelable(DocumentActivity._documentActivityInstance._fileForCurrentActionBar.path);
            }

            DocumentActivity._documentActivityInstance.onLoad(_selectedFile.path,
                                                              _selectedFile.path,
                                                              1);
          } else {
            // Copy file
            ExoFile _fileCopied = DocumentHelper.getInstance().getFileCopy();
            if (_fileCopied != null) {
              String lastPathComponent = ExoDocumentUtils.getLastPathComponent(_fileCopied.path);
              String destinationUrl = _selectedFile.path + "/" + lastPathComponent;

              DocumentActivity._documentActivityInstance.onLoad(_fileCopied.path, destinationUrl, 2);

            }
            ExoFile _fileMoved = DocumentHelper.getInstance().getFileMove();
            if (_fileMoved != null) {
              if (!_fileMoved.path.equalsIgnoreCase(_selectedFile.path)) {
                String lastPathComponent = ExoDocumentUtils.getLastPathComponent(_fileMoved.path);
                String destinationUrl = _selectedFile.path + "/" + lastPathComponent;

                DocumentActivity._documentActivityInstance.onLoad(_fileMoved.path,
                                                                  destinationUrl,
                                                                  3);
              }

            }
            DocumentHelper.getInstance().setFileMove(null);
            DocumentHelper.getInstance().setFileCopy(null);
          }

        } else if (pos == 5)// Rename file
        {
          extendDialog = new DocumentExtendDialog(_mContext, _selectedFile, 5);
          extendDialog.show();

        } else if (pos == 6) { // Create folder
          extendDialog = new DocumentExtendDialog(_mContext, _selectedFile, 6);
          extendDialog.show();
        }

      }

    });

    bindView(rowView, fileActionList[position]);
    return (rowView);

  }

  private void bindView(View view, DocumentActionDescription fileAction) {
    TextView label = (TextView) view.findViewById(R.id.label);
    label.setText(fileAction.actionName);
    ImageView icon = (ImageView) view.findViewById(R.id.icon);
    icon.setImageResource(fileAction.imageID);

    if (_selectedFile.isFolder) {
      if (fileAction.actionName.equalsIgnoreCase(strCopy)
          || fileAction.actionName.equalsIgnoreCase(strMove)
          || (fileAction.actionName.equalsIgnoreCase(strPaste) && (DocumentHelper.getInstance()
                                                                                 .getFileCopy() == null && DocumentHelper.getInstance()
                                                                                                                         .getFileMove() == null))) {

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

    return fileActionList.length;
  }

  // Set language
  public void changeLanguage() {
    Resources resource = _mContext.getResources();

    strAddAPhoto = resource.getString(R.string.AddAPhoto);
    strCopy = resource.getString(R.string.Copy);
    strMove = resource.getString(R.string.Move);
    strDelete = resource.getString(R.string.Delete);
    strRename = resource.getString(R.string.Rename);
    strPaste = resource.getString(R.string.Paste);
    strCreateFolder = resource.getString(R.string.CreateFolder);

    fileActionList = new DocumentActionDescription[] {
        new DocumentActionDescription(strAddAPhoto, R.drawable.documentactionpopupphotoicon),
        new DocumentActionDescription(strCopy, R.drawable.documentactionpopupcopyicon),
        new DocumentActionDescription(strMove, R.drawable.documentactionpopupcuticon),
        new DocumentActionDescription(strPaste, R.drawable.documentactionpopuppasteicon),
        new DocumentActionDescription(strDelete, R.drawable.documentactionpopupdeleteicon),
        new DocumentActionDescription(strRename, R.drawable.documentactionpopuprenameicon),
        new DocumentActionDescription(strCreateFolder, R.drawable.documentactionpopupaddfoldericon) };

  }

}
