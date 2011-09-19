package org.exoplatform.controller.login;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.proxy.ServerObj;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ServerAdapter extends BaseAdapter {
  private ArrayList<ServerObj> serverInfoList;

  private ListView             _listViewServer;

  private Context              mContext;

  private int                  _intDomainIndex;

  private String               _strDomain;

  public ServerAdapter(Context context, ListView lv) {
    mContext = context;
    _listViewServer = lv;
    serverInfoList = ServerSettingHelper.getInstance().getServerInfoList();
    _intDomainIndex = AccountSetting.getInstance().getDomainIndex();
  }

  @Override
  public int getCount() {
    return serverInfoList.size();
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
  public View getView(final int position, View convertView, ViewGroup parent) {
    final int pos = position;
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.serverlistitem, parent, false);

    final ServerObj serverObj = serverInfoList.get(pos);

    TextView txtvServerName = (TextView) rowView.findViewById(R.id.TextView_ServerName);
    txtvServerName.setText(serverObj._strServerName);

    TextView txtvUrl = (TextView) rowView.findViewById(R.id.TextView_URL);
    txtvUrl.setText(serverObj._strServerUrl);

    ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);

    if (_intDomainIndex == pos)
      imgView.setBackgroundResource(R.drawable.authenticatecheckmarkiphoneon);
    else
      imgView.setBackgroundResource(R.drawable.authenticatecheckmarkiphoneoff);

    rowView.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {

        if (_intDomainIndex < 0)
          _intDomainIndex = pos;

        View rowView = getView(_intDomainIndex, null, _listViewServer);

        ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
        imgView.setBackgroundResource(R.drawable.authenticatecheckmarkiphoneoff);

        _intDomainIndex = pos;
        _strDomain = serverObj._strServerUrl;
        AccountSetting.getInstance().setDomainIndex(_intDomainIndex);
        AccountSetting.getInstance().setDomainName(_strDomain);

        rowView = getView(_intDomainIndex, null, _listViewServer);
        imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
        imgView.setBackgroundResource(R.drawable.authenticatecheckmarkiphoneon);

        notifyDataSetChanged();
      }
    });

    return (rowView);

  }

}
