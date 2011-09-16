package org.exoplatform.controller.setting;

import org.exoplatform.setting.AddEditServerDialog;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class ModifyServerListenner implements OnItemClickListener {
  private Context mContext;
  
  public ModifyServerListenner(Context context){
    mContext = context;
  }

  @Override
  public void onItemClick(AdapterView<?> adapter, View view, int pos, long id) {
    AddEditServerDialog.isNewServer = false;
    AddEditServerDialog.selectedServerIndex = pos;
    AddEditServerDialog customizeDialog = new AddEditServerDialog(mContext);
    customizeDialog.show();
  }

}
