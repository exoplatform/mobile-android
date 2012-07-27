package org.exoplatform.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * View displaying the list with sectioned header.
 */
public class SectionListView extends ListView implements OnScrollListener {

  private int     currentPage   = 0;

  private int     previousTotal = 0;

  private boolean loading       = true;

  private View    transparentView;

  public SectionListView(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    commonInitialisation();
  }

  public SectionListView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    commonInitialisation();
  }

  public SectionListView(final Context context) {
    super(context);
    commonInitialisation();
  }

  protected final void commonInitialisation() {
    setOnScrollListener(this);
    setVerticalFadingEdgeEnabled(false);
    setFadingEdgeLength(0);
  }

  @Override
  public void setAdapter(final ListAdapter adapter) {
    if (!(adapter instanceof SectionListAdapter)) {
      throw new IllegalArgumentException("The adapter needds to be of type "
          + SectionListAdapter.class + " and is " + adapter.getClass());
    }
    super.setAdapter(adapter);
    final ViewParent parent = getParent();
    if (!(parent instanceof FrameLayout)) {
      throw new IllegalStateException("Section List should have FrameLayout as parent!");
    }
    if (transparentView != null) {
      ((FrameLayout) parent).removeView(transparentView);
    }
    transparentView = ((SectionListAdapter) adapter).getTransparentSectionView();
    final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(LayoutParams.FILL_PARENT,
                                                                     LayoutParams.WRAP_CONTENT);
    ((FrameLayout) parent).addView(transparentView, lp);
    if (adapter.isEmpty()) {
      transparentView.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  public void onScroll(final AbsListView view,
                       final int firstVisibleItem,
                       final int visibleItemCount,
                       final int totalItemCount) {
    final SectionListAdapter adapter = (SectionListAdapter) getAdapter();
    if (adapter != null) {

      // adapter.makeSectionInvisibleIfFirstInList(firstVisibleItem);
      // if (loading) {
      // if (totalItemCount > previousTotal) {
      // loading = false;
      // previousTotal = totalItemCount;
      // currentPage++;
      // }
      // }
      // if (!loading
      // && (totalItemCount - visibleItemCount) <= (firstVisibleItem +
      // ExoConstants.NUMBER_OF_ACTIVITY)) {
      // I load the next page of gigs using a background task,
      // but you can call any function here.
      // new LoadGigsTask().execute(currentPage + 1);
      // int tabId = SocialTabsActivity.instance.mPager.getCurrentItem();
      //
      // switch (tabId) {
      // case SocialTabsActivity.ALL_UPDATES:
      // AllUpdatesFragment.instance.onPrepareLoad(currentPage +
      // ExoConstants.NUMBER_OF_ACTIVITY,
      // true);
      // break;
      // case SocialTabsActivity.MY_CONNECTIONS:
      // MyConnectionsFragment.instance.onPrepareLoad(currentPage
      // + ExoConstants.NUMBER_OF_ACTIVITY, true);
      // break;
      // case SocialTabsActivity.MY_SPACES:
      // MySpacesFragment.instance.onPrepareLoad(currentPage +
      // ExoConstants.NUMBER_OF_ACTIVITY,
      // true);
      // break;
      // case SocialTabsActivity.MY_STATUS:
      // MyStatusFragment.instance.onPrepareLoad(currentPage +
      // ExoConstants.NUMBER_OF_ACTIVITY,
      // true);
      // break;
      // }
      // loading = true;
      // }
    }

  }

  @Override
  public void onScrollStateChanged(final AbsListView view, final int scrollState) {
    // do nothing
  }

}
