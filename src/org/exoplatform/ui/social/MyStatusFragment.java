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
public class MyStatusFragment extends Fragment {

  private ArrayList<SocialActivityInfo> socialList;

  private HomeController                homeController;

  private SectionListView               listview;

  private StandardArrayAdapter          arrayAdapter;

  private SectionListAdapter            sectionAdapter;

  private View                          emptyStubView;

  private MyStausLoadTask               mLoadTask;

  public static MyStatusFragment        instance;

  public static MyStatusFragment getInstance(HomeController homeController) {
    MyStatusFragment fragment = new MyStatusFragment();
    fragment.socialList = SocialServiceHelper.getInstance().myStatusList;
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
    View view = inflater.inflate(R.layout.social_my_status_layout, container, false);
    listview = (SectionListView) view.findViewById(R.id.my_status_listview);
    listview.setDivider(null);
    listview.setDividerHeight(0);
    listview.setFadingEdgeLength(0);
    listview.setCacheColorHint(Color.TRANSPARENT);
    emptyStubView = ((ViewStub) view.findViewById(R.id.social_my_status_empty_stub)).inflate();
    ImageView emptyImage = (ImageView) emptyStubView.findViewById(R.id.empty_image);
    emptyImage.setBackgroundResource(R.drawable.icon_for_no_activities);
    TextView emptyStatus = (TextView) emptyStubView.findViewById(R.id.empty_status);
    emptyStatus.setText(getActivity().getString(R.string.EmptyActivity));

    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setListAdapter(socialList);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    onCancelLoad();
    instance = null;
  }

  public void onPrepareLoad(boolean isRefresh) {
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
      if (mLoadTask == null || mLoadTask.getStatus() == MyStausLoadTask.Status.FINISHED) {
        mLoadTask = (MyStausLoadTask) new MyStausLoadTask(getActivity(), homeController.loader).execute(50,
                                                                                                        SocialTabsActivity.MY_STATUS);
      }
    } else {
      new ConnectionErrorDialog(getActivity()).show();
    }
  }

  private void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == MyStausLoadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  public boolean isLoading() {
    if (mLoadTask != null && mLoadTask.getStatus() == MyStausLoadTask.Status.RUNNING) {
      return true;
    }

    return false;
  }

  public void setListAdapter(ArrayList<SocialActivityInfo> list) {
    if (list == null || list.size() == 0) {
      emptyStubView.setVisibility(View.VISIBLE);
      return;
    }
    emptyStubView.setVisibility(View.GONE);

    arrayAdapter = new StandardArrayAdapter(getActivity(), list);
    arrayAdapter.notifyDataSetChanged();
    sectionAdapter = new SectionListAdapter(getActivity(),
                                            getActivity().getLayoutInflater(),
                                            arrayAdapter);
    sectionAdapter.notifyDataSetChanged();
    listview.setAdapter(sectionAdapter);
  }

  private class MyStausLoadTask extends SocialLoadTask {

    public MyStausLoadTask(Context context, LoaderActionBarItem loader) {
      super(context, loader);
    }

    @Override
    public void setResult(ArrayList<SocialActivityInfo> result) {
      super.setResult(result);
      SocialServiceHelper.getInstance().myStatusList = result;
      socialList = result;
      setListAdapter(result);
    }

  }
}
