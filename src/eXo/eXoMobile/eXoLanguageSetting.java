package eXo.eXoMobile;

import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

/** Class Must extends with Dialog */
/** Implement onClickListener to dismiss dialog when OK Button is pressed */
public class eXoLanguageSetting extends Dialog implements OnClickListener {
	Context myContext;
	Button btnClose;
	Button btnUserGuide;
	RadioButton myOptionEnglish, myOptionFrench;
	int pageIDForChangeLanguage;//0: AppController, 1: eXoApplicationController
								//2: eXoFileController, 3: eXoChatList, 4: eXoChat
								//5: eXoWebView
	TextView changeLanguageTittle; 
	
	Activity _activity;	

	public eXoLanguageSetting(Context context, int pageID, Activity activity) {
		super(context);
		
		myContext = context;
		pageIDForChangeLanguage = pageID;
		_activity = activity;
		
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/** Design the dialog in main.xml file */
		setContentView(R.layout.exolanguagesetting);
		
		btnClose = (Button) findViewById(R.id.Button_Close);
		btnClose.setOnClickListener(this);
		
		btnUserGuide = (Button) findViewById(R.id.Button_Help);
		btnUserGuide.setOnClickListener(this);
		
		changeLanguageTittle = (TextView) findViewById(R.id.changeLanguage);
	     
	     myOptionEnglish = (RadioButton)findViewById(R.id.english);
	     myOptionFrench = (RadioButton)findViewById(R.id.french);
	     
	     myOptionEnglish.setOnClickListener(this);
	     myOptionFrench.setOnClickListener(this);
	     
	     changeLanguage(AppController.bundle);
	     
	     String locallize = AppController.sharedPreference.getString(AppController.EXO_PRF_LOCALIZE, "exo_prf_localize");
	     if(locallize.equalsIgnoreCase("LocalizeEN.properties"))
	    	 myOptionEnglish.setChecked(true);
	     else if(locallize.equalsIgnoreCase("LocalizeFR.properties"))
	   	  	myOptionFrench.setChecked(true); 
	     else
	    	 myOptionEnglish.setChecked(true);
	    
	}

	
	public void onClick(View v) {
		/** When OK Button is clicked, dismiss the dialog */
		if (v == btnClose)
		{
			dismiss();
			return;
		}
		if(v == myOptionEnglish)
		{
			updateLocallize("LocalizeEN.properties");
			//AppController.changeLanguage(AppController.bundle);
		}
		else if(v == myOptionFrench)
		{
			updateLocallize("LocalizeFR.properties");
			//AppController.changeLanguage(AppController.bundle);
		}
		else
		{
			AppController.thisClass.showUserGuide();
		}
		
	}

	private void updateLocallize(String localize)
	{
		try 
		{
			SharedPreferences.Editor editor = AppController.sharedPreference.edit();
			editor.putString(AppController.EXO_PRF_LOCALIZE, localize);
			editor.commit();
			   
			AppController.bundle = new PropertyResourceBundle(myContext.getAssets().open(localize));
			changeLanguage(AppController.bundle);
			   
			if(pageIDForChangeLanguage == 0)
			{
				AppController controller = (AppController)_activity;
				controller.changeLanguage(AppController.bundle);
			}
			else if(pageIDForChangeLanguage == 1)
			{
				eXoApplicationsController controller = (eXoApplicationsController)_activity;
				controller.changeLanguage(AppController.bundle);
				controller.createAdapter();
			}
			else if(pageIDForChangeLanguage == 2)
			{
				eXoFilesController controller = (eXoFilesController)_activity;
				controller.changeLanguage(AppController.bundle);
			}
			else if(pageIDForChangeLanguage == 3)
			{
				eXoChatList controller = (eXoChatList)_activity;
				controller.changeLanguage(AppController.bundle);
			}
			else if(pageIDForChangeLanguage == 4)
			{
				eXoChat controller = (eXoChat)_activity;
				controller.changeLanguage(AppController.bundle);
			}
			else if(pageIDForChangeLanguage == 5)
			{
				eXoWebViewController controller = (eXoWebViewController)_activity;
				controller.changeLanguage(AppController.bundle);
			}
			else if(pageIDForChangeLanguage == 6)
			{
				eXoGadgetViewController controller = (eXoGadgetViewController)_activity;
				controller.changeLanguage(AppController.bundle);
			}
			
		}
		catch (Exception e) 
		{
			// TODO: handle exception
		}
		   
	}
	   
	public void changeLanguage(ResourceBundle resourceBundle)
	   {
		   
		   	String strchangeLanguageTittle = "";
	   		String strmyOptionEnglish = "";
	   		String strmyOptionFrench  = "";
	   		String strCloseButton = "";
	   		String strHelpButton = "";
	   	
	   		try {
	   			strchangeLanguageTittle = new String(resourceBundle.getString("Language").getBytes("ISO-8859-1"), "UTF-8");
	   			strmyOptionEnglish = new String(resourceBundle.getString("English").getBytes("ISO-8859-1"), "UTF-8");
	   			strmyOptionFrench = new String(resourceBundle.getString("French").getBytes("ISO-8859-1"), "UTF-8");
	   			strCloseButton = new String(resourceBundle.getString("CloseButton").getBytes("ISO-8859-1"), "UTF-8");
	   			strHelpButton = new String(resourceBundle.getString("UserGuide").getBytes("ISO-8859-1"), "UTF-8");
			} catch (Exception e) {
				// TODO: handle exception
			}
	   	
			changeLanguageTittle.setText(strchangeLanguageTittle);
			myOptionEnglish.setText(strmyOptionEnglish);
			myOptionFrench.setText(strmyOptionFrench);
			
			btnClose.setText(strCloseButton);
			btnUserGuide.setText(strHelpButton);
		   
	   }

}
