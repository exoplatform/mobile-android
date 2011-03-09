package eXo.eXoPlatform;

import java.util.List;
import java.util.ResourceBundle;

import org.apache.http.cookie.Cookie;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//Display gadget
public class eXoWebViewController extends Activity
{
	Button _btnClose;
	WebView _wvGadget;
	TextView _txtvTitleBar;
	String strCannotBackToPreviousPage;
	
	 public void onCreate(Bundle icicle) 
	 {
		 		 
		 super.onCreate(icicle);
//		 requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		 requestWindowFeature(Window.FEATURE_NO_TITLE);
		 setContentView(R.layout.webview);
		 
		
		 setupCookies();
		 
		 
		 _wvGadget = (WebView) findViewById(R.id.WebView_01);
		 _txtvTitleBar = (TextView) findViewById(R.id.TextView_TitleBar);
		 
		 
		 _wvGadget.getSettings().setJavaScriptEnabled(true);
		 _wvGadget.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		 _wvGadget.getSettings().setPluginsEnabled(true);
		 _wvGadget.getSettings().setLoadsImagesAutomatically(true);
		 _wvGadget.addJavascriptInterface(this, "MainScreen");
		 
		 _wvGadget.getSettings().setBuiltInZoomControls(false);
		 
		 String url = null;
		 if(eXoApplicationsController.webViewMode == 0)
		 {
			 url = eXoGadgetViewController.currentGadget.getGadgetUrl();
			 String titlebar = eXoGadgetViewController.currentGadget._strGadgetName;
			 _txtvTitleBar.setText(titlebar.replace("%20", " "));
		 }
		 else if(eXoApplicationsController.webViewMode == 1)
		 {
//			 
//			 url = eXoFilesController._strCurrentDirectory; 
//			 String fileName = eXoFilesController.myFile.fileName;
////			 url = "file:///data/data/eXo.eXoMobile/files/" + fileName;
			 url = "file:///sdcard/eXo/" + eXoFilesController.myFile.fileName;
			 _txtvTitleBar.setText(eXoFilesController.myFile.fileName);
//			 _wvGadget.loadUrl(url);
			 
//			String host = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN, "exo_prf_domain");
//			String strUserName = AppController.sharedPreference.getString(AppController.EXO_PRF_USERNAME, "exo_prf_username");
//			String strPassword = AppController.sharedPreference.getString(AppController.EXO_PRF_PASSWORD, "exo_prf_password");
//			
//			String[] test = _wvGadget.getHttpAuthUsernamePassword(host, "123");
//			WebViewDatabase.getInstance(this).clearHttpAuthUsernamePassword();
//			String[] test2 = _wvGadget.getHttpAuthUsernamePassword(host, "123");
//			_wvGadget.setHttpAuthUsernamePassword(host, "123", strUserName, strPassword);
//			String[] test3 = _wvGadget.getHttpAuthUsernamePassword(host, "123");
//			_wvGadget.loadUrl(eXoFilesController._strCurrentDirectory);
			
			
			//_wvGadget.loadUrl("http://globaltvlive.files.wordpress.com/2007/04/jesu-angel.jpg");
		 }
		 else if(eXoApplicationsController.webViewMode == 2)
		 {
			 _txtvTitleBar.setText("User guide");
			 String locallize = AppController.sharedPreference.getString(AppController.EXO_PRF_LOCALIZE, "exo_prf_localize");
		     if(locallize.equalsIgnoreCase("LocalizeEN.properties"))
		    	 url = "file:///android_asset/HowtoUse-EN.htm";
		     else if(locallize.equalsIgnoreCase("LocalizeFR.properties"))
		    	 url = "file:///android_asset/HowtoUse-FR.htm";
		     else
		   	  	url = "file:///android_asset/HowtoUse-VN.htm";
		 }

		 _wvGadget.loadUrl(url);
		
		 _btnClose = (Button) findViewById(R.id.Button_Back);
		 _btnClose.setOnClickListener(new View.OnClickListener() 
	     {	
			 
			 public void onClick(View v) 
			 {
				 
				 if(eXoApplicationsController.webViewMode == 1)
				 {
					 int index = eXoFilesController.myFile.urlStr.lastIndexOf("/");
					 eXoFilesController.myFile.urlStr = eXoFilesController.myFile.urlStr.substring(0, index);
					  
				 }
				 
				 eXoWebViewController.this.finish();
//				 Intent next = new Intent(eXoWebViewController.this, eXoApplicationsController.class);
//				 eXoWebViewController.this.startActivity(next);
			 }
	     });
		 
		changeLanguage(AppController.bundle);
	 } 
	 
	 
	 private void setupCookies() {
		    List<Cookie> cookies = AppController._eXoConnection._sessionCookies;// .authenticateAndReturnCookies();
		 
		    if (cookies != null) {
		    		    	
		       //CookieManager.getInstance().removeSessionCookie();

		      for (Cookie cookie : cookies) {
		    		String cookieString = cookie.getName() + "=" + cookie.getValue();
		    		CookieManager.getInstance().setCookie(cookie.getDomain(), cookieString);
		      }
		      
		      //CookieManager.getInstance().setCookie("mobile.demo.exoplatform.org","JSESSIONID="+ cookie.getValue()+";domain=mobile.demo.exoplatform.org");
		    	
		      CookieSyncManager.getInstance().sync();
		    }
	 }

	 public boolean onKeyDown(int keyCode, KeyEvent event) {
	      //Save data to the server once the user hits the back button
	      if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	          Toast.makeText(eXoWebViewController.this, strCannotBackToPreviousPage ,Toast.LENGTH_LONG).show();
	      }
	      return false;
	  }
	 
	 public void changeLanguage(ResourceBundle resourceBundle)
	 {
		 
		 String str_btnBack = "";
		 try {
			 str_btnBack = new String(resourceBundle.getString("CloseButton").getBytes("ISO-8859-1"), "UTF-8");
			 strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage").getBytes("ISO-8859-1"), "UTF-8");
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		 _btnClose.setText(str_btnBack);
	 }
}
