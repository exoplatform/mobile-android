package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import org.exoplatform.controller.chat.ChatListController;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.widget.MyActionBar;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.cyrilmottier.android.greendroid.R;

public class ChatListActivity extends MyActionBar {

  private ListView               lvChatList;

  public static ChatListActivity chatListActivity;

  private String                 emptyChatString;

  private View                   empty_stub;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.exochatlist);

    chatListActivity = this;

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    lvChatList = (ListView) findViewById(R.id.ListViewChatList);
    changeLanguage();
    ChatListController controller = new ChatListController(this, lvChatList);
    controller.onLoad();
  }

  private void changeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    String strTitle = bundle.getString("ChatTitle");
    setTitle(strTitle);
    emptyChatString = bundle.getString("EmptyContact");
  }

  // set empty view
  public void setEmptyView(int status) {
    if (empty_stub == null) {
      initStubView();
    }
    empty_stub.setVisibility(status);
  }

  private void initStubView() {
    empty_stub = ((ViewStub) findViewById(R.id.chat_empty_stub)).inflate();
    ImageView emptyImage = (ImageView) empty_stub.findViewById(R.id.empty_image);
    emptyImage.setBackgroundResource(R.drawable.icon_for_no_contact);
    TextView emptyStatus = (TextView) empty_stub.findViewById(R.id.empty_status);
    emptyStatus.setText(emptyChatString);
  }

  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      finish();
      break;
    }
    return true;
  }

  @Override
  public void onBackPressed() {
    finish();
  }

}