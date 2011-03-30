package eXo.eXoPlatform;

import java.util.ResourceBundle;

import eXo.eXoPlatform.AppController.Configuration;
import eXo.eXoPlatform.AppController.ServerObj;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/** Class Must extends with Dialog */
/** Implement onClickListener to dismiss dialog when OK Button is pressed */
//Display language setting & user guide page
public class eXoLanguageSettingDialog extends Dialog implements OnClickListener {
	
	Button btnOK;
	Button btnDeleteCancel;
	
	TextView txtvTittle; 
	TextView txtvServerName;
	TextView txtvServerUrl;
	
	EditText editTextServerName;
	EditText editTextServerUrl;
	
	static int selectedServerIndex;
	static boolean isNewServer;
	ServerObj serverObj;
	
	
//	Constructor
	public eXoLanguageSettingDialog(Context context) {
		
		super(context);
		
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/** Design the dialog in main.xml file */
		setContentView(R.layout.exolanguagesetting);
		
		btnOK = (Button) findViewById(R.id.Button_OK);
		btnOK.setOnClickListener(this);
		
		btnDeleteCancel = (Button) findViewById(R.id.Button_Delete_Cancel);
		btnDeleteCancel.setOnClickListener(this);
		
		txtvTittle = (TextView) findViewById(R.id.TextView_Title);
		txtvServerName = (TextView)findViewById(R.id.TextView_Server_Name);
		txtvServerUrl = (TextView)findViewById(R.id.TextView_Server_URL);
	    
		editTextServerName = (EditText)findViewById(R.id.EditText_Server_Name);
		editTextServerUrl = (EditText)findViewById(R.id.EditText_Server_URL);
		if(isNewServer)
		{
			serverObj = AppController.appControllerInstance.new ServerObj();
			serverObj._bSystemServer = false;
			serverObj._strServerName = "";
			serverObj._strServerUrl = "";
		}
		else
		{
			serverObj = AppController.configurationInstance._arrServerList.get(selectedServerIndex);
		}
		
		editTextServerName.setText(serverObj._strServerName);
		editTextServerUrl.setText(serverObj._strServerUrl);
		
	    changeLanguage(AppController.bundle);
	}

//	Show user guide or change language
	public void onClick(View v) {
		/** When OK Button is clicked, dismiss the dialog */
		
		Configuration conf = AppController.configurationInstance;
		
		ServerObj myServerObj = AppController.appControllerInstance.new ServerObj();
		
		myServerObj._strServerName = editTextServerName.getText().toString();
		myServerObj._strServerUrl = editTextServerUrl.getText().toString();
		
		if (v == btnOK)
		{
			if(myServerObj._strServerName.equalsIgnoreCase("") ||
					myServerObj._strServerUrl.equalsIgnoreCase(""))
			{
				//Server name or server url is empty
				return;
			}
			if(isNewServer)
			{
				boolean isExisted = false;
				for(int i = 0; i < conf._arrServerList.size(); i++)
				{
					ServerObj tmp = conf._arrServerList.get(i);
					if(myServerObj._strServerName.equalsIgnoreCase(tmp._strServerName) &&
							myServerObj._strServerUrl.equalsIgnoreCase(tmp._strServerUrl))
					{
						isExisted = true;
						break;
						//New server is the same with the old one
					}
				}
				if(!isExisted)
				{
//					Create new server
					AppController.configurationInstance._arrServerList.add(myServerObj);
					eXoModifyServerList.eXoModifyServerListInstance.createServersAdapter(AppController.configurationInstance._arrServerList);
					eXoSetting.eXoSettingInstance.createServersAdapter(AppController.configurationInstance._arrServerList);
					AppController.appControllerInstance.createServersAdapter(AppController.configurationInstance._arrServerList);
				}
			}
			else //Update server
			{
				boolean isExisted = false;
				for(int i = 0; i < conf._arrServerList.size(); i++)
				{
					ServerObj tmp = conf._arrServerList.get(i);
					
					if(i == selectedServerIndex)
					{
						continue;
					}
					
					if(myServerObj._strServerName.equalsIgnoreCase(tmp._strServerName) &&
							myServerObj._strServerUrl.equalsIgnoreCase(tmp._strServerUrl))
					{
						isExisted = true;
						break;
						//updated server is the same with the old one
					}
					
				}
				if(isExisted)
				{
//					update server
				}
			}
		}
		else if(v == btnDeleteCancel)
		{
			if(isNewServer)	//Cancel
			{
				
			}
			else	//Delete sever
			{
				AppController.configurationInstance._arrServerList.remove(selectedServerIndex);
				eXoModifyServerList.eXoModifyServerListInstance.createServersAdapter(AppController.configurationInstance._arrServerList);
				eXoSetting.eXoSettingInstance.createServersAdapter(AppController.configurationInstance._arrServerList);
				AppController.appControllerInstance.createServersAdapter(AppController.configurationInstance._arrServerList);
			}
		}
		
		dismiss();
		
	}

	//	Set language   
	public void changeLanguage(ResourceBundle resourceBundle)
	   {
		   
		   	String strTittle = "";
		   	String strServerName = "";
		   	String strServerUrl = "";
	   		
	   		String strOKButton = "";
	   		String strDeleteCancelButton = "";
	   	
	   		try {
	   			if(isNewServer)	//New server
	   			{
	   				strTittle = new String(resourceBundle.getString("NewServer").getBytes("ISO-8859-1"), "UTF-8");
	   				strDeleteCancelButton = new String(resourceBundle.getString("Cancel").getBytes("ISO-8859-1"), "UTF-8");
	   			}
	   			else	//Server detail
	   			{
	   				strTittle = new String(resourceBundle.getString("ServerDetail").getBytes("ISO-8859-1"), "UTF-8");
	   				strDeleteCancelButton = new String(resourceBundle.getString("Delete").getBytes("ISO-8859-1"), "UTF-8");
	   			}
	   			strServerName = new String(resourceBundle.getString("NameOfTheServer").getBytes("ISO-8859-1"), "UTF-8");
	   			strServerUrl = new String(resourceBundle.getString("URLOfTheSerVer").getBytes("ISO-8859-1"), "UTF-8");
	   			
	   			strOKButton = new String(resourceBundle.getString("OK").getBytes("ISO-8859-1"), "UTF-8");
	   			
			} catch (Exception e) {
				// TODO: handle exception
			}
	   	
			txtvTittle.setText(strTittle);
			txtvServerName.setText(strServerName);
			txtvServerUrl.setText(strServerUrl);
			
			btnOK.setText(strOKButton);
			btnDeleteCancel.setText(strDeleteCancelButton);
		   
	   }

}
