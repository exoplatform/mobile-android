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

import java.util.List;

import org.exoplatform.R;
import org.exoplatform.model.ExoFile;
import org.exoplatform.ui.DocumentActionDialog;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.utils.CompatibleFileOpen.FileOpenRequest;
import org.exoplatform.utils.CompatibleFileOpen.FileOpenRequestResult;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.widget.UnreadableFileDialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class DocumentAdapter extends BaseAdapter {

  private List<ExoFile>       _documentList;

  private DocumentActivity    _mContext;

  public DocumentActionDialog _documentActionDialog;

  public DocumentAdapter(DocumentActivity context, List<ExoFile> list) {

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
    // TODO use ViewHolder pattern
    LayoutInflater inflater = (LayoutInflater) _mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    final ExoFile myFile = _documentList.get(pos);

    if ("".equals(myFile.name) && "".equals(myFile.path)) {
      convertView = inflater.inflate(R.layout.gadget_tab_layout, parent, false);
      TextView textViewTabTitle = (TextView) convertView.findViewById(R.id.textView_Tab_Title);
      switch (myFile.driveName) {
      case ExoConstants.DOCUMENT_PERSONAL_DRIVER:
        textViewTabTitle.setText(_mContext.getResources().getString(R.string.Personal));
        break;
      case ExoConstants.DOCUMENT_GROUP_DRIVER:
        textViewTabTitle.setText(_mContext.getResources().getString(R.string.Group));
        break;
      case ExoConstants.DOCUMENT_GENERAL_DRIVER:
        textViewTabTitle.setText(_mContext.getResources().getString(R.string.General));
        break;
      }
      return (convertView);
    } else {
      convertView = inflater.inflate(R.layout.fileitem, parent, false);

      ImageButton btnAction = (ImageButton) convertView.findViewById(R.id.Button_FileAction);
      ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
      TextView lb = (TextView) convertView.findViewById(R.id.label);
      // using the natural name here is possible
      lb.setText(myFile.getName());

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
            if (ExoDocumentUtils.isForbidden(myFile.nodeType)) {
              new UnreadableFileDialog(_mContext, null).show();
            } else {
              FileOpenRequest fileOpenReq = ExoDocumentUtils.fileOpen(_mContext, myFile.nodeType, myFile.path, myFile.name);
              if (fileOpenReq.mResult == FileOpenRequestResult.EXTERNAL) {
                DocumentActivity._documentActivityInstance.mFileOpenController = fileOpenReq.mFileOpenController;
              }
            }
          } else {
            DocumentActivity._documentActivityInstance.loadFolderContent(myFile);
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
