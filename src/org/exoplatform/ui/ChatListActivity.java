package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import org.exoplatform.controller.chat.ChatListController;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.widget.MyActionBar;

import android.os.Bundle;
import android.view.Window;
import android.widget.ListView;

import com.cyrilmottier.android.greendroid.R;

public class ChatListActivity extends MyActionBar {

  private ListView               lvChatList;

  public static ChatListActivity chatListActivity;

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
