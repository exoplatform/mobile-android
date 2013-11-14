package org.exoplatform.widget;

/**
import android.support.v7.app.ActionBarActivity;
import com.cyrilmottier.android.greendroid.R;


//import greendroid.util.Config;
//import greendroid.widget.ActionBar;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;


@SuppressLint("Registered")
public class MyListActivity
    extends ActionBarActivity {
    //extends MyActionBar {

    private ListAdapter mAdapter;
    private ListView mList;
    private View mEmptyView;

    private Handler mHandler = new Handler();
    private boolean mFinishedStart = false;

    private Runnable mRequestFocus = new Runnable() {
        public void run() {
            mList.focusableViewAvailable(mList);
        }
    };
    
    public MyListActivity() {
        super();
    }

  /**
    public MyListActivity(ActionBar.Type actionBarType) {
        super(actionBarType);
    }
   **/

    /**
     * This method will be called when an item in the list is selected.
     * Subclasses should override. Subclasses can call
     * getListView().getItemAtPosition(position) if they need to access the data
     * associated with the selected item.
     * 
     * @param l The ListView where the click happened
     * @param v The view that was clicked within the ListView
     * @param position The position of the view in the list
     * @param id The row id of the item that was clicked
     */
/**
    protected void onListItemClick(ListView l, View v, int position, long id) {
    }

    /**
     * Provide the cursor for the list view.
     */
/**
    public void setListAdapter(ListAdapter adapter) {
        synchronized (this) {
            ensureLayout();
            mAdapter = adapter;
            mList.setAdapter(adapter);
        }
    }

    /**
     * Set the currently selected list item to the specified position with the
     * adapter's data
     * 
     * @param position
     */
/**
    public void setSelection(int position) {
        mList.setSelection(position);
    }

    /**
     * Get the position of the currently selected list item.
     */
/**
    public int getSelectedItemPosition() {
        return mList.getSelectedItemPosition();
    }

    /**
     * Get the cursor row ID of the currently selected list item.
     */
/**
    public long getSelectedItemId() {
        return mList.getSelectedItemId();
    }
 **/

    /**
     * Get the activity's list view widget.
     */
/**
    public ListView getListView() {
        //ensureLayout();
        return mList;
    }

    /**
     * Get the ListAdapter associated with this activity's ListView.
     */
/**
    public ListAdapter getListAdapter() {
        return mAdapter;
    }

    @Override
    public int createLayout() {
        //if (Config.GD_INFO_LOGS_ENABLED) {
//      //      Log.d(LOG_TAG, "No layout specified : creating the default layout");
        //}

      /**
        switch (getActionBarType()) {
            case Dashboard:
                return R.layout.gd_list_content_dashboard;
            case Empty:
                return R.layout.gd_list_content_empty;
            case Normal:
                
            default:
                return R.layout.gd_list_content_normal;
        }
       **/
/**
    }


/**
    @Override
    protected boolean verifyLayout() {
        return super.verifyLayout() && mList != null;
    }

    @Override
    public void onPreContentChanged() {
        //super.onPreContentChanged();

        mEmptyView = findViewById(android.R.id.empty);
        mList = (ListView) findViewById(android.R.id.list);
        if (mList == null) {
            throw new RuntimeException("Your content must have a ListView whose id attribute is "
                    + "'android.R.id.list'");
        }
    }

    @Override
    public void onPostContentChanged() {
        super.onPostContentChanged();

        if (mEmptyView != null) {
            mList.setEmptyView(mEmptyView);
        }
        mList.setOnItemClickListener(mOnClickListener);
        if (mFinishedStart) {
            setListAdapter(mAdapter);
        }
        mHandler.post(mRequestFocus);
        mFinishedStart = true;
    }

    private AdapterView.OnItemClickListener mOnClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            onListItemClick((ListView) parent, v, position, id);
        }
    };

}
**/
