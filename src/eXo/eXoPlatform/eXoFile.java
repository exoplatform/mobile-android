package eXo.eXoPlatform;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.client.ClientProtocolException;

import eXo.eXoPlatform.AppController;

public class eXoFile {

	public String		fatherUrl;
	public String		fileName;
	public String		contentType;
	public boolean 		isFolder;
	
	
	public eXoFile(String currentDirectory)
	{
		HttpURLConnection con = null;
		
		try 
		{
			String strUserName = AppController.sharedPreference.getString(AppController.EXO_PRF_USERNAME, "exo_prf_username");
			String strPassword = AppController.sharedPreference.getString(AppController.EXO_PRF_PASSWORD, "exo_prf_password");
			
			currentDirectory = currentDirectory.replace(" ", "%20");
			int index = currentDirectory.lastIndexOf("/");
			fatherUrl = currentDirectory.substring(0, index);
			fileName = currentDirectory.substring(index + 1, currentDirectory.length());
			
			URL url = new URL(currentDirectory);
			con = (HttpURLConnection) url.openConnection();
			
		    
		    con.setRequestMethod("GET");		     
		    con.setRequestProperty( "Authorization", AppController._eXoConnection.stringOfAuthorizationHeaderWithUserNameAndPassword(strUserName, strPassword));
		    
		    contentType = con.getContentType();
//		    int contentLength = con.getContentLength();
		     
		    if(contentType.indexOf("text/html") >= 0)
		    {
		    	if(con.getContentEncoding() == null)
		    	{
		    		isFolder = true;
		    	}
		    	else
		    	{
		    		isFolder = false;
		    	}
		    	
		    }
		    else
		    {
		    	isFolder = false;
		    }

		} 
		catch (ClientProtocolException e) 
		{
			 e.getMessage();	  
		} 
		catch (IOException e) 
		{	
//			String str = e.toString();
//			String msg = e.getMessage();
//			Log.v(str, msg);
		}
		
		con.disconnect();
		
	}
}
