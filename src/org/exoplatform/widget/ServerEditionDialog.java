/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.widget;

import org.exoplatform.R;
import org.exoplatform.controller.setting.SettingController;
import org.exoplatform.model.ServerObjInfo;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Aug
 * 31, 2012
 */
public class ServerEditionDialog extends Dialog implements android.view.View.OnClickListener {
  private Button            btnOK;

  private Button            btnDeleteCancel;

  private TextView          txtvTittle;

  private TextView          txtvServerName;

  private TextView          txtvServerUrl;

  private EditText          editTextServerName;

  private EditText          editTextServerUrl;

  private String            serverNameURLInvalid;

  private ServerObjInfo     serverObj;

  private int               serverIndex;

  private Context           mContext;

  private boolean           isNewServer = false;  ;

  private SettingController settingController;

  public ServerEditionDialog(Context context,
                             SettingController controller,
                             ServerObjInfo info,
                             int index) {
    super(context);
    /** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    /** Design the dialog in main.xml file */
    setContentView(R.layout.exolanguagesetting);
    mContext = context;
    this.settingController = controller;
    serverObj = info;
    serverIndex = index;
    init();
  }

  private void init() {
    btnOK = (Button) findViewById(R.id.Button_OK);
    btnOK.setOnClickListener(this);

    btnDeleteCancel = (Button) findViewById(R.id.Button_Delete_Cancel);
    btnDeleteCancel.setOnClickListener(this);

    txtvTittle = (TextView) findViewById(R.id.TextView_Title);

    txtvServerName = (TextView) findViewById(R.id.TextView_Server_Name);
    txtvServerUrl = (TextView) findViewById(R.id.TextView_Server_URL);

    editTextServerName = (EditText) findViewById(R.id.EditText_Server_Name);
    editTextServerUrl = (EditText) findViewById(R.id.EditText_Server_URL);

    // isNewServer = ServerSettingHelper.getInstance().getIsNewServer();
    if (serverObj == null) {
      isNewServer = true;
      serverObj = new ServerObjInfo();
      serverObj._bSystemServer = false;
      serverObj._strServerName = "";
      serverObj._strServerUrl = "";

    }

    editTextServerName.setText(serverObj._strServerName);
    editTextServerUrl.setText(serverObj._strServerUrl);

    changeLanguage();

  }

  @Override
  public void onClick(View view) {
    ServerObjInfo myServerObj = new ServerObjInfo();

    myServerObj._strServerName = editTextServerName.getText().toString();

    myServerObj._strServerUrl = editTextServerUrl.getText().toString();
    if (myServerObj._strServerUrl != null) {
      myServerObj._strServerName = myServerObj._strServerName.trim();
      myServerObj._strServerUrl = myServerObj._strServerUrl.trim();

      if (view.equals(btnOK)) {
        if (isNewServer) {
          if (settingController.onAdd(myServerObj)) {
            settingController.setServerList();
            dismiss();
          }
        } else {
          if (settingController.onUpdate(myServerObj, serverIndex)) {
            settingController.setServerList();
            dismiss();
          }
        }

        // if (settingController.onAccept(myServerObj)) {
        // settingController.setServerList();
        // dismiss();
        // }
      }

      if (view.equals(btnDeleteCancel)) {
        if (!isNewServer) {
          settingController.onDelete(serverIndex);
          settingController.setServerList();
        }

        dismiss();
      }

    } else {
      if (view.equals(btnDeleteCancel)) {
        dismiss();
      }
      Toast.makeText(mContext, serverNameURLInvalid, Toast.LENGTH_SHORT).show();
    }

  }

  private void changeLanguage() {

    Resources resource = mContext.getResources();
    String strTittle = "";
    String strServerName = "";
    String strServerUrl = "";

    String strOKButton = "";
    String strDeleteCancelButton = "";

    if (isNewServer) // New server
    {
      strTittle = resource.getString(R.string.NewServer);
      strDeleteCancelButton = resource.getString(R.string.Cancel);
      strOKButton = resource.getString(R.string.Add);
    } else // Server detail
    {
      strTittle = resource.getString(R.string.ServerDetail);
      strDeleteCancelButton = resource.getString(R.string.Delete);
      strOKButton = resource.getString(R.string.Edit);

    }

    strServerName = resource.getString(R.string.NameOfTheServer);
    strServerUrl = resource.getString(R.string.URLOfTheSerVer);
    serverNameURLInvalid = resource.getString(R.string.SpecialCharacters);

    txtvTittle.setText(strTittle);

    txtvServerName.setText(strServerName);
    txtvServerUrl.setText(strServerUrl);

    btnOK.setText(strOKButton);
    btnDeleteCancel.setText(strDeleteCancelButton);

  }
}
