package org.exoplatform.controller.chat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.exoplatform.model.ChatMemberInfo;
import org.exoplatform.model.ChatMessageContent;
import org.exoplatform.singleton.ChatServiceHelper;
import org.exoplatform.singleton.LocalizationHelper;
import org.exoplatform.ui.ChatDetailActivity;
import org.exoplatform.ui.ChatListActivity;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;

import com.cyrilmottier.android.greendroid.R;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChatListController {
  private ListView         lvChatList;

  private Context          mContext;

  private ChatListLoadTask mLoadTask;

  private Handler          mHandler = new Handler();

  private String           fromChatStr;

  private ChatListAdapter  chatsAdapter;

  public ChatListController(Context context, ListView listView) {
    mContext = context;
    lvChatList = listView;
  }

  private ArrayList<ChatMemberInfo> getListChat() {
    ArrayList<ChatMemberInfo> list = new ArrayList<ChatMemberInfo>();

    Roster roster = ChatServiceHelper.getInstance().getXMPPConnection().getRoster();

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

  public void init() {
    setPacketListener();
    setRosterListener();
  }

  private void setPacketListener() {
    // Add a packet listener to get messages sent to us
    PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
    PacketListener packetListener = new PacketListener() {

      public void processPacket(Packet arg0) {

        // check coming message
        Message message = (Message) arg0;
        if (message.getBody() != null) {

          String fromName = StringUtils.parseBareAddress(message.getFrom());

          for (int i = 0; i < ChatServiceHelper.getInstance().getChatListRosterEntry().size(); i++) {
            ChatMemberInfo member = ChatServiceHelper.getInstance().getChatListRosterEntry().get(i);
            fromChatStr = member.address;
            final String chatFromName = fromChatStr.substring(0, fromChatStr.lastIndexOf("@"));
            if (fromName.equalsIgnoreCase(fromChatStr)) {
              List<ChatMessageContent> str = ChatServiceHelper.getInstance().getListChat().get(i);
              str.add(new ChatMessageContent(chatFromName, message.getBody()));
              ChatServiceHelper.getInstance().getListChat().set(i, str);

              if (fromName.equalsIgnoreCase(ChatDetailActivity.currentChatStr)) {
                // eXoChatController.setListAdapter();
                chatsAdapter.notifyDataSetChanged();
              } else {
                ((Activity) mContext).runOnUiThread(new Runnable() {

                  public void run() {
                    Toast msg = Toast.makeText(mContext,
                                               "Message from " + chatFromName,
                                               Toast.LENGTH_LONG);
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
    ChatServiceHelper.getInstance().setPacketListener(packetListener);
    ChatServiceHelper.getInstance().getXMPPConnection().addPacketListener(packetListener, filter);
  }

  private void setRosterListener() {
    RosterListener rosterListener = new RosterListener() {

      public void presenceChanged(Presence arg0) {

        final Presence tmpPresence = arg0;

        ((Activity) mContext).runOnUiThread(new Runnable() {

          public void run() {
            String from = StringUtils.parseBareAddress(tmpPresence.getFrom());
            String status = tmpPresence.getType().toString();
            if (ChatServiceHelper.getInstance().getXMPPConnection().isConnected()) {
              Toast msg = Toast.makeText(mContext, from + ": " + status, Toast.LENGTH_LONG);
              msg.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
              msg.show();

              for (int i = 0; i < ChatServiceHelper.getInstance().getChatListRosterEntry().size(); i++) {
                ChatMemberInfo tmp = ChatServiceHelper.getInstance()
                                                      .getChatListRosterEntry()
                                                      .get(i);
                if (from.equalsIgnoreCase(tmp.address)) {
                  if (status.equalsIgnoreCase("available"))
                    tmp.isOnline = true;
                  else
                    tmp.isOnline = false;

                  ChatServiceHelper.getInstance().getChatListRosterEntry().set(i, tmp);

                  break;
                }
              }

              chatsAdapter = new ChatListAdapter(mContext,
                                                 ChatServiceHelper.getInstance()
                                                                  .getChatListRosterEntry());
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
    if (ChatServiceHelper.getInstance().getXMPPConnection().isAuthenticated()) {
      ChatServiceHelper.getInstance()
                       .getXMPPConnection()
                       .getRoster()
                       .addRosterListener(rosterListener);

      setListAdapter();
      setArrayListChat();
    }
  }

  // Set adapter for Chat list view
  private void setListAdapter() {
    ArrayList<ChatMemberInfo> listChatRosterEntry = getListChat();
    ChatServiceHelper.getInstance().setChatListRosterEntry(listChatRosterEntry);
    if (listChatRosterEntry.size() == 0) {
      ChatListActivity.chatListActivity.setEmptyView(View.VISIBLE);
    } else {
      ChatListActivity.chatListActivity.setEmptyView(View.GONE);
      chatsAdapter = new ChatListAdapter(mContext, listChatRosterEntry);
      lvChatList.setAdapter(chatsAdapter);
      lvChatList.setOnItemClickListener(chatsAdapter);
    }

  }

  // Set roster array
  private void setArrayListChat() {
    if (ChatServiceHelper.getInstance().getListChat() == null) {
      int size = ChatServiceHelper.getInstance().getChatListRosterEntry().size();
      ArrayList<List<ChatMessageContent>> arrListChat = new ArrayList<List<ChatMessageContent>>(size);
      for (int i = 0; i < size; i++) {
        List<ChatMessageContent> tmp = new ArrayList<ChatMessageContent>();
        arrListChat.add(tmp);
      }
      ChatServiceHelper.getInstance().setListChat(arrListChat);
    }

  }

  public void onLoad() {
    if (mLoadTask == null || mLoadTask.getStatus() == ChatListLoadTask.Status.FINISHED) {
      mLoadTask = (ChatListLoadTask) new ChatListLoadTask(mContext, this).execute();
    }
  }

  public void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == ChatListLoadTask.Status.RUNNING) {
      mLoadTask.onCancelled();
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }
}
