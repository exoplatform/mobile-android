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

  private ListView                 _listViewServer;

  private Context                  mContext;

  /** current selected server */
  private int                      mDomainIdx;

  private AccountSetting           mSetting;

  private static final String TAG = "eXoServerAdapter";

  public ServerAdapter(Context context, ListView lv) {
    mContext        = context;
    _listViewServer = lv;
    serverInfoList  = ServerSettingHelper.getInstance().getServerInfoList();
    mSetting        = AccountSetting.getInstance();
    mDomainIdx      = Integer.valueOf(mSetting.getDomainIndex());
  }

  // @Override
  public int getCount() {
    return serverInfoList.size();
  }

  // @Override
  public Object getItem(int pos) {
    return serverInfoList.get(pos);
  }

  // @Override
  public long getItemId(int pos) {
    return pos;
  }

  // @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    final int pos = position;
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View rowView = inflater.inflate(R.layout.server_list_item, parent, false);

    final ServerObjInfo serverObj = serverInfoList.get(pos);
    TextView txtvServerName = (TextView) rowView.findViewById(R.id.TextView_ServerName);
    txtvServerName.setText(serverObj.serverName);

    TextView txtvUrl = (TextView) rowView.findViewById(R.id.TextView_URL);
    txtvUrl.setText(serverObj.serverUrl);

    ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
    if (mDomainIdx == pos) {
      imgView.setBackgroundResource(R.drawable.authenticate_checkmark_on);
    } else {
      imgView.setBackgroundResource(R.drawable.authenticate_checkmark_off);
    }

    rowView.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        Log.i(TAG, "onClick server list item: " + serverObj.serverUrl);

        View rowView = getView(pos, null, _listViewServer);
        ImageView imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
        imgView.setBackgroundResource(R.drawable.authenticate_checkmark_off);
        mDomainIdx = pos;

        /* changes setting */
        mSetting.setDomainIndex(String.valueOf(mDomainIdx));
        mSetting.setCurrentServer(serverObj);
        Log.i(TAG, "is remember me: " + mSetting.isRememberMeEnabled());
        Log.i(TAG, "is remember me: " + mSetting.getCurrentServer().serverUrl);

        rowView = getView(mDomainIdx, null, _listViewServer);
        imgView = (ImageView) rowView.findViewById(R.id.ImageView_Checked);
        imgView.setBackgroundResource(R.drawable.authenticate_checkmark_on);
        notifyDataSetChanged();
      }
    });

    return (rowView);

  }
}
