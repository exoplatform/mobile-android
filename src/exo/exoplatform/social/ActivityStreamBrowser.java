package org.exoplatform.social;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

import java.util.ResourceBundle;

import org.exoplatform.R;
import org.exoplatform.controller.AppController;
import org.exoplatform.controller.ExoApplicationsController2;
import org.exoplatform.mocks.Mock_Social_Activity;
import org.exoplatform.utils.ExoConstants;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

//Chat list view controller
public class ActivityStreamBrowser extends GDActivity {

  // Activity cell info object
  class ActivityInfo {

    Bitmap _bmAvatar;  // Avatar

    String _strName;   // Name

    String _strMessage; // Message

    ActivityInfo() {

    }

  }

  private ListView                    _lvActivity;

  public static ActivityStreamBrowser activityStreamBrowserInstance; // Instance

  static ExoApplicationsController2   _delegate;                    // Main app

  private BaseAdapter                 adapter;

  // ArrayList<ActivityInfo> _arrActivity = new ArrayList<ActivityInfo>();
  Mock_Social_Activity                mock;

  // Constructor
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setActionBarContentView(R.layout.activitybrowserview);

    addActionBarItem(Type.Add, R.drawable.gd_action_bar_add);
    // setContentView(R.layout.socialbrowserview);

    activityStreamBrowserInstance = this;

//    _lvActivity = (ListView) findViewById(R.id.listView_Avtivity);

    changeLanguage(AppController.bundle);
    // for (int i = 0; i < 5; i++) {
    //
    // ActivityInfo activity = new ActivityInfo();
    //
    // activity._bmAvatar = BitmapFactory.decodeResource(getResources(),
    // R.drawable.homeactivitystreamsiconiphone);
    // activity._strName = "Name";
    // activity._strMessage = "Hi all. This message is for testing";
    //
    // _arrActivity.add(activity);
    //
    // }

    mock = new Mock_Social_Activity(false);

    createActivityAdapter();

  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {

    switch (item.getItemId()) {
    
    case R.drawable.gd_action_bar_add:

      break;

    case R.id.action_bar_export:

      break;

    case R.drawable.gd_action_bar_compose:
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(ExoConstants.COMPOSE_TYPE, 0);
      startActivity(intent);

      break;

    default:
//      Log.e("12312", "13123");
    }

    return true;
  }

  public void finishMe() {

    Log.e("Close", "313");
    //
    Intent next = new Intent(activityStreamBrowserInstance, ExoApplicationsController2.class);
    startActivity(next);
    activityStreamBrowserInstance = null;

  }

  // Keydown listener
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Save data to the server once the user hits the back button
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      // Toast.makeText(eXoChatListController.this,
      // strCannotBackToPreviousPage, Toast.LENGTH_SHORT)
      // .show();
    }
    return false;
  }

  // Create activity browser adapter
  public void createActivityAdapter() {
    adapter = new BaseAdapter() {

      public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final int pos = position;

        LayoutInflater inflater = getLayoutInflater();
        View rowView = inflater.inflate(R.layout.activitybrowserviewcell, parent, false);

        ImageView imageViewAvatar = (ImageView) rowView.findViewById(R.id.imageView_Avatar);

        TextView textViewName = (TextView) rowView.findViewById(R.id.textView_Name);
        // textViewName.setText(activity._strName);

        TextView textViewMessage = (TextView) rowView.findViewById(R.id.textView_Message);
        // textViewMessage.setText(activity._strMessage);

        Button buttonComment = (Button) rowView.findViewById(R.id.button_Comment);

        Button buttonLike = (Button) rowView.findViewById(R.id.button_Like);

        TextView textViewTime = (TextView) rowView.findViewById(R.id.textView_Time);

        TextView textViewShowMore = (TextView) rowView.findViewById(R.id.textView_Show_More);

        if (position < mock.arrayOfActivities.size()) {
          imageViewAvatar.setImageBitmap(BitmapFactory.decodeResource(getResources(),
                                                                      R.drawable.homeactivitystreamsiconiphone));
          textViewName.setText(mock.arrayOfActivities.get(position).userID);
          textViewMessage.setText(mock.arrayOfActivities.get(position).title);
          buttonComment.setText(Integer.toString(mock.arrayOfActivities.get(position).nbComments));
          buttonLike.setText(Integer.toString(mock.arrayOfActivities.get(position).nbLikes));
          textViewTime.setText(mock.arrayOfActivities.get(position).postedTime / 60 + "minutes ago");
        } else {
          textViewShowMore.setVisibility(View.VISIBLE);

          LayoutParams params = rowView.getLayoutParams();
          params.height = 40;
          rowView.setLayoutParams(params);

          imageViewAvatar.setVisibility(View.INVISIBLE);
          textViewName.setVisibility(View.INVISIBLE);
          textViewMessage.setVisibility(View.INVISIBLE);
          buttonComment.setVisibility(View.INVISIBLE);
          buttonLike.setVisibility(View.INVISIBLE);
          textViewTime.setVisibility(View.INVISIBLE);
        }

        rowView.setOnClickListener(new View.OnClickListener() {

          public void onClick(View v) {

            if (pos == mock.arrayOfActivities.size()) {
              Log.e("Show more", "No more activity");
            } else {
//              GDActivity.TYPE = 1;

              Intent next = new Intent(ActivityStreamBrowser.this, ActivityStreamDisplay.class);
              startActivity(next);
            }
          }
        });

        return (rowView);

      }

      public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
      }

      public Object getItem(int position) {
        // TODO Auto-generated method stub
        // return mock.arrayOfActivities.get(position);
        return null;
      }

      public int getCount() {
        // TODO Auto-generated method stub
        return mock.arrayOfActivities.size() + 1;
      }

    };

    _lvActivity.setAdapter(adapter);
  }

  // Set language
  public void changeLanguage(ResourceBundle resourceBundle) {

    String strTitle = "Activity Stream";

    try {
      // strTitle = new
      // String(resourceBundle.getString("ActivityStream").getBytes("ISO-8859-1"),
      // "UTF-8");
      // strCannotBackToPreviousPage = new
      // String(resourceBundle.getString("CannotBackToPreviousPage")
      // .getBytes("ISO-8859-1"), "UTF-8");
    } catch (Exception e) {

    }

    setTitle(strTitle);

    // _delegate.changeLanguage(resourceBundle);
    // _delegate.createAdapter();
  }
}
