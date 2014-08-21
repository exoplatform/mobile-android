/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ui.setting;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.login.LoginProxy;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.ServerItemLayout;

import java.util.ArrayList;

/**
 * Server list used in setting screen
 */
public class ServerList extends LinearLayout {

  private Context        mContext;

  private AccountSetting mSetting;

  private Handler        mHandler    = new Handler();


  public ServerList(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
  }

  public ServerList(Context context) {
    super(context);
    mContext = context;
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    mSetting    = AccountSetting.getInstance();

    setServerList();
  }

  /**
   * Populate server list
   * Run only in case of updating new list of server
   */
  public void setServerList() {
    removeAllViews();

    ArrayList<ServerObjInfo> serverList = ServerSettingHelper.getInstance().getServerInfoList(mContext);
    LayoutParams layoutParams =
        new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    layoutParams.setMargins(0, 0, 0, -1);

    ServerItemLayout serverItemLayout;
    for (int i = 0; i < serverList.size(); i++) {
      serverItemLayout = initServerItem(serverList.get(i), i);
      addView(serverItemLayout, i, layoutParams);
    }
  }

  /**
   * Simply update the newly changed server instead of the whole list of servers
   *
   * @param operation
   * @param serverIdx
   */
  public void updateServerList(int operation, int serverIdx) {
    if (operation ==-1 || serverIdx == -1) return;

    ServerItemLayout serverItemLayout;
    LayoutParams layoutParams =
        new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
    layoutParams.setMargins(0, 0, 0, -1);

    ArrayList<ServerObjInfo> serverList = ServerSettingHelper.getInstance().getServerInfoList(mContext);

    switch (operation) {
      case ServerEditionActivity.SETTING_ADD:
        serverItemLayout = initServerItem(serverList.get(serverIdx), serverIdx);

        addView(serverItemLayout, serverIdx, layoutParams);
        Animation addAnim = AnimationUtils.loadAnimation(mContext, R.anim.anim_right_to_left);
        addAnim.setDuration(1000);
        serverItemLayout.layout.startAnimation(addAnim);
        break;

      case ServerEditionActivity.SETTING_UPDATE:
        serverItemLayout = initServerItem(serverList.get(serverIdx), serverIdx);

        removeViewAt(serverIdx);
        addView(serverItemLayout, serverIdx, layoutParams);
        Animation updateAnim = new AlphaAnimation(0.4f, 1.0f);
        updateAnim.setDuration(1000);
        updateAnim.setFillAfter(true);
        serverItemLayout.layout.startAnimation(updateAnim);
        break;

      case ServerEditionActivity.SETTING_DELETE:

        final int _serverIdx = serverIdx;
        serverItemLayout = (ServerItemLayout) getChildAt(serverIdx);
        Animation deleteAnim = AnimationUtils.loadAnimation(mContext, R.anim.anim_right_to_left_reverse);
        deleteAnim.setDuration(1000);
        deleteAnim.setFillAfter(true);
        serverItemLayout.layout.startAnimation(deleteAnim);

        mHandler.postDelayed(new Runnable() {
          @Override
          public void run() {
            removeViewAt(_serverIdx);
          }
        }, deleteAnim.getDuration());
        break;
    }
  }

  /**
   * Generate layout for a server item
   *
   * @param _serverObj
   * @param serverIdx
   * @return
   */
  private ServerItemLayout initServerItem(ServerObjInfo _serverObj, int serverIdx) {
    final ServerObjInfo serverObj = _serverObj;
    ServerItemLayout serverItem = new ServerItemLayout(mContext);
    serverItem.serverName.setText(serverObj.serverName);
    serverItem.serverUrl.setText(serverObj.serverUrl);
    
    // disable this account item if it is selected AND the user is logged in 
    final boolean isDisabled = (Integer.valueOf(mSetting.getDomainIndex()) == serverIdx && LoginProxy.userIsLoggedIn);

    if (isDisabled) {
      AlphaAnimation alpha = new AlphaAnimation(0.3F, 0.3F);
      alpha.setDuration(0);     // Make animation instant
      alpha.setFillAfter(true); // Tell it to persist after the animation ends
      serverItem.layout.startAnimation(alpha);
    }

    serverItem.layout.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (isDisabled) {
          String strCannotEdit = mContext.getString(R.string.CannotEditServer);
          Toast.makeText(mContext, strCannotEdit, Toast.LENGTH_SHORT).show();
          return;
        }

        Intent next  = new Intent(mContext, ServerEditionActivity.class);
        next.putExtra(ExoConstants.SETTING_ADDING_SERVER, false);
        next.putExtra(ExoConstants.EXO_SERVER_OBJ, serverObj);
        mContext.startActivity(next);
      }
    });

    return serverItem;
  }


}
