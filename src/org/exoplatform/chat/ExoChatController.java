package org.exoplatform.chat;

import greendroid.widget.ActionBarItem;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.model.ChatMessageContent;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.widget.MyActionBar;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cyrilmottier.android.greendroid.R;

//Chat windows view controller
public class ExoChatController extends MyActionBar {
  // List message content for each user
  public static List<ChatMessageContent> listChatContent = new ArrayList<ChatMessageContent>();

  public static String                      currentChatStr  = "";                                    // Current

  // chat
  // user

  private EditText                          messageEditText;                                         // Chat

  // text
  // field

  private Button                            sendMessageBtn;                                          // Send

  // button

  private static ListView                   conversationView;                                        // Chat

  // conversation
  public static ExoChatController           eXoChatControllerInstance;                               // Instance

  // app
  // view
  // controller

  // Receive message
  public static PacketListener              packetListener;

  private static Handler                    mHandler;

  private static Runnable                   runnable;

  private String                            fromChatStr;                                             // Source

  private String                            strCannotBackToPreviousPage;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);

    setActionBarContentView(R.layout.exochat);
    eXoChatControllerInstance = this;

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    // addActionBarItem();
    // getActionBar().getItem(0).setDrawable(R.drawable.back);

    messageEditText = (EditText) findViewById(R.id.message);

    conversationView = (ListView) findViewById(R.id.chatContent);
    conversationView.setDivider(null);
    conversationView.setDividerHeight(0);

    String currentChatNickName = currentChatStr.substring(0, currentChatStr.lastIndexOf("@"));
    setTitle(currentChatNickName);

    if (mHandler == null)
      mHandler = new Handler();
    if (runnable == null) {
      runnable = new Runnable() {

        public void run() {

          setListAdapter();
        }
      };
    }

    sendMessageBtn = (Button) findViewById(R.id.Send);
    sendMessageBtn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {

        // Send a message using content of the edit text widget
        String msg = messageEditText.getText().toString();
        if (msg != null && !msg.equalsIgnoreCase("")) {
          try {

            Message message = new Message(currentChatStr, Message.Type.chat);
            message.setBody(msg);
            ExoChatListController.conn.sendPacket(message);

            listChatContent.add(new ChatMessageContent("Me", msg));
            messageEditText.setText("");
            setListAdapter();

          } catch (Exception e) {

          }
        }

      }
    });

    PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
    packetListener = new PacketListener() {

      public void processPacket(Packet arg0) {

        // check coming message
        Message message = (Message) arg0;
        if (message.getBody() != null) {

          String fromName = StringUtils.parseBareAddress(message.getFrom());

          for (int i = 0; i < ExoChatListController.listChatRosterEntry.size(); i++) {
            fromChatStr = ExoChatListController.listChatRosterEntry.get(i).address;
            final String chatFromName = fromChatStr.substring(0, fromChatStr.lastIndexOf("@"));
            if (fromName.equalsIgnoreCase(fromChatStr)) {
              List<ChatMessageContent> msgContent = ExoChatListController.arrListChat.get(i);
              msgContent.add(new ChatMessageContent(chatFromName, message.getBody()));
              ExoChatListController.arrListChat.set(i, msgContent);

              if (fromName.equalsIgnoreCase(ExoChatController.currentChatStr)) {
                listChatContent = ExoChatListController.arrListChat.get(i);
              } else {
                runOnUiThread(new Runnable() {

                  public void run() {
                    Toast msg = Toast.makeText(getApplicationContext(), "Message from "
                        + chatFromName, Toast.LENGTH_SHORT);
                    msg.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    msg.show();
                  }
                });
              }

              break;
            }
          }

          // Add the incoming message to the list view
          mHandler.post(runnable);

        }
      }
    };

    ExoChatListController.conn.addPacketListener(packetListener, filter);

    changeLanguage();

    setListAdapter();

  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      if (ExoChatListController.eXoChatListControllerInstance != null) {
        ExoChatListController.eXoChatListControllerInstance.finish();
      }
      finishMe();
      // your method here
      break;

    case 0:
      // your method here
      break;

    default:
      // home button is clicked
      break;
    }

    return true;
  }

  @Override
  public void onBackPressed() {
    finishMe();
  }

  // Create adapter for conversation list
  public static void setListAdapter() {
    BaseAdapter adapter = new BaseAdapter() {

      public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = eXoChatControllerInstance.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.exochatmessagecontentitem, parent, false);
        bindView(rowView, listChatContent.get(position));
        return (rowView);
      }

      private void bindView(View view, ChatMessageContent msgContent) {
        TextView name = (TextView) view.findViewById(R.id.TextView_Name);
        name.setText(msgContent.name);

        if (msgContent.name.equalsIgnoreCase("Me")) {
          // view.setBackgroundResource(R.drawable.chatbackgroundwhite);
        } else {
          // view.setBackgroundResource(R.drawable.chatbackgroundwhite);
          // view.setBackgroundResource(0xFFFFFFFF);
        }
        TextView content = (TextView) view.findViewById(R.id.TextView_Content);
        content.setText(msgContent.content);

        name.setBackgroundResource(R.drawable.chatnamebackground);
      }

      public long getItemId(int position) {

        return position;
      }

      public Object getItem(int position) {

        return position;
      }

      public int getCount() {

        return listChatContent.size();
      }
    };
    conversationView.setAdapter(adapter);
  }

  // Set language
  public void changeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    String strsendMessageBtn = "";
    String strcloseBtn = "";
    strsendMessageBtn = bundle.getString("Send");
    strcloseBtn = bundle.getString("CloseButton");
    strCannotBackToPreviousPage = bundle.getString("CannotBackToPreviousPage");

    sendMessageBtn.setText(strsendMessageBtn);

  }

  public void finishMe() {
    currentChatStr = "";
    ExoChatListController.conn.removePacketListener(packetListener);
    finish();
  }
}
