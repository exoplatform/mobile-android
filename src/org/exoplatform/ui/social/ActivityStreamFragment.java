package org.exoplatform.ui.social;

import org.exoplatform.controller.home.SocialLoadTask;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.ui.social.AllUpdatesFragment.AllUpdateLoadTask;
import org.exoplatform.ui.social.MyConnectionsFragment.MyConnectionLoadTask;
import org.exoplatform.ui.social.MySpacesFragment.MySpacesLoadTask;
import org.exoplatform.ui.social.MyStatusFragment.MyStatusLoadTask;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.SectionListAdapter;
import org.exoplatform.widget.SectionListView;
import org.exoplatform.widget.StandardArrayAdapter;

import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

public abstract class ActivityStreamFragment extends Fragment {

	  protected SectionListView          listview;
	
	  protected View                     emptyStubView;

	  protected StandardArrayAdapter     arrayAdapter;

	  protected SectionListAdapter       sectionAdapter;
	  
	  protected int                      firstIndex;
	  
	  protected SocialLoadTask			mLoadTask;

	  protected int                      currentPosition = 0;

	  public int                       actNumbers      = ExoConstants.NUMBER_OF_ACTIVITY;
	  
	  protected boolean				   isLoadingMoreActivities = false;
	  
	  
	  @Override
	  public void onDestroy() {
	    super.onDestroy();
	    onCancelLoad();
	  }
	  
	  public void onPrepareLoad(int actNums, boolean isRefresh, int position) {
		    currentPosition = position;
		    if (isRefresh) {
		    	Log.d("EXO_MOB", "*** Reloading list");
		      onLoad(actNums);
		      return;
		    }

		    if (isEmpty()) {
		    	Log.d("EXO_MOB", "*** List is empty, loading");
		      onLoad(actNums);
		      return;
		    }

		  }
	  
	  public abstract boolean isEmpty();
	  
	  private void onLoad(int actNums) {
		    if (ExoConnectionUtils.isNetworkAvailableExt(getActivity())) {
		      if (mLoadTask == null || mLoadTask.getStatus() == SocialLoadTask.Status.FINISHED) {
		    	  switch (SocialTabsActivity.instance.mPager.getCurrentItem()) {
		    	  case SocialTabsActivity.ALL_UPDATES:
				        mLoadTask = (AllUpdateLoadTask) AllUpdatesFragment.instance.new AllUpdateLoadTask(
				        		getActivity(),
				        		SocialTabsActivity.instance.loaderItem).execute(
				        				actNums,
				        				SocialTabsActivity.ALL_UPDATES);
		    		  break;
		    		  
		    	  case SocialTabsActivity.MY_CONNECTIONS:
		    		  mLoadTask = (MyConnectionLoadTask) MyConnectionsFragment.instance.new MyConnectionLoadTask(
		    				  	getActivity(),
		    				  	SocialTabsActivity.instance.loaderItem).execute(
		    				  			actNums,
                                        SocialTabsActivity.MY_CONNECTIONS);
		    		  break;
		    		  
		    	  case SocialTabsActivity.MY_SPACES:
		    		  mLoadTask = (MySpacesLoadTask) MySpacesFragment.instance.new MySpacesLoadTask(
		    				  getActivity(),
                              SocialTabsActivity.instance.loaderItem).execute(
                            		  actNums,
                                      SocialTabsActivity.MY_SPACES);
		    		  break;
		    		  
		    	  case SocialTabsActivity.MY_STATUS:
		    		  mLoadTask = (MyStatusLoadTask) MyStatusFragment.instance.new MyStatusLoadTask(
		    				  getActivity(),
                              SocialTabsActivity.instance.loaderItem).execute(
                            		  actNums,
                                      SocialTabsActivity.MY_STATUS);
		    		  break;
		    	  
		    	  }
		      }
		    } else {
		      new ConnectionErrorDialog(getActivity()).show();
		    }
	 }
	  
	  public void onLoadMore(int numberOfActivities, int currentPos) {
		  if (ExoConnectionUtils.isNetworkAvailableExt(getActivity())) {
		      if (mLoadTask == null || mLoadTask.getStatus() == SocialLoadTask.Status.FINISHED) {
		    	  isLoadingMoreActivities = true;
		    	  currentPosition = currentPos;
		    	  switch (SocialTabsActivity.instance.mPager.getCurrentItem()) {
		    	  
		    	  case SocialTabsActivity.ALL_UPDATES:
		    		  mLoadTask = (AllUpdateLoadTask) AllUpdatesFragment.instance.new AllUpdateLoadTask(
							  getActivity(),
                              SocialTabsActivity.instance.loaderItem).execute(
                            		  numberOfActivities,
                                      SocialTabsActivity.ALL_UPDATES,
                                      SocialServiceHelper.getInstance().socialInfoList.size()-1);
		    		  break;
		    		
		    	  case SocialTabsActivity.MY_CONNECTIONS:
		    		  mLoadTask = (MyConnectionLoadTask) MyConnectionsFragment.instance.new MyConnectionLoadTask(
		    				  	getActivity(),
		    				  	SocialTabsActivity.instance.loaderItem).execute(
		    				  			numberOfActivities,
                                        SocialTabsActivity.MY_CONNECTIONS,
                                        SocialServiceHelper.getInstance().myConnectionsList.size()-1);
		    		  break;
		    	  
		    	  case SocialTabsActivity.MY_SPACES:
		    		  mLoadTask = (MySpacesLoadTask) MySpacesFragment.instance.new MySpacesLoadTask(
		    				  getActivity(),
                              SocialTabsActivity.instance.loaderItem).execute(
                            		  numberOfActivities,
                                      SocialTabsActivity.MY_SPACES,
                                      SocialServiceHelper.getInstance().mySpacesList.size()-1);
		    		  break;
		    		  
		    	  case SocialTabsActivity.MY_STATUS:
		    		  mLoadTask = (MyStatusLoadTask) MyStatusFragment.instance.new MyStatusLoadTask(
		    				  getActivity(),
                              SocialTabsActivity.instance.loaderItem).execute(
                            		  numberOfActivities,
                                      SocialTabsActivity.MY_STATUS,
                                      SocialServiceHelper.getInstance().myStatusList.size()-1);
		    		  break;
		    	  }
		      }
		    } else {
		      new ConnectionErrorDialog(getActivity()).show();
		    }
	  }
	
	  private void onCancelLoad() {
		    if (mLoadTask != null && mLoadTask.getStatus() == AllUpdateLoadTask.Status.RUNNING) {
		      mLoadTask.cancel(true);
		      mLoadTask = null;
		      isLoadingMoreActivities = false;
		    }
		  }
	  
	  public boolean isLoading() {
		    if (mLoadTask != null && mLoadTask.getStatus() == AllUpdateLoadTask.Status.RUNNING) {
		      return true;
		    }

		    return false;
	}
		  
	public int getPosition() {
			  return currentPosition;
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
