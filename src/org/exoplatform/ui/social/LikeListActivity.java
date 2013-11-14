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

import android.support.v7.app.ActionBarActivity;
//import greendroid.widget.ActionBarItem;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.utils.ExoConstants;
//import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.ShaderImageView;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jun
 * 4, 2012
 */
public class LikeListActivity
    extends ActionBarActivity {
    //extends MyActionBar {

  /*
   * This class for displaying the liker list information include avatar and
   * liker's name
   */

  private ArrayList<SocialLikeInfo> likeList;

  private GridView                  likedGridView;

  private StringBuffer              title;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.like_list_activity_layout);
    //setActionBarContentView(R.layout.like_list_activity_layout);


    /*
     * Get liker list from intent extra
     */
    likeList = getIntent().getParcelableArrayListExtra(ExoConstants.SOCIAL_LIKED_LIST_EXTRA);
    int size = 0;
    if (likeList != null) {
      size = likeList.size();
      initUI();
    }

    /*
     * Set title for activity
     */

    String liker;
    if (size == 0 || size == 1) {
      liker = getResources().getString(R.string.Liker);
    } else {
      liker = getResources().getString(R.string.Likers);
    }
    title = new StringBuffer();
    title.append(size);
    title.append(" ");
    title.append(liker);
    setTitle(title.toString());

  }

  private void initUI() {
    likedGridView = (GridView) findViewById(R.id.like_list_gridview);
    likedGridView.setAdapter(new LikedItemAdapter(this));
    likedGridView.setOnItemClickListener(new OnItemClickListener() {

      @Override
      public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {

      }
    });
  }

  /** TODO - replace this function
  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      if (SocialDetailActivity.socialDetailActivity != null) {
        SocialDetailActivity.socialDetailActivity.finish();
      }
      if (SocialTabsActivity.instance != null) {
        SocialTabsActivity.instance.finish();
      }

      finish();

      break;

    default:
      break;
    }

    return true;
  }
  **/

  /*
   * The adapter for liker grid
   */

  private class LikedItemAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    public LikedItemAdapter(Context context) {
      mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
      return likeList.size();
    }

    @Override
    public Object getItem(int position) {
      return position;
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder viewHolder;
      if (convertView == null) {
        /*
         * Inflate layout from layout resource
         */
        convertView = mInflater.inflate(R.layout.liked_grid_item, null);
        viewHolder = new ViewHolder();
        viewHolder.imageView = (ShaderImageView) convertView.findViewById(R.id.liked_avatar);
        viewHolder.textView = (TextView) convertView.findViewById(R.id.liked_name);
        convertView.setTag(viewHolder);
      } else {
        viewHolder = (ViewHolder) convertView.getTag();
      }
      viewHolder.imageView.setDefaultImageResource(R.drawable.default_avatar);
      viewHolder.imageView.setUrl(likeList.get(position).likedImageUrl);
      viewHolder.textView.setText(likeList.get(position).getLikeName());

      return convertView;
    }

  }

  private class ViewHolder {
    public ShaderImageView imageView;

    public TextView        textView;
  }

}
