package eXo.eXoPlatform;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class AppController extends Activity 
{
    /** Called when the activity is first created. */	
	public static final String EXO_PREFERENCE = "exo_preference";
	public static final String EXO_PRF_DOMAIN = "exo_prf_domain";
	public static final String EXO_PRF_USERNAME = "exo_prf_username";
	public static final String EXO_PRF_PASSWORD = "exo_prf_password";
	public static final String EXO_PRF_LANGUAGE = "exo_prf_language";
	public static final String EXO_PRF_LOCALIZE = "exo_prf_localize";
	
	public static AuthScope auth = null;
	public static UsernamePasswordCredentials credential = null;
	public static SharedPreferences sharedPreference;
	public static ResourceBundle bundle;

	
	private Runnable viewOrders;
	
	String _strDomain = "";
	String _strUserName = "";
	String _strPassword = "";
	
	//private eXoConnection _eXoConnection = new eXoConnection();
	public static eXoConnection _eXoConnection = new eXoConnection();
	
	TextView _txtViewDomain;
	TextView _txtViewUserName;
	TextView _txtViewPassword;
	EditText _edtxDomain;
	EditText _edtxUserName;
	EditText _edtxPassword;
	Button _btnSignIn;
	Button _btnLanguageHelp;
	
	TextView _txtvTitleBar;
	
	String strWait;
	String strSigning;
	String strNetworkConnextionFailed;
	String strUserNamePasswordFailed;
	String strCannotBackToPreviousPage;
	String strLoadingDataFromServer;
	
	
	public static AppController thisClass;
	Intent next;
	Thread thread;

	public static ProgressDialog _progressDialog = null; 	
	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.login);

        String strLocalize;
    	
        thisClass = this;
        if(sharedPreference == null)
        	sharedPreference = getSharedPreferences(EXO_PREFERENCE, 0);

        
        strLocalize = sharedPreference.getString(EXO_PRF_LOCALIZE, "exo_prf_localize");
        if(strLocalize == null || strLocalize.equalsIgnoreCase("exo_prf_localize"))
        	strLocalize = "LocalizeEN.properties";
        
        try {

        	bundle = new PropertyResourceBundle(getAssets().open(strLocalize));
    		
		} catch (Exception e) {
			// TODO: handle exception
		}
        
        _txtViewDomain = (TextView) findViewById(R.id.TextView_Domain);
        _txtViewUserName = (TextView) findViewById(R.id.TextView_UserName);
        _txtViewPassword = (TextView) findViewById(R.id.TextView_Password);
        
        _edtxDomain = (EditText) findViewById(R.id.EditText_Domain);
        _edtxUserName = (EditText) findViewById(R.id.EditText_UserName);
        _edtxPassword = (EditText) findViewById(R.id.EditText_Password);
        
        _btnSignIn = (Button) findViewById(R.id.Button_SignIn);
        _btnLanguageHelp = (Button) findViewById(R.id.Button_Language_Help);
        _txtvTitleBar = (TextView) findViewById(R.id.TextView_TitleBar);
        
        _strDomain = sharedPreference.getString(EXO_PRF_DOMAIN, "");
        _strUserName = sharedPreference.getString(EXO_PRF_USERNAME, "");
        _strPassword = sharedPreference.getString(EXO_PRF_PASSWORD, "");

        _edtxDomain.setText(_strDomain);
    	_edtxUserName.setText(_strUserName);
    	_edtxPassword.setText(_strPassword);
    	
    	_edtxUserName.setSingleLine(true);
    	
    	
        changeLanguage(bundle);
        
       _btnSignIn.setOnClickListener(new View.OnClickListener() 
        {	
        	public void onClick(View v) 
			{
                viewOrders = new Runnable()
                {
                    public void run()
                    {
                    	InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    	imm.hideSoftInputFromWindow(_edtxDomain.getWindowToken(), 0);
                    	imm.hideSoftInputFromWindow(_edtxUserName.getWindowToken(), 0);
                    	imm.hideSoftInputFromWindow(_edtxPassword.getWindowToken(), 0);

                    	signInProgress();
                    }
                };
                
        		thread =  new Thread(null, viewOrders, "SigningIn"); 
                thread.start();
                
                _progressDialog = ProgressDialog.show(AppController.this, strWait, strSigning);
//                _progressDialog.setIcon(R.drawable.wait);
                
			}	
		});     
        
        _btnLanguageHelp.setOnClickListener(new View.OnClickListener() 
        {	
        	public void onClick(View v) 
			{
        		eXoLanguageSetting customizeDialog = new eXoLanguageSetting(AppController.this, 0, thisClass);
        		customizeDialog.show();
			}	
		});

    }
     
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //Save data to the server once the user hits the back button
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            Toast.makeText(AppController.this, strCannotBackToPreviousPage, Toast.LENGTH_LONG).show();
        }
       
        return false;
    }
    
    private Runnable returnRes = new Runnable() 
    {
    	public void run() 
    	{
    		
    		_progressDialog.setMessage(strLoadingDataFromServer);
    		
               //    		_progressDialog.dismiss();
//    		
//            _progressDialog = ProgressDialog.show(AppController.this, strWait, strLoadingDataFromServer);
//            _progressDialog.setIcon(R.drawable.wait);
    		
    		eXoApplicationsController.arrGadgets = listOfGadgets();
//    		eXoApplicationsController.arrGadgets = null;
    		Intent next = new Intent(AppController.this, eXoApplicationsController.class);
    		startActivity(next);
    		
    		thread.stop();
    		
        }
    };
    
    private Runnable returnResFaileConnection = new Runnable() 
    {
    	public void run() 
    	{
    		_progressDialog.dismiss();
    		
    		AlertDialog.Builder builder = new AlertDialog.Builder(thisClass); 
	        builder.setMessage(strNetworkConnextionFailed); 
	        builder.setCancelable(false); 
	        
	        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
	        	public void onClick(DialogInterface dialog, int id) {
	        		
	        	} 
	        });
	        
	        AlertDialog alert = builder.create(); 
	        alert.show(); 
	        
	        thread.stop();
    			
        }
    };
    
    private Runnable returnResFaileUserNamePassword = new Runnable() 
    {
    	public void run() 
    	{
    		_progressDialog.dismiss();
    		
    		AlertDialog.Builder builder = new AlertDialog.Builder(thisClass); 
	        builder.setMessage(strUserNamePasswordFailed); 
	        builder.setCancelable(false); 
	        
	        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
	        	public void onClick(DialogInterface dialog, int id) {
	        		
	        	} 
	        });
	        
	        AlertDialog alert = builder.create(); 
	        alert.show(); 
	        
	        thread.stop();
    			
        }
    };
        
    public void signInProgress()
    {	
    	
    	_strDomain = _edtxDomain.getText().toString();

    	try {
    		
    		
    		if(_strDomain.indexOf("http://") == -1)
        	{
        		_strDomain = "http://" + _strDomain;
        	}

    		
    		URL url = new URL(_strDomain);
//    		HttpURLConnection con = (HttpURLConnection) url.openConnection();
//    		int code = con.getResponseCode();
    		

        	_strUserName = _edtxUserName.getText().toString();
        	_strPassword = _edtxPassword.getText().toString();
        	
        	//String strResult = "NO";
        	String strResult = _eXoConnection.sendAuthentication(_strDomain, _strUserName, _strPassword);
        	if(strResult == "YES")
        	{
        		
        		SharedPreferences.Editor editor = sharedPreference.edit();
        		editor.putString(EXO_PRF_DOMAIN, _strDomain);
        		editor.putString(EXO_PRF_USERNAME, _strUserName);
        		editor.putString(EXO_PRF_PASSWORD, _strPassword);
        		editor.commit();
        		
        		createAuthorization(url.getHost(), url.getPort());
        		
        		runOnUiThread(returnRes);
        	}
        	else if(strResult == "NO")
        	{
        		runOnUiThread(returnResFaileUserNamePassword);
        	}
        	else
        	{
        		runOnUiThread(returnResFaileConnection);
        	}
        	
		} catch (Exception e) {
			// TODO: handle exception
//			String msg = e.getMessage();
//			String str = e.toString();
//			Log.v(str, msg);
			runOnUiThread(returnResFaileConnection);
		}
    }
   
    public List<eXoGadget> getGadgetsList()
	{
		List<eXoGadget> arrGadgets = new ArrayList<eXoGadget>();
		_strDomain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN, "exo_prf_domain");
		String strHomeUrl = _strDomain + "/portal/private/classic"; 
        String strContent = AppController._eXoConnection.sendRequestAndReturnString(strHomeUrl); 
		
        String strGadgetMark = "eXo.gadget.UIGadget.createGadget";
        String title;
        String url;
        String description;
        Bitmap bmp;
        
        int indexStart;
        int indexEnd;
        String tmpStr = strContent;
        indexStart = tmpStr.indexOf(strGadgetMark);
        
        while(indexStart >= 0)
        {
        	tmpStr = tmpStr.substring(indexStart + 1);
        	indexEnd = tmpStr.indexOf(strGadgetMark);
        	String tmpStr2;
    		
        	
        	if(indexEnd < 0)
        		tmpStr2 = tmpStr;
        	else
        		tmpStr2 = tmpStr.substring(0, indexEnd);
        	
    		//Get title
    		title = parseUrl(tmpStr2, "\"title\":\"", true, "\"");
    		
    		//Get description
    		description = parseUrl(tmpStr2, "\"description\":\"", true, "\"");
    		
    		//Get url
    		url = _strDomain + "/eXoGadgetServer/gadgets/ifr?container=default&mid=0&nocache=0";
    		
    		//Get country
    		url += parseUrl(tmpStr2, "&country=", false, "&");
    		
    		//Get view
    		url += parseUrl(tmpStr2, "&view=", false, "&");
    		
    		//Get language
    		url += parseUrl(tmpStr2, "&lang=", false, "&");
    		
    		url += "&parent=" + _strDomain + "&st=";
    		
    		
    		//Get token
    		url += parseUrl(tmpStr2, "default:", false, "\"");
    		
    		
    		//Get xml url
    		url += parseUrl(tmpStr2, "&url=", false, "\"");
    		
    		
    		//Get bitmap
    		String bmpUrl = parseUrl(tmpStr2, "\"thumbnail\":\"", true, "\"");
    		bmpUrl = bmpUrl.replace("localhost", _strDomain);
    		bmp = BitmapFactory.decodeStream(AppController._eXoConnection.sendRequest(bmpUrl));
    		
    		eXoGadget tempGadget = new eXoGadget(title, description, url, bmp);
    		arrGadgets.add(tempGadget);
    		
    		indexStart = indexEnd;
    		
        }
        
        return arrGadgets;
	}
	
    public String getStringForGadget(String gadgetStr, String startStr, String endStr)
    {
    	String returnValue = "";
    	int index1;
    	int index2;
    	
    	index1 = gadgetStr.indexOf(startStr);
    	
    	if(index1 > 0)
    	{
    		String tmpStr = gadgetStr.substring(index1 + startStr.length());
    		index2 = tmpStr.indexOf(endStr);
    		if(index2 > 0)
    			returnValue = tmpStr.substring(0, index2);
    	}
    		
    	return returnValue;
    }

    public List<eXoGadget> listOfGadgetsWithURL(String url)
    {
    	List<eXoGadget> arrTmpGadgets = new ArrayList<eXoGadget>();
    	
    	String strGadgetName;
    	String strGadgetDescription;
    	Bitmap imgGadgetIcon = null;
    	
    	String domain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN, "exo_prf_domain");
    	String userName = AppController.sharedPreference.getString(AppController.EXO_PRF_USERNAME, "exo_prf_domain");
    	String password = AppController.sharedPreference.getString(AppController.EXO_PRF_PASSWORD, "exo_prf_domain");
    	
    	
    	String strContent = "";
    	
    	int indexOfSocial = domain.indexOf("social");
    	if (indexOfSocial > 0) 
    	{
    		//dataReply = [[_delegate getConnection] sendRequestToSocialToGetGadget:[url absoluteString]];
    	}
    	else
    	{
    		
    		strContent = AppController._eXoConnection.sendRequestToGetGadget(url, userName, password);
    		//dataReply = [[_delegate getConnection] sendRequestWithAuthorization:[url absoluteString]];
    		//dataReply = [[_delegate getConnection] sendRequestToGetGadget:[url absoluteString]];
    	}
    	
    	int index1;
    	int index2;
    	
    	index1 = strContent.indexOf("eXo.gadget.UIGadget.createGadget");
    	
    	do {
    		if(index1 < 0)
        		return null;
    		strContent = strContent.substring(index1 + 32);
    		index2 = strContent.indexOf("'/eXoGadgetServer/gadgets',");
    		if(index2 < 0)
    			return null;
    		String tmpStr = strContent.substring(0, index2 + 45);
    		
    		strGadgetName = getStringForGadget(tmpStr, "\"title\":\"", "\","); 
    		strGadgetDescription = getStringForGadget(tmpStr, "\"description\":\"", "\",");
    		String gadgetIconUrl = getStringForGadget(tmpStr, "\"thumbnail\":\"", "\",");

    		gadgetIconUrl = gadgetIconUrl.replace("http://localhost:8080", domain);
    		
//    		if(gadgetIconUrl == "")
//    		{
//    			try {
//    				imgGadgetIcon = BitmapFactory.decodeStream(getAssets().open("portletsicon.png"));
//				} catch (Exception e) {
//					// TODO: handle exception
//					imgGadgetIcon = null;
//				}
//    		}
//    		else
//    			imgGadgetIcon = BitmapFactory.decodeStream(AppController._eXoConnection.sendRequest(gadgetIconUrl));
    		
    		try {
				imgGadgetIcon = BitmapFactory.decodeStream(AppController._eXoConnection.sendRequest(gadgetIconUrl));
				if(imgGadgetIcon == null)
				{
					try {
						imgGadgetIcon = BitmapFactory.decodeStream(getAssets().open("portletsicon.png"));
					} catch (Exception e2) {
						// TODO: handle exception
						imgGadgetIcon = null;
					}
				}
				
			} catch (Exception e) {
				// TODO: handle exception
				try {
					imgGadgetIcon = BitmapFactory.decodeStream(getAssets().open("portletsicon.png"));
				} catch (Exception e2) {
					// TODO: handle exception
					imgGadgetIcon = null;
				}
				
			}
    			
    			
    		String gadgetUrl = domain;
    		
    		gadgetUrl += getStringForGadget(tmpStr, "'home', '", "',") + "/";
    		gadgetUrl += "ifr?container=default&mid=1&nocache=0&lang=" + getStringForGadget(tmpStr, "&lang=", "\",") + "&debug=1&st=default";
    		
    		String token = ":" + getStringForGadget(tmpStr, "\"default:", "\",");
    		token = token.replace(":", "%3A");
    		token = token.replace("/", "%2F");
    		token = token.replace("+", "%2B");
    		
    		
    		gadgetUrl += token + "&url=";
    		
    		String gadgetXmlFile = getStringForGadget(tmpStr, "\"url\":\"", "\",");
    		gadgetXmlFile = gadgetXmlFile.replace(":", "%3A");
    		gadgetXmlFile = gadgetXmlFile.replace("/", "%2F");
    		
    		gadgetUrl += gadgetXmlFile;
    		
    		eXoGadget gadget = new eXoGadget(strGadgetName, strGadgetDescription, gadgetUrl, imgGadgetIcon);
    		
    		
    		arrTmpGadgets.add(gadget);

    		strContent = strContent.substring(index2 + 35);
    		index1 = strContent.indexOf("eXo.gadget.UIGadget.createGadget");

    		
    	} while (index1 > 0);
    		
    		
    	return arrTmpGadgets;
    }

    public List<GateInDbItem>listOfGadgets()
    {    	
    	_strDomain = AppController.sharedPreference.getString(AppController.EXO_PRF_DOMAIN, "exo_prf_domain");
    	List<GateInDbItem> arrTmpGadgets = new ArrayList<GateInDbItem>();

    	String strContent = AppController._eXoConnection.getFirstLoginContent();

    	int index1;
    	int index2;
    	int index3;
    	
    	index1 = strContent.indexOf("DashboardIcon TBIcon");
    	
    	if(index1 < 0)
    		return null;
    	
    	strContent = strContent.substring(index1 + 20);
    	index1 = strContent.indexOf("TBIcon");
    	
    	if(index1 < 0)
    		return null;
    	
    	strContent = strContent.substring(0, index1);
    	
    	
    	do {
    		index1 = strContent.indexOf("ItemIcon DefaultPageIcon\" href=\"");
    		index2 = strContent.indexOf("\" >");
    		if(index1 < 0 && index2 < 0)
    			return null;
    		String gadgetTabUrlStr = strContent.substring(index1 + 32, index2);
    		
    		strContent = strContent.substring(index2 + 3);
    		index3 = strContent.indexOf("</a>");
    		if(index3 < 0)
    			return null;
    		String gadgetTabName = strContent.substring(0, index3); 
    		List<eXoGadget> arrTmpGadgetsInItem = listOfGadgetsWithURL(_strDomain + gadgetTabUrlStr);
    		GateInDbItem tmpGateInDbItem = new GateInDbItem(gadgetTabName, gadgetTabUrlStr, arrTmpGadgetsInItem);
    		arrTmpGadgets.add(tmpGateInDbItem);
    		
    		strContent = strContent.substring(index3);
    		index1 = strContent.indexOf("ItemIcon DefaultPageIcon\" href=\"");
    		
    	} while (index1 > 0);
    	
    	return arrTmpGadgets;
    }

	private String parseUrl(String urlStr, String neededStr, boolean offset, String enddedStr)
	{
		String str;
		int idx = urlStr.indexOf(neededStr);
		String tmp = urlStr.substring(idx + neededStr.length());
		idx = tmp.indexOf(enddedStr);
		if(!offset)
			str = neededStr + tmp.substring(0, idx);
		else 
			str = tmp.substring(0, idx);
		
		return str;
	}
   
	public void showUserGuide()
	{
		eXoApplicationsController.webViewMode = 2;
		Intent next = new Intent(thisClass, eXoWebViewController.class);
    	thisClass.startActivity(next);
	}
	
	private void createAuthorization(String url, int port)
    {
    	auth = new AuthScope(url, port);
		String userName = sharedPreference.getString(EXO_PRF_USERNAME, "");
		String password = sharedPreference.getString(EXO_PRF_PASSWORD, "");
		credential = new UsernamePasswordCredentials(userName, password);
    }
    
    public void changeLanguage(ResourceBundle resourceBundle)
    {
    	
    	String strSignIn = "";
    	String strDomain = "";
    	String strUserName = "";
    	String strPassword  = "";
    	String strTitleBar = "";
    	
    	try {
    		strTitleBar = new String(resourceBundle.getString("SignInInformation").getBytes("ISO-8859-1"), "UTF-8"); 
    		strSignIn = new String(resourceBundle.getString("SignInButton").getBytes("ISO-8859-1"), "UTF-8");
    		strDomain = new String(resourceBundle.getString("DomainCellTitle").getBytes("ISO-8859-1"), "UTF-8");
    		strUserName = new String(resourceBundle.getString("UserNameCellTitle").getBytes("ISO-8859-1"), "UTF-8");
        	strPassword = new String(resourceBundle.getString("PasswordCellTitle").getBytes("ISO-8859-1"), "UTF-8");
        	strWait = new String(resourceBundle.getString("PleaseWait").getBytes("ISO-8859-1"), "UTF-8");
        	strSigning = new String(resourceBundle.getString("SigningIn").getBytes("ISO-8859-1"), "UTF-8");
        	strNetworkConnextionFailed = new String(resourceBundle.getString("NetworkConnectionFailed").getBytes("ISO-8859-1"), "UTF-8");
        	strUserNamePasswordFailed = new String(resourceBundle.getString("UserNamePasswordFailed").getBytes("ISO-8859-1"), "UTF-8");
        	strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage").getBytes("ISO-8859-1"), "UTF-8");
        	strLoadingDataFromServer = new String(resourceBundle.getString("LoadingDataFromServer").getBytes("ISO-8859-1"), "UTF-8");
        	
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
		_btnSignIn.setText(strSignIn);
		_txtViewDomain.setText(strDomain);
		_txtViewUserName.setText(strUserName);
		_txtvTitleBar.setText(strTitleBar);
	
		_txtViewPassword.setText(strPassword);
			
    }
    
   
}