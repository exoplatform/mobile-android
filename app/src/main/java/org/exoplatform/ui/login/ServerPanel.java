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
package org.exoplatform.ui.login;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import android.widget.*;
import org.exoplatform.R;
import org.exoplatform.controller.login.ServerAdapter;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;

import java.util.ArrayList;

/**
 * Represents the server panel in login screen contains a list of server
 */
public class ServerPanel extends LinearLayout implements AdapterView.OnItemClickListener {

  private Context        mContext;

  private AccountSetting mSetting;

  /** list view that contains list of servers */
  private ListView       mServerListView;

  public ServerPanel(Context context) {
    super(context);
    mContext = context;
  }

  public ServerPanel(Context context, AttributeSet attrs) {
    super(context, attrs);
    mContext = context;
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();

    mSetting = AccountSetting.getInstance();

    initSubViews();
  }

  private void initSubViews() {

    mServerListView = (ListView) findViewById(R.id.ListView_Servers);
    mServerListView.setCacheColorHint(Color.TRANSPARENT);
    mServerListView.setFadingEdgeLength(0);
    mServerListView.setDivider(null);
    mServerListView.setDividerHeight(1);

    mServerListView.setOnItemClickListener(this);
    mServerListView.setAdapter(new ServerAdapter(mContext));
  }

  public void turnOn() {
    setVisibility(View.VISIBLE);
    mServerListView.setVisibility(View.VISIBLE);
  }

  public void repopulateServerList() {
    mServerListView.setAdapter(new ServerAdapter(mContext));
  }

  public void turnOff() {
    setVisibility(View.INVISIBLE);
    mServerListView.setVisibility(View.INVISIBLE);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View rowView, int position, long id) {

    int selectedIdx = Integer.valueOf(mSetting.getDomainIndex());

    if (selectedIdx == position) {

      rowView.findViewById(R.id.ImageView_Checked).setBackgroundResource(R.drawable.authenticate_checkmark_off);
      mSetting.setDomainIndex(String.valueOf(-1));
      mSetting.setCurrentAccount(null);
      return;
    }

    int firstVisiblePosition = parent.getFirstVisiblePosition();
    if ((firstVisiblePosition <= selectedIdx) && (selectedIdx <= parent.getLastVisiblePosition()))
      parent.getChildAt(selectedIdx - firstVisiblePosition)
            .findViewById(R.id.ImageView_Checked)
            .setBackgroundResource(R.drawable.authenticate_checkmark_off);

    rowView.findViewById(R.id.ImageView_Checked).setBackgroundResource(R.drawable.authenticate_checkmark_on);

    ArrayList<ExoAccount> serverList = ServerSettingHelper.getInstance().getServerInfoList(mContext);
    mSetting.setDomainIndex(String.valueOf(position));
    mSetting.setCurrentAccount(serverList.get(position));
  }

}
