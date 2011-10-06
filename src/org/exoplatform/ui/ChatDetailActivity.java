package org.exoplatform.ui;

import greendroid.widget.ActionBarItem;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.model.ChatMemberInfo;
import org.exoplatform.model.ChatMessageContent;
import org.exoplatform.singleton.ChatServiceHelper;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.WarningDialog;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
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
public class ChatDetailActivity extends MyActionBar {
  // List message content for each user
  public static List<ChatMessageContent> listChatContent = new ArrayList<ChatMessageContent>();

  public static String                   currentChatStr  = "";                                 // Current

  // chat
  // user

  private EditText                       messageEditText;                                      // Chat

  // text
  // field

  private Button                         sendMessageBtn;                                       // Send

  // button

  private static ListView                conversationView;                                     // Chat

  // conversation
  public static ChatDetailActivity        eXoChatControllerInstance;                            // Instance

  // app
  // view
  // controller

  // Receive message
  public static PacketListener           packetListener;

  private static Handler                 mHandler;

  private static Runnable                runnable;

  private String                         meText;

  private String                         titleString;

  private String                         okString;

  private String                         messageFromText;

  private String                         fromChatStr;                                          // Source

  private XMPPConnection                 connection;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setTheme(R.style.Theme_eXo);

    setActionBarContentView(R.layout.exochat);
    eXoChatControllerInstance = this;

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    connection = ChatServiceHelper.getInstance().getXMPPConnection();

    messageEditText = (EditText) findViewById(R.id.message);

    conversationView = (ListView) findViewById(R.id.chatContent);
    conversationView.setDivider(null);
    conversationView.setDividerHeight(0);
    sendMessageBtn = (Button) findViewById(R.id.Send);
    changeLanguage();

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

    sendMessageBtn.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {

        // Send a message using content of the edit text widget
        String msg = messageEditText.getText().toString();
        if (msg != null && !msg.equalsIgnoreCase("")) {
          try {

            Message message = new Message(currentChatStr, Message.Type.chat);
            message.setBody(msg);
            connection.sendPacket(message);

            listChatContent.add(new ChatMessageContent(meText, msg));
            messageEditText.setText("");
            
            setListAdapter();

          } catch (Exception e) {
            WarningDialog warning = new WarningDialog(getApplicationContext(),
                                                      titleString,
                                                      e.getMessage(),
                                                      okString);
            warning.show();
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
          ArrayList<ChatMemberInfo> listChatRosterEntry = ChatServiceHelper.getInstance()
                                                                           .getChatListRosterEntry();
          ArrayList<List<ChatMessageContent>> arrListChat = ChatServiceHelper.getInstance()
                                                                             .getListChat();
          for (int i = 0; i < listChatRosterEntry.size(); i++) {
            fromChatStr = listChatRosterEntry.get(i).address;
            final String chatFromName = fromChatStr.substring(0, fromChatStr.lastIndexOf("@"));
            if (fromName.equalsIgnoreCase(fromChatStr)) {
              List<ChatMessageContent> msgContent = arrListChat.get(i);
              msgContent.add(new ChatMessageContent(chatFromName, message.getBody()));
              arrListChat.set(i, msgContent);

              if (fromName.equalsIgnoreCase(currentChatStr)) {
                listChatContent = arrListChat.get(i);
              } else {
                runOnUiThread(new Runnable() {

                  public void run() {
                    Toast msg = Toast.makeText(getApplicationContext(), messageFromText + " "
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
    ChatServiceHelper.getInstance().setPacketListener(packetListener);
    ChatServiceHelper.getInstance().getXMPPConnection().addPacketListener(packetListener, filter);

    setListAdapter();

  }

  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {
    case -1:
      if (ChatListActivity.chatListActivity != null) {
        ChatListActivity.chatListActivity.finish();
      }
      finishMe();
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
  public void setListAdapter() {
    BaseAdapter adapter = new BaseAdapter() {

      public View getView(int position, View convertView, ViewGroup parent) {
        
        LayoutInflater inflater = eXoChatControllerInstance.getLayoutInflater();
        
        View rowView = null;
        
        ChatMessageContent msgContent = listChatContent.get(position);
        if(msgContent.name.equalsIgnoreCase(meText))
          rowView = inflater.inflate(R.layout.exochatmessagecontentitemme, parent, false);
        else
          rowView = inflater.inflate(R.layout.exochatmessagecontentitem, parent, false);
        
        bindView(rowView, msgContent);
        return (rowView);
      }

      private void bindView(View view, ChatMessageContent msgContent) {
        
        TextView content = (TextView) view.findViewById(R.id.TextView_Content);
        content.setText(msgContent.content);
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
  private void changeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    String strsendMessageBtn = bundle.getString("Send");
    meText = bundle.getString("Me");
    messageFromText = bundle.getString("MessageFrom");
    titleString = bundle.getString("Warning");
    okString = bundle.getString("OK");
    sendMessageBtn.setText(strsendMessageBtn);

  }

  public void finishMe() {
    currentChatStr = "";
    ChatServiceHelper.getInstance().getXMPPConnection().removePacketListener(packetListener);
    finish();
  }
}
