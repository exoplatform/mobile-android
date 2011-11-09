package org.exoplatform.controller.document;

import org.exoplatform.R;
import org.exoplatform.model.DocumentActionDescription;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.DocumentActionDialog;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.widget.DocumentExtendDialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DocumentActionAdapter extends BaseAdapter {

  public ExoFile                      _selectedFile;

  private Context                     _mContext;

  private DocumentActionDialog        _delegate;

  // Localization string

  private String                      strTakePicture = "";

  private String                      strCopy        = "";

  private String                      strMove        = "";

  private String                      strDelete      = "";

  private String                      strRename      = "";

  private String                      strPaste       = "";

  private String                      strCreateFolder;

  private DocumentActionDescription[] fileActionList = null;

  private ExoFile                     _fileCopied;

  private ExoFile                     _fileMoved;

  private DocumentExtendDialog        extendDialog;

  public DocumentActionAdapter(Context context, DocumentActionDialog parent, ExoFile file) {
    _mContext = context;
    _delegate = parent;
    _selectedFile = file;

    changeLanguage();

  }

  public void setSelectedFile(ExoFile file) {

    _selectedFile = file;
  }

  public View getView(int position, View convertView, ViewGroup parent) {

    final int pos = position;
    LayoutInflater inflater = (LayoutInflater) _mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.fileactionitem, parent, false);
    rowView.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        _delegate.dismiss();
        if (pos == 0)// Take picture
        {
          DocumentActivity._documentActivityInstance.takePicture();

        } else if (pos == 1)// Copy file
        {
          _fileCopied = _selectedFile;
          _fileMoved = null;
        } else if (pos == 2)// move file
        {
          _fileMoved = _selectedFile;
          _fileCopied = null;
        } else if (pos == 3 || pos == 4) {

          if (pos == 4)// Delete file, folder
          {
            _fileCopied = null;
            _fileMoved = null;

            DocumentActivity._documentActivityInstance.onLoad(_selectedFile.urlStr,
                                                              _selectedFile.urlStr,
                                                              1);
          } else {
            // Copy file
            if (_fileCopied != null) {
              int index = _fileCopied.urlStr.lastIndexOf("/");
              String lastPathComponent = _fileCopied.urlStr.substring(index);
              String destinationUrl = _selectedFile.urlStr.concat(lastPathComponent);

              DocumentActivity._documentActivityInstance.onLoad(_fileCopied.urlStr,
                                                                destinationUrl,
                                                                2);

            }
            if (_fileMoved != null) {
              if (!_fileMoved.urlStr.equalsIgnoreCase(_selectedFile.urlStr)) {
                int index = _fileMoved.urlStr.lastIndexOf("/");
                String lastPathComponent = _fileMoved.urlStr.substring(index);
                String destinationUrl = _selectedFile.urlStr.concat(lastPathComponent);

                DocumentActivity._documentActivityInstance.onLoad(_fileMoved.urlStr,
                                                                  destinationUrl,
                                                                  3);

              }

            }
          }

        } else if (pos == 5)// Rename file
        {
          extendDialog = new DocumentExtendDialog(_mContext, _selectedFile, 5);
          extendDialog.show();

        } else if (pos == 6) { //Create folder
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
          || (fileAction.actionName.equalsIgnoreCase(strPaste) && _fileCopied == null && _fileMoved == null)) {

        label.setTextColor(android.graphics.Color.GRAY);
        view.setEnabled(false);
      }
    } else {
      if (fileAction.actionName.equalsIgnoreCase(strTakePicture)
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

    LocalizationHelper local = LocalizationHelper.getInstance();

    strTakePicture = local.getString("TakePicture");
    strCopy = local.getString("Copy");
    strMove = local.getString("Move");
    strDelete = local.getString("Delete");
    strRename = local.getString("Rename");
    strPaste = local.getString("Paste");
    strCreateFolder = local.getString("CreateFolder");

    fileActionList = new DocumentActionDescription[] {
        new DocumentActionDescription(strTakePicture, R.drawable.documentactionpopupphotoicon),
        new DocumentActionDescription(strCopy, R.drawable.documentactionpopupcopyicon),
        new DocumentActionDescription(strMove, R.drawable.documentactionpopupcuticon),
        new DocumentActionDescription(strPaste, R.drawable.documentactionpopuppasteicon),
        new DocumentActionDescription(strDelete, R.drawable.documentactionpopupdeleteicon),
        new DocumentActionDescription(strRename, R.drawable.documentactionpopuprenameicon),
        new DocumentActionDescription(strCreateFolder, R.drawable.documentactionpopupaddfoldericon) };

  }

}
