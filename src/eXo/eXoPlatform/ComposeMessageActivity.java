package eXo.eXoPlatform;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

public class ComposeMessageActivity extends GDActivity implements
		OnClickListener {

	private int composeType;

	private EditText composeEditText;
	private Button sendButton;
	private Button cancelButton;
	
	private String composeMessage;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setActionBarContentView(R.layout.compose_message_layout);
		composeType = getIntent().getIntExtra(eXoConstants.COMPOSE_TYPE,
				composeType);
		if (composeType == 0) {
			setTitle("Status Update");
			addActionBarItem(Type.TakePhoto,
					R.drawable.gd_action_bar_take_photo);
		} else {
			setTitle("Comment");
		}
		initComponents();

	}

	private void initComponents() {
		composeEditText = (EditText) findViewById(R.id.compose_text_view);

		sendButton = (Button) findViewById(R.id.compose_send_button);
		sendButton.setOnClickListener(this);

		cancelButton = (Button) findViewById(R.id.compose_cancel_button);
		cancelButton.setOnClickListener(this);

	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case R.drawable.gd_action_bar_take_photo:

			break;
		}

		return super.onHandleActionBarItemClick(item, position);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		finish();

	}

	public void onClick(View view) {
		if (view == sendButton) {

		}

		if (view == cancelButton) {
			finish();
		}
	}

}
