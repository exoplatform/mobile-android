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

import greendroid.widget.LoaderActionBarItem;

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

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jul
 * 23, 2012
 */
public class MySpacesFragment extends ActivityStreamFragment {

  public static WeakReference<MySpacesFragment> instance;
  
  @Override
	public int getThisTabId() {
		return SocialTabsActivity.MY_SPACES;
	}

  public static MySpacesFragment getInstance() {
    MySpacesFragment fragment = new MySpacesFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    instance = new WeakReference<MySpacesFragment>(this);
    fragment_layout = R.layout.social_my_spaces_layout;
    fragment_list_view_id = R.id.my_spaces_listview;
    fragment_empty_view_id = R.id.social_my_spaces_empty_stub;
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
    MySpacesFragment frag = Utils.getVal(instance);
    if (frag == this)
      instance = null;
  }
  public boolean isEmpty() {
	  return (SocialServiceHelper.getInstance().mySpacesList == null
	        || SocialServiceHelper.getInstance().mySpacesList.size() == 0);
  }
  
  @Override
  public SocialLoadTask getThisLoadTask() {
    LoaderActionBarItem loader = null;
    SocialTabsActivity act = SocialTabsActivity.getInstance();
    if (act != null) {
     loader = act.loaderItem;
    }
  	return new MySpacesLoadTask(getActivity(), loader);
  }

  public void setListAdapter() {
	  super.setListAdapter(SocialServiceHelper.getInstance().mySpacesList);
  }
  
  public class MySpacesLoadTask extends SocialLoadTask {

    public MySpacesLoadTask(Context context, LoaderActionBarItem loader) {
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
		return activityService.getSpacesActivityStream(identity, params);
	}

	@Override
	protected ArrayList<SocialActivityInfo> getSocialActivityList() {
		return SocialServiceHelper.getInstance().mySpacesList;
	}

  }

	@Override
	public void setActivityList(ArrayList<SocialActivityInfo> list) {
		if (!isLoadingMoreActivities)
			SocialServiceHelper.getInstance().mySpacesList = list;
		else
			SocialServiceHelper.getInstance().mySpacesList.addAll(list);
	}

}
