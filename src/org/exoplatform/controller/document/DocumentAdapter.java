package org.exoplatform.controller.document;

import java.util.List;

import org.exoplatform.R;
import org.exoplatform.model.ExoFile;
import org.exoplatform.ui.DocumentActionDialog;
import org.exoplatform.ui.DocumentActivity;
import org.exoplatform.ui.WebViewActivity;
import org.exoplatform.utils.ExoDocumentUtils;

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
  public List<ExoFile>        _documentList;

  private Context             _mContext;

  public String               _urlStr;

  public DocumentActionDialog _documentActionDialog;

  public DocumentAdapter(Context context, String urlStr) {

    _mContext = context;
    _urlStr = urlStr;

    _documentList = ExoDocumentUtils.getPersonalDriveContent(_urlStr);

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

    ImageView icon = (ImageView) rowView.findViewById(R.id.icon);
    TextView lb = (TextView) rowView.findViewById(R.id.label);
    lb.setText(myFile.fileName);

    if (!myFile.isFolder) {

      String iconFileName = ExoDocumentUtils.getFileFolderIconName(myFile.contentType);
      icon.setImageResource(ExoDocumentUtils.getPicIDFromName(iconFileName));

    } else {
      icon.setImageResource(R.drawable.documenticonforfolder);
    }

    rowView.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        if (!myFile.isFolder) {
          // Action for display file
          // eXoFilesControllerInstance.runOnUiThread(fileItemClickRunnable);
          // new UnreadableFileDialog(_mContext).show();
          WebViewActivity._url = myFile.urlStr;
          WebViewActivity._titlebar = myFile.fileName;
          Intent intent = new Intent(_mContext, WebViewActivity.class);
          _mContext.startActivity(intent);

        } else {

          _urlStr = myFile.urlStr;

          DocumentActivity._documentActivityInstance.onLoad(_urlStr, null, 0);

        }

      }
    });

    Button btn = (Button) rowView.findViewById(R.id.Button_FileAction);
    btn.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        _documentActionDialog = new DocumentActionDialog(_mContext, _documentList.get(pos));

        ExoFile file = _documentList.get(pos);
        _documentActionDialog._documentActionAdapter.setSelectedFile(file);
        _documentActionDialog._documentActionAdapter.notifyDataSetChanged();
        _documentActionDialog.show();

      }
    });

    return (rowView);
  }

}
