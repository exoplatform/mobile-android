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
import org.exoplatform.controller.home.SocialLoadTask;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.SectionListAdapter;
import org.exoplatform.widget.SectionListView;
import org.exoplatform.widget.StandardArrayAdapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
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
public class MySpacesFragment extends ActivityStreamFragment {

//  private SectionListView        listview;

//  private StandardArrayAdapter   arrayAdapter;

//  private SectionListAdapter     sectionAdapter;

//  private View                   emptyStubView;

//  private MySpacesLoadTask       mLoadTask;

  public static MySpacesFragment instance;
  
//  private int                      firstIndex;

//  private int                    currentPosition = 0;

//  public int                     actNumbers      = ExoConstants.NUMBER_OF_ACTIVITY;

  public static MySpacesFragment getInstance() {
    MySpacesFragment fragment = new MySpacesFragment();
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    instance = this;
    onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, false, 0);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.social_my_spaces_layout, container, false);
    listview = (SectionListView) view.findViewById(R.id.my_spaces_listview);
    listview.setDivider(null);
    listview.setDividerHeight(0);
    listview.setFadingEdgeLength(0);
    listview.setCacheColorHint(Color.TRANSPARENT);
    listview.setParentFragment(this);
    emptyStubView = ((ViewStub) view.findViewById(R.id.social_my_spaces_empty_stub)).inflate();
    ImageView emptyImage = (ImageView) emptyStubView.findViewById(R.id.empty_image);
    emptyImage.setBackgroundResource(R.drawable.icon_for_no_activities);
    TextView emptyStatus = (TextView) emptyStubView.findViewById(R.id.empty_status);
    emptyStatus.setText(getActivity().getString(R.string.EmptyActivity));

    return view;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setListAdapter();
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
//    onCancelLoad();
    instance = null;
  }

  
  public boolean isEmpty() {
	  return (SocialServiceHelper.getInstance().mySpacesList == null
	        || SocialServiceHelper.getInstance().mySpacesList.size() == 0);
  }


  public void setListAdapter() {
	  ArrayList<SocialActivityInfo> list = SocialServiceHelper.getInstance().mySpacesList;
    if (list == null || list.size() == 0) {
      emptyStubView.setVisibility(View.VISIBLE);
      return;
    }
    emptyStubView.setVisibility(View.GONE);

    arrayAdapter = new StandardArrayAdapter(getActivity(), list);
    sectionAdapter = new SectionListAdapter(getActivity(),
                                            getActivity().getLayoutInflater(),
                                            arrayAdapter);
    listview.setAdapter(sectionAdapter);
    /*
     * Make section invisible at first in list
     */
    sectionAdapter.makeSectionInvisibleIfFirstInList(firstIndex);

    listview.setSelectionFromTop(currentPosition, 0);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    
  }
  
  public class MySpacesLoadTask extends SocialLoadTask {

    public MySpacesLoadTask(Context context, LoaderActionBarItem loader) {
      super(context, loader);
    }

    @Override
    public void setResult(ArrayList<SocialActivityInfo> result) {
      super.setResult(result);
      setListAdapter();
    }

  }

}
