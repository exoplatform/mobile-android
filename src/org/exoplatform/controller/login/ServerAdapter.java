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
    Log.i(TAG, "getView: " + position + " - " + convertView);
    int domainIdx = Integer.valueOf(mSetting.getDomainIndex());
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.server_list_item, parent, false);
    Log.i(TAG, "rowView after inflation: " + rowView);

    final ServerObjInfo serverObj = serverInfoList.get(position);
    TextView txtvServerName = (TextView) rowView.findViewById(R.id.TextView_ServerName);
    txtvServerName.setText(serverObj.serverName);
    Log.i(TAG, "server: " + serverObj);

    TextView txtvUrl = (TextView) rowView.findViewById(R.id.TextView_URL);
    txtvUrl.setText(serverObj.serverUrl);
    Log.i(TAG, "server url: " + serverObj.serverUrl);
    Log.i(TAG, "username: " + serverObj.username);
    Log.i(TAG, "domainIdx: " + domainIdx);

    ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
    if (domainIdx == position) {
      imgView.setBackgroundResource(R.drawable.authenticate_checkmark_on);
    } else {
      imgView.setBackgroundResource(R.drawable.authenticate_checkmark_off);
    }

    /**
    rowView.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        Log.i(TAG, "rowView - onClick server list item: " + serverObj.serverUrl);

        Log.i(TAG, "rowView - ===> getView: " + pos + " - domainIdx: " + mDomainIdx );
        View rowView = getView(pos, null, _listViewServer);
        ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
        imgView.setBackgroundResource(R.drawable.authenticate_checkmark_off);
        mDomainIdx = pos;

        mSetting.setDomainIndex(String.valueOf(mDomainIdx));
        mSetting.setCurrentServer(serverObj);
        Log.i(TAG, "server: " + mSetting.getCurrentServer());
        Log.i(TAG, "is remember me: " + mSetting.isRememberMeEnabled());
        Log.i(TAG, "server url: " + mSetting.getCurrentServer().serverUrl);
        Log.i(TAG, "user : " + mSetting.getUsername());

        rowView = getView(mDomainIdx, null, _listViewServer);
        imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
        imgView.setBackgroundResource(R.drawable.authenticate_checkmark_on);
        notifyDataSetChanged();
      }
    });
    **/

    return rowView;
  }
}
