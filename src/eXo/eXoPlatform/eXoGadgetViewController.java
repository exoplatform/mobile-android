package eXo.eXoPlatform;


import java.util.ResourceBundle;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;


public class eXoGadgetViewController extends Activity 
{
    /** Called when the activity is first created. */	
		
	Button _btnClose;
	Button _btnLanguageHelp;
	TextView _txtvTitleBar;
	
	ListView _lstvGadgets;
	
	String strCannotBackToPreviousPage;
	String strConnectionTimedOut;
	
	eXoGadgetViewController thisClass;
	static eXoApplicationsController _delegate;
	
	public static eXoGadget currentGadget;
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.exogadgetview);
        
        thisClass = this;

        _btnClose = (Button) findViewById(R.id.Button_Close);
        _btnClose.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				eXoGadgetViewController.this.finish();
			}
		});

        _btnLanguageHelp = (Button) findViewById(R.id.Button_Language_Help);
        _btnLanguageHelp.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				eXoLanguageSetting customizeDialog = new eXoLanguageSetting(eXoGadgetViewController.this, 6, thisClass);
        		customizeDialog.show();
				
			}
		});
        
        _txtvTitleBar = (TextView) findViewById(R.id.TextView_TitleBar);
        
        _lstvGadgets = (ListView) findViewById(R.id.ListView_Gadgets);
      
        BaseAdapter adapter = new BaseAdapter() {
			
        	 
        	public View getView(int position, View convertView, ViewGroup parent) 
		    {
        		LayoutInflater inflater = thisClass.getLayoutInflater();
			   	View rowView = inflater.inflate(R.layout.rowinlistview, parent, false);
			   	
        		GateInDbItem gadgetTab = eXoApplicationsController.gadgetTab;
        		if(gadgetTab._arrGadgetsInItem == null)
        			return rowView;
        		
        		_txtvTitleBar.setText(gadgetTab._strDbItemName);
        		final eXoGadget gadget = gadgetTab._arrGadgetsInItem.get(position);
        		
        		
			   	rowView.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						// TODO Auto-generated method stub
						eXoApplicationsController.webViewMode = 0;
						currentGadget = gadget;
						DefaultHttpClient client = new DefaultHttpClient();
						AppController._eXoConnection.loginForStandaloneGadget(currentGadget._strGadgetUrl, "demo", "gtn");
					    HttpGet get = new HttpGet(currentGadget._strGadgetUrl);
					    try {
					    	HttpResponse response = client.execute(get);
					        int status = response.getStatusLine().getStatusCode();
					        if(status < 200 || status >= 300)
					        {
					        	Toast.makeText(eXoGadgetViewController.this, strConnectionTimedOut, Toast.LENGTH_LONG).show();
					        	return;
					        }
					    } catch (Exception e) {
								// TODO: handle exception
					    	return;
						}
						
			            Intent next = new Intent(eXoGadgetViewController.this, eXoWebViewController.class);
			            eXoGadgetViewController.this.startActivity(next);
					}
				});
			   	
			    	
			   	TextView label = (TextView) rowView.findViewById(R.id.label);
		    	label.setText(gadget._strGadgetName);
		    	TextView description = (TextView) rowView.findViewById(R.id.description);
		    	description.setText(gadget._strGadgetDescription);
		    	ImageView icon=(ImageView) rowView.findViewById(R.id.icon);    		
	    		icon.setImageBitmap(gadget._btmGadgetIcon);
			    	
			   	return(rowView);
		    }

		   
			public long getItemId(int arg0) {
				// TODO Auto-generated method stub
				return arg0;
			}
			
			public Object getItem(int arg0) {
				// TODO Auto-generated method stub
				return arg0;
			}
			
			public int getCount() {
				// TODO Auto-generated method stub
				return eXoApplicationsController.gadgetTab._arrGadgetsInItem.size();
			}
		};
		if(eXoApplicationsController.gadgetTab._arrGadgetsInItem != null)
			_lstvGadgets.setAdapter(adapter);
        
        changeLanguage(AppController.bundle);
        
    }
    
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Save data to the server once the user hits the back button
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Toast.makeText(eXoGadgetViewController.this, strCannotBackToPreviousPage, Toast.LENGTH_LONG).show();
        }
        return false;
    }

    public void changeLanguage(ResourceBundle resourceBundle)
    {
    	
    	String strClose = "";
    	
    	try {
//    		strTitleBar = new String(resourceBundle.getString("SignInInformation").getBytes("ISO-8859-1"), "UTF-8"); 
    		strClose = new String(resourceBundle.getString("CloseButton").getBytes("ISO-8859-1"), "UTF-8");
    		
        	strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage").getBytes("ISO-8859-1"), "UTF-8");
        	strConnectionTimedOut = new String(resourceBundle.getString("ConnectionTimedOut").getBytes("ISO-8859-1"), "UTF-8");
        	
//        	
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
		_btnClose.setText(strClose);
		
		_delegate.changeLanguage(resourceBundle);
    	_delegate.createAdapter();
    }
    
   
}