package org.exoplatform.controller.setting;

import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.SettingServerEditionDialog;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class ModifyServerListenner implements OnItemClickListener {
  private Context  mContext;

  private ListView listViewServer;

  public ModifyServerListenner(Context context, ListView listView) {
    mContext = context;
    listViewServer = listView;
  }

//  @Override
  public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
    ServerSettingHelper.getInstance().setIsNewServer(false);
    ServerSettingHelper.getInstance().setSelectedServerIndex(pos);
    new SettingServerEditionDialog(mContext, listViewServer).show();
  }

}
