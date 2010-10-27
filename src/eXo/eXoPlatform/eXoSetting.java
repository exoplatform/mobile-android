package eXo.eXoPlatform;

import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;

public class eXoSetting extends Dialog implements OnClickListener {
	Context myContext;
	RadioButton myOptionEnglish, myOptionFrench, myOptionVietnamese;
	Button btnClose;
	Button btnHelpAndSetting;
	TextView changeLanguageTittle; 
 
 public eXoSetting(Context context) {
	 super(context);
      
      myContext = context;
      
      /** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
      //requestWindowFeature(Window.FEATURE_NO_TITLE);
      /** Design the dialog in main.xml file */
     setContentView(R.layout.exohelpandsetting);
    
     btnHelpAndSetting = (Button) findViewById(R.id.Button_HelpAndSetting);
     btnHelpAndSetting.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				Intent next = new Intent(eXoSetting.this, eXoApplicationsController.class);
//				eXoSetting.this.startActivity(next);
			}
		});
     
     btnClose = (Button) findViewById(R.id.Button_Close);
     btnClose.setOnClickListener(this);
     
    
     
     changeLanguageTittle = (TextView) findViewById(R.id.changeLanguage);
     
     myOptionEnglish = (RadioButton)findViewById(R.id.english);
     myOptionFrench = (RadioButton)findViewById(R.id.french);
     myOptionVietnamese = (RadioButton)findViewById(R.id.vietnamese);
	 	        
		        
     myOptionEnglish.setOnClickListener(new View.OnClickListener() {
    	 public void onClick(View v) {
//    		 TODO Auto-generated method stub
			updateLocallize("LocalizeEN.properties");
			}
	});
     
     myOptionFrench.setOnClickListener(new View.OnClickListener() {
    	 public void onClick(View v) {
		// TODO Auto-generated method stub
		updateLocallize("LocalizeFR.properties");
    	}
     });
     
     myOptionVietnamese.setOnClickListener(new View.OnClickListener() {
    	 public void onClick(View v) {
		// TODO Auto-generated method stub
    	updateLocallize("LocalizeVN.properties");
    	 }
     });
		        
     changeLanguage(AppController.bundle);
     String locallize = AppController.sharedPreference.getString(AppController.EXO_PRF_LOCALIZE, "exo_prf_localize");
     if(locallize.equalsIgnoreCase("LocalizeEN.properties"))
   	  myOptionEnglish.setChecked(true);
     else if(locallize.equalsIgnoreCase("LocalizeFR.properties"))
   	  myOptionFrench.setChecked(true);
     else
   	  myOptionVietnamese.setChecked(true);
		        
}
	 	 

   private void updateLocallize(String localize)
   {
	   try 
	   {
		   SharedPreferences.Editor editor = AppController.sharedPreference.edit();
		   AppController.bundle = new PropertyResourceBundle(myContext.getAssets().open(localize));
		   editor.putString(AppController.EXO_PRF_LOCALIZE, localize);
		   editor.commit();
			
		   changeLanguage(AppController.bundle);
	   }
	   catch (Exception e) 
	   {
		// TODO: handle exception
	   }
	   
   }
   
   public  void changeLanguage(ResourceBundle resourceBundle)
   {
	   
	   	String strchangeLanguageTittle = "";
   		String strbackBtn = "";
   		String strmyOptionEnglish = "";
   		String strmyOptionFrench  = "";
   		String strmyOptionVietnamese = "";
   	
   		try {
   			strchangeLanguageTittle = new String(resourceBundle.getString("Language").getBytes("ISO-8859-1"), "UTF-8");
   			strbackBtn = new String(resourceBundle.getString("BackButton").getBytes("ISO-8859-1"), "UTF-8");
   			strmyOptionEnglish = new String(resourceBundle.getString("English").getBytes("ISO-8859-1"), "UTF-8");
   			strmyOptionFrench = new String(resourceBundle.getString("French").getBytes("ISO-8859-1"), "UTF-8");
   			strmyOptionVietnamese = new String(resourceBundle.getString("Vietnamese").getBytes("ISO-8859-1"), "UTF-8");
       		
		} catch (Exception e) {
			// TODO: handle exception
		}
   	
   	
		changeLanguageTittle.setText(strchangeLanguageTittle);
		btnClose.setText(strbackBtn);
		myOptionEnglish.setText(strmyOptionEnglish);
		myOptionFrench.setText(strmyOptionFrench);
		myOptionVietnamese.setText(strmyOptionVietnamese);
	   
   }


public void onClick(View v) {
	// TODO Auto-generated method stub
	dismiss();
}
}