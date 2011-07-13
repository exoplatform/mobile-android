package exo.exoplatform.chat;

import exo.exoplatform.controller.AppController;
import exo.exoplatform.controller.ExoApplicationsController2;
import exo.exoplatform.widget.MyActionBar;
import greendroid.widget.ActionBarItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ResourceBundle;

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

import com.cyrilmottier.android.greendroid.R;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.KeyEvent;
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

//Chat list view controller
public class ExoChatListController extends MyActionBar {

  private ListView                                     lvChatList;                   // Chat

  // list
  // view

  public static ExoChatListController                  eXoChatListControllerInstance; // Instance

  public static ExoApplicationsController2                    _delegate;                    // Main

  // app
  // view
  // controller

  public static XMPPConnection                         conn;                         // Interact

  // with
  // server

  public static List<ChatMember>                       listChatRosterEntry;          // Roster

  // array

  public static ArrayList<List<ExoChatMessageContent>> arrListChat = null;           // Chat

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

  eXoChatListAdapter                                   chatsAdapter;

  String                                               strCannotBackToPreviousPage;

  // Chat user info
  public class ChatMember {
    String  address;

    boolean isOnline;

    public ChatMember(String addr, boolean bool) {
      address = addr;
      isOnline = bool;

    }

    public String getChatName() {
      int index = this.address.indexOf("@");
      return this.address.substring(0, index);

    }
  }

  // Constructor
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setActionBarContentView(R.layout.exochatlist);

    eXoChatListControllerInstance = this;
    
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
//    addActionBarItem();
//    getActionBar().getItem(0).setDrawable(R.drawable.home);

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
            ChatMember member = listChatRosterEntry.get(i);
            fromChatStr = member.address;
            final String chatFromName = fromChatStr.substring(0, fromChatStr.lastIndexOf("@"));
            if (fromName.equalsIgnoreCase(fromChatStr)) {
              List<ExoChatMessageContent> str = arrListChat.get(i);
              str.add(new ExoChatMessageContent(chatFromName, message.getBody()));
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
                ChatMember tmp = listChatRosterEntry.get(i);
                if (from.equalsIgnoreCase(tmp.address)) {
                  if (status.equalsIgnoreCase("available"))
                    tmp.isOnline = true;
                  else
                    tmp.isOnline = false;

                  listChatRosterEntry.set(i, tmp);

                  break;
                }
              }

              eXoChatListAdapter chatsAdapter = new eXoChatListAdapter(listChatRosterEntry);
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

    changeLanguage(AppController.bundle);

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

  public void finishMe() {

    finish();
//    Intent next = new Intent(eXoChatListController.this, eXoApplicationsController2.class);
//    startActivity(next);
//    eXoChatListControllerInstance = null;

  }

  // Keydown listener
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Save data to the server once the user hits the back button
    if ((keyCode == KeyEvent.KEYCODE_BACK)) {
      Toast.makeText(ExoChatListController.this, strCannotBackToPreviousPage, Toast.LENGTH_SHORT)
           .show();
    }
    return false;
  }

  // Set adapter for Chat list view
  private void setListAdapter() {
    listChatRosterEntry = getListChat();
    chatsAdapter = new eXoChatListAdapter(listChatRosterEntry);
    lvChatList.setAdapter(chatsAdapter);
    lvChatList.setOnItemClickListener(chatsAdapter);
  }

  // Set roster array
  private void setArrayListChat() {
    if (arrListChat == null) {
      int size = listChatRosterEntry.size();
      arrListChat = new ArrayList<List<ExoChatMessageContent>>(size);
      for (int i = 0; i < size; i++) {
        List<ExoChatMessageContent> tmp = new ArrayList<ExoChatMessageContent>();
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
  public List<ChatMember> getListChat() {
    List<ChatMember> list = new ArrayList<ChatMember>();

    Roster roster = conn.getRoster();

    Collection<RosterEntry> rosterEntry = roster.getUnfiledEntries();

    for (RosterEntry x : rosterEntry) {
      String user = x.getUser();
      Presence presence = roster.getPresence(user);
      boolean isAvailable = false;
      if (presence.getType().toString().equalsIgnoreCase("available"))
        isAvailable = true;
      ChatMember tmp = new ChatMember(user, isAvailable);
      list.add(tmp);

    }

    return list;
  }

  // Create adapter for Chat user
  class eXoChatListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

    private List<ChatMember> arrExoChats;

    public eXoChatListAdapter(List<ChatMember> chats) {
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

    private void bindView(View view, ChatMember chat) {
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

      ExoChatController._delegate = _delegate;
      Intent next = new Intent(ExoChatListController.this, ExoChatController.class);
      ExoChatListController.this.startActivity(next);
    }

  }

  // Set language
  public void changeLanguage(ResourceBundle resourceBundle) {

    String strTitle = "";

    try {
      strTitle = new String(resourceBundle.getString("ChatTitle").getBytes("ISO-8859-1"), "UTF-8");
      strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage")
                                                             .getBytes("ISO-8859-1"), "UTF-8");
    } catch (Exception e) {

    }

    setTitle(strTitle);

    _delegate.changeLanguage(resourceBundle);
    // _delegate.createAdapter();
  }
}
