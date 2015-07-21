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
package org.exoplatform.accountswitcher;

//import greendroid.widget.AsyncImageView;

import java.util.ArrayList;
import java.util.Locale;

import org.exoplatform.R;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.utils.SocialActivityUtil;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Sep 3, 2014  
 */
public class AccountListAdapter extends BaseAdapter {
  
  private ArrayList<ExoAccount> mAccountList;
  
  private final String TAG = "eXo____AccountListAdapter____";
  
  public AccountListAdapter(Context ctx) {
    mAccountList = ServerSettingHelper.getInstance().getServerInfoList(ctx);
  }

  @Override
  public int getCount() {
    return mAccountList.size();
  }

  @Override
  public Object getItem(int index) {
    return mAccountList.get(index);
  }

  @Override
  public long getItemId(int index) {
    return index;
  }

  @Override
  public View getView(int index, View convertView, ViewGroup parent) {
    ViewHolder holder;
    Context mContext = parent.getContext();
    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      convertView = inflater.inflate(R.layout.account_switcher_item_layout, parent, false);
      holder = new ViewHolder();
      holder.accountName = (TextView)convertView.findViewById(R.id.account_name_textview);
      holder.accountServerURL = (TextView)convertView.findViewById(R.id.account_server_textview);
      holder.userFullName = (TextView)convertView.findViewById(R.id.account_user_fullname_textview);
      holder.accountUsername = (TextView)convertView.findViewById(R.id.account_username_textview);
      holder.connectionStatus = (TextView)convertView.findViewById(R.id.account_connection_status_textview);
      holder.userAvatar = (ImageView)convertView.findViewById(R.id.account_avatar_imageview);
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder)convertView.getTag();
    }

    ExoAccount account = mAccountList.get(index);
    holder.accountName.setText(account.accountName.toUpperCase(Locale.getDefault()));
    holder.accountServerURL.setText(account.serverUrl);
    holder.accountUsername.setText(account.username);
    holder.userFullName.setText(account.userFullName);
    String connStatus;
    if (String.valueOf(index).equals(AccountSetting.getInstance().getDomainIndex())) {
      // load 'Connected' label from resources
      connStatus = mContext.getResources().getString(R.string.Connected);
      holder.connectionStatus.setVisibility(View.VISIBLE);
    } else {
      if (account.lastLoginDate == -1) {
        // last login date unknown, display nothing
        connStatus = "";
        holder.connectionStatus.setVisibility(View.GONE);
      } else {
        // load 'LastLoginDate' label from resources
        connStatus = mContext.getResources().getString(R.string.LastLoginDate);
        // append the date written in words
        connStatus = connStatus+": "+SocialActivityUtil.getPostedTimeString(mContext, account.lastLoginDate);
        holder.connectionStatus.setVisibility(View.VISIBLE);
      }
    }
    holder.connectionStatus.setText(connStatus);
    
    if ("".equalsIgnoreCase(account.avatarUrl)) {
      // no avatar URL, load a standard image
      Picasso.with(mContext).load(R.drawable.default_avatar).resizeDimen(R.dimen.account_list_avatar_size,R.dimen.account_list_avatar_size).centerCrop().into(holder.userAvatar);
    } else {
      // load the avatar from its URL
      Picasso.with(mContext).load(account.avatarUrl).resizeDimen(R.dimen.account_list_avatar_size,R.dimen.account_list_avatar_size).centerCrop().into(holder.userAvatar);
    }
    
    return convertView;
  }
  
  static class ViewHolder {
    TextView accountName;
    TextView accountServerURL;
    TextView userFullName;
    TextView accountUsername;
    TextView connectionStatus; // contains either the last login date or the label 'connected'
    ImageView userAvatar;
  }
  
}
