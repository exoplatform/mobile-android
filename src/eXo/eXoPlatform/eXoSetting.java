package eXo.eXoPlatform;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RadioButton;

public class eXoSetting extends Activity {

	Button btnHome;
	Button btnModifyTheList;
	Button btnUserGuide;
	
	RadioButton myOptionEnglish, myOptionFrench;
	int pageIDForChangeLanguage;//0: AppController, 1: eXoApplicationController
								//2: eXoFileController, 3: eXoChatList, 4: eXoChat
								//5: eXoWebView
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.exosetting);
        
        btnModifyTheList = (Button)findViewById(R.id.Button_Modify_The_List);
        btnUserGuide = (Button)findViewById(R.id.Button_User_Guide);
        btnHome = (Button)findViewById(R.id.Button_Home);
        
        myOptionEnglish = (RadioButton)findViewById(R.id.RadioButton_English);
	    myOptionFrench = (RadioButton)findViewById(R.id.RadioButton_French);
	    
	    btnHome.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	    
	    btnModifyTheList.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
	    
	    btnUserGuide.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});

	    myOptionEnglish.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
	    myOptionFrench.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
        
        
    }
        
}
