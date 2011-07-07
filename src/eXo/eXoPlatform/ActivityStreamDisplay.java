package eXo.eXoPlatform;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.AsyncImageView;
import greendroid.widget.ActionBarItem.Type;

import java.util.ArrayList;
import java.util.ResourceBundle;

import com.cyrilmottier.android.greendroid.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

//Chat list view controller
public class ActivityStreamDisplay extends MyActionBar implements OnClickListener {

  // Activity cell info object
  class ActivityDisplayInfo {

    Bitmap _bmAvatar;  // Avatar

    String _strName;   // Name

    String _strMessage; // Message

    ActivityDisplayInfo() {

    }

  }

  // private ListView _lvActivityDisplayComment;

  private LinearLayout                commentLayoutWrap;

  public static ActivityStreamDisplay activityStreamDisplayInstance; // Instance

  private BaseAdapter                 adapter;

  private EditText                    editTextComment;

  Mock_Activity_Detail                activityDetail;

  Mock_Activity                       selectedActivity;

  Mock_Social_Activity                mock;

  // Constructor
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo_Back);
    setActionBarContentView(R.layout.activity_display_view);
    // setContentView(R.layout.socialbrowserview);

    activityStreamDisplayInstance = this;

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    mock = new Mock_Social_Activity(true);

    activityDetail = mock.activityDetail;

    selectedActivity = AsyncImageViewListActivity.asyncImageViewListActivityInstance.selectedActivity;

    AsyncImageView imageView_Avatar = (AsyncImageView) findViewById(R.id.imageView_Avatar);
    imageView_Avatar.setUrl(selectedActivity.imageUrl);

    TextView textView_Name = (TextView) findViewById(R.id.textView_Name);
    textView_Name.setText(selectedActivity.userID);
    TextView textView_Message = (TextView) findViewById(R.id.textView_Message);
    textView_Message.setText(selectedActivity.title);
    TextView textView_Time = (TextView) findViewById(R.id.textView_Time);
    textView_Time.setText(AsyncImageViewListActivity.asyncImageViewListActivityInstance.getPostedTimeString(selectedActivity.postedTime));

    commentLayoutWrap = (LinearLayout) findViewById(R.id.activity_display_comment_wrap);

    TextView textView_Like_Count = (TextView) findViewById(R.id.textView_Like_Count);
    StringBuffer buffer = new StringBuffer();
    int count = activityDetail.arrLikes.size();

    if (count == 0) {
      buffer.append("No like for the moment");
    } else if (count == 1) {
      Mock_Activity activity = (Mock_Activity) activityDetail.arrLikes.get(0);
      buffer.append(activity.userID);
      buffer.append(" liked this");
    } else if (count < 4) {
      for (int i = 0; i < count - 1; i++) {
        Mock_Activity activity = (Mock_Activity) activityDetail.arrLikes.get(i);
        buffer.append(activity.userID);
        buffer.append(", ");
      }
      buffer.deleteCharAt(buffer.length() - 2);
      Mock_Activity activity = (Mock_Activity) activityDetail.arrLikes.get(count - 1);
      buffer.append("and ");
      buffer.append(activity.userID);
      buffer.append(" liked this");
    } else {
      for (int i = 0; i < 3; i++) {
        Mock_Activity activity = (Mock_Activity) activityDetail.arrLikes.get(i);
        buffer.append(activity.userID);
        buffer.append(", ");
      }
      buffer.deleteCharAt(buffer.length() - 2);
      int remain = count - 3;
      buffer.append("and ");
      buffer.append(remain);
      if (remain > 1) {
        buffer.append(" peoples liked this");
      } else
        buffer.append(" people liked this");

    }

    textView_Like_Count.setText(buffer.toString());

    changeLanguage(AppController.bundle);

    // createActivityAdapter();
    createCommentList();
    initTextComment();
  }

  private void initTextComment() {
    editTextComment = (EditText) findViewById(R.id.editText_Comment);
    editTextComment.setOnClickListener(this);
  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      finish();
      // your method here
      break;
    case 0:
      // your method here
      break;

    case 1:
      // your method here
      break;

    default:
      // home button is clicked
      // finishMe();
      break;
    }

    return super.onHandleActionBarItemClick(item, position);
  }

  public void finishMe() {

    // GDActivity.TYPE = 1;
    Intent next = new Intent(activityStreamDisplayInstance, AsyncImageViewListActivity.class);
    startActivity(next);
    activityStreamDisplayInstance = null;

  }

  private void createCommentList() {
    if (activityDetail.arrComments != null) {

      LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

      for (Mock_Activity activity : activityDetail.arrComments) {
        CommentItemLayout commentItem = new CommentItemLayout(this);
        commentItem.comAvatarImage.setUrl(activity.imageUrl);
        commentItem.comTextViewName.setText(activity.userID);
        commentItem.comTextViewMessage.setText(activity.title);

        commentLayoutWrap.addView(commentItem, params);

      }
    }

  }

  // Create activity browser adapter
  public void createActivityAdapter() {
    adapter = new BaseAdapter() {

      public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final int pos = position;

        LayoutInflater inflater = getLayoutInflater();
        View rowView = inflater.inflate(R.layout.activitydisplayviewcell, parent, false);

        Mock_Activity activity = activityDetail.arrComments.get(position);

        AsyncImageView imageViewAvatar = (AsyncImageView) rowView.findViewById(R.id.imageView_Avatar);
        imageViewAvatar.setUrl(activity.imageUrl);
        // imageViewAvatar.setImageBitmap(BitmapFactory.decodeResource(
        // getResources(),
        // R.drawable.homeactivitystreamsiconiphone));

        TextView textViewName = (TextView) rowView.findViewById(R.id.textView_Name);
        textViewName.setText(activity.userID);

        TextView textViewMessage = (TextView) rowView.findViewById(R.id.textView_Message);
        textViewMessage.setText(activity.title);

        // Button buttonComment = (Button)
        // findViewById(R.id.button_Comment);
        // Button buttonLike = (Button) findViewById(R.id.button_Like);
        // TextView textViewTime = (TextView)
        // findViewById(R.id.textView_Time);
        // TextView textViewShowMore = (TextView)
        // findViewById(R.id.textView_Show_More);
        //
        return (rowView);

      }

      public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
      }

      public Object getItem(int position) {
        // TODO Auto-generated method stub
        return mock.activityDetail.arrComments.get(position);
      }

      public int getCount() {
        // TODO Auto-generated method stub
        Log.e("Count", Integer.toString(mock.activityDetail.arrComments.size()));
        return mock.activityDetail.arrComments.size();
      }
    };

    // _lvActivityDisplayComment.setAdapter(adapter);
  }

  // Set language
  public void changeLanguage(ResourceBundle resourceBundle) {

    String strTitle = "Activity Detail";

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

  public void onClick(View view) {
    // TODO Auto-generated method stub
    if (view == editTextComment) {
      Intent intent = new Intent(this, ComposeMessageActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      intent.putExtra(eXoConstants.COMPOSE_TYPE, eXoConstants.COMPOSE_COMMENT_TYPE);
      startActivity(intent);
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    activityStreamDisplayInstance = null;
    finishFromChild(this);
  }

  // Comment item layout

  private class CommentItemLayout extends RelativeLayout {
    private AsyncImageView comAvatarImage;

    private TextView       comTextViewName;

    private TextView       comTextViewMessage;

    public CommentItemLayout(Context context) {
      super(context);

      LayoutInflater inflate = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View view = inflate.inflate(R.layout.activitydisplayviewcell, this);
      comAvatarImage = (AsyncImageView) view.findViewById(R.id.imageView_Avatar);
      comTextViewName = (TextView) view.findViewById(R.id.textView_Name);
      comTextViewMessage = (TextView) view.findViewById(R.id.textView_Message);
    }
  }
}
