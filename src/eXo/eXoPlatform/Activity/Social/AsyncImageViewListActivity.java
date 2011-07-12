package eXo.eXoPlatform.Activity.Social;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.cyrilmottier.android.greendroid.R;

import eXo.eXoPlatform.Activity.Home.eXoApplicationsController2;
import eXo.eXoPlatform.GDSubClasses.MyListActivity;
import eXo.eXoPlatform.mocks.Mock_Activity;
import eXo.eXoPlatform.mocks.Mock_Social_Activity;
import eXo.eXoPlatform.utils.eXoConstants;
import greendroid.image.ImageProcessor;
import greendroid.widget.ActionBarItem;
import greendroid.widget.AsyncImageView;

public class AsyncImageViewListActivity extends MyListActivity implements OnScrollListener {

  public static AsyncImageViewListActivity asyncImageViewListActivityInstance;

  Mock_Social_Activity                     mock;

  Mock_Activity                            selectedActivity;

  private Resources                        resource;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setTheme(R.style.Theme_eXo);

    asyncImageViewListActivityInstance = this;
    setTitle("Activity Streams");

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    addActionBarItem();
    getActionBar().getItem(0).setDrawable(R.drawable.gd_action_bar_compose);
    resource = getResources();
    mock = new Mock_Social_Activity(false);

    getListView().setDivider(null);
    getListView().setDividerHeight(0);

    setListAdapter(new MyAdapter(this, mock));
    // getListView().setOnScrollListener(this);
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      finish();
      break;
    case 0:
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(eXoConstants.COMPOSE_TYPE, eXoConstants.COMPOSE_POST_TYPE);
      startActivity(intent);

      break;

    default:
      // Log.e("12312", "13123");

    }
    return true;

  }

  /**
   * Gets prettyTime by timestamp.
   * 
   * @param resourceBundle
   * @param postedTime
   * @return String
   */
  public String getPostedTimeString(long postedTime) {
    // long currentTime = new Date().getTime();
    long time = (new Date().getTime() - postedTime) / 1000;
    long value;
    if (time < 60) {
      return "Less Than A Minute";
    } else {
      if (time < 120) {
        return "A Minute Ago";
      } else {
        if (time < 3600) {
          value = Math.round(time / 60);
          return "About " + String.valueOf(value) + " Minutes";
        } else {
          if (time < 7200) {
            return "An Hour Ago";
          } else {
            if (time < 86400) {
              value = Math.round(time / 3600);
              return String.valueOf(value) + " Hours Ago";
            } else {
              if (time < 172800) {
                return "A Day Ago";
              } else {
                if (time < 2592000) {
                  value = Math.round(time / 86400);
                  return String.valueOf(value) + " Days Ago";
                } else {
                  if (time < 5184000) {
                    return "A Month Ago";
                  } else {
                    value = Math.round(time / 2592000);
                    return String.valueOf(value) + " Months Ago";
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private class MyAdapter extends BaseAdapter implements ImageProcessor {

//    private final StringBuilder               BUILDER                  = new StringBuilder();

    ArrayList<String>                         _arrayOfSectionsTitle;

    HashMap<String, ArrayList<Mock_Activity>> _sortedActivities;

    ArrayList<Mock_Activity>                  _arrayOfActivityListView = new ArrayList<Mock_Activity>();

    private final Paint                       mPaint                   = new Paint(Paint.ANTI_ALIAS_FLAG);

    private final Rect                        mRectSrc                 = new Rect();

    private final Rect                        mRectDest                = new Rect();

    // private final String mImageForPosition;

    class ViewHolder {

      public AsyncImageView imageViewAvatar;

      public TextView       textViewName;

      public TextView       textViewMessage;

      public TextView       buttonComment;

      public TextView       buttonLike;

      public TextView       textViewTime;

      public TextView       textViewShowMore;

      // public StringBuilder textBuilder = new StringBuilder();
    }

    private Bitmap         mMask;

    private int            mThumbnailSize;

    private int            mThumbnailRadius;

    private LayoutInflater mInflater;

    Mock_Social_Activity   mock;

    public MyAdapter(Context context, Mock_Social_Activity _mock) {
      mInflater = LayoutInflater.from(context);

      mock = _mock;

      sortActivities();

      for (int i = 0; i < _arrayOfSectionsTitle.size(); i++) {
        String strHeader = _arrayOfSectionsTitle.get(i);
        Mock_Activity activity = new Mock_Activity();
        activity.isHeader = true;
        activity.title = _arrayOfSectionsTitle.get(i);
        _arrayOfActivityListView.add(activity);
        ArrayList<Mock_Activity> array = _sortedActivities.get(strHeader);
        for (int j = 0; j < array.size(); j++) {
          _arrayOfActivityListView.add(array.get(j));
        }

      }

      Mock_Activity activity = new Mock_Activity();
      activity.isShowMore = true;
      _arrayOfActivityListView.add(activity);

      // mImageForPosition = context.getString(R.string.image_for_position);

      mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

      mThumbnailSize = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_size);
      mThumbnailRadius = context.getResources().getDimensionPixelSize(R.dimen.thumbnail_radius);

      prepareMask();
    }

    private void prepareMask() {
      mMask = Bitmap.createBitmap(mThumbnailSize, mThumbnailSize, Bitmap.Config.ARGB_8888);

      Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
      paint.setColor(Color.RED);
      paint.setStyle(Paint.Style.FILL_AND_STROKE);

      Canvas c = new Canvas(mMask);
      c.drawRoundRect(new RectF(0, 0, mThumbnailSize, mThumbnailSize),
                      mThumbnailRadius,
                      mThumbnailRadius,
                      paint);
    }

    public int getCount() {
      return _arrayOfActivityListView.size();
    }

    public Object getItem(int position) {
      return null;
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

      ViewHolder holder = null;

      final Mock_Activity activity = _arrayOfActivityListView.get(position);
      
      RelativeLayout imgShadowView;
      
      // if (convertView == null) {
      if (activity.isHeader) {
        
        convertView = mInflater.inflate(R.layout.activityheadersection, parent, false);
        TextView title = (TextView) convertView.findViewById(R.id.textView_Section_Title);
        if (position == 0) {
          title.setBackgroundDrawable(resource.getDrawable(R.drawable.social_activity_browse_header_highlighted_bg));
        } else {
          title.setBackgroundDrawable(resource.getDrawable(R.drawable.social_activity_browse_header_normal_bg));
        }
        title.setText(activity.title);
      } else {
        convertView = mInflater.inflate(R.layout.activitybrowserviewcell, parent, false);
        holder = new ViewHolder();

        holder.imageViewAvatar = (AsyncImageView) convertView.findViewById(R.id.imageView_Avatar);
        holder.imageViewAvatar.setImageProcessor(this);

        imgShadowView = (RelativeLayout) convertView.findViewById(R.id.imageView_Avatar_wrap);
        
        holder.textViewName = (TextView) convertView.findViewById(R.id.textView_Name);

        holder.textViewMessage = (TextView) convertView.findViewById(R.id.textView_Message);

        holder.buttonComment = (TextView) convertView.findViewById(R.id.button_Comment);

        holder.buttonLike = (TextView) convertView.findViewById(R.id.button_Like);

        holder.textViewTime = (TextView) convertView.findViewById(R.id.textView_Time);

        holder.textViewShowMore = (TextView) convertView.findViewById(R.id.textView_Show_More);

        if (activity.isShowMore) {
          holder.textViewShowMore.setVisibility(View.VISIBLE);

          LayoutParams params = convertView.getLayoutParams();
          params.height = 40;
          convertView.setLayoutParams(params);
          imgShadowView.setVisibility(View.INVISIBLE);
          holder.imageViewAvatar.setVisibility(View.INVISIBLE);
          holder.textViewName.setVisibility(View.INVISIBLE);
          holder.textViewMessage.setVisibility(View.INVISIBLE);
          holder.buttonComment.setVisibility(View.INVISIBLE);
          holder.buttonLike.setVisibility(View.INVISIBLE);
          holder.textViewTime.setVisibility(View.INVISIBLE);
          RelativeLayout showMoreBg = (RelativeLayout) convertView.findViewById(R.id.relativeLayout_Content);
          showMoreBg.setBackgroundDrawable(null);

          convertView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

              Log.e("Show more", "No more activity");

            }
          });

        } else {
//          BUILDER.setLength(0);
//          BUILDER.append(mock.mStrings[position]);
          holder.imageViewAvatar.setUrl(activity.imageUrl);

          holder.textViewName.setText(activity.userID);
          holder.textViewMessage.setText(activity.title);
          
          String strNbComment = Integer.toString(activity.nbComments);
          if(strNbComment.length() > 2)
          {
            LayoutParams params = holder.buttonComment.getLayoutParams();
            params.width = 50;
            holder.buttonComment.setLayoutParams(params);
          }
          holder.buttonComment.setText(Integer.toString(activity.nbComments));
          
          String strNbLike = Integer.toString(activity.nbLikes);
          if(strNbLike.length() > 2)
          {
            LayoutParams params = holder.buttonLike.getLayoutParams();
            params.width = 50;
            holder.buttonLike.setLayoutParams(params);
          }
          holder.buttonLike.setText(Integer.toString(activity.nbLikes));
          holder.textViewTime.setText(getPostedTimeString(activity.postedTime));

          convertView.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {

              // GDActivity.TYPE = 1;
              selectedActivity = activity;
              Intent next = new Intent(asyncImageViewListActivityInstance,
                                       ActivityStreamDisplay.class);
              asyncImageViewListActivityInstance.startActivity(next);
            }
          });

        }
        // convertView.setTag(holder);
      }

      // } else {
      // // holder = (ViewHolder) convertView.getTag();
      // }

      return convertView;
    }

    public Bitmap processImage(Bitmap bitmap) {
      Bitmap result = Bitmap.createBitmap(mThumbnailSize, mThumbnailSize, Bitmap.Config.ARGB_8888);
      Canvas c = new Canvas(result);

      mRectSrc.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
      mRectDest.set(0, 0, mThumbnailSize, mThumbnailSize);
      c.drawBitmap(bitmap, mRectSrc, mRectDest, null);
      c.drawBitmap(mMask, 0, 0, mPaint);

      return result;
    }

    public void sortActivities() {

      _arrayOfSectionsTitle = new ArrayList<String>();

      _sortedActivities = new HashMap<String, ArrayList<Mock_Activity>>();

      // Browse each activities
      for (int i = 0; i < mock.arrayOfActivities.size(); i++) {
        Mock_Activity a = mock.arrayOfActivities.get(i);
        // long time = (new Date().getTime() - a.postedTime) / 1000;
        String strSection = getPostedTimeString(a.postedTime);
        // Check activities of today
        if (strSection.contains("Minute") || strSection.contains("Hours")) {

          // Search the current array of activities for today
          ArrayList<Mock_Activity> arrayOfToday = _sortedActivities.get("Today");

          // if the array not yet exist, we create it
          if (arrayOfToday == null) {
            // create the array
            arrayOfToday = new ArrayList<Mock_Activity>();
            // set it into the dictonary
            _sortedActivities.put("Today", arrayOfToday);

            // set the key to the array of sections title
            _arrayOfSectionsTitle.add("Today");
          }

          // finally add the object to the array
          arrayOfToday.add(a);

        } else {

          // Search the current array of activities for current key
          ArrayList<Mock_Activity> arrayOfCurrentKeys = _sortedActivities.get(strSection);

          // if the array not yet exist, we create it
          if (arrayOfCurrentKeys == null) {
            // create the array
            arrayOfCurrentKeys = new ArrayList<Mock_Activity>();
            // set it into the dictonary
            _sortedActivities.put(strSection, arrayOfCurrentKeys);

            // set the key to the array of sections title
            _arrayOfSectionsTitle.add(strSection);
          }

          // finally add the object to the array
          arrayOfCurrentKeys.add(a);

        }

      }
    }

  }

  public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
  }

  public void onScrollStateChanged(AbsListView listView, int scrollState) {
    if (getListView() == listView) {
      searchAsyncImageViews(listView, scrollState == OnScrollListener.SCROLL_STATE_FLING);
    }
  }

  private void searchAsyncImageViews(ViewGroup viewGroup, boolean pause) {
    final int childCount = viewGroup.getChildCount();
    for (int i = 0; i < childCount; i++) {
      AsyncImageView image = (AsyncImageView) viewGroup.getChildAt(i)
                                                       .findViewById(R.id.imageView_Avatar);
      if (image != null) {
        image.setPaused(pause);
      }
    }
  }

  public void finishMe() {

    // GDActivity.TYPE = 0;
    //
    Intent next = new Intent(asyncImageViewListActivityInstance, eXoApplicationsController2.class);
    startActivity(next);
    // asyncImageViewListActivityInstance = null;

  }

  @Override
  public void onBackPressed() {
    // TODO Auto-generated method stub
    super.onBackPressed();
    finish();
  }
}
