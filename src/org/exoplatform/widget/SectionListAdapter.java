package org.exoplatform.widget;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.utils.SocialActivityUtil;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

/**
 * Adapter for sections.
 */
public class SectionListAdapter extends BaseAdapter implements ListAdapter, OnItemClickListener {
  private final DataSetObserver       dataSetObserver     = new DataSetObserver() {
                                                            @Override
                                                            public void onChanged() {
                                                              super.onChanged();
                                                              updateSessionCache();
                                                            }

                                                            @Override
                                                            public void onInvalidated() {
                                                              super.onInvalidated();
                                                              updateSessionCache();
                                                            };
                                                          };

  /**
   * The list of items, as a ListAdapter.
   */
  private final ListAdapter           linkedAdapter;

  /**
   * Map the position of each section in the list with the name of this section
   */
  private final Map<Integer, String>  sectionPositions    = new LinkedHashMap<Integer, String>();

  /**
   * Map the position of each item after adding the sections with the actual position in the adapter
   */
  private final Map<Integer, Integer> itemPositions       = new LinkedHashMap<Integer, Integer>();

  private final Map<View, String>     currentViewSections = new HashMap<View, String>();

  /**
   * Returns the number of types of Views that will be created by getView(int, View, ViewGroup).
   */
  private int                         viewTypeCount;

  protected final LayoutInflater      inflater;

  /**
   * The fixed section header at the top-right of the list
   */
  private View                        transparentSectionView;

  private OnItemClickListener         linkedListener;

  private Context                     mContext;

  public SectionListAdapter(Context context,
                            final LayoutInflater inflater,
                            final ListAdapter linkedAdapter) {
    mContext = context;
    this.linkedAdapter = linkedAdapter;
    this.inflater = inflater;
    linkedAdapter.registerDataSetObserver(dataSetObserver);
    updateSessionCache();
  }

  private boolean isTheSame(final String previousSection, final String newSection) {
    if (previousSection == null) {
      return newSection == null;
    } else {
      return previousSection.equals(newSection);
    }
  }

  private synchronized void updateSessionCache() {
    int currentPosition = 0;
    sectionPositions.clear();
    itemPositions.clear();
    // The number of view types in the adapter + 1 for the section headers.
    viewTypeCount = linkedAdapter.getViewTypeCount() + 1;
    String currentSection = null;
    final int count = linkedAdapter.getCount();
    String section = null;
    for (int i = 0; i < count; i++) {
      final SocialActivityInfo item = (SocialActivityInfo) linkedAdapter.getItem(i);
      // a label that indicates when this activity was created (approximatively)
      section = SocialActivityUtil.getActivityStreamHeader(mContext, item.getPostedTime());
      if (!isTheSame(currentSection, section)) {
    	// we're entering a new section (activities were created at a different date)
        sectionPositions.put(currentPosition, section);
        currentSection = section;
        currentPosition++;
      }
      itemPositions.put(currentPosition, i);
      currentPosition++;
    }
  }

  @Override
  public synchronized int getCount() {
    return sectionPositions.size() + itemPositions.size();
  }

  @Override
  public synchronized Object getItem(final int position) {
    if (isSection(position)) {
      return sectionPositions.get(position);
    } else {
      final int linkedItemPosition = getLinkedPosition(position);
      return linkedAdapter.getItem(linkedItemPosition);
    }
  }

  public synchronized boolean isSection(final int position) {
    return sectionPositions.containsKey(position);
  }

  public synchronized String getSectionName(final int position) {
    if (isSection(position)) {
      return sectionPositions.get(position);
    } else {
      return null;
    }
  }

  public int getSectionPosition(String sectionName) {
    if (sectionPositions.containsValue(sectionName)) {
      return getKey(sectionPositions, sectionName);
    } else
      return 0;
  }

  private int getKey(Map<Integer, String> map, String value) {
    for (Iterator i = map.keySet().iterator(); i.hasNext();) {
      int key = (Integer) i.next();
      if (map.get(key).equals(value)) {
        return key;
      }
    }
    return 0;
  }

  @Override
  public long getItemId(final int position) {
    if (isSection(position)) {
      return sectionPositions.get(position).hashCode();
    } else {
      return linkedAdapter.getItemId(getLinkedPosition(position));
    }
  }

  protected Integer getLinkedPosition(final int position) {
    return itemPositions.get(position);
  }

  @Override
  public int getItemViewType(final int position) {
    if (isSection(position)) {
      return viewTypeCount - 1;
    }
    return linkedAdapter.getItemViewType(getLinkedPosition(position));
  }

  private View getSectionView(final View convertView, final String section) {
    View theView = convertView;
    if (theView == null) {
      theView = createNewSectionView();
    }
    setSectionText(section, theView);
    replaceSectionViewsInMaps(section, theView);
    return theView;
  }

  /**
   * Writes <code>section</code> on the view <code>sectionView</code>.
   * @param section The label to write for this section.
   * @param sectionView The view on which the label is set.
   */
  protected void setSectionText(final String section, final View sectionView) {
    final TextView textView = (TextView) sectionView.findViewById(R.id.textView_Section_Title);
    textView.setText(section);
    if (section.equalsIgnoreCase(mContext.getString(R.string.Today))) {
      textView.setBackgroundResource(R.drawable.social_activity_browse_header_highlighted_bg);
    } else {
      textView.setBackgroundResource(R.drawable.social_activity_browse_header_normal_bg);
      textView.setTextColor(Color.rgb(59, 59, 59));
    }
  }

  protected synchronized void replaceSectionViewsInMaps(final String section, final View theView) {
    if (currentViewSections.containsKey(theView)) {
      currentViewSections.remove(theView);
    }
    currentViewSections.put(theView, section);
  }

  protected View createNewSectionView() {
    return inflater.inflate(R.layout.activityheadersection, null);
  }

  @Override
  public View getView(final int position, final View convertView, final ViewGroup parent) {
    if (isSection(position)) {
      return getSectionView(convertView, sectionPositions.get(position));
    }
    return linkedAdapter.getView(getLinkedPosition(position), convertView, parent);
  }

  @Override
  public int getViewTypeCount() {
    return viewTypeCount;
  }

  @Override
  public boolean hasStableIds() {
    return linkedAdapter.hasStableIds();
  }

  @Override
  public boolean isEmpty() {
    return linkedAdapter.isEmpty();
  }

  @Override
  public void registerDataSetObserver(final DataSetObserver observer) {
    linkedAdapter.registerDataSetObserver(observer);
  }

  @Override
  public void unregisterDataSetObserver(final DataSetObserver observer) {
    linkedAdapter.unregisterDataSetObserver(observer);
  }

  @Override
  public boolean areAllItemsEnabled() {
    return linkedAdapter.areAllItemsEnabled();
  }

  @Override
  public boolean isEnabled(final int position) {
    if (isSection(position)) {
      return true;
    }
    return linkedAdapter.isEnabled(getLinkedPosition(position));
  }

  /**
   * Hide the section header when it reaches the top of the list.
   * @param firstVisibleItem The position of the item currently at the top of the list.
   */
  public void makeSectionInvisibleIfFirstInList(final int firstVisibleItem) {
    final String section = getSectionName(firstVisibleItem);
    // only make invisible the first section with that name in case there
    // are more with the same name
    boolean alreadySetFirstSectionIvisible = false;
    for (final Entry<View, String> itemView : currentViewSections.entrySet()) {
      if (itemView.getValue().equals(section) && !alreadySetFirstSectionIvisible) {
        itemView.getKey().setVisibility(View.INVISIBLE);
        alreadySetFirstSectionIvisible = true;
      } else {
        itemView.getKey().setVisibility(View.VISIBLE);
      }
    }
    for (final Entry<Integer, String> entry : sectionPositions.entrySet()) {
      if (entry.getKey() > firstVisibleItem + 1) {
        break;
      }
      setSectionText(entry.getValue(), getTransparentSectionView());
    }
  }

  /**
   * @return The fixed section header view. Inflate it first if it doesn't exist.
   */
  public synchronized View getTransparentSectionView() {
    if (transparentSectionView == null) {
      transparentSectionView = createNewSectionView();
    }
    return transparentSectionView;
  }

  protected void sectionClicked(final String section) {
    // do nothing
  }

  @Override
  public void onItemClick(final AdapterView<?> parent,
                          final View view,
                          final int position,
                          final long id) {
    if (isSection(position)) {
      sectionClicked(getSectionName(position));
    } else if (linkedListener != null) {
      linkedListener.onItemClick(parent, view, getLinkedPosition(position), id);
    }
  }

  public void setOnItemClickListener(final OnItemClickListener linkedListener) {
    this.linkedListener = linkedListener;
  }
}
