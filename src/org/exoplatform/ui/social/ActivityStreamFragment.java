package org.exoplatform.ui.social;

import java.util.ArrayList;

import android.widget.AbsListView;
import org.exoplatform.poc.tabletversion.R;
import org.exoplatform.controller.home.SocialLoadTask;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.SectionListAdapter;
import org.exoplatform.widget.SectionListView;
import org.exoplatform.widget.StandardArrayAdapter;

import android.util.Log;
import android.graphics.Color;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public abstract class ActivityStreamFragment extends Fragment {

  protected SectionListView          mActivityListView;

  protected View                     emptyStubView;

  protected StandardArrayAdapter     mActivityListAdapter;

  protected SectionListAdapter       sectionAdapter;

  protected int                      firstIndex;

  protected SocialLoadTask			     mLoadTask;

  protected int                      currentPosition = 0;

  public int                         actNumbers      = ExoConstants.NUMBER_OF_ACTIVITY;

  protected boolean				           isLoadingMoreActivities = false;

  /**
   * This fragment's layout resource
   */
  protected int						fragment_layout;

  /**
   * This fragment's empty view ID
   */
  protected int						fragment_empty_view_id;

  /**
   * This fragment's ListView ID
   */
  protected int						fragment_list_view_id;

  /** runs in narrow or wide mode */
  private int                                 mMode = 1;

  public static final int                     WIDE_MODE   = 1;

  public static final int                     NARROW_MODE = 2;

  private static final String TAG = "eXo____ActivityStreamFragment____";

  /**
   * Returns whether the current stream's activity list is empty.
   */
  public abstract boolean isEmpty();

  /**
   * Sets the current stream's activity list.
   * If the task is loading more activities, automatically appends the list to the existing list rather than replacing it.
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
    Log.i(TAG, "onCreate");
    super.onCreate(savedInstanceState);
    onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, false, 0);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(fragment_layout, container, false);

    mActivityListView = (SectionListView) view.findViewById(fragment_list_view_id);
    mActivityListView.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
    mActivityListView.setSelector(R.drawable.list_item_selector);
    mActivityListView.setDivider(null);
    mActivityListView.setDividerHeight(0);
    mActivityListView.setFadingEdgeLength(0);
    mActivityListView.setCacheColorHint(Color.TRANSPARENT);
    mActivityListView.setParentFragment(this);

    emptyStubView = ((ViewStub) view.findViewById(fragment_empty_view_id)).inflate();
    ImageView emptyImage = (ImageView) emptyStubView.findViewById(R.id.empty_image);
    emptyImage.setBackgroundResource(R.drawable.icon_for_no_activities);
    TextView emptyStatus = (TextView) emptyStubView.findViewById(R.id.empty_status);
    emptyStatus.setText(getActivity().getString(R.string.EmptyActivity));

    return view;
  }

  /**
   * Interface for loading activities.
   * Make sure the conditions to load activities are met:<br/>
   * - either the user is refreshing the stream with the refresh button<br/>
   * - or the list is empty and needs to be populated
   * @param actNums The number of activities to load.
   * @param isRefresh Whether the user is refreshing the stream manually.
   * @param position The position of the 1st item of the list.
   */
  public void onPrepareLoad(int actNums, boolean isRefresh, int position) {
    currentPosition = position;
    if (isRefresh) {
      onLoad(actNums);
      return;
    }

    if (isEmpty()) {
      onLoad(actNums);
      return;
    }
  }

  /**
   * Load <code>actNums</code> (default 100) newest activities and update the stream of this fragment.
   * @param actNums The number of activities to load.
   */
  private void onLoad(int actNums) {
    Log.i(TAG, "onLoad");
    if (!ExoConnectionUtils.isNetworkAvailableExt(getActivity())) {
      new ConnectionErrorDialog(getActivity()).show();
      return ;
    }

    if (mLoadTask == null || mLoadTask.getStatus() == Status.FINISHED) {
      ((SocialTabsActivity) getActivity()).setRefreshActionButtonState(true);

      int currentTab = getThisTabId();
      mLoadTask = getThisLoadTask();
      mLoadTask.setListener((SocialTabsActivity) getActivity());
      mLoadTask.execute(actNums, currentTab);
      firstIndex = 0;
      isLoadingMoreActivities = false;
    }
  }

  /**
   * Load <code>numberOfActivities</code> (default 100) more activities from the end of the list.
   * @param numberOfActivities The number of activities to add to the current list.
   * @param currentPos The position of the 1st newly loaded activity.
   */
  public void onLoadMore(int numberOfActivities, int currentPos, int firstVisible) {
    if (ExoConnectionUtils.isNetworkAvailableExt(getActivity())) {
      if (mLoadTask == null || mLoadTask.getStatus() == Status.FINISHED) {
        int currentTab = SocialTabsActivity.instance != null ? SocialTabsActivity.instance.getTabId() : 0;
        int lastActivity = 0;
        ArrayList<SocialActivityInfo> list = SocialServiceHelper.getInstance().getSocialListForTab(currentTab);
        mLoadTask = getThisLoadTask();

        Log.d(TAG, "loading more data - flush image cache");
        // TODO
        //((SocialTabsActivity) getActivity()).getGDApplication().getImageCache().flush();
        // System.gc();

        if (list != null) { // if we can identify the last activity, we load the previous/older ones
          lastActivity = list.size()-1;
          isLoadingMoreActivities = true;
          currentPosition = currentPos;
          firstIndex = firstVisible;
          mLoadTask.execute(numberOfActivities, currentTab, lastActivity);
        } else { 			  // otherwise we simply reload the current tab's activities and inform the user
          Toast.makeText(getActivity(), getActivity().getString(R.string.CannotLoadMoreActivities), Toast.LENGTH_LONG).show();
          currentPosition = 0;
          mLoadTask.execute(numberOfActivities, currentTab);
        }
      }
    } else {
      new ConnectionErrorDialog(getActivity()).show();
    }
  }


  public void onCancelLoad() {
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
   * Set the array adapter with the content of the given list.
   * Create a section adapter from the new array adapter.
   * @param activityList The list of activities that the list adapter uses.
   * @param mode         The display mode of list view
   */
  public void setListAdapter(ArrayList<SocialActivityInfo> activityList, int mode) {
    Log.i(TAG, "set list adapter - mode : " + mode + " - current adapter : " + getTag());
    if (activityList == null || activityList.size() == 0) {
      emptyStubView.setVisibility(View.VISIBLE);
      return;
    }
    Log.i(TAG, "load new view");
    emptyStubView.setVisibility(View.GONE);

    mActivityListAdapter = new StandardArrayAdapter(getActivity(), activityList);
    mActivityListAdapter.setMode(mode);
    mActivityListAdapter.setOnItemClickListener((SocialTabsActivity) getActivity());
    sectionAdapter = new SectionListAdapter(getActivity(), getActivity().getLayoutInflater(), mActivityListAdapter);
    mActivityListView.setAdapter(sectionAdapter);

	  /** Hide the section header if it's the first item of the list */
    sectionAdapter.makeSectionInvisibleIfFirstInList(firstIndex);

	  /** Keep the current position when the listView is refreshed or more activities are loaded */
    int h = 0;
    if (isLoadingMoreActivities)
      h = mActivityListView.getHeight()-mActivityListView.getAutoLoadProgressBar().getHeight();
    mActivityListView.setSelectionFromTop(currentPosition, h);
  }


  /**
   * Switch to different display mode
   */
  public void switchMode(int mode, boolean forceReloadView) {
    Log.i(TAG, "switchMode - mode : " + mode + " - reloadView : " + forceReloadView
    + " - current tag : " + getTag());
    if (mode == mMode) return;
    if (!forceReloadView) {
      mMode = mode;
      return ;
    }

    mMode = mode;
    int currentTab = SocialTabsActivity.instance != null ? SocialTabsActivity.instance.getTabId() : 0;
    ArrayList<SocialActivityInfo> activityList = SocialServiceHelper.getInstance()
        .getSocialListForTab(currentTab);
    if (activityList == null || activityList.size() == 0) {
      emptyStubView.setVisibility(View.VISIBLE);
      return;
    }
    emptyStubView.setVisibility(View.GONE);

    mActivityListAdapter = new StandardArrayAdapter(getActivity(), activityList);
    mActivityListAdapter.setMode(mode);
    sectionAdapter = new SectionListAdapter(getActivity(), getActivity().getLayoutInflater(), mActivityListAdapter);
    mActivityListView.setAdapter(sectionAdapter);

    /** Hide the section header if it's the first item of the list */
    sectionAdapter.makeSectionInvisibleIfFirstInList(firstIndex);

    /** Keep the current position when the listView is refreshed or more activities are loaded */
    int h = 0;
    if (isLoadingMoreActivities)
      h = mActivityListView.getHeight()-mActivityListView.getAutoLoadProgressBar().getHeight();
    mActivityListView.setSelectionFromTop(currentPosition, h);
  }

  /**
   * Clear the selection
   */
  public void clearSelectedItem() {
    if (mActivityListView != null) {
      Log.i(TAG, "clearSelectedItem");
      mActivityListView.clearChoices();
      mActivityListView.requestLayout();
    }
  }


  public void onSaveInstanceState(Bundle savedState) {


  }

  public int getMode() {  return mMode; }

  @Override
  public void onDestroyView() {
    super.onDestroyView();

		/** Store the current first visible position of list view */
    if (mActivityListView != null) {
      firstIndex = mActivityListView.getFirstVisiblePosition();
    }
  }
}
