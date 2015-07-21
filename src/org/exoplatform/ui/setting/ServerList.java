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

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.ServerItemLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

/**
 * Server list used in setting screen
 */
public class ServerList extends LinearLayout {

    private Handler mHandler = new Handler();

    public ServerList(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ServerList(Context context) {
        super(context);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setServerList();
    }

    /**
     * Populate server list Run only in case of updating new list of server
     */
    public void setServerList() {
        removeAllViews();

        ArrayList<ExoAccount> serverList = ServerSettingHelper.getInstance()
                                                              .getServerInfoList(getContext());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT,
                                                     LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, -1);

        ServerItemLayout serverItemLayout;
        for (int i = 0; i < serverList.size(); i++) {
            serverItemLayout = initServerItem(serverList.get(i), i);
            addView(serverItemLayout, i, layoutParams);
        }
    }

    /**
     * Simply update the newly changed server instead of the whole list of
     * servers
     * 
     * @param operation
     * @param serverIdx
     */
    public void updateServerList(int operation, int serverIdx) {
        if (operation == -1 || serverIdx == -1)
            return;
        Context mContext = getContext();
        ServerItemLayout serverItemLayout;
        LayoutParams layoutParams = new LayoutParams(LayoutParams.FILL_PARENT,
                                                     LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 0, -1);

        ArrayList<ExoAccount> serverList = ServerSettingHelper.getInstance()
                                                              .getServerInfoList(mContext);

        switch (operation) {

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
            Animation deleteAnim = AnimationUtils.loadAnimation(mContext,
                                                                R.anim.anim_right_to_left_reverse);
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
    private ServerItemLayout initServerItem(ExoAccount _serverObj, int serverIdx) {
        final ExoAccount serverObj = _serverObj;
        ServerItemLayout serverItem = new ServerItemLayout(getContext());
        serverItem.serverName.setText(serverObj.accountName);
        serverItem.serverUrl.setText(serverObj.serverUrl);

        serverItem.layout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent next = new Intent(getContext(), ServerEditionActivity.class);
                next.putExtra(ExoConstants.EXO_SERVER_OBJ, serverObj);
                getContext().startActivity(next);
            }
        });

        return serverItem;
    }

}
