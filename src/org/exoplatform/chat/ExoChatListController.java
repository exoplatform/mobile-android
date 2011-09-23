package org.exoplatform.chat;

import greendroid.widget.ActionBarItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.model.ChatMemberInfo;
import org.exoplatform.model.ChatMessageContent;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.widget.MyActionBar;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.cyrilmottier.android.greendroid.R;

//Chat list view controller
public class ExoChatListController extends MyActionBar {

  private ListView                                     lvChatList;                   // Chat

  // list
  // view

  public static ExoChatListController                  eXoChatListControllerInstance; // Instance

  public static XMPPConnection                         conn;                         // Interact

  // with
  // server

  public static List<ChatMemberInfo>                       listChatRosterEntry;          // Roster

  // array

  public static ArrayList<List<ChatMessageContent>> arrListChat = null;           // Chat

  // message
  // array

  private String                                       fromChatStr;                  // Source

  public static int                                    posOfChatingMember;           // User

  // index
  // Update
  // roster

  private Handler                                      mHandler    = new Handler();

  public static RosterListener                         rosterListener;

  public static PacketListener                         packetListener;

  private ExoChatListAdapter                           chatsAdapter;

  private String                                       strCannotBackToPreviousPage;

  // Chat user info

  // Constructor
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.exochatlist);

    eXoChatListControllerInstance = this;

    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
    // addActionBarItem();
    // getActionBar().getItem(0).setDrawable(R.drawable.home);

    lvChatList = (ListView) findViewById(R.id.ListViewChatList);

    // Add a packet listener to get messages sent to us
    PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
    packetListener = new PacketListener() {

      public void processPacket(Packet arg0) {

        // check coming message
        Message message = (Message) arg0;
        if (message.getBody() != null) {

          String fromName = StringUtils.parseBareAddress(message.getFrom());

          for (int i = 0; i < listChatRosterEntry.size(); i++) {
            ChatMemberInfo member = listChatRosterEntry.get(i);
            fromChatStr = member.address;
            final String chatFromName = fromChatStr.substring(0, fromChatStr.lastIndexOf("@"));
            if (fromName.equalsIgnoreCase(fromChatStr)) {
              List<ChatMessageContent> str = arrListChat.get(i);
              str.add(new ChatMessageContent(chatFromName, message.getBody()));
              arrListChat.set(i, str);

              if (fromName.equalsIgnoreCase(ExoChatController.currentChatStr)) {
                // eXoChatController.setListAdapter();
                chatsAdapter.notifyDataSetChanged();
              } else {
                runOnUiThread(new Runnable() {

                  public void run() {
                    Toast msg = Toast.makeText(getApplicationContext(), "Message from "
                        + chatFromName, Toast.LENGTH_LONG);
                    msg.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                    msg.show();
                  }
                });
              }

              break;
            }
          }

          // Add the incoming message to the list view
          mHandler.post(new Runnable() {
            public void run() {

            }
          });
        }
      }
    };

    conn.addPacketListener(packetListener, filter);

    rosterListener = new RosterListener() {

      public void presenceChanged(Presence arg0) {

        final Presence tmpPresence = arg0;

        runOnUiThread(new Runnable() {

          public void run() {
            String from = StringUtils.parseBareAddress(tmpPresence.getFrom());
            String status = tmpPresence.getType().toString();
            if (conn.isConnected()) {
              Toast msg = Toast.makeText(getApplicationContext(),
                                         from + ": " + status,
                                         Toast.LENGTH_LONG);
              msg.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
              msg.show();

              for (int i = 0; i < listChatRosterEntry.size(); i++) {
                ChatMemberInfo tmp = listChatRosterEntry.get(i);
                if (from.equalsIgnoreCase(tmp.address)) {
                  if (status.equalsIgnoreCase("available"))
                    tmp.isOnline = true;
                  else
                    tmp.isOnline = false;

                  listChatRosterEntry.set(i, tmp);

                  break;
                }
              }

              ExoChatListAdapter chatsAdapter = new ExoChatListAdapter(listChatRosterEntry);
              lvChatList.setAdapter(chatsAdapter);
              lvChatList.setOnItemClickListener(chatsAdapter);
            }

          }
        });

      }

      public void entriesUpdated(Collection<String> arg0) {

      }

      public void entriesDeleted(Collection<String> arg0) {

      }

      public void entriesAdded(Collection<String> arg0) {

      }
    };

    changeLanguage();

    if (conn.isAuthenticated()) {
      conn.getRoster().addRosterListener(rosterListener);

      setListAdapter();
      setArrayListChat();
    }

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

    default:
      // home button is clicked
      break;
    }

    return true;
  }

  @Override
  public void onBackPressed() {
    finish();
  }

  // Set adapter for Chat list view
  private void setListAdapter() {
    listChatRosterEntry = getListChat();
    chatsAdapter = new ExoChatListAdapter(listChatRosterEntry);
    lvChatList.setAdapter(chatsAdapter);
    lvChatList.setOnItemClickListener(chatsAdapter);
  }

  // Set roster array
  private void setArrayListChat() {
    if (arrListChat == null) {
      int size = listChatRosterEntry.size();
      arrListChat = new ArrayList<List<ChatMessageContent>>(size);
      for (int i = 0; i < size; i++) {
        List<ChatMessageContent> tmp = new ArrayList<ChatMessageContent>();
        arrListChat.add(tmp);
      }
    }

  }

  // Connect to chat server (Open file)
  public static void connectToChatServer(String host, int port, String userName, String password) {
    if (conn != null && conn.isConnected())
      return;

    ConnectionConfiguration config = new ConnectionConfiguration(host, port, "Work");
    conn = new XMPPConnection(config);

    try {
      conn.connect();
      conn.login(userName, password);

    } catch (XMPPException e) {

    }

  }

  // Get chat user list
  public List<ChatMemberInfo> getListChat() {
    List<ChatMemberInfo> list = new ArrayList<ChatMemberInfo>();

    Roster roster = conn.getRoster();

    Collection<RosterEntry> rosterEntry = roster.getUnfiledEntries();

    for (RosterEntry x : rosterEntry) {
      String user = x.getUser();
      Presence presence = roster.getPresence(user);
      boolean isAvailable = false;
      if (presence.getType().toString().equalsIgnoreCase("available"))
        isAvailable = true;
      ChatMemberInfo tmp = new ChatMemberInfo(user, isAvailable);
      list.add(tmp);

    }

    return list;
  }

  // Create adapter for Chat user
  private class ExoChatListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    private List<ChatMemberInfo> arrExoChats;

    public ExoChatListAdapter(List<ChatMemberInfo> chats) {
      arrExoChats = chats;
    }

    public int getCount() {
      int count = arrExoChats.size();
      return count;
    }

    public Object getItem(int position) {
      return position;
    }

    public long getItemId(int position) {
      return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
      LayoutInflater inflater = getLayoutInflater();
      View rowView = inflater.inflate(R.layout.chatitem, parent, false);
      bindView(rowView, arrExoChats.get(position));
      return (rowView);
    }

    private void bindView(View view, ChatMemberInfo chat) {
      TextView label = (TextView) view.findViewById(R.id.label);

      label.setText(chat.getChatName());

      ImageView icon = (ImageView) view.findViewById(R.id.icon);

      if (chat.isOnline)
        icon.setImageResource(R.drawable.onlinechat);
      else
        icon.setImageResource(R.drawable.offlinechat);
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

      posOfChatingMember = position;
      ExoChatController.currentChatStr = listChatRosterEntry.get(position).address;
      ExoChatController.listChatContent = arrListChat.get(position);
      conn.removePacketListener(packetListener);

      Intent next = new Intent(ExoChatListController.this, ExoChatController.class);
      ExoChatListController.this.startActivity(next);
    }

  }

  // Set language
  public void changeLanguage() {
    LocalizationHelper bundle = LocalizationHelper.getInstance();
    String strTitle = "";

    strTitle = bundle.getString("ChatTitle");
    strCannotBackToPreviousPage = bundle.getString("CannotBackToPreviousPage");

    setTitle(strTitle);

  }
}
