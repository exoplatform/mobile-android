package org.exoplatform.controller.setting;

import java.util.ArrayList;

import org.exoplatform.proxy.ServerObj;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class ModifyServerAdapter extends BaseAdapter {
  private Context              mContext;

  private ArrayList<ServerObj> serverList;

  public ModifyServerAdapter(Context context) {
    mContext = context;
    serverList = ServerSettingHelper.getInstance().getServerInfoList();

  }

  @Override
  public int getCount() {
    return serverList.size();
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
  public View getView(int position, View convertView, ViewGroup viewGroup) {
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.serverlistitemforsetting, null);

    ServerObj serverObj = serverList.get(position);

    TextView serverName = (TextView) rowView.findViewById(R.id.TextView_ServerName);
    serverName.setText(serverObj._strServerName);

    TextView txtvUrl = (TextView) rowView.findViewById(R.id.TextView_URL);
    txtvUrl.setText(serverObj._strServerUrl);

    ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
    if (AccountSetting.getInstance().getDomainIndex() == position)
      imgView.setVisibility(View.VISIBLE);
    else
      imgView.setVisibility(View.INVISIBLE);
    return rowView;
  }

}
