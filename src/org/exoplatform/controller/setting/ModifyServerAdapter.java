package org.exoplatform.controller.setting;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ModifyServerAdapter extends BaseAdapter {
  private Context              mContext;

  private ArrayList<ServerObjInfo> serverList;

  public ModifyServerAdapter(Context context) {
    mContext = context;
    serverList = ServerSettingHelper.getInstance().getServerInfoList();

  }

//  @Override
  public int getCount() {
    return serverList.size();
  }

//  @Override
  public Object getItem(int pos) {
    return pos;
  }

//  @Override
  public long getItemId(int pos) {
    return pos;
  }

//  @Override
  public View getView(int position, View convertView, ViewGroup viewGroup) {
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.server_list_item_for_setting, null);

    ServerObjInfo serverObj = serverList.get(position);

    TextView serverName = (TextView) rowView.findViewById(R.id.TextView_ServerName);
    serverName.setText(serverObj.serverName);

    TextView txtvUrl = (TextView) rowView.findViewById(R.id.TextView_URL);
    txtvUrl.setText(serverObj.serverUrl);

    ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
    if (Integer.valueOf(AccountSetting.getInstance().getDomainIndex()) == position)
      imgView.setVisibility(View.VISIBLE);
    else
      imgView.setVisibility(View.INVISIBLE);
    return rowView;
  }

}
