package org.exoplatform.controller.login;

import java.util.ArrayList;

import android.util.Log;
import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Adapter for server list view
 */
public class ServerAdapter extends BaseAdapter {
  private ArrayList<ServerObjInfo> serverInfoList;

  private Context                  mContext;

  private AccountSetting           mSetting;

  private static final String TAG = "eXoServerAdapter";

  public ServerAdapter(Context context) {
    mContext        = context;
    serverInfoList  = ServerSettingHelper.getInstance().getServerInfoList();
    mSetting        = AccountSetting.getInstance();
  }

  @Override
  public int getCount() {
    return serverInfoList.size();
  }

  @Override
  public Object getItem(int pos) {
    return serverInfoList.get(pos);
  }

  @Override
  public long getItemId(int pos) {
    return pos;
  }

  /**
   * This method is called 3 times for each item
   *
   * @param position
   * @param convertView
   * @param parent
   * @return rowView - view for the item, this view will be saved in the RecycleBin
   * and will be passed as convertView for the next getView call
   *
   */
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder;
    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.server_list_item, parent, false);
      holder = new ViewHolder();
      holder.name = (TextView) convertView.findViewById(R.id.TextView_ServerName);
      holder.url  = (TextView) convertView.findViewById(R.id.TextView_URL);
      holder.bg   = (ImageView) convertView.findViewById(R.id.ImageView_Checked);
      convertView.setTag(holder);
    }
    else {
      holder = (ViewHolder) convertView.getTag();
    }

    ServerObjInfo serverObj = serverInfoList.get(position);
    holder.name.setText(serverObj.serverName);
    holder.url.setText(serverObj.serverUrl);
    if (Integer.valueOf(mSetting.getDomainIndex()) == position)
      holder.bg.setBackgroundResource(R.drawable.authenticate_checkmark_on);
    else holder.bg.setBackgroundResource(R.drawable.authenticate_checkmark_off);

    return convertView;
  }

  static class ViewHolder {
    TextView name;
    TextView url;
    ImageView bg;
  }
}
