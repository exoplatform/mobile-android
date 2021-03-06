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

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.controller.home.SocialLoadTask;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.SectionListAdapter;
import org.exoplatform.widget.SectionListView;
import org.exoplatform.widget.StandardArrayAdapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public abstract class ActivityStreamFragment extends Fragment {

  protected SectionListView      listview;

  protected View                 emptyStubView;

  protected StandardArrayAdapter arrayAdapter;

  protected SectionListAdapter   sectionAdapter;

  protected int                  firstIndex;

  protected SocialLoadTask       mLoadTask;

  protected int                  currentPosition         = 0;

  public int                     actNumbers              = ExoConstants.NUMBER_OF_ACTIVITY;

  protected boolean              isLoadingMoreActivities = false;

  /**
   * This fragment's layout resource
   */
  protected int                  fragment_layout;

  /**
   * This fragment's empty view ID
   */
  protected int                  fragment_empty_view_id;

  /**
   * This fragment's ListView ID
   */
  protected int                  fragment_list_view_id;

  private static final String    TAG                     = ActivityStreamFragment.class.getName();

  /**
   * Returns whether the current stream's activity list is empty.
   */
  public abstract boolean isEmpty();

  /**
   * Sets the current stream's activity list. If the task is loading more
   * activities, automatically appends the list to the existing list rather than
   * replacing it.
   * 
   * @param list This stream's list of activities.
   */
  public abstract void setActivityList(ArrayList<SocialActivityInfo> list);

  /**
   * Gets this fragment's tab ID.
   * 
   * @return The int value identifying this fragment's tab.
   */
  public abstract int getThisTabId();

  /**
   * Gets this fragment task to load proper activities.
   * 
   * @return The specific SocialLoadTask of this fragment.
   * @see AllUpdatesFragment.AllUpdateLoadTask
   * @see MySpacesFragment.MySpacesLoadTask
   * @see MyStatusFragment.MyStatusLoadTask
   * @see MyConnectionsFragment.MyConnectionLoadTask
   */
  public abstract SocialLoadTask getThisLoadTask();

  @Override
  public void onDestroy() {
    super.onDestroy();
    onCancelLoad();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
    onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, false, 0);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(fragment_layout, container, false);
    listview = (SectionListView) view.findViewById(fragment_list_view_id);
    listview.setDivider(null);
    listview.setDividerHeight(0);
    listview.setFadingEdgeLength(0);
    listview.setCacheColorHint(Color.TRANSPARENT);
    listview.setParentFragment(this);
    emptyStubView = ((ViewStub) view.findViewById(fragment_empty_view_id)).inflate();
    TextView emptyStatus = (TextView) emptyStubView.findViewById(R.id.empty_status);
    emptyStatus.setText(getActivity().getString(R.string.EmptyActivity));
    Drawable icon = getResources().getDrawable(R.drawable.icon_for_no_activities);
    icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
    emptyStatus.setCompoundDrawables(null, icon, null, null);
    return view;
  }

  /**
   * Interface for loading activities. Make sure the conditions to load
   * activities are met:<br/>
   * - either the user is refreshing the stream with the refresh button<br/>
   * - or the list is empty and needs to be populated
   * 
   * @param actNums The number of activities to load.
   * @param isRefresh Whether the user is refreshing the stream manually.
   * @param position The position of the 1st item of the list.
   */
  public void onPrepareLoad(int actNums, boolean isRefresh, int position) {
    currentPosition = position;
    if (isRefresh || isEmpty()) {
      onLoad(actNums);
    }
  }

  /**
   * Load <code>actNums</code> (default 50) newest activities and update the
   * stream of this fragment.
   * 
   * @param actNums The number of activities to load.
   */
  private void onLoad(int actNums) {
    if (ExoConnectionUtils.isNetworkAvailableExt(getActivity())) {
      if (mLoadTask == null || mLoadTask.getStatus() == Status.FINISHED) {
        int currentTab = getThisTabId();
        mLoadTask = getThisLoadTask();
        mLoadTask.execute(actNums, currentTab);
        firstIndex = 0;
        isLoadingMoreActivities = false;
      }
    } else {
      new ConnectionErrorDialog(getActivity()).show();
    }
  }

  /**
   * Load <code>numberOfActivities</code> (default 100) more activities from the
   * end of the list.
   * 
   * @param numberOfActivities The number of activities to add to the current
   *          list.
   * @param currentPos The position of the 1st newly loaded activity.
   */
  public void onLoadMore(int numberOfActivities, int currentPos, int firstVisible) {
    if (ExoConnectionUtils.isNetworkAvailableExt(getActivity())) {
      if (mLoadTask == null || mLoadTask.getStatus() == Status.FINISHED) {
        int currentTab = SocialTabsActivity.instance.mPager.getCurrentItem();
        int lastActivity = 0;
        ArrayList<SocialActivityInfo> list = SocialServiceHelper.getInstance().getSocialListForTab(currentTab);
        mLoadTask = getThisLoadTask();

        Log.i(TAG, "Loading more activities");

        if (list != null) { // if we can identify the last activity, we load the
                            // previous/older ones
          lastActivity = list.size() - 1;
          isLoadingMoreActivities = true;
          currentPosition = currentPos;
          firstIndex = firstVisible;
          mLoadTask.execute(numberOfActivities, currentTab, lastActivity);
        } else { // otherwise we simply reload the current tab's activities and
                 // inform the user
          Toast.makeText(getActivity(), getActivity().getString(R.string.CannotLoadMoreActivities), Toast.LENGTH_LONG).show();
          currentPosition = 0;
          mLoadTask.execute(numberOfActivities, currentTab);
        }
      }
    } else {
      new ConnectionErrorDialog(getActivity()).show();
    }
  }

  private void onCancelLoad() {
    if (isLoading()) {
      mLoadTask.cancel(true);
      mLoadTask = null;
      isLoadingMoreActivities = false;
    }
  }

  public boolean isLoading() {
    return (mLoadTask != null && mLoadTask.getStatus() == Status.RUNNING);
  }

  public int getPosition() {
    return currentPosition;
  }

  /**
   * Set the array adapter with the content of the given list. Create a section
   * adapter from the new array adapter.
   * 
   * @param activityList The list of activities that the list adapter uses.
   */
  @SuppressLint("NewApi")
  // lint warns that listview.setSelectionFromTop is from API 21 but it's 1
  public void setListAdapter(ArrayList<SocialActivityInfo> activityList) {
    if (activityList == null || activityList.size() == 0) {
      emptyStubView.setVisibility(View.VISIBLE);
      return;
    }
    emptyStubView.setVisibility(View.GONE);

    arrayAdapter = new StandardArrayAdapter(getActivity(), activityList);
    sectionAdapter = new SectionListAdapter(getActivity(), getActivity().getLayoutInflater(), arrayAdapter);
    listview.setAdapter(sectionAdapter);
    /*
     * Hide the section header if it's the first item of the list
     */
    sectionAdapter.makeSectionInvisibleIfFirstInList(firstIndex);

    /*
     * Keep the current position when the listview is refreshed or more
     * activities are loaded
     */
    int h = 0;
    if (isLoadingMoreActivities)
      h = listview.getHeight() - listview.getAutoLoadProgressBar().getHeight();
    listview.setSelectionFromTop(currentPosition, h);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    /*
     * Store the current first visible position of listview
     */
    if (listview != null) {
      firstIndex = listview.getFirstVisiblePosition();
    }
  }
}
