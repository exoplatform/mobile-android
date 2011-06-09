package eXo.eXoPlatform;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;

import java.util.ResourceBundle;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    
    Bitmap  _bmAvatar; // Avatar
    String  _strName; // Name
    String  _strMessage; // Message
    
    ActivityInfo()
    {
      
    }
    
  }

  private ListView                                     _lvActivity;                   

  public static ActivityStreamBrowser                  activityStreamBrowserInstance; // Instance

  static eXoApplicationsController2                     _delegate;                    // Main
                                                                                      // app
  private BaseAdapter adapter;
  
//  ArrayList<ActivityInfo> _arrActivity = new ArrayList<ActivityInfo>();
  Mock_Social_Activity mock;

  
  // Constructor
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    
    setActionBarContentView(R.layout.activitybrowserview);
//    setContentView(R.layout.socialbrowserview);

    activityStreamBrowserInstance = this;

    _lvActivity = (ListView) findViewById(R.id.listView_Avtivity);
    
    changeLanguage(AppController.bundle);
//    for (int i = 0; i < 5; i++) {
//    
//      ActivityInfo activity = new ActivityInfo();
//      
//      activity._bmAvatar = BitmapFactory.decodeResource(getResources(), R.drawable.homeactivitystreamsiconiphone);
//      activity._strName = "Name";
//      activity._strMessage = "Hi all. This message is for testing";
//      
//      _arrActivity.add(activity);
//      
//    }
    
    mock = new Mock_Social_Activity(false);
    
    createActivityAdapter();
    
  }
  
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
        case 0: 
            // your method here
            break;
 
        case 1: 
            // your method here
            break;
 
        default: 
            // home button is clicked
          finishMe();
          break;
    }
    
    return true;
}
  
 public void finishMe()
 {
//   GDActivity.TYPE = 0;
//   
//   Intent next = new Intent(eXoChatListController.this, eXoApplicationsController2.class);
//   startActivity(next);
//   eXoChatListControllerInstance = null;
   
 }

  // Keydown listener
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Save data to the server once the user hits the back button
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
//      Toast.makeText(eXoChatListController.this, strCannotBackToPreviousPage, Toast.LENGTH_SHORT)
//           .show();
    }
    return false;
  }

//  Create activity browser adapter
  public void createActivityAdapter()
  {
      adapter = new BaseAdapter() {
      
      public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final int pos = position;

        LayoutInflater inflater = getLayoutInflater();
        View rowView = inflater.inflate(R.layout.activitybrowserviewcell, parent, false);
                
        
        ImageView imageViewAvatar = (ImageView) rowView.findViewById(R.id.imageView_Avatar); 
        imageViewAvatar.setImageBitmap( BitmapFactory.decodeResource(getResources(), R.drawable.homeactivitystreamsiconiphone));
        
        TextView textViewName = (TextView) rowView.findViewById(R.id.textView_Name);
//        textViewName.setText(activity._strName);
        textViewName.setText(mock.arrayOfActivities.get(position).userID);
        
        TextView textViewMessage = (TextView) rowView.findViewById(R.id.textView_Message);
//        textViewMessage.setText(activity._strMessage);
        textViewMessage.setText(mock.arrayOfActivities.get(position).title);
        
        Button buttonComment = (Button) rowView.findViewById(R.id.button_Comment);
        buttonComment.setText(Integer.toString(mock.arrayOfActivities.get(position).nbComments));
        
        Button buttonLike = (Button) rowView.findViewById(R.id.button_Like);
        buttonLike.setText(Integer.toString(mock.arrayOfActivities.get(position).nbLikes));
        
        TextView textViewTime = (TextView) rowView.findViewById(R.id.textView_Time);
        textViewTime.setText(mock.arrayOfActivities.get(position).postedTime/60 + "minutes ago");
        
        TextView textViewShowMore = (TextView) rowView.findViewById(R.id.textView_Show_More);
        
        rowView.setOnClickListener(new View.OnClickListener() {
          
          public void onClick(View v) {
            
            GDActivity.TYPE = 1;
            
//          posOfChatingMember = position;
//          eXoChatController.currentChatStr = listChatRosterEntry.get(position).address;
//          eXoChatController.listChatContent = arrListChat.get(position);
//          conn.removePacketListener(packetListener);
  //
//          eXoChatController._delegate = _delegate;
          Intent next = new Intent(ActivityStreamBrowser.this, ActivityStreamDisplay.class);
          startActivity(next);
            
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
        return mock.arrayOfActivities.get(position);
      }
      
      public int getCount() {
        // TODO Auto-generated method stub
        return mock.arrayOfActivities.size();
      }
            
    };
  
    _lvActivity.setAdapter(adapter);
  }
  
  // Set language
  public void changeLanguage(ResourceBundle resourceBundle) {

    String strTitle = "Activity Stream";

    try {
//      strTitle = new String(resourceBundle.getString("ActivityStream").getBytes("ISO-8859-1"), "UTF-8");
//      strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage")
//                                                             .getBytes("ISO-8859-1"), "UTF-8");
    } catch (Exception e) {

    }

    setTitle(strTitle);

//    _delegate.changeLanguage(resourceBundle);
//    _delegate.createAdapter();
  }
}
