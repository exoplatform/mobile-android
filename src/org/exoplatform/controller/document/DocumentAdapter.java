package org.exoplatform.controller.document;

import java.util.ArrayList;

import android.util.TypedValue;
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
  private ArrayList<ExoFile>  mDocumentList;

  private DocumentActivity    mContext;

  public DocumentActionDialog mActionDialog;

  public DocumentAdapter(DocumentActivity context, ArrayList<ExoFile> list) {
    mContext      = context;
    mDocumentList = list;
  }

  @Override
  public int getCount() {
    return mDocumentList.size();
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
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    // View rowView = inflater.inflate(R.layout.fileitem, parent, false);

    final ExoFile myFile = mDocumentList.get(pos);

    /** view is tab with text */
    if ("".equals(myFile.name) && "".equals(myFile.path)) {
      convertView = inflater.inflate(R.layout.gadget_tab_layout, parent, false);
      TextView textViewTabTitle = (TextView) convertView.findViewById(R.id.textView_Tab_Title);

      if (myFile.driveName.equals(ExoConstants.DOCUMENT_PERSONAL_DRIVER))
        textViewTabTitle.setText(mContext.getString(R.string.Personal));
      else if (myFile.driveName.equals(ExoConstants.DOCUMENT_GROUP_DRIVER))
        textViewTabTitle.setText(mContext.getString(R.string.Group));
      else if (myFile.driveName.equals(ExoConstants.DOCUMENT_GENERAL_DRIVER))
        textViewTabTitle.setText(mContext.getString(R.string.General));

      return convertView;

    }
    /** view is a folder */
    else {

      convertView = inflater.inflate(R.layout.fileitem, parent, false);

      Button btnAction = (Button)    convertView.findViewById(R.id.Button_FileAction);
      ImageView icon   = (ImageView) convertView.findViewById(R.id.icon);
      TextView lb      = (TextView)  convertView.findViewById(R.id.label);
      lb.setText(myFile.name);
      lb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);


      final ExoFile file = mContext._fileForCurrentActionBar;
      if ("".equals(myFile.currentFolder)) {

        /** If current folder is null, make the action button is invisible */
        btnAction.setVisibility(View.INVISIBLE);
      }

      if (position == 0) {
        if (mDocumentList.size() == 1)
          convertView.setBackgroundResource(R.drawable.dashboard_single_background_shape);
        else {
          convertView.setBackgroundResource(R.drawable.dashboard_top_background_shape);
        }
      } else {
        if (position + 1 == mDocumentList.size())
          convertView.setBackgroundResource(R.drawable.dasboard_bottom_background_shape);
        else {
          ExoFile previousItem = mDocumentList.get(position - 1);
          ExoFile nextItem = mDocumentList.get(position + 1);

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

          /** Open file or folder using appropriate app */
          if (!myFile.isFolder) {
            if (ExoDocumentUtils.isFileReadable(myFile.nodeType)) {
              ExoDocumentUtils.fileOpen(mContext, myFile.nodeType, myFile.path, myFile.name);
            } else {
              new UnreadableFileDialog(mContext, null).show();
            }
          } else {
            mContext._fileForCurrentActionBar = myFile;

            /** Put the selected file and its parent to mapping dictionary */
            DocumentHelper.getInstance().currentFileMap.putParcelable(myFile.path, file);
            mContext.startLoadingDocuments(myFile.path, null, DocumentActivity.ACTION_DEFAULT);
          }
        }
      });

      btnAction.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {
          ExoFile file = mDocumentList.get(pos);
          mActionDialog = new DocumentActionDialog(mContext, file, false);
          mActionDialog.myFile = file;
          mActionDialog._documentActionAdapter.setSelectedFile(file);
          mActionDialog._documentActionAdapter.notifyDataSetChanged();
          mActionDialog.setTileForDialog(file.name);
          mActionDialog.show();
        }
      });
      return convertView;
    }
  }
}
