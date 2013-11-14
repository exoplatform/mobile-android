package org.exoplatform.widget;

import android.util.Log;
import android.widget.*;
//import greendroid.widget.LoaderActionBarItem;

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
import org.exoplatform.ui.social.SocialDetailActivity;
import org.exoplatform.ui.social.SocialItem;
import org.exoplatform.ui.social.SocialTabsActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;

/**
 * Adapter for activity item in activity stream
 *
 */
public class StandardArrayAdapter extends ArrayAdapter<SocialActivityInfo> {

  private final ArrayList<SocialActivityInfo> items;

  private Context                             mContext;

  private LayoutInflater                      mInflater;

  private ViewHolder                          holder = null;

  private LikeLoadTask                        mLoadTask;

  private static final String TAG = "eXo____StandardArrayAdapter____";

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
      holder.imageViewAvatar = (ShaderImageView) convertView.findViewById(R.id.imageView_Avatar);
      holder.imageViewAvatar.setDefaultImageResource(R.drawable.default_avatar);
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

    SocialItem socialItem = new SocialItem(mContext, holder, actInfo, false);
    socialItem.initCommonInfo();

    /** Click on content to open activity detail */
    holder.contentLayoutWrap.setOnClickListener(new OnClickListener() {

      public void onClick(View v) {
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
      //mLoadTask = (LikeLoadTask) new LikeLoadTask(SocialTabsActivity.instance.loaderItem, position).execute(info);
      mLoadTask = (LikeLoadTask) new LikeLoadTask(position).execute(info);
    }
  }

  public static class ViewHolder {
    public LinearLayout    contentLayoutWrap;

    public ShaderImageView imageViewAvatar;

    public TextView        textViewName;

    public TextView        textViewMessage;

    public TextView        textViewTempMessage;

    public TextView        textViewCommnet;

    public Button          buttonComment;

    public Button          buttonLike;

    public ImageView       typeImageView;

    public TextView        textViewTime;

    public View            attachStubView;
  }

  private class LikeLoadTask extends AsyncTask<SocialActivityInfo, Void, Boolean> {

    //private LoaderActionBarItem loaderItem;

    private int                 currentPosition;

    /**
    public LikeLoadTask(LoaderActionBarItem item, int pos) {
      loaderItem = item;
      currentPosition = pos;
    }
     **/

    public LikeLoadTask(int pos) {
      currentPosition = pos;
    }

    @Override
    protected void onPreExecute() {
      super.onPreExecute();
      //loaderItem.setLoading(true);
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
        return false;
      } catch (RuntimeException e) {
        return false;
      }
    }

    @Override
    protected void onPostExecute(Boolean result) {
      //loaderItem.setLoading(false);
      if (result) {
        if (SocialTabsActivity.instance != null) {
          int tabId = SocialTabsActivity.instance.mPager.getCurrentItem();
          switch (tabId) {
          case SocialTabsActivity.ALL_UPDATES:

            AllUpdatesFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY,
                                                      true,
                                                      currentPosition);
            if (AllUpdatesFragment.instance.isLoading())
              holder.buttonLike.setClickable(false);
            else
              holder.buttonLike.setClickable(true);
            break;
          case SocialTabsActivity.MY_CONNECTIONS:
            MyConnectionsFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY,
                                                         true,
                                                         currentPosition);
            if (MyConnectionsFragment.instance.isLoading())
              holder.buttonLike.setClickable(false);
            else
              holder.buttonLike.setClickable(true);
            break;
          case SocialTabsActivity.MY_SPACES:
            MySpacesFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY,
                                                    true,
                                                    currentPosition);
            if (MySpacesFragment.instance.isLoading())
              holder.buttonLike.setClickable(false);
            else
              holder.buttonLike.setClickable(true);
            break;
          case SocialTabsActivity.MY_STATUS:
            MyStatusFragment.instance.onPrepareLoad(ExoConstants.NUMBER_OF_ACTIVITY,
                                                    true,
                                                    currentPosition);
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
