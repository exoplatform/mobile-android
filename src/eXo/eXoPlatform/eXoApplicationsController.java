package eXo.eXoPlatform;


import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class eXoApplicationsController extends Activity 
{
	ListView _lstvApps;
	ListView _lstvGadgets;
	TextView txtVieweXoAppsTittle;
	TextView txtVieweXoGadgetsTittle;
	
	Button _btnSignOut;
	Button  _btnLanguageHelp;
	
	public	static short webViewMode;//0: view gadget, 1: View file, 2: view help;
	
	String fileDescription;
	String chatDescription;
	String fileTittle;
	String chatTittle;
	
	private static eXoApp exoapp;
	private static eXoAppsAdapter exoAppsAdapter;
	ProgressDialog _progressDialog;
	eXoApplicationsController thisClass;
	
	public static List<GateInDbItem> arrGadgets;
	public static GateInDbItem gadgetTab;
	
	Thread thread;
	TextView textViewFileDescription;
	TextView textViewChatDescription;
	TextView labelFileName;
	TextView labelChatName;
	
	String strCannotBackToPreviousPage;
	
	@Override
    public void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.appsview);
        
        thisClass = this;
        
        txtVieweXoAppsTittle = (TextView) findViewById(R.id.TextView_AppsTitle);
        txtVieweXoGadgetsTittle = (TextView) findViewById(R.id.TextView_GadgetsTitle);
        
        //Sign Out button
        _btnSignOut = (Button) findViewById(R.id.Button_SignOut);
        _btnSignOut.setOnClickListener(new View.OnClickListener() 
        {	
        	public void onClick(View v) 
			{
        		if(eXoChatList.conn != null && eXoChatList.conn.isAuthenticated())
        		{
        			eXoChatList.conn.getRoster().removeRosterListener(eXoChatList.rosterListener);
            		eXoChatList.conn.disconnect();
        		}
        		
        		Intent next = new Intent(eXoApplicationsController.this, AppController.class);
        		eXoApplicationsController.this.startActivity(next);
			}
        }); 
        
        _btnLanguageHelp = (Button) findViewById(R.id.Button_Language_Help);
        _btnLanguageHelp.setOnClickListener(new View.OnClickListener() 
        {	
        	public void onClick(View v) 
			{
        		
        		eXoLanguageSetting customizeDialog = new eXoLanguageSetting(eXoApplicationsController.this, 1, thisClass);
        		customizeDialog.show();
			}	
		});
        
        changeLanguage(AppController.bundle);
       
        
        _lstvApps = (ListView) findViewById(R.id.ListView_Apps);   
//        
        createAdapter();
        
        //Gadgets List
        _lstvGadgets = (ListView) findViewById(R.id.ListView_Gadgets);
        if(arrGadgets != null)
        {
        	GadgetsAdapter gadgetsAdapter = new GadgetsAdapter(arrGadgets);
            _lstvGadgets.setAdapter(gadgetsAdapter);
            _lstvGadgets.setOnItemClickListener(gadgetsAdapter);
            _lstvGadgets.setAdapter(gadgetsAdapter);      
            _lstvGadgets.setOnItemClickListener(gadgetsAdapter);
        }
        
        AppController._progressDialog.dismiss();
        
    } 
	
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
	      //Save data to the server once the user hits the back button
	      if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	          Toast.makeText(eXoApplicationsController.this, strCannotBackToPreviousPage ,Toast.LENGTH_LONG).show();
	          
	      }
	      return false;
	  }
	 
	 public void createAdapter()
	 {
		 List<eXoApp> exoapps = new ArrayList<eXoApp>(2);
	     exoapps.add(new eXoApp(fileTittle, fileDescription));
	     exoapps.add(new eXoApp(chatTittle, chatDescription));
	     exoAppsAdapter = new eXoAppsAdapter(exoapps);
	     _lstvApps.setAdapter(exoAppsAdapter);
	     _lstvApps.setOnItemClickListener(exoAppsAdapter);
	 }
	 
//	public boolean onCreateOptionsMenu(Menu menu) { 
//	    MenuInflater inflater = getMenuInflater(); 
//	    inflater.inflate(R.menu.menu, menu); 
//	    MenuItem mnItem = menu.getItem(0);
//	    mnItem.setTitle(settingTittle);
//	    
//	    return true; 
//	} 
	
//	public boolean onOptionsItemSelected(MenuItem item) { 
//	    
//		Intent next = new Intent(eXoApplicationsController.this, eXoSetting.class);
//		eXoApplicationsController.this.startActivity(next);
//		
//		
//		return true; 
//	} 

	
	//eXoAppsAdapter - it was used to be Data Source for Applications List View
	class eXoAppsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener 
	{
		
		private final List<eXoApp> _arreXoApps;
		
		public eXoAppsAdapter(List<eXoApp> exoapps) 
		{
			_arreXoApps = exoapps;
		}

	    public int getCount() 
	    {
	    	return _arreXoApps.size();
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
	    	View rowView = inflater.inflate(R.layout.rowinlistview, parent, false);
	    	bindView(rowView, _arreXoApps.get(position));
	    	return(rowView);
	    }
	    
	    private void bindView(View view, eXoApp app) 
	    {
	    	ImageView icon=(ImageView)view.findViewById(R.id.icon);
	    	if(app._streXoAppName.equalsIgnoreCase(fileTittle))
	    	{
	    		textViewFileDescription = (TextView)view.findViewById(R.id.description);
	    		textViewFileDescription.setText(app._streXoAppDescription);
	    		
	    		labelFileName = (TextView)view.findViewById(R.id.label);
	    		labelFileName.setText(app._streXoAppName);
	    		
	    		icon.setImageResource(R.drawable.files_app_icn);
	    	}
	    	else if (app._streXoAppName.equalsIgnoreCase(chatTittle))
	    	{
	    		textViewChatDescription = (TextView)view.findViewById(R.id.description);
	    		textViewChatDescription.setText(app._streXoAppDescription);
	    		
	    		labelChatName = (TextView)view.findViewById(R.id.label);
	    		labelChatName.setText(app._streXoAppName);
	    		
	    		if(eXoChatList.conn == null || !eXoChatList.conn.isConnected() || !eXoChatList.conn.isAuthenticated())
	    			icon.setImageResource(R.drawable.offlineicon);
	    		else
	    			icon.setImageResource(R.drawable.onlineicon);
	    	}
	    }
	    
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	    {
	    	exoapp = _arreXoApps.get(position);
	    	launchApp();
	    }
	    
	    private Runnable dismissProgressDialog = new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				_progressDialog.dismiss();
				
				thread.stop();
				thread = null;
			}
		};
		
	    public void launchApp()
	    {
	            
	    	 Runnable loadingDataRunnable = new Runnable()
	         {
	         public void run()
	         {
	        	 		
	        	 if(exoapp._streXoAppName == fileTittle)
	        	 {
	        		 launchFilesApp();
	        	 }
	        	 else if(exoapp._streXoAppName == chatTittle)
	        	 {
	        		 launchMessengerApp();
	        	 }
	        	    	
	        	 runOnUiThread(dismissProgressDialog);
	        	 }
	         };
            
	         String strLoadingDataFromServer = "";
	         try {
			
	        	 strLoadingDataFromServer = new String(AppController.bundle.getString("LoadingDataFromServer").getBytes("ISO-8859-1"), "UTF-8");
	        	 
			} catch (Exception e) {
				// TODO: handle exception
				strLoadingDataFromServer = "";
			}
	         
	        _progressDialog = ProgressDialog.show(thisClass, null, strLoadingDataFromServer);
            
            thread =  new Thread(loadingDataRunnable, "LoadDingData"); 
            thread.start();
            
	    }
	    
	    public void launchFilesApp()
	    {
	    	String userName = AppController.sharedPreference.getString(AppController.EXO_PRF_USERNAME, "exo_prf_username");
	    	String domain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN, "exo_prf_domain");
	    	
	    	if(eXoFilesController._strCurrentDirectory == null)
	    	{
	    		eXoFilesController._strCurrentDirectory = domain + "/rest/private/jcr/repository/collaboration/Users/" + userName + "/Private";
	    		eXoFilesController._rootUrl = eXoFilesController._strCurrentDirectory;
	    	}
	    	
	    	eXoFilesController.arrFiles = eXoFilesController.getPersonalDriveContent();
	    	eXoFilesController._delegate = thisClass;
	    	Intent next = new Intent(eXoApplicationsController.this, eXoFilesController.class);
	    	eXoApplicationsController.this.startActivity(next);
    		
	    }
	    
	    
	    public void launchMessengerApp()
	    {
	    	
	    	SharedPreferences sharedPreference = getSharedPreferences(AppController.EXO_PREFERENCE, 0);
        	String urlStr = sharedPreference.getString(AppController.EXO_PRF_DOMAIN, "exo_prf_domain");
        	
        	URI url = null;
        	
        	try {
        		url = new URI(urlStr);
			} catch (Exception e) {
				// TODO: handle exception
			}
        	
        	String userName = AppController.sharedPreference.getString(AppController.EXO_PRF_USERNAME, "exo_prf_username");
    		String password = AppController.sharedPreference.getString(AppController.EXO_PRF_PASSWORD, "exo_prf_password");
	    	
    		connectToChatServer(url.getHost(), 5224, userName, password);
    		  if(eXoChatList.conn == null || !eXoChatList.conn.isConnected())
    		    	return;
	    	
    		eXoChatList._delegate = thisClass;
	    	Intent next = new Intent(eXoApplicationsController.this, eXoChatList.class);
	    	eXoApplicationsController.this.startActivity(next);
	    }
	}

	private void connectToChatServer(String host, int port, String userName, String password)
	 {
	    if(eXoChatList.conn != null && eXoChatList.conn.isConnected())
	    	return;
	    
		ConnectionConfiguration config = new ConnectionConfiguration(host, port, "Work");
		eXoChatList.conn = new XMPPConnection(config);

	    try {
	    	eXoChatList.conn.connect();
	    	eXoChatList.conn.login(userName, password);
	    	
	    	runOnUiThread(new Runnable() {
				
				public void run() {
					// TODO Auto-generated method stub
//					icon.setBackgroundResource(R.drawable.onlineicon);	
					 List<eXoApp> exoapps = new ArrayList<eXoApp>(2);
				        exoapps.add(new eXoApp(fileTittle, fileDescription));
				        exoapps.add(new eXoApp(chatTittle, chatDescription));
				        exoAppsAdapter = new eXoAppsAdapter(exoapps);
				        _lstvApps.setAdapter(exoAppsAdapter);
				        _lstvApps.setOnItemClickListener(exoAppsAdapter);
					
				}
			});
	    	
//	        exoAppsAdapter.notifyDataSetChanged();
				
			} catch (XMPPException e) {
				// TODO Auto-generated catch block
				String str = e.toString();
				String msg = e.getMessage();
				Log.e(str, msg);
				e.printStackTrace();
			}
	    	
	    }
	
	//Gadget Adapter - it was used to be Data Source for Gadgets List View
	class GadgetsAdapter extends BaseAdapter implements AdapterView.OnItemClickListener 
	{
		private final List<GateInDbItem> _arrGadgets;
		
		public GadgetsAdapter(List<GateInDbItem> gadgets) 
		{
			_arrGadgets = gadgets;
		}

	    public int getCount() 
	    {
	    	if(_arrGadgets == null)
	    		return 0;
	    	return _arrGadgets.size();
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
	    	View rowView = inflater.inflate(R.layout.gadgettabitem, parent, false);
	    	bindView(rowView, _arrGadgets.get(position));
	        return(rowView);
	    }

	    private void bindView(View view, GateInDbItem gadgetTab) 
	    {
	    	
	    	TextView label = (TextView)view.findViewById(R.id.label);
	    	
	    	label.setText(gadgetTab._strDbItemName);
	    	
	    	ImageView icon = (ImageView)view.findViewById(R.id.icon);
	    	icon.setImageResource(R.drawable.dashboard);
	    	
	    }
	    
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	    {
//	    	launchGadget(_arrGadgets.get(position));
	    	gadgetTab = _arrGadgets.get(position);
	    	launchGadget();
	    }
	    
	    public void launchGadget()
	    {
	    	eXoGadgetViewController._delegate = thisClass;
	    	Intent next = new Intent(eXoApplicationsController.this, eXoGadgetViewController.class);
	    	eXoApplicationsController.this.startActivity(next);
    		
	    }
	}

	
	public void changeLanguage(ResourceBundle resourceBundle)
    {
		
		String strSignOut = "";
    	String strTxtVieweXoAppsTittle = "";
    	String strtxtVieweXoGadgetsTittle = "";
    	String strfileDescription  = "";
    	String strfileTittle = "";
    	String strchatDescription = "";
    	String strchatTittle = "";
    	
    	try {
    		
    		strSignOut = new String(resourceBundle.getString("SignOutButton").getBytes("ISO-8859-1"), "UTF-8");
    		strTxtVieweXoAppsTittle = new String(resourceBundle.getString("NativeApplicationsHeader").getBytes("ISO-8859-1"), "UTF-8");
        	strtxtVieweXoGadgetsTittle = new String(resourceBundle.getString("GadgetsHeader").getBytes("ISO-8859-1"), "UTF-8");
        	strfileDescription  = new String(resourceBundle.getString("FileDescription").getBytes("ISO-8859-1"), "UTF-8");
        	strfileTittle = new String(resourceBundle.getString("FilesApplication").getBytes("ISO-8859-1"), "UTF-8");
        	strchatDescription = new String(resourceBundle.getString("ChatDescription").getBytes("ISO-8859-1"), "UTF-8");
        	strchatTittle = new String(resourceBundle.getString("ChatApplication").getBytes("ISO-8859-1"), "UTF-8");
        	strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage").getBytes("ISO-8859-1"), "UTF-8");	
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		_btnSignOut.setText(strSignOut);
		txtVieweXoAppsTittle.setText(strTxtVieweXoAppsTittle);
		txtVieweXoGadgetsTittle.setText(strtxtVieweXoGadgetsTittle);
		
		fileDescription = strfileDescription;
		fileTittle = strfileTittle;
    	chatDescription = strchatDescription; 
    	chatTittle = strchatTittle;
    	
    }

}



