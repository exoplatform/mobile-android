package org.exoplatform.controller.chat;

import java.util.List;

import org.exoplatform.R;
import org.exoplatform.model.ChatMemberInfo;
import org.exoplatform.singleton.ChatServiceHelper;
import org.exoplatform.ui.ChatDetailActivity;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {

  private List<ChatMemberInfo> arrExoChats;

  private Context              mContext;

  public ChatListAdapter(Context context, List<ChatMemberInfo> chats) {
    mContext = context;
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
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
    ChatServiceHelper serviceHelper = ChatServiceHelper.getInstance();
    ChatDetailActivity.currentChatStr = serviceHelper.getChatListRosterEntry().get(position).address;
    ChatDetailActivity.listChatContent = serviceHelper.getListChat().get(position);
    serviceHelper.getXMPPConnection().removePacketListener(serviceHelper.getPacketListener());

    Intent next = new Intent(mContext, ChatDetailActivity.class);
    mContext.startActivity(next);
  }

}
