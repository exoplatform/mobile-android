package org.exoplatform.controller.login;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ServerAdapter extends BaseAdapter {
  private ArrayList<ServerObjInfo> serverInfoList;

  private ListView                 _listViewServer;

  private Context                  mContext;

  private int                      _intDomainIndex;

  private String                   _strDomain;

  public ServerAdapter(Context context, ListView lv) {
    mContext = context;
    _listViewServer = lv;
    serverInfoList = ServerSettingHelper.getInstance().getServerInfoList();
    _intDomainIndex = Integer.valueOf(AccountSetting.getInstance().getDomainIndex());
  }

  // @Override
  public int getCount() {
    return serverInfoList.size();
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
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.serverlistitem, parent, false);

    final ServerObjInfo serverObj = serverInfoList.get(pos);
//    RelativeLayout listItem = (RelativeLayout) rowView.findViewById(R.id.authenticate_server_listview_item);
    TextView txtvServerName = (TextView) rowView.findViewById(R.id.TextView_ServerName);
    txtvServerName.setText(serverObj._strServerName);

    TextView txtvUrl = (TextView) rowView.findViewById(R.id.TextView_URL);
    txtvUrl.setText(serverObj._strServerUrl);

    ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
    if (_intDomainIndex == pos) {
      imgView.setBackgroundResource(R.drawable.authenticatecheckmarkiphoneon);
//      listItem.setBackgroundResource(R.drawable.authenticate_server_cell_bg_selected);
    } else {
      imgView.setBackgroundResource(R.drawable.authenticatecheckmarkiphoneoff);
//      listItem.setBackgroundResource(R.drawable.authenticate_server_cell_bg_normal);
    }

    rowView.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {

        View rowView = getView(pos, null, _listViewServer);
//        RelativeLayout listItem = (RelativeLayout) rowView.findViewById(R.id.authenticate_server_listview_item);
        ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
        imgView.setBackgroundResource(R.drawable.authenticatecheckmarkiphoneoff);
//        listItem.setBackgroundResource(R.drawable.authenticate_server_cell_bg_normal);

        _intDomainIndex = pos;
        _strDomain = serverObj._strServerUrl;
        Log.i("Server URL", _strDomain);
        AccountSetting.getInstance().setDomainIndex(String.valueOf(_intDomainIndex));
        AccountSetting.getInstance().setDomainName(_strDomain);

        rowView = getView(_intDomainIndex, null, _listViewServer);
        imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
        imgView.setBackgroundResource(R.drawable.authenticatecheckmarkiphoneon);
//        listItem = (RelativeLayout) rowView.findViewById(R.id.authenticate_server_listview_item);
//        listItem.setBackgroundResource(R.drawable.authenticate_server_cell_bg_selected);

        notifyDataSetChanged();
      }
    });

    return (rowView);

  }
}
