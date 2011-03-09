package eXo.eXoPlatform;

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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

//Chat list view controller
public class eXoChatListController extends Activity{
	
	private static Button btnClose;	//Close view
	private static Button _btnLanguageHelp;	//Setting
	private static TextView tvTitle;	//Title
	private ListView lvChatList;	//Chat list view
	eXoChatListController thisClass;	//Instance
	static eXoApplicationsController _delegate;	//Main app view controller

	public static XMPPConnection conn;	//Interact with server
	public static List<ChatMember> listChatRosterEntry;	//Roster array
	public static ArrayList<List<eXoChatMessageContent>> arrListChat = null;	//Chat message array
	private String fromChatStr;	//Source
	public static int posOfChatingMember;	//User index
//	Update roster
	private Handler mHandler = new Handler();
	public static RosterListener rosterListener;
	public static PacketListener packetListener;
	
	String strCannotBackToPreviousPage;
//	Chat user info
	public class ChatMember
	{
		String address;	
		boolean isOnline;
		
		public ChatMember(String addr, boolean bool)
		{
			address = addr;
			isOnline = bool;
			
		}
		
		public String getChatName()
		{
			int index = this.address.indexOf("@");
			return this.address.substring(0, index);
			
		}
	}
//	Constructor
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.exochatlist);
        
        thisClass = this;
        
        btnClose = (Button) findViewById(R.id.ButtonClose);
        btnClose.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent next = new Intent(eXoChatListController.this, eXoApplicationsController.class);
				eXoChatListController.this.startActivity(next);
//				eXoChatList.this.finish();
				
			}
		});
        
        _btnLanguageHelp = (Button) findViewById(R.id.Button_Language_Help);
        _btnLanguageHelp.setOnClickListener(new View.OnClickListener() 
        {	
        	public void onClick(View v) 
			{
        		
        		eXoLanguageSettingDialog customizeDialog = new eXoLanguageSettingDialog(eXoChatListController.this, 3, thisClass);
        		customizeDialog.setTitle("User guide & language setting");
        		customizeDialog.show();
			}	
		});
        
        tvTitle = (TextView) findViewById(R.id.TextViewChatList);
        
        lvChatList = (ListView) findViewById(R.id.ListViewChatList);
        
//        Add a packet listener to get messages sent to us
    	PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
    	packetListener = new PacketListener() {
	    	
			public void processPacket(Packet arg0) {
				// TODO Auto-generated method stub
		        
				//check coming message
				Message message = (Message) arg0;
	    		if (message.getBody() != null) {
	    			
	    			
	    			String fromName = StringUtils.parseBareAddress(message.getFrom());
	    			
	    			for(int i = 0; i < listChatRosterEntry.size(); i++)
	    			{
	    				ChatMember member = listChatRosterEntry.get(i);
	    				fromChatStr = member.address;
	    				final String chatFromName = fromChatStr.substring(0, fromChatStr.lastIndexOf("@"));
	    				if(fromName.equalsIgnoreCase(fromChatStr))
	    				{
	    					List<eXoChatMessageContent> str = arrListChat.get(i);
	    					str.add(new eXoChatMessageContent(chatFromName, message.getBody()));
	    					arrListChat.set(i, str);
	    					
	    					if(fromName.equalsIgnoreCase(eXoChatController.currentChatStr))
	    					{
	    						eXoChatController.setListAdapter();
	    					}
	    					else
	    					{
	    						runOnUiThread(new Runnable(){
	                                
	    							public void run() {
	    								Toast msg = Toast.makeText(getApplicationContext(), "Message from " + chatFromName, Toast.LENGTH_LONG);
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
				// TODO Auto-generated method stub
				final Presence tmpPresence = arg0;
				
				runOnUiThread(new Runnable(){
                    
					public void run() {
						String from = StringUtils.parseBareAddress(tmpPresence.getFrom());
						String status = tmpPresence.getType().toString();
						if(conn.isConnected())
						{
							Toast msg = Toast.makeText(getApplicationContext(), from + ": " + status, Toast.LENGTH_LONG);
							msg.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
							msg.show();
							
							for(int i = 0; i < listChatRosterEntry.size(); i++)
							{
								ChatMember tmp = listChatRosterEntry.get(i);
								if(from.equalsIgnoreCase(tmp.address))
								{
									 if(status.equalsIgnoreCase("available"))
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
				// TODO Auto-generated method stub
				
			}
			
			public void entriesDeleted(Collection<String> arg0) {
				// TODO Auto-generated method stub
				
			}
			
			public void entriesAdded(Collection<String> arg0) {
				// TODO Auto-generated method stub
				
			}
		};
		
		changeLanguage(AppController.bundle);
		
		if(conn.isAuthenticated())
		{
			conn.getRoster().addRosterListener(rosterListener);
	    	  
	        setListAdapter();
	        setArrayListChat();
		}
		
	}
//	Keydown listener
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
	      //Save data to the server once the user hits the back button
	      if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	          Toast.makeText(eXoChatListController.this, strCannotBackToPreviousPage ,Toast.LENGTH_SHORT).show();
	      }
	      return false;
	  }
//	 Set adapter for Chat list view
	private void setListAdapter() 
    {
    	listChatRosterEntry = getListChat();
        eXoChatListAdapter chatsAdapter = new eXoChatListAdapter(listChatRosterEntry);
        lvChatList.setAdapter(chatsAdapter);      
        lvChatList.setOnItemClickListener(chatsAdapter);
    }
	//Set roster array
	private void setArrayListChat()
    {
    	if(arrListChat == null)
    	{
    		int size = listChatRosterEntry.size();		
    		arrListChat = new ArrayList<List<eXoChatMessageContent>>(size);
        	for(int i = 0; i < size; i++)
        	{
        		List<eXoChatMessageContent> tmp = new ArrayList<eXoChatMessageContent>();
        		arrListChat.add(tmp);
        	}
    	}
    		
    }
//	Connect to chat server (Open file)
	 public static void connectToChatServer(String host, int port, String userName, String password)
	 {
	    if(conn != null && conn.isConnected())
	    	return;
	    
		ConnectionConfiguration config = new ConnectionConfiguration(host, port, "Work");
	    conn = new XMPPConnection(config);

	    try {
	    	conn.connect();
			conn.login(userName, password);
				
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	
	    }
//	Get chat user list
	 public List<ChatMember> getListChat()
	    {
	    	List<ChatMember> list = new ArrayList<ChatMember>();
	    	
	    	Roster roster = conn.getRoster();
	    	
	    	Collection<RosterEntry> rosterEntry = roster.getUnfiledEntries();
			
			for(RosterEntry x:rosterEntry)
			{
				String user = x.getUser();
				Presence presence = roster.getPresence(user);
				boolean isAvailable = false;
				if(presence.getType().toString().equalsIgnoreCase("available"))
					isAvailable = true;
				ChatMember tmp = new ChatMember(user, isAvailable);
				list.add(tmp);
				
			}
			
	    	return list;
	    }
//	 Create adapter for Chat user
	class eXoChatListAdapter extends BaseAdapter implements AdapterView.OnItemClickListener 
	{
		
		private List<ChatMember> arrExoChats;
		
		public eXoChatListAdapter(List<ChatMember> chats) 
		{
			arrExoChats = chats;
		}

	    public int getCount() 
	    {
	    	int count = arrExoChats.size();
	    	return count;
	    }

	    public Object getItem(int position) 
	    {
	    	return position;
	    }

	    public long getItemId(int position) 
	    {
	    	return position;
	    }

	    public View getView(int position, View convertView, ViewGroup parent) 
	    {
	    	LayoutInflater inflater = getLayoutInflater();
	    	View rowView = inflater.inflate(R.layout.chatitem, parent, false);
	    	bindView(rowView, arrExoChats.get(position));
	        return(rowView);
	    }

	    private void bindView(View view, ChatMember chat) 
	    {
	    	TextView label = (TextView)view.findViewById(R.id.label);
	    	
	    	label.setText(chat.getChatName());
	    	
	    	ImageView icon = (ImageView)view.findViewById(R.id.icon);
	    	
	    	if(chat.isOnline)
	    		icon.setImageResource(R.drawable.onlinechat);
	    	else
	    		icon.setImageResource(R.drawable.offlinechat);
	    }
	    
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	    {
	    	posOfChatingMember = position;
	    	eXoChatController.currentChatStr = listChatRosterEntry.get(position).address;
	    	eXoChatController.listChatContent = arrListChat.get(position);
	    	conn.removePacketListener(packetListener);
	    	
	    	eXoChatController._delegate = _delegate;
	    	Intent next = new Intent(eXoChatListController.this, eXoChatController.class);
	    	eXoChatListController.this.startActivity(next);
	    }
	   
	}
//	Set language
	 public void changeLanguage(ResourceBundle resourceBundle)
	 {
		 
		 String strTitle = "";
		 String strcloseBtn = "";
		 
		 try {
			 strTitle = new String(resourceBundle.getString("ChatTitle").getBytes("ISO-8859-1"), "UTF-8");
			 strcloseBtn = new String(resourceBundle.getString("CloseButton").getBytes("ISO-8859-1"), "UTF-8");
			 strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage").getBytes("ISO-8859-1"), "UTF-8");
		 } catch (Exception e) {
			 // TODO: handle exception
			 e.printStackTrace();
		 }
	    	
		 btnClose.setText(strcloseBtn);
		 tvTitle.setText(strTitle);
	    	
		 
		 _delegate.changeLanguage(resourceBundle);
		 _delegate.createAdapter();
	 }
}
