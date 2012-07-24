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

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
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
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jul
 * 23, 2012
 */
public class AllUpdatesFragment extends Fragment {

  private ArrayList<SocialActivityInfo> socialList;

  private CommonFragment                common;

  // private LinearLayout activityStreamWrap;

  private SectionListView               listview;

  private View                          emptyStubView;

  private StandardArrayAdapter          arrayAdapter;

  private SectionListAdapter            sectionAdapter;

  public static AllUpdatesFragment      instance;

  public static AllUpdatesFragment getInstance(CommonFragment common,
                                               ArrayList<SocialActivityInfo> list) {
    AllUpdatesFragment fragment = new AllUpdatesFragment();
    fragment.socialList = list;
    fragment.common = common;
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    instance = this;
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

  public void setListAdapter(ArrayList<SocialActivityInfo> list) {

    if (list == null || list.size() == 0) {
      emptyStubView.setVisibility(View.VISIBLE);
      return;
    }
    emptyStubView.setVisibility(View.GONE);

    arrayAdapter = new StandardArrayAdapter(getActivity(), R.layout.social_all_updates_layout, list);
    sectionAdapter = new SectionListAdapter(getActivity(),
                                            getActivity().getLayoutInflater(),
                                            arrayAdapter);
    arrayAdapter.notifyDataSetChanged();
    listview.setAdapter(sectionAdapter);
    sectionAdapter.notifyDataSetChanged();
  }


}
