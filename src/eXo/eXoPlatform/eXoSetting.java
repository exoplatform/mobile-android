package eXo.eXoPlatform;

import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import eXo.eXoPlatform.AppController.ServerObj;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class eXoSetting extends Activity {

	static eXoSetting eXoSettingInstance;
	
	Button btnHome;
	Button btnModifyTheList;
	Button btnUserGuide;
	
	RadioButton myOptionEnglish, myOptionFrench;
	int pageIDForChangeLanguage;//0: AppController, 1: eXoApplicationController
								//2: eXoFileController, 3: eXoChatList, 4: eXoChat
								//5: eXoWebView
	
	TextView txtvLanguage;	//Language label
	TextView txtvServer;	//Server label
	
	static ListView listViewServer;
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.exosetting);
        
        eXoSettingInstance = this;
         
        txtvLanguage = (TextView)findViewById(R.id.TextView_Language);
        txtvServer = (TextView)findViewById(R.id.TextView_Server_List);
        
        btnModifyTheList = (Button)findViewById(R.id.Button_Modify_The_List);
        btnUserGuide = (Button)findViewById(R.id.Button_User_Guide);
        btnHome = (Button)findViewById(R.id.Button_Home);
        
        myOptionEnglish = (RadioButton)findViewById(R.id.RadioButton_English);
	    myOptionFrench = (RadioButton)findViewById(R.id.RadioButton_French);
	    
	    listViewServer = (ListView)findViewById(R.id.ListView_Servers);
	    
	    btnHome.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	    
	    btnModifyTheList.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)))
				{
					Toast.makeText(eXoSettingInstance, "You dont't have permission for this because SDCard is not available!", Toast.LENGTH_LONG);
				}
				else
				{
					Intent next = new Intent(eXoSetting.this, eXoModifyServerList.class);
					eXoSettingInstance.startActivity(next);
				}
				
			}
		});
	    
	    btnUserGuide.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				AppController.appControllerInstance.showUserGuide();
			}
		});

	    myOptionEnglish.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				updateLocallize("LocalizeEN.properties");
			}
		});
		
	    myOptionFrench.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				updateLocallize("LocalizeFR.properties");
			}
		});
        
	    String locallize = AppController.sharedPreference.getString(AppController.EXO_PRF_LOCALIZE, "exo_prf_localize");
	     if(locallize.equalsIgnoreCase("LocalizeEN.properties"))
	    	 myOptionEnglish.setChecked(true);
	     else if(locallize.equalsIgnoreCase("LocalizeFR.properties"))
	   	  	myOptionFrench.setChecked(true); 
	     else
	     {
	    	 myOptionEnglish.setChecked(true);
	    	 updateLocallize("LocalizeEN.properties"); 
	     }
	     
	     createServersAdapter(AppController.configurationInstance._arrServerList);
        
    }

//	Create server list adapter
	public void createServersAdapter(List<ServerObj> serverObjs)
	{	
		
		final List<ServerObj> serverObjsTmp = serverObjs;
		
   		BaseAdapter serverAdapter = new BaseAdapter() {
			
			  public View getView(int position, View convertView, ViewGroup parent) 
			    {
			    	LayoutInflater inflater = eXoSettingInstance.getLayoutInflater();
			    	View rowView = inflater.inflate(R.layout.serverlistitem, parent, false);
			    	
			    	ServerObj serverObj = serverObjsTmp.get(position);
			    	
			    	TextView serverName = (TextView)rowView.findViewById(R.id.TextView_ServerName);
			    	serverName.setText(serverObj._strServerName);
			    	
			    	TextView txtvUrl = (TextView)rowView.findViewById(R.id.TextView_URL);
			    	txtvUrl.setText(serverObj._strServerUrl);
			    	LinearLayout.LayoutParams layout = (LinearLayout.LayoutParams)txtvUrl.getLayoutParams();
			        layout.width = 160;
			        txtvUrl.setLayoutParams(layout);

			        return(rowView);
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
				return serverObjsTmp.size();
			}
		};
		
		listViewServer.setAdapter(serverAdapter);      
   		//_lstvFiles.setOnItemClickListener(test);
	}

	
	//	Change language
	private void updateLocallize(String localize)
	{
		try 
		{
			SharedPreferences.Editor editor = AppController.sharedPreference.edit();
			editor.putString(AppController.EXO_PRF_LOCALIZE, localize);
			editor.commit();
			   
			AppController.bundle = new PropertyResourceBundle(this.getAssets().open(localize));
			changeLanguage(AppController.bundle);
			   
			if(pageIDForChangeLanguage == 0)
			{
				AppController controller = AppController.appControllerInstance;  
				controller.changeLanguage(AppController.bundle);
			}
			else if(pageIDForChangeLanguage == 1)
			{
				eXoApplicationsController controller = eXoApplicationsController.eXoApplicationsControllerInstance;
				controller.changeLanguage(AppController.bundle);
				controller.createAdapter();
			}
			else if(pageIDForChangeLanguage == 2)
			{
				eXoFilesController controller = eXoFilesController.eXoFilesControllerInstance;
				controller.changeLanguage(AppController.bundle);
			}
			else if(pageIDForChangeLanguage == 3)
			{
				eXoChatListController controller = eXoChatListController.eXoChatListControllerInstance;
				controller.changeLanguage(AppController.bundle);
			}
			else if(pageIDForChangeLanguage == 4)
			{
				eXoChatController controller = eXoChatController.eXoChatControllerInstance;
				controller.changeLanguage(AppController.bundle);
			}
			else if(pageIDForChangeLanguage == 5)
			{
				eXoWebViewController controller = eXoWebViewController.eXoWebViewControllerInstance;
				controller.changeLanguage(AppController.bundle);
			}
			else if(pageIDForChangeLanguage == 6)
			{
				eXoGadgetViewController controller = eXoGadgetViewController.eXoGadgetViewControllerInstance;
				controller.changeLanguage(AppController.bundle);
			}
			
		}
		catch (Exception e) 
		{
			
		}
		   
	}
	
//	Set language   
	public void changeLanguage(ResourceBundle resourceBundle)
	{
		String strLanguageTittle = "";
	   	String strServerTittle = "";
   		String strmyOptionEnglish = "";
   		String strmyOptionFrench  = "";
   		String strCloseModifyServerLisrButton = "";
   		String strUserGuideButton = "";
   	
   		try 
   		{
   			strLanguageTittle = new String(resourceBundle.getString("Language").getBytes("ISO-8859-1"), "UTF-8");
   			strServerTittle = new String(resourceBundle.getString("Server").getBytes("ISO-8859-1"), "UTF-8");
   			strmyOptionEnglish = new String(resourceBundle.getString("English").getBytes("ISO-8859-1"), "UTF-8");
   			strmyOptionFrench = new String(resourceBundle.getString("French").getBytes("ISO-8859-1"), "UTF-8");
   			strCloseModifyServerLisrButton = new String(resourceBundle.getString("ModifyServerList").getBytes("ISO-8859-1"), "UTF-8");
   			strUserGuideButton = new String(resourceBundle.getString("UserGuide").getBytes("ISO-8859-1"), "UTF-8");
		} catch (Exception e) {
			
		}
   	
		myOptionEnglish.setText(strmyOptionEnglish);
		myOptionFrench.setText(strmyOptionFrench);
		
		btnModifyTheList.setText(strCloseModifyServerLisrButton);
		btnUserGuide.setText(strUserGuideButton);
	   
		txtvServer.setText(strServerTittle);
		txtvLanguage.setText(strLanguageTittle);
		
	}
}
