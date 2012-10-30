package org.exoplatform.widget;

import java.security.InvalidParameterException;

import org.exoplatform.ui.social.ActivityStreamFragment;
import org.exoplatform.utils.ExoConstants;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

/**
 * View displaying the list with sectioned header.
 */
public class SectionListView extends ListView implements OnScrollListener {

  /**
   * The view that contains the fixed section header
   */
  private View transparentView;
  /**
   * The Fragment that holds this list view
   */
  private ActivityStreamFragment parentFragment;
  /**
   * The progress bar in the footer
   */
  private ProgressBar autoLoadProgress;
  /**
   * A flag that indicates whether the user has scrolled to reach the current position in the list
   */
  private boolean hasScrolled;

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
    autoLoadProgress = new ProgressBar(getContext(), null, android.R.attr.progressBarStyleSmall);
    autoLoadProgress.setLayoutParams(new ListView.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
    autoLoadProgress.setPadding(0, 0, 0, 10); // 0px left, top and right, 10px bottom
    autoLoadProgress.setVisibility(View.INVISIBLE);
    LinearLayout layout = new LinearLayout(getContext());
    layout.setGravity(Gravity.CENTER);
    layout.addView(autoLoadProgress);
    addFooterView(layout);
    hasScrolled = false;
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
  
  public void setParentFragment(ActivityStreamFragment p) {
	  if (p != null) {
		  parentFragment = p;
	  } else {
		  throw new InvalidParameterException("You must set a proper Fragment object.");
	  }
  }

  @Override
  public void onScroll(final AbsListView view,
                       final int firstVisibleItem,
                       final int visibleItemCount,
                       final int totalItemCount) {
	  ListAdapter a = getAdapter();
	  SectionListAdapter adapter = null; 
	  if (a instanceof HeaderViewListAdapter)
		  adapter = (SectionListAdapter)((HeaderViewListAdapter) a).getWrappedAdapter();
	  else
		  adapter = (SectionListAdapter) getAdapter();
    
    if (adapter != null) {
      adapter.makeSectionInvisibleIfFirstInList(firstVisibleItem);
    }
    // More activities will be automatically loaded if:
    // - the user has scrolled AND
    // - the user has reached the bottom of the list
    if (hasScrolled && totalItemCount > 0 && firstVisibleItem+visibleItemCount==totalItemCount) {
		autoLoadProgress.setVisibility(View.VISIBLE);
		parentFragment.onLoadMore(ExoConstants.NUMBER_OF_ACTIVITY, totalItemCount-1, firstVisibleItem);
		// set back to false to avoid multiple calls to the onLoadMore(...) method
		hasScrolled = false;
    }
  }
  
  public ProgressBar getAutoLoadProgressBar() {
	  return autoLoadProgress;
  }

    @Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
    	// false if the user is not scrolling, true otherwise
		hasScrolled = (scrollState!=OnScrollListener.SCROLL_STATE_IDLE);
 	}

}
