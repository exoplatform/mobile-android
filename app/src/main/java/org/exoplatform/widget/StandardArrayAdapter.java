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
package org.exoplatform.widget;

import java.util.ArrayList;

import org.exoplatform.R;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.SocialDetailHelper;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.ui.social.AllUpdatesFragment;
import org.exoplatform.ui.social.ComposeMessageActivity;
import org.exoplatform.ui.social.MyConnectionsFragment;
import org.exoplatform.ui.social.MySpacesFragment;
import org.exoplatform.ui.social.MyStatusFragment;
import org.exoplatform.ui.social.SocialActivityStreamItem;
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.ui.social.SocialTabsActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoUtils;
import org.exoplatform.utils.Log;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StandardArrayAdapter extends ArrayAdapter<SocialActivityInfo> {

  private final ArrayList<SocialActivityInfo> items;

  private Context                             mContext;

  private LayoutInflater                      mInflater;

  private ViewHolder                          holder = null;

  private LikeLoadTask                        mLoadTask;

  private static final String                 TAG    = StandardArrayAdapter.class.getName();

  public StandardArrayAdapter(Context context, ArrayList<SocialActivityInfo> items) {
    super(context, R.layout.activitybrowserviewcell, items);
    mContext = context;
    this.items = items;
    mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
  }

  @Override
  public View getView(final int position, View convertView, ViewGroup parent) {
    final SocialActivityInfo actInfo = items.get(position);

    if (convertView == null) {
      convertView = mInflater.inflate(R.layout.activitybrowserviewcell, null);
      holder = new ViewHolder();
      holder.imageViewAvatar = (ImageView) convertView.findViewById(R.id.imageView_Avatar);
      holder.contentLayoutWrap = (LinearLayout) convertView.findViewById(R.id.relativeLayout_Content);
      holder.textViewName = (TextView) convertView.findViewById(R.id.textView_Name);
      holder.textViewName.setLinkTextColor(Color.rgb(21, 94, 173));
      holder.textViewMessage = (TextView) convertView.findViewById(R.id.textView_Message);
      holder.textViewTempMessage = (TextView) convertView.findViewById(R.id.textview_temp_message);
      holder.textViewCommnet = (TextView) convertView.findViewById(R.id.activity_comment_view);
      holder.buttonComment = (Button) convertView.findViewById(R.id.button_Comment);
      holder.buttonLike = (Button) convertView.findViewById(R.id.button_Like);
      holder.typeImageView = (ImageView) convertView.findViewById(R.id.activity_image_type);
      holder.textViewTime = (TextView) convertView.findViewById(R.id.textView_Time);
      holder.attachStubView = ((ViewStub) convertView.findViewById(R.id.attached_image_stub_activity)).inflate();
      convertView.setTag(holder);
    } else {
      holder = (ViewHolder) convertView.getTag();
    }

    SocialActivityStreamItem socialActivityStreamItem = new SocialActivityStreamItem(mContext, holder, actInfo, false);
    socialActivityStreamItem.initCommonInfo();
    holder.contentLayoutWrap.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
        // TODO open the image immediately if there is one,
        // otherwise open the details screen
        String activityId = actInfo.getActivityId();
        SocialDetailHelper.getInstance().setActivityId(activityId);
        SocialDetailHelper.getInstance().setAttachedImageUrl(actInfo.getAttachedImageUrl());
        Intent intent = new Intent(mContext, SocialDetailActivity.class);
        intent.putExtra(ExoConstants.ACTIVITY_CURRENT_POSITION, position);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        mContext.startActivity(intent);
      }
    });

    holder.buttonComment.setOnClickListener(new View.OnClickListener() {

      public void onClick(View v) {

        SocialDetailHelper.getInstance().setActivityId(actInfo.getActivityId());
        Intent intent = new Intent(mContext, ComposeMessageActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(ExoConstants.ACTIVITY_CURRENT_POSITION, position);
        intent.putExtra(ExoConstants.COMPOSE_TYPE, ExoConstants.COMPOSE_COMMENT_TYPE);
        mContext.startActivity(intent);

      }
    });

    holder.buttonLike.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
          onLikeLoad(actInfo, position);
        } else {
          new ConnectionErrorDialog(mContext).show();
        }

      }
    });

    return convertView;
  }

  private void onLikeLoad(SocialActivityInfo info, int position) {
    if (mLoadTask == null || mLoadTask.getStatus() == LikeLoadTask.Status.FINISHED) {
      mLoadTask = (LikeLoadTask) new LikeLoadTask(position).execute(info);
    }
  }

  public static class ViewHolder {
    public LinearLayout contentLayoutWrap;

    public ImageView    imageViewAvatar;

    public TextView     textViewName;

    public TextView     textViewMessage;

    public TextView     textViewTempMessage;

    public TextView     textViewCommnet;

    public Button       buttonComment;

    public Button       buttonLike;

    public ImageView    typeImageView;

    public TextView     textViewTime;

    public View         attachStubView;
  }

  private class LikeLoadTask extends AsyncTask<SocialActivityInfo, Void, Boolean> {

    private MenuItem loaderItem;

    private int      currentPosition;

    public LikeLoadTask(int pos) {
      loaderItem = (SocialTabsActivity.instance != null) ? SocialTabsActivity.instance.loaderItem : null;
      currentPosition = pos;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      ExoUtils.setLoadingItem(loaderItem, true);
    }

    @Override
    protected Boolean doInBackground(SocialActivityInfo... params) {
      SocialActivityInfo actInfo = params[0];
      try {
        RestActivity activity = SocialServiceHelper.getInstance().activityService.get(actInfo.getActivityId());
        if (activity.isLiked())
          SocialServiceHelper.getInstance().activityService.unlike(activity);
        else
          SocialServiceHelper.getInstance().activityService.like(activity);

        return true;

      } catch (SocialClientLibException e) {
        Log.d(TAG, e.getMessage(), Log.getStackTraceString(e));
        return false;
      } catch (RuntimeException e) {
        // XXX cannot replace because SocialClientLib can throw exceptions like
        // ServerException, UnsupportMethod ,..
        Log.d(TAG, e.getMessage(), Log.getStackTraceString(e));
        return false;
      }
    }

    @Override
    protected void onCancelled() {
      ExoUtils.setLoadingItem(loaderItem, false);
      super.onCancelled();
    }

    @Override
    protected void onPostExecute(Boolean result) {
      ExoUtils.setLoadingItem(loaderItem, false);
      if (result) {
        if (SocialTabsActivity.instance != null) {
          int tabId = SocialTabsActivity.instance.mPager.getCurrentItem();
          switch (tabId) {
          case SocialTabsActivity.ALL_UPDATES:

            AllUpdatesFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, currentPosition);
            if (AllUpdatesFragment.instance.isLoading())
              holder.buttonLike.setClickable(false);
            else
              holder.buttonLike.setClickable(true);
            break;
          case SocialTabsActivity.MY_CONNECTIONS:
            MyConnectionsFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, currentPosition);
            if (MyConnectionsFragment.instance.isLoading())
              holder.buttonLike.setClickable(false);
            else
              holder.buttonLike.setClickable(true);
            break;
          case SocialTabsActivity.MY_SPACES:
            MySpacesFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, currentPosition);
            if (MySpacesFragment.instance.isLoading())
              holder.buttonLike.setClickable(false);
            else
              holder.buttonLike.setClickable(true);
            break;
          case SocialTabsActivity.MY_STATUS:
            MyStatusFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY, true, currentPosition);
            if (MyStatusFragment.instance.isLoading())
              holder.buttonLike.setClickable(false);
            else
              holder.buttonLike.setClickable(true);
            break;
          }
        }
      } else {
        WarningDialog dialog = new WarningDialog(mContext,
                                                 mContext.getString(R.string.Warning),
                                                 mContext.getString(R.string.ErrorOnLike),
                                                 mContext.getString(R.string.OK));
        dialog.show();
      }
    }

  }

}
