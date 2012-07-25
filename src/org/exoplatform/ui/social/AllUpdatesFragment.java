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

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.controller.home.HomeController;
import org.exoplatform.controller.home.SocialLoadTask;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.SectionListAdapter;
import org.exoplatform.widget.SectionListView;
import org.exoplatform.widget.StandardArrayAdapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jul
 * 23, 2012
 */
public class AllUpdatesFragment extends Fragment {

  private ArrayList<SocialActivityInfo> socialList;

  private HomeController                homeController;

  private SectionListView               listview;

  private View                          emptyStubView;

  private StandardArrayAdapter          arrayAdapter;

  private SectionListAdapter            sectionAdapter;

  private AllUpdateLoadTask             mLoadTask;

  public static AllUpdatesFragment      instance;

  public static AllUpdatesFragment getInstance(HomeController homeController) {
    AllUpdatesFragment fragment = new AllUpdatesFragment();
    fragment.socialList = SocialServiceHelper.getInstance().socialInfoList;
    fragment.homeController = homeController;
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    instance = this;
    onPrepareLoad(false);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.social_all_updates_layout, container, false);
    listview = (SectionListView) view.findViewById(R.id.all_updates_listview);
    listview.setDivider(null);
    listview.setDividerHeight(0);
    listview.setFadingEdgeLength(0);
    listview.setCacheColorHint(Color.TRANSPARENT);
    emptyStubView = ((ViewStub) view.findViewById(R.id.social_all_updates_empty_stub)).inflate();
    ImageView emptyImage = (ImageView) emptyStubView.findViewById(R.id.empty_image);
    emptyImage.setBackgroundResource(R.drawable.icon_for_no_activities);
    TextView emptyStatus = (TextView) emptyStubView.findViewById(R.id.empty_status);
    emptyStatus.setText(getActivity().getString(R.string.EmptyActivity));
    setListAdapter(socialList);

    return view;
  }

  public void onPrepareLoad(boolean isRefresh) {
    // if (socialList == null) {
    // if (SocialServiceHelper.getInstance().activityService == null) {
    // homeController.launchNewsService();
    // } else
    // onLoad();
    // } else {
    // if (isRefresh) {
    // onLoad();
    // }
    // }
    if (isRefresh) {
      onLoad();
      return;
    }

    if (socialList == null || socialList.size() == 0) {
      onLoad();
      return;
    }

  }

  private void onLoad() {
    if (ExoConnectionUtils.isNetworkAvailableExt(getActivity())) {
      if (mLoadTask == null || mLoadTask.getStatus() == AllUpdateLoadTask.Status.FINISHED) {
        mLoadTask = (AllUpdateLoadTask) new AllUpdateLoadTask(getActivity(), homeController.loader).execute(50,
                                                                                                            SocialTabsActivity.ALL_UPDATES);
      }
    } else {
      new ConnectionErrorDialog(getActivity()).show();
    }
  }

  public void setListAdapter(ArrayList<SocialActivityInfo> list) {

    if (list == null || list.size() == 0) {
      emptyStubView.setVisibility(View.VISIBLE);
      return;
    }
    emptyStubView.setVisibility(View.GONE);

    arrayAdapter = new StandardArrayAdapter(getActivity(), list);
    sectionAdapter = new SectionListAdapter(getActivity(),
                                            getActivity().getLayoutInflater(),
                                            arrayAdapter);
    arrayAdapter.notifyDataSetChanged();
    listview.setAdapter(sectionAdapter);
    sectionAdapter.notifyDataSetChanged();
  }

  private class AllUpdateLoadTask extends SocialLoadTask {

    public AllUpdateLoadTask(Context context, LoaderActionBarItem loader) {
      super(context, loader);
    }

    @Override
    public void setResult(ArrayList<SocialActivityInfo> result) {
      super.setResult(result);
      socialList = result;
      SocialServiceHelper.getInstance().socialInfoList = result;
      if (HomeActivity.homeActivity != null) {
        HomeActivity.homeActivity.setSocialInfo(result);
      }
      setListAdapter(result);
    }

  }

}
