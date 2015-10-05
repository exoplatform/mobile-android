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
import org.exoplatform.model.SocialLikeInfo;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.image.ExoPicasso;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jun
 * 4, 2012
 */
public class LikeListActivity extends Activity {

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
    // setActionBarContentView(R.layout.like_list_activity_layout);
    setContentView(R.layout.like_list_activity_layout);
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

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case android.R.id.home:
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
        viewHolder.imageView = (ImageView) convertView.findViewById(R.id.liked_avatar);
        viewHolder.textView = (TextView) convertView.findViewById(R.id.liked_name);
        convertView.setTag(viewHolder);
      } else {
        viewHolder = (ViewHolder) convertView.getTag();
      }

      ExoPicasso.picasso(getApplicationContext())
                .load(Uri.parse(likeList.get(position).likedImageUrl))
                .error(R.drawable.default_avatar)
                .into(viewHolder.imageView);
      viewHolder.textView.setText(likeList.get(position).getLikeName());

      return convertView;
    }

  }

  private class ViewHolder {
    public ImageView imageView;

    public TextView  textView;
  }

}
