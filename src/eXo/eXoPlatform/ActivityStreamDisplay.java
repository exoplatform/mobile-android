package eXo.eXoPlatform;

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

import java.util.ArrayList;
import java.util.ResourceBundle;

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
import android.widget.ListView;
import android.widget.TextView;

//Chat list view controller
public class ActivityStreamDisplay extends GDActivity implements
		OnClickListener {

	// Activity cell info object
	class ActivityDisplayInfo {

		Bitmap _bmAvatar; // Avatar
		String _strName; // Name
		String _strMessage; // Message

		ActivityDisplayInfo() {

		}

	}

	private ListView _lvActivityDisplayComment;

	public static ActivityStreamDisplay activityStreamDisplayInstance; // Instance

	private BaseAdapter adapter;

	private EditText editTextComment;

	// ArrayList<ActivityDisplayInfo> _arrActivity = new
	// ArrayList<ActivityDisplayInfo>();
	Mock_Social_Activity mock;

	// Constructor
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setActionBarContentView(R.layout.activitydisplayview);
		// setContentView(R.layout.socialbrowserview);

		activityStreamDisplayInstance = this;

		_lvActivityDisplayComment = (ListView) findViewById(R.id.listView_Comment);

		changeLanguage(AppController.bundle);
		// for (int i = 0; i < 5; i++) {
		//
		// ActivityDisplayInfo activity = new ActivityDisplayInfo();
		//
		// activity._bmAvatar = BitmapFactory.decodeResource(getResources(),
		// R.drawable.homeactivitystreamsiconiphone);
		// activity._strName = "Some text";
		// activity._strMessage = "A seconline of text";
		//
		// _arrActivity.add(activity);
		//
		// }

		mock = new Mock_Social_Activity(true);

		createActivityAdapter();
		initTextComment();
	}

	private void initTextComment() {
		editTextComment = (EditText) findViewById(R.id.editText_Comment);
		editTextComment.setOnClickListener(this);
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

	public void finishMe() {

		GDActivity.TYPE = 1;
		//
		Intent next = new Intent(activityStreamDisplayInstance,
				AsyncImageViewListActivity.class);
		startActivity(next);
		activityStreamDisplayInstance = null;

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
				View rowView = inflater.inflate(
						R.layout.activitydisplayviewcell, parent, false);

				// ActivityDisplayInfo activity = _arrActivity.get(position);

				ImageView imageViewAvatar = (ImageView) rowView
						.findViewById(R.id.imageView_Avatar);
				// imageViewAvatar.setImageBitmap(activity._bmAvatar);
				imageViewAvatar.setImageBitmap(BitmapFactory.decodeResource(
						getResources(),
						R.drawable.homeactivitystreamsiconiphone));

				TextView textViewName = (TextView) rowView
						.findViewById(R.id.textView_Name);
				textViewName
						.setText(mock.arrayOfActivityComments.get(position).statusID);

				TextView textViewMessage = (TextView) rowView
						.findViewById(R.id.textView_Message);
				textViewMessage.setText(mock.arrayOfActivityComments
						.get(position).title);

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
				return mock.arrayOfActivityComments.get(position);
			}

			public int getCount() {
				// TODO Auto-generated method stub
				return mock.arrayOfActivityComments.size();
			}
		};

		_lvActivityDisplayComment.setAdapter(adapter);
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

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		if (view == editTextComment) {
			Intent intent = new Intent(this, ComposeMessageActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putExtra(eXoConstants.COMPOSE_TYPE, 1);
			startActivity(intent);
		}
	}
}
