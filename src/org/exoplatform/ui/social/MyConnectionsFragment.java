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
package org.exoplatform.ui.social;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.controller.home.SocialLoadTask;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.common.RealtimeListAccess;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.service.QueryParams;
import org.exoplatform.utils.Utils;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import greendroid.widget.LoaderActionBarItem;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jul
 * 23, 2012
 */
public class MyConnectionsFragment extends ActivityStreamFragment {

  public static WeakReference<MyConnectionsFragment> instance;
  
  @Override
	public int getThisTabId() {
		return SocialTabsActivity.MY_CONNECTIONS;
	}

  public static MyConnectionsFragment getInstance() {
    MyConnectionsFragment fragment = new MyConnectionsFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    instance = new WeakReference<MyConnectionsFragment>(this);
    fragment_layout = R.layout.social_my_connections_layout;
    fragment_list_view_id = R.id.my_connections_listview;
    fragment_empty_view_id = R.id.social_my_connections_empty_stub;
    super.onCreate(savedInstanceState);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setListAdapter();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    MyConnectionsFragment frag = Utils.getVal(MyConnectionsFragment.instance);
    if (frag == this) 
      instance = null;
  }
  
  public boolean isEmpty() {
	  return (SocialServiceHelper.getInstance().myConnectionsList == null
	        || SocialServiceHelper.getInstance().myConnectionsList.size() == 0);
  }
  
  @Override
  public SocialLoadTask getThisLoadTask() {
    LoaderActionBarItem loader = null;
    SocialTabsActivity act = SocialTabsActivity.getInstance();
    if (act != null) {
     loader = act.loaderItem;
    }
  	return new MyConnectionLoadTask(getActivity(), loader);
  }

  public void setListAdapter() {
	  super.setListAdapter(SocialServiceHelper.getInstance().myConnectionsList);
  }

  public class MyConnectionLoadTask extends SocialLoadTask {

    public MyConnectionLoadTask(Context context, LoaderActionBarItem loader) {
      super(context, loader);
    }

    @Override
    public void setResult(ArrayList<SocialActivityInfo> result) {
    	setActivityList(result);
    	setListAdapter();
    	listview.getAutoLoadProgressBar().setVisibility(View.GONE);
    	super.setResult(result);
    }

	@Override
	protected RealtimeListAccess<RestActivity> getRestActivityList(
			RestIdentity identity, QueryParams params)
			throws SocialClientLibException {
		return activityService.getConnectionsActivityStream(identity, params);
	}

	@Override
	protected ArrayList<SocialActivityInfo> getSocialActivityList() {
		return SocialServiceHelper.getInstance().myConnectionsList;
	}

  }

	@Override
	public void setActivityList(ArrayList<SocialActivityInfo> list) {
		if (!isLoadingMoreActivities)
			SocialServiceHelper.getInstance().myConnectionsList = list;
		else
			SocialServiceHelper.getInstance().myConnectionsList.addAll(list);
	}

}
