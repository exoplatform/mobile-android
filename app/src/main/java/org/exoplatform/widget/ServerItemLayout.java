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
package org.exoplatform.widget;

import org.exoplatform.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ServerItemLayout extends RelativeLayout {

  public TextView  serverName;

  public TextView  serverUrl;

  public LinearLayout      layout;

  public ServerItemLayout(Context context) {
    super(context);
    LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View  view = inflate.inflate(R.layout.server_list_item_for_setting, this);
    layout = (LinearLayout) view.findViewById(R.id.server_list_item_wrap);
    serverName = (TextView) view.findViewById(R.id.TextView_ServerName);
    serverUrl = (TextView) view.findViewById(R.id.TextView_URL);
  }

}
