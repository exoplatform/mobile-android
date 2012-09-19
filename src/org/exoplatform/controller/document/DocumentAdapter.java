package org.exoplatform.controller.document;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.ui.DocumentActionDialog;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.widget.UnreadableFileDialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DocumentAdapter extends BaseAdapter {
  private ArrayList<ExoFile>  _documentList;

  private DocumentActivity    _mContext;

  public DocumentActionDialog _documentActionDialog;

  public DocumentAdapter(DocumentActivity context, ArrayList<ExoFile> list) {

    _mContext = context;
    _documentList = list;

  }

  @Override
  public int getCount() {
    return _documentList.size();
  }

  @Override
  public Object getItem(int pos) {
    return pos;
  }

  @Override
  public long getItemId(int pos) {
    return pos;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    final int pos = position;
    LayoutInflater inflater = (LayoutInflater) _mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    // View rowView = inflater.inflate(R.layout.fileitem, parent, false);

    final ExoFile myFile = _documentList.get(pos);

    if ("".equals(myFile.name) && "".equals(myFile.path)) {
      convertView = inflater.inflate(R.layout.gadget_tab_layout, parent, false);
      TextView textViewTabTitle = (TextView) convertView.findViewById(R.id.textView_Tab_Title);
      if (myFile.driveName.equals(ExoConstants.DOCUMENT_PERSONAL_DRIVER))
        textViewTabTitle.setText(_mContext.getResources().getString(R.string.Personal));
      else if (myFile.driveName.equals(ExoConstants.DOCUMENT_GROUP_DRIVER))
        textViewTabTitle.setText(_mContext.getResources().getString(R.string.Group));
      else if (myFile.driveName.equals(ExoConstants.DOCUMENT_GENERAL_DRIVER))
        textViewTabTitle.setText(_mContext.getResources().getString(R.string.General));
      return (convertView);
    } else {
      convertView = inflater.inflate(R.layout.fileitem, parent, false);

      Button btnAction = (Button) convertView.findViewById(R.id.Button_FileAction);
      ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
      TextView lb = (TextView) convertView.findViewById(R.id.label);
      lb.setText(myFile.name);

      final ExoFile file = DocumentActivity._documentActivityInstance._fileForCurrentActionBar;
      if ("".equals(myFile.currentFolder)) {
        /*
         * If current folder is null, make the action button is invisible
         */
        btnAction.setVisibility(View.INVISIBLE);

      }

      if (position == 0) {
        if (_documentList.size() == 1)
          convertView.setBackgroundResource(R.drawable.dashboard_single_background_shape);
        else {
          convertView.setBackgroundResource(R.drawable.dashboard_top_background_shape);
        }
      } else {
        if (position + 1 == _documentList.size())
          convertView.setBackgroundResource(R.drawable.dasboard_bottom_background_shape);
        else {
          ExoFile previousItem = _documentList.get(position - 1);
          ExoFile nextItem = _documentList.get(position + 1);

          if ("".equals(previousItem.name) && "".equals(previousItem.path)
              && "".equals(nextItem.name) && "".equals(nextItem.path)) {
            convertView.setBackgroundResource(R.drawable.dashboard_single_background_shape);
          } else if ("".equals(previousItem.name) && "".equals(previousItem.path)) {
            convertView.setBackgroundResource(R.drawable.dashboard_top_background_shape);
          } else if ("".equals(nextItem.name) && "".equals(nextItem.path))
            convertView.setBackgroundResource(R.drawable.dasboard_bottom_background_shape);
          else
            convertView.setBackgroundResource(R.drawable.dashboard_middle_background_shape);
        }

      }

      if (!myFile.isFolder) {
        btnAction.setVisibility(View.VISIBLE);

        icon.setImageResource(ExoDocumentUtils.getIconFromType(myFile.nodeType));

      } else {
        icon.setImageResource(R.drawable.documenticonforfolder);
      }

      convertView.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {

          if (!myFile.isFolder) {
            /*
             * Open file with compatible application
             */
            if (ExoDocumentUtils.isFileReadable(myFile.nodeType)) {
              ExoDocumentUtils.fileOpen(_mContext, myFile.nodeType, myFile.path, myFile.name);
            } else {
              new UnreadableFileDialog(_mContext, null).show();
            }
          } else {
            DocumentActivity._documentActivityInstance._fileForCurrentActionBar = myFile;
            /*
             * Put the selected file and its parent to mapping dictionary
             */
            DocumentHelper.getInstance().currentFileMap.putParcelable(myFile.path, file);
            DocumentActivity._documentActivityInstance.onLoad(myFile.path,
                                                              null,
                                                              DocumentActivity.ACTION_DEFAULT);
          }
        }
      });

      btnAction.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {
          ExoFile file = _documentList.get(pos);
          _documentActionDialog = new DocumentActionDialog(_mContext, file, false);
          _documentActionDialog.myFile = file;
          _documentActionDialog._documentActionAdapter.setSelectedFile(file);
          _documentActionDialog._documentActionAdapter.notifyDataSetChanged();
          _documentActionDialog.setTileForDialog(file.name);
          _documentActionDialog.show();

        }
      });
      return convertView;
    }
  }
}
