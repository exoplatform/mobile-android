package org.exoplatform.controller.document;

import org.exoplatform.R;
import org.exoplatform.model.DocumentActionDescription;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.DocumentActionDialog;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoDocumentUtils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DocumentActionAdapter extends BaseAdapter {
  
  public  ExoFile              _selectedFile;
  private Context              _mContext;
  private DocumentActionDialog         _delegate;
  
//Localization string
  String                  strClose         = "";

  String                  strTakePicture   = "";

  String                  strCopy          = "";

  String                  strMove          = "";

  String                  strDelete        = "";

  String                  strRename        = "";

  String                  strPaste         = "";

  DocumentActionDescription[] fileActionList   = null;
  ExoFile                 _fileCopied;
  ExoFile                 _fileMoved;

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

//         ExoFilesController.showProgressDialog(false);
         _delegate.dismiss();
         if (pos == 0)// Take picture
         {
           DocumentActivity._documentActivityInstance.takePicture();
           
         }
         else if (pos == 1)// Copy file
         {
           _fileCopied = _selectedFile;
           _fileMoved = null;
         } 
         else if (pos == 2)// move file
         {
           _fileMoved = _selectedFile;
           _fileCopied = null;
         }
         else if (pos == 3)// Delete file, folder
         {
           _fileCopied = null;
           _fileMoved = null;
           
           boolean deleteFile = DocumentActivity._documentActivityInstance.deleteFile(_selectedFile.urlStr);
           if(deleteFile)
           {
             String url = ExoDocumentUtils.getParentUrl(_selectedFile.urlStr);
             
             DocumentAdapter adapter = DocumentActivity._documentActivityInstance._documentAdapter;
             adapter._documentList = ExoDocumentUtils.getPersonalDriveContent(url);
             adapter.notifyDataSetChanged();
           }
           
         }
         else if (pos == 4)// Paste copy, move file
         {
        // Copy file
           if (_fileCopied != null)
           {
             int index = _fileCopied.urlStr.lastIndexOf("/");
             String lastPathComponent = _fileCopied.urlStr.substring(index);
             String destinationUrl = _selectedFile.urlStr.concat(lastPathComponent);
             DocumentActivity._documentActivityInstance.copyFile(_fileCopied.urlStr, destinationUrl);
             
           }
           if (_fileMoved != null) 
           {
             if (!_fileMoved.urlStr.equalsIgnoreCase(_selectedFile.urlStr)) 
             {
               int index = _fileMoved.urlStr.lastIndexOf("/");
               String lastPathComponent = _fileMoved.urlStr.substring(index);
               String destinationUrl = _selectedFile.urlStr.concat(lastPathComponent);
               DocumentActivity._documentActivityInstance.moveFile(_fileMoved.urlStr, destinationUrl);

             }
             
           }
            
         }
         else// Rename file
         {

         }

       }

     });

     bindView(rowView, fileActionList[position]);
     return (rowView);
     
   }

   private void bindView(View view, DocumentActionDescription fileAction) {
      TextView label = (TextView) view.findViewById(R.id.label);
      label.setText(fileAction.actionName.replace("%20", " "));
      ImageView icon = (ImageView) view.findViewById(R.id.icon);
      icon.setImageResource(fileAction.imageID);
  
      if (_selectedFile.isFolder) {
        if (fileAction.actionName.equalsIgnoreCase(strCopy)
            || fileAction.actionName.equalsIgnoreCase(strMove)) {
          
          label.setTextColor(android.graphics.Color.DKGRAY);
          view.setEnabled(false);
        }
      } else {
        if (fileAction.actionName.equalsIgnoreCase(strTakePicture)
            || fileAction.actionName.equalsIgnoreCase(strPaste)) {
          
          label.setTextColor(android.graphics.Color.DKGRAY);
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
  
      return 5;
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
      
      fileActionList = new DocumentActionDescription[] {
          new DocumentActionDescription(strTakePicture, R.drawable.takephoto),
          new DocumentActionDescription(strCopy, R.drawable.copy),
          new DocumentActionDescription(strMove, R.drawable.move),
          new DocumentActionDescription(strDelete, R.drawable.delete),
          new DocumentActionDescription(strPaste, R.drawable.paste) };

    }

}
