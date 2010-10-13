package eXo.eXoMobile;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;

import eXo.eXoMobile.file.eXoChatMessageContent;

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

public class eXoChat extends Activity{
	
	public static List<eXoChatMessageContent> listChatContent = new ArrayList<eXoChatMessageContent>();
	public static String currentChatStr = "";
	EditText messageEditText;
	Button sendMessageBtn;
	static ListView conversationView;
	Button btnClose;
	Button _btnLanguageHelp;
	TextView tvCurrentChat;

	public static eXoChat thisClass;
	static eXoApplicationsController _delegate;
	
	public static PacketListener packetListener;
	private static Handler mHandler;
	private static Runnable runnable;
	
	private String fromChatStr;
	String strCannotBackToPreviousPage;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.exochat);
        thisClass = this;
        messageEditText = (EditText) findViewById(R.id.message);
        
        conversationView = (ListView) findViewById(R.id.chatContent);
        tvCurrentChat = (TextView) findViewById(R.id.TextViewCurrentChat);
        String currentChatNickName = currentChatStr.substring(0, currentChatStr.lastIndexOf("@"));
        tvCurrentChat.setText(currentChatNickName);

        if(mHandler == null)
        	mHandler = new Handler();
        if(runnable == null)
        {
        	runnable = new Runnable() {
				
				public void run() {
					// TODO Auto-generated method stub
					setListAdapter();
				}
			};
        }
        
        sendMessageBtn = (Button) findViewById(R.id.Send);
        sendMessageBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	
                // Send a message using content of the edit text widget
                String msg = messageEditText.getText().toString();
                if(msg != null && msg != "")
                {
                	try {
                		
                		Message message = new Message(currentChatStr, Message.Type.chat);
                		message.setBody(msg);
                		eXoChatList.conn.sendPacket(message);
                		
                		listChatContent.add(new eXoChatMessageContent("Me", msg));
            			messageEditText.setText("");
            			setListAdapter();
                		
					} catch (Exception e) {
						// TODO: handle exception
					}
                }
                	
            }
        });
        
        btnClose = (Button) findViewById(R.id.Close);
        btnClose.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				currentChatStr = "";
				//eXoChatList.arrListChat.set(eXoChatList.posOfChatingMember, listChatContent);
				eXoChatList.conn.removePacketListener(packetListener);
				//eXoChatList.conn.addPacketListener(eXoChatList.packetListener, new MessageTypeFilter(Message.Type.chat));
				
//				eXoChat.this.finish();
				Intent next = new Intent(eXoChat.this, eXoChatList.class);
				eXoChat.this.startActivity(next);
			}
		});
        
        _btnLanguageHelp = (Button) findViewById(R.id.Button_Language_Help);
        _btnLanguageHelp.setOnClickListener(new View.OnClickListener() 
        {	
        	public void onClick(View v) 
			{
        		eXoLanguageSetting customizeDialog = new eXoLanguageSetting(eXoChat.this, 4, thisClass);
        		customizeDialog.setTitle("User guide & language setting");
        		customizeDialog.show();
			}	
		});
        
        PacketFilter filter = new MessageTypeFilter(Message.Type.chat);
    	packetListener = new PacketListener() {
	    	
			public void processPacket(Packet arg0) {
				// TODO Auto-generated method stub
		        
				//check coming message
				Message message = (Message) arg0;
	    		if (message.getBody() != null) {
	    			
	    			
	    			String fromName = StringUtils.parseBareAddress(message.getFrom());
	    			
	    			for(int i = 0; i < eXoChatList.listChatRosterEntry.size(); i++)
	    			{
	    				fromChatStr = eXoChatList.listChatRosterEntry.get(i).address;
	    				final String chatFromName = fromChatStr.substring(0, fromChatStr.lastIndexOf("@"));
	    				if(fromName.equalsIgnoreCase(fromChatStr))
	    				{
	    					List<eXoChatMessageContent> msgContent = eXoChatList.arrListChat.get(i);
	    					msgContent.add(new eXoChatMessageContent(chatFromName, message.getBody()));
	    					eXoChatList.arrListChat.set(i, msgContent);
	    					
	    					if(fromName.equalsIgnoreCase(eXoChat.currentChatStr))
	    					{
	    						listChatContent = eXoChatList.arrListChat.get(i);
	    					}
	    					else
	    					{
	    						runOnUiThread(new Runnable(){
	                                
	    							public void run() {
	    								Toast msg = Toast.makeText(getApplicationContext(), "Message from " + chatFromName, Toast.LENGTH_SHORT);
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
	    
	    eXoChatList.conn.addPacketListener(packetListener, filter);
	    
        changeLanguage(AppController.bundle);
        
        setListAdapter();
       
	}
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Save data to the server once the user hits the back button
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Toast.makeText(eXoChat.this, strCannotBackToPreviousPage ,Toast.LENGTH_LONG).show();
        }
        return false;
    }
    
    public static void setListAdapter() 
    {
    	BaseAdapter adapter = new BaseAdapter() {
			
    		public View getView(int position, View convertView, ViewGroup parent) 
    	    {
    	    	LayoutInflater inflater = thisClass.getLayoutInflater();
    	    	View rowView = inflater.inflate(R.layout.exochatmessagecontentitem, parent, false);
    	    	bindView(rowView, listChatContent.get(position));
    	        return(rowView);
    	    }

    	    private void bindView(View view, eXoChatMessageContent msgContent) 
    	    {
    	    	TextView name = (TextView)view.findViewById(R.id.TextView_Name);
    	    	name.setText(msgContent.name);
    	    	
    	    	if(msgContent.name.equalsIgnoreCase("Me"))
    	    	{
    	    		view.setBackgroundResource(R.drawable.chatbackgroundwhite);
    	    	}
    	    	else
    	    	{
    	    		view.setBackgroundResource(R.drawable.chatbackgroundwhite);
//    	    		view.setBackgroundResource(0xFFFFFFFF);
    	    	}
    	    	TextView content = (TextView)view.findViewById(R.id.TextView_Content);
    	    	content.setText(msgContent.content);
    	    	
    	    	name.setBackgroundResource(R.drawable.chatnamebackground);
    	    }
			
			public long getItemId(int position) {
				// TODO Auto-generated method stub
				return position;
			}
			
			public Object getItem(int position) {
				// TODO Auto-generated method stub
				return position;
			}
			
			public int getCount() {
				// TODO Auto-generated method stub
				return listChatContent.size();
			}
		};
    	conversationView.setAdapter(adapter);
    }
     
    public void changeLanguage(ResourceBundle resourceBundle)
    {
    	
    	String strsendMessageBtn = "";
    	String strcloseBtn = "";
    	try {
    		strsendMessageBtn = new String(resourceBundle.getString("Send").getBytes("ISO-8859-1"), "UTF-8");
    		strcloseBtn = new String(resourceBundle.getString("CloseButton").getBytes("ISO-8859-1"), "UTF-8");
    		strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage").getBytes("ISO-8859-1"), "UTF-8");	
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
    	
   		sendMessageBtn.setText(strsendMessageBtn);
   		btnClose.setText(strcloseBtn);
   		
   		_delegate.changeLanguage(resourceBundle);
    	_delegate.createAdapter();
    	
    }
}

