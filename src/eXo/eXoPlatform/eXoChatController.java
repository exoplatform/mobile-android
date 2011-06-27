package eXo.eXoPlatform;

import greendroid.app.GDActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//Chat windows view controller
public class eXoChatController extends GDActivity {
  // List message content for each user
  public static List<eXoChatMessageContent> listChatContent = new ArrayList<eXoChatMessageContent>();

  public static String                      currentChatStr  = "";                                    // Current

  // chat
  // user

  EditText                                  messageEditText;                                         // Chat

  // text
  // field

  Button                                    sendMessageBtn;                                          // Send

  // button

  static ListView                           conversationView;                                        // Chat

  // conversation
  public static eXoChatController           eXoChatControllerInstance;                               // Instance

  static eXoApplicationsController2         _delegate;                                               // Main

  // app
  // view
  // controller

  // Receive message
  public static PacketListener              packetListener;

  private static Handler                    mHandler;

  private static Runnable                   runnable;

  private String                            fromChatStr;                                             // Source

  String                                    strCannotBackToPreviousPage;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    setActionBarContentView(R.layout.exochat);
    eXoChatControllerInstance = this;

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
            eXoChatListController.conn.sendPacket(message);

            listChatContent.add(new eXoChatMessageContent("Me", msg));
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

          for (int i = 0; i < eXoChatListController.listChatRosterEntry.size(); i++) {
            fromChatStr = eXoChatListController.listChatRosterEntry.get(i).address;
            final String chatFromName = fromChatStr.substring(0, fromChatStr.lastIndexOf("@"));
            if (fromName.equalsIgnoreCase(fromChatStr)) {
              List<eXoChatMessageContent> msgContent = eXoChatListController.arrListChat.get(i);
              msgContent.add(new eXoChatMessageContent(chatFromName, message.getBody()));
              eXoChatListController.arrListChat.set(i, msgContent);

              if (fromName.equalsIgnoreCase(eXoChatController.currentChatStr)) {
                listChatContent = eXoChatListController.arrListChat.get(i);
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

    eXoChatListController.conn.addPacketListener(packetListener, filter);

    changeLanguage(AppController.bundle);

    setListAdapter();

  }

  // Keydown listener
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Save data to the server once the user hits the back button
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      Toast.makeText(eXoChatController.this, strCannotBackToPreviousPage, Toast.LENGTH_LONG).show();
    }
    return false;
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

      private void bindView(View view, eXoChatMessageContent msgContent) {
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
  public void changeLanguage(ResourceBundle resourceBundle) {

    String strsendMessageBtn = "";
    String strcloseBtn = "";
    try {
      strsendMessageBtn = new String(resourceBundle.getString("Send").getBytes("ISO-8859-1"),
                                     "UTF-8");
      strcloseBtn = new String(resourceBundle.getString("CloseButton").getBytes("ISO-8859-1"),
                               "UTF-8");
      strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage")
                                                             .getBytes("ISO-8859-1"), "UTF-8");
    } catch (Exception e) {

    }

    sendMessageBtn.setText(strsendMessageBtn);

    _delegate.changeLanguage(resourceBundle);
    // _delegate.createAdapter();

  }

  public void finishMe() {
    currentChatStr = "";
    // eXoChatList.arrListChat.set(eXoChatList.posOfChatingMember,
    // listChatContent);
    eXoChatListController.conn.removePacketListener(packetListener);
    // eXoChatList.conn.addPacketListener(eXoChatList.packetListener, new
    // MessageTypeFilter(Message.Type.chat));

    // eXoChat.this.finish();
    GDActivity.TYPE = 1;
    Intent next = new Intent(eXoChatController.this, eXoChatListController.class);
    eXoChatController.this.startActivity(next);

    eXoChatControllerInstance = null;
  }
}
