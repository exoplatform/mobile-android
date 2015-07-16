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
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.DocumentHelper;
import org.exoplatform.ui.DocumentActionDialog;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.Utils;
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
        return Utils.getSize(_documentList);
    }

    @Override
    public Object getItem(int pos) {
        return pos;
    }

    @Override
    public long getItemId(int pos) {
        return pos;
    }

  private static final int VIEW_TYPE_DRIVE_ITEM   = 0;

  private static final int VIEW_TYPE_FILEITEM = 1;

  @Override
  public int getViewTypeCount() {
    // there are 2 type, for gadget tab and for file item tab;
    return 2;
  }

  @Override
  public int getItemViewType(int position) {
    int ret = VIEW_TYPE_DRIVE_ITEM;
    final ExoFile myFile = Utils.getItem(_documentList, position);
    if (myFile == null || ("".equals(myFile.name) && "".equals(myFile.path))) {
      ret = VIEW_TYPE_DRIVE_ITEM;
    } else {
      ret = VIEW_TYPE_FILEITEM;
    }
    return ret;
  }
  
  static class DriveHolder {
    TextView textViewTabTitle;
  }
  static class FileHolder {
    Button btnAction;
    ImageView icon;
    TextView lb;
  }
  
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    final int pos = position;
    LayoutInflater inflater = (LayoutInflater) _mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    final ExoFile myFile = Utils.getItem(_documentList, pos);
    if (myFile == null)
      return null;
    int viewType = getItemViewType(position);
    if (viewType == VIEW_TYPE_DRIVE_ITEM) {
      DriveHolder holder;
      if (convertView == null) {
        convertView = inflater.inflate(R.layout.gadget_tab_layout, parent, false);
        holder = new DriveHolder();
        holder.textViewTabTitle = (TextView) convertView.findViewById(R.id.textView_Tab_Title);
        convertView.setTag(holder);
      } else {
        holder = (DriveHolder) convertView.getTag();
      }
      if (ExoConstants.DOCUMENT_PERSONAL_DRIVER.equals(myFile.driveName))
        holder.textViewTabTitle.setText(_mContext.getResources().getString(R.string.Personal));
      else if (ExoConstants.DOCUMENT_GROUP_DRIVER.equals(myFile.driveName))
        holder.textViewTabTitle.setText(_mContext.getResources().getString(R.string.Group));
      else if (ExoConstants.DOCUMENT_GENERAL_DRIVER.equals(myFile.driveName))
        holder.textViewTabTitle.setText(_mContext.getResources().getString(R.string.General));
      return (convertView);
    } else {
      FileHolder holder;
      if (convertView == null) {
        convertView = inflater.inflate(R.layout.fileitem, parent, false);

        holder = new FileHolder();
        holder.btnAction = (Button) convertView.findViewById(R.id.Button_FileAction);
        holder.icon = (ImageView) convertView.findViewById(R.id.icon);
        holder.lb = (TextView) convertView.findViewById(R.id.label);
        convertView.setTag(holder);
      } else {
        holder = (FileHolder) convertView.getTag();
      }

      // using the natural name here is possible
      holder.lb.setText(myFile.getName());
      DocumentActivity docAct = Utils.getVal(DocumentActivity._documentActivityInstance);
      final ExoFile file = docAct == null ? null : docAct._fileForCurrentActionBar;
      if ("".equals(myFile.currentFolder)) {
        /*
         * If current folder is null, make the action button is invisible
         */
        holder.btnAction.setVisibility(View.INVISIBLE);

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

          if ("".equals(previousItem.name) && "".equals(previousItem.path) && "".equals(nextItem.name)
              && "".equals(nextItem.path)) {
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
        holder.btnAction.setVisibility(View.VISIBLE);

        holder.icon.setImageResource(ExoDocumentUtils.getIconFromType(myFile.nodeType));

      } else {
        holder.icon.setImageResource(R.drawable.documenticonforfolder);
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
            /*
             * Put the selected file and its parent to mapping dictionary
             */
            DocumentHelper.getInstance().currentFileMap.putParcelable(myFile.path, file);
            DocumentActivity docAct = Utils.getVal(DocumentActivity._documentActivityInstance);
            if (docAct != null) {
              docAct._fileForCurrentActionBar = myFile;
              docAct.onLoad(myFile.path, null, DocumentActivity.ACTION_DEFAULT);
            }
          }
        }
      });

      holder.btnAction.setOnClickListener(new View.OnClickListener() {

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
