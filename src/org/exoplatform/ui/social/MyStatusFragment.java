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
package org.exoplatform.ui.social;

//import greendroid.widget.LoaderActionBarItem;

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
import android.content.Context;
import android.os.Bundle;
import android.view.View;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jul
 * 23, 2012
 */
public class MyStatusFragment extends ActivityStreamFragment {

  public static MyStatusFragment instance;
  
  @Override
	public int getThisTabId() {
		return SocialTabsActivity.MY_STATUS;
	}

  public static MyStatusFragment getInstance() {
    MyStatusFragment fragment = new MyStatusFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    instance = this;
    fragment_layout = R.layout.social_my_status_layout;
    fragment_list_view_id = R.id.my_status_listview;
    fragment_empty_view_id = R.id.social_my_status_empty_stub;
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
    instance = null;
  }
  public boolean isEmpty() {
	  return (SocialServiceHelper.getInstance().myStatusList == null
	        || SocialServiceHelper.getInstance().myStatusList.size() == 0);
  }
  
  @Override
  public SocialLoadTask getThisLoadTask() {
  	//return new MyStatusLoadTask(getActivity(), SocialTabsActivity.instance.loaderItem);
  	return new MyStatusLoadTask(getActivity());
  }

  public void setListAdapter() {
	  super.setListAdapter(SocialServiceHelper.getInstance().myStatusList);
  }
  
  @Override
	public void setActivityList(ArrayList<SocialActivityInfo> list) {
		if (!isLoadingMoreActivities)
			SocialServiceHelper.getInstance().myStatusList = list;
		else
			SocialServiceHelper.getInstance().myStatusList.addAll(list);
	}

  public class MyStatusLoadTask extends SocialLoadTask {

    /**
    public MyStatusLoadTask(Context context, LoaderActionBarItem loader) {
      super(context, loader);
    }
     **/

    public MyStatusLoadTask(Context context) {
      super(context);
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
		return activityService.getActivityStream(identity, params);
	}

	@Override
	protected ArrayList<SocialActivityInfo> getSocialActivityList() {
		return SocialServiceHelper.getInstance().myStatusList;
	}

  }
}
