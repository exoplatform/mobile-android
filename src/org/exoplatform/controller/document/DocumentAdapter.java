package org.exoplatform.controller.document;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ExoFile;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.DocumentActionDialog;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.ui.WebViewActivity;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.widget.UnreadableFileDialog;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class DocumentAdapter extends BaseAdapter {
  public ArrayList<ExoFile>   _documentList;

  private Context             _mContext;

  public DocumentActionDialog _documentActionDialog;

  public DocumentAdapter(Context context, ExoFile file) {

    _mContext = context;

    _documentList = ExoDocumentUtils.getPersonalDriveContent(file);

  }

  // @Override
  public int getCount() {
    return _documentList.size();
  }

  // @Override
  public Object getItem(int pos) {
    return pos;
  }

  // @Override
  public long getItemId(int pos) {
    return pos;
  }

  // @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    final int pos = position;
    LayoutInflater inflater = (LayoutInflater) _mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.fileitem, parent, false);

    final ExoFile myFile = _documentList.get(pos);

    if (myFile == null) {
      convertView = inflater.inflate(R.layout.gadget_tab_layout, parent, false);
      TextView textViewTabTitle = (TextView) convertView.findViewById(R.id.textView_Tab_Title);
      LocalizationHelper local = LocalizationHelper.getInstance();
      if (pos == 0)
        textViewTabTitle.setText(local.getString("Personal"));
      else
        textViewTabTitle.setText(local.getString("Group"));

      return (convertView);
    }

    Button btnAction = (Button) rowView.findViewById(R.id.Button_FileAction);
    ImageView icon = (ImageView) rowView.findViewById(R.id.icon);
    TextView lb = (TextView) rowView.findViewById(R.id.label);
    lb.setText(myFile.name);

    ExoFile file = DocumentActivity._documentActivityInstance._fileForCurrentActionBar;
    if (file == null) {

      if (position == 0) {
        if (_documentList.size() == 1)
          rowView.setBackgroundResource(R.drawable.dashboard_single_background_shape);
        else {
          rowView.setBackgroundResource(R.drawable.dashboard_top_background_shape);
        }
      } else {
        ExoFile previousItem = _documentList.get(position - 1);
        if (previousItem == null)
          rowView.setBackgroundResource(R.drawable.dashboard_top_background_shape);
        else if (position + 1 == _documentList.size())
          rowView.setBackgroundResource(R.drawable.dasboard_bottom_background_shape);
        else
          rowView.setBackgroundResource(R.drawable.dashboard_middle_background_shape);
      }

      // btnAction.setVisibility(View.INVISIBLE);

    } else {

      rowView.setBackgroundResource(R.drawable.dashboard_middle_background_shape);
    }

    if (!myFile.isFolder) {

      String iconFileName = ExoDocumentUtils.getFileFolderIconName(myFile.nodeType);
      icon.setImageResource(ExoDocumentUtils.getPicIDFromName(iconFileName));

    } else {
      icon.setImageResource(R.drawable.documenticonforfolder);
    }

    rowView.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        if (!myFile.isFolder) {
          // Action for display file
          if (myFile.nodeType != null
              && (myFile.nodeType.contains("image") || myFile.nodeType.contains("text"))) {
            WebViewActivity._url = myFile.path;
            WebViewActivity._titlebar = myFile.name;
            Intent intent = new Intent(_mContext, WebViewActivity.class);
            _mContext.startActivity(intent);
          } else {
            new UnreadableFileDialog(_mContext).show();
          }

        } else {
          DocumentActivity._documentActivityInstance._fileForCurrentActionBar = myFile;
          DocumentActivity._documentActivityInstance.onLoad(myFile.path, null, 0);
        }

      }
    });

    btnAction.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        ExoFile file = _documentList.get(pos);
        DocumentActivity._documentActivityInstance._fileForCurrnentCell = file;

        if (_documentActionDialog == null)
          _documentActionDialog = new DocumentActionDialog(_mContext, file);
        _documentActionDialog.myFile = file;
        _documentActionDialog._documentActionAdapter.setSelectedFile(file);
        _documentActionDialog._documentActionAdapter.notifyDataSetChanged();
        _documentActionDialog.setTileForDialog(file.name);
        _documentActionDialog.show();

      }
    });

    return (rowView);
  }

}
