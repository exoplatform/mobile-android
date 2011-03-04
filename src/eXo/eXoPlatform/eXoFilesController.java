package eXo.eXoPlatform;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.apache.commons.lang.StringEscapeUtils;

public class eXoFilesController extends Activity 
{
	
	static final String FILE_CONTENT_TYPE = "image/bmp image/cgm image/gif image/jpeg image/png image/tiff image/x-icon " + 
												"video/mpeg video/quicktime video/x-msvideo " + 
												"audio/midi audio/mpeg audio/x-aiff audio/x-mpegurl " + 
												"audio/x-pn-realaudio audio/x-wav " +
												"application/msword application/pdf application/vnd.ms-excel application/vnd.ms-powerpoint application/zip"; 
	static ListView _lstvFiles;
	static TextView _textViewFolder;
	static Button _btnCloseBack;
	TextView _textViewEmptyPage;

	Button _btnLanguageHelp;
	
//	for eXo image View
	EditText txtFileName;
	ImageView imgView;
	ImageView imgViewEmptyPage;
	Button _btnUploadImage;
	Button _btnCancelUploadImage;
	static ProgressDialog _progressDialog;
	
	public static String _rootUrl;
		
	public static String localFilePath = "/sdcard/eXo/";

	public static Uri	_uri;
	boolean				_deleteFile;
	boolean				_copyFile;
	boolean				_moveFile;
	
	static eXoFilesController thisClass;
	static eXoApplicationsController _delegate;
	static public eXoFile myFile;
	static public int positionOfFileItem = 0;
	public static List<eXoFile> arrFiles;
	
	String strCannotBackToPreviousPage;
	static String strDownloadFileIntoSDCard;
	
	static Thread thread;
	
	@Override
    public void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
        thisClass = this;
//        requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.exofilesview);
		
    	_btnUploadImage = (Button) findViewById(R.id.ButtonUpImage);
    	_btnUploadImage.setOnClickListener( new OnClickListener(){
            public void onClick(View v ){
            	
            Runnable loadingDataRunnable = new Runnable()
   	         {
   	         public void run()
   	         {
   	        	 		
   	        	String fileName = txtFileName.getText().toString(); 
            	if(fileName == null || fileName.equalsIgnoreCase(""))
            	{
            		fileName = txtFileName.getHint().toString();
            	}
            	
            	String encodedePath = _uri.getEncodedPath();
            	
            	saveFileToLocal(AppController.auth, AppController.credential, encodedePath, localFilePath, fileName, true);
            	putFileToServerFromLocal(AppController.auth, AppController.credential, myFile.urlStr + "/" + fileName.replace(" ", "%20"), localFilePath, fileName, "image/jpeg");
            	
            	runOnUiThread(reloadFileAdapter);
   	        	runOnUiThread(dismissProgressDialog);
   	         }
   	         };
              
   	        showProgressDialog(true);
              
              thread =  new Thread(null, loadingDataRunnable, "CloseBackFileItem"); 
              thread.start();
            	
            }

        });
        
    	_btnCancelUploadImage = (Button) findViewById(R.id.ButtonCancel);
    	_btnCancelUploadImage.setOnClickListener( new OnClickListener(){
            public void onClick(View v ){
            	
            	setVieweXoImage(false);
            	//eXoFilesController.this.finish();
            }

         });
    	
        imgView = (ImageView) findViewById(R.id.ImageView);
        imgViewEmptyPage = (ImageView) findViewById(R.id.ImageViewEmptyPage);
        imgViewEmptyPage.setVisibility(View.INVISIBLE);
//        DisplayMetrics dm = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(dm);
        _textViewEmptyPage = (TextView)findViewById(R.id.TextView_EmptyPage);
        _textViewEmptyPage.setVisibility(View.INVISIBLE);
        
        _btnCloseBack = (Button) findViewById(R.id.Button_Close);
        _btnCloseBack.setOnClickListener(new OnClickListener() {
        public void onClick(View v) {
			// TODO Auto-generated method stub
        	
        	Runnable loadingDataRunnable = new Runnable()
	         {
	         public void run()
	         {
	        	 
	        	 if(myFile.urlStr.equalsIgnoreCase(_rootUrl))
	 			{
	 				eXoFilesController.this.finish();
	 			}
	 			else
	 			{
	 				int index = myFile.urlStr.lastIndexOf("/");
	 				myFile.urlStr = myFile.urlStr.substring(0, index);
	 				arrFiles = getPersonalDriveContent(myFile.urlStr);
	 				
	 				runOnUiThread(closeBackRunnable);
	 			}
	 				
	        	 runOnUiThread(dismissProgressDialog);
	        	 }
	         };
           
	        showProgressDialog(true);
           
           thread =  new Thread(null, loadingDataRunnable, "CloseBackFileItem"); 
           thread.start();
           
		}
        });
        
        _btnLanguageHelp = (Button) findViewById(R.id.Button_Language_Help);
        _btnLanguageHelp.setOnClickListener(new View.OnClickListener() {	
        	public void onClick(View v) 
			{
        		eXoLanguageSettingDialog customizeDialog = new eXoLanguageSettingDialog(eXoFilesController.this, 2, thisClass);
        		customizeDialog.show();
			}	
		});
        
        
        _lstvFiles = (ListView) findViewById(R.id.ListView_Files);
        
        _textViewFolder = (TextView) findViewById(R.id.TextView_Directoties);
        _textViewFolder.setText(getFolderNameFromUrl(_rootUrl));
        
        txtFileName = (EditText) findViewById(R.id.EditTextImageName);
        
        setVieweXoImage(false);
        
        changeLanguage(AppController.bundle);
		
        createExoFilesAdapter();

        
    } 

	public boolean onKeyDown(int keyCode, KeyEvent event) {
	      //Save data to the server once the user hits the back button
	      if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	          Toast.makeText(eXoFilesController.this, strCannotBackToPreviousPage ,Toast.LENGTH_LONG).show();
	      }
	      return false;
	  }
	
	static Runnable cannotAceesSDCard = new Runnable() {
		
		public void run() {
			// TODO Auto-generated method stub
			Toast toast = Toast.makeText(thisClass, "SDCard is not available", Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
		}
	};
	
	Runnable reloadFileAdapter = new Runnable() {
		
		public void run() {
			// TODO Auto-generated method stub
			//Files List   
        	createExoFilesAdapter();
        	setVieweXoImage(false);
			
		}
	};
	
	public static Runnable dismissProgressDialog = new Runnable() {
		
		public void run() {
			// TODO Auto-generated method stub
			
			if(arrFiles.isEmpty())
			{
				 thisClass.imgViewEmptyPage.setVisibility(View.VISIBLE);
				 thisClass._textViewEmptyPage.setVisibility(View.VISIBLE);
			}
			else
			{
				thisClass.imgViewEmptyPage.setVisibility(View.INVISIBLE);
				thisClass._textViewEmptyPage.setVisibility(View.INVISIBLE);
			}
				
			_progressDialog.dismiss();
			thread.stop();
			
		}
	};
	
	public Runnable closeBackRunnable = new Runnable() {
		
		public void run() {
			// TODO Auto-generated method stub
	   			_textViewFolder.setText(getFolderNameFromUrl(myFile.urlStr));
	  	    	
		   			//Files List
	   			
	   			createExoFilesAdapter();
	   			try {	   				
		   			if(myFile.urlStr.equalsIgnoreCase(_rootUrl))
			   			_btnCloseBack.setText( new String(AppController.bundle.getString("CloseButton").getBytes("ISO-8859-1"), "UTF-8"));
			   		else
			   			_btnCloseBack.setText( new String(AppController.bundle.getString("BackButton").getBytes("ISO-8859-1"), "UTF-8"));
				} catch (Exception e) {
					// TODO: handle exception
					//_btnCloseBack.setText("");
				}	
		}
	};
	
	public static Runnable fileItemClickRunnable = new Runnable() {
		
		public void run() {
			// TODO Auto-generated method stub
			
			//myFile = arrFiles.get(positionOfFileItem);
			
			try
			  {
				_btnCloseBack.setText(new String(AppController.bundle.getString("BackButton").getBytes("ISO-8859-1"), "UTF-8"));
			  } 
			  catch (Exception e) 
			  {
				// TODO: handle exception
				  try {
					  _btnCloseBack.setText(new String(AppController.bundle.getString("CloseButton").getBytes("ISO-8859-1"), "UTF-8"));
				} catch (Exception e2) {
					// TODO: handle exception
				}
			  }
			  		if(myFile.isFolder)
			    	{
		    			_textViewFolder.setText(myFile.fileName.replace("%20", " "));
			    		createExoFilesAdapter();
			    		
			    	}
			    	else
			    	{
			    		//_strCurrentDirectory = _strCurrentDirectory + "/" + myFile.fileName;
			    		eXoApplicationsController.webViewMode = 1;
			    		
			    		AlertDialog.Builder builder = new AlertDialog.Builder(thisClass); 
//		    	        builder.setMessage("Do you want to download " + myFile.fileName.replace("%20", " ") + " into sdcard?");
			    		builder.setMessage(strDownloadFileIntoSDCard);
		    	        builder.setCancelable(false); 
		    	        
		    	        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() { 
		    	        	public void onClick(DialogInterface dialog, int id) {
		    	        		
		    	        		Runnable loadingDataRunnable = new Runnable()
						         {
						         public void run()
						         {
						        	 saveFileToLocal(AppController.auth, AppController.credential, myFile.urlStr, localFilePath, myFile.fileName.replace("%20", " "), false);
				    	        		
				    	        		int index = myFile.urlStr.lastIndexOf("/");
				    	        		myFile.urlStr = myFile.urlStr.substring(0, index);
							 			
						        	 thisClass.runOnUiThread(dismissProgressDialog);
						        	 }
						         };
					            
						         showProgressDialog(true);
					             
					            thread =  new Thread(loadingDataRunnable, "fileItemClickOnIcon"); 
					            thread.start();
					            dialog.dismiss();
					            
		    	        	} 
		    	        }); 
		    	        
		    	        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() { 
		    	        	public void onClick(DialogInterface dialog, int id) { 
		    	        		
		    	        		int index = myFile.urlStr.lastIndexOf("/");
		    	        		myFile.urlStr = myFile.urlStr.substring(0, index);
		    	        	} 
		    	        });   
		    	        
		    	        AlertDialog alert = builder.create(); 
		    	        alert.show(); 
		   	        
		    	}	
		}
	};
	
	public static void takePicture()
    {
    	Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        thisClass.startActivityForResult(intent, 0);
	        
    }
	   
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0 && resultCode == Activity.RESULT_OK)
        {
        	setVieweXoImage(true);
        	_uri = data.getData();
        	Bitmap bmp = (Bitmap) data.getExtras().get("data");
        	imgView.setImageBitmap(bmp);
        }
        else if(resultCode == Activity.RESULT_CANCELED)
        {
        	return;
        	//finish();
        }
        	
	}

	private void setVieweXoImage(boolean isVieweXoImage)
	{
		int viewImageMode;
		int viewFileMode;
		if(isVieweXoImage)
		{
			viewImageMode = View.VISIBLE;
			viewFileMode = View.INVISIBLE;
		}
		else
		{
			viewImageMode = View.INVISIBLE;
			viewFileMode = View.VISIBLE;
		}
		
		_lstvFiles.setVisibility(viewFileMode);
		_textViewFolder.setVisibility(viewFileMode);
		//_btnCloseBack.setVisibility(viewFileMode);
		
//		for eXo image View
		txtFileName.setVisibility(viewImageMode);
		imgView.setVisibility(viewImageMode);
		_btnUploadImage.setVisibility(viewImageMode);
		_btnCancelUploadImage.setVisibility(viewImageMode);
		_btnCloseBack.setVisibility(viewFileMode);
	}
	
	static private Bitmap fileFolderIcon(eXoFile file)
	{
		String contentType = "unknown.png";
		     
		if(file.contentType.indexOf("image") >= 0)
			contentType = "image.png";
		else if(file.contentType.indexOf("video") >= 0)
			contentType = "video.png";
		else if(file.contentType.indexOf("audio") >= 0)
			contentType = "music.png";
		else if(file.contentType.indexOf("application/msword") >= 0)
			contentType = "word.png";
		else if(file.contentType.indexOf("application/pdf") >= 0)
			contentType = "pdf.png";
		else if(file.contentType.indexOf("application/vnd.ms-excel") >= 0)
			contentType = "xls.png";
		else if(file.contentType.indexOf("application/vnd.ms-powerpoint") >= 0)
			contentType = "ppt.png";
		else if(file.contentType.indexOf("text") >= 0)
			contentType = "text.png";
		
		Bitmap bmp = null;
		try {
			bmp = BitmapFactory.decodeStream(thisClass.getAssets().open(contentType));
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return bmp;
	}
		
	public static List<eXoFile> getPersonalDriveContent(String url)
	{
		List<eXoFile> arrFilesTmp = new ArrayList <eXoFile>();
		
		String responseStr = AppController._eXoConnection.sendRequestWithAuthorizationReturnString(url.replace(" ", "%20"));
		
		int local1;
		int local2;
		do 
		{
			local1 = responseStr.indexOf("alt=\"\"> ");
		
			if(local1 > 0)
			{
				int local3 = responseStr.indexOf("<a href=\"");
				int local4 = responseStr.indexOf("\"><img src");
				String urlStr = responseStr.substring(local3 + 9, local4);
				
				responseStr = responseStr.substring(local1 + 8);
				local2 =  responseStr.indexOf("</a>");
				String fileName = responseStr.substring(0, local2);
				if(!fileName.equalsIgnoreCase(".."))
				{
					fileName = StringEscapeUtils.unescapeHtml(fileName);
					fileName = StringEscapeUtils.unescapeJava(fileName);
					
					eXoFile file = new eXoFile(urlStr, fileName);
					arrFilesTmp.add(file);
				}
					
				
				if(local2 > 0)
					responseStr = responseStr.substring(local2);
			}
			
		} while (local1 > 0);
		
		if(arrFilesTmp.isEmpty()) 
		{
//			ImageView imgView = new ImageView(thisClass.getApplicationContext());
//			imgView.setImageResource(R.drawable.emptypage);
//			
//			_lstvFiles.addView(imgView, 200, 200);
		}
		
		return arrFilesTmp;
	}

	public static void  showProgressDialog(boolean isloadingData)
	{
		String strLoadingDataFromServer = "";
        try {
        	if(isloadingData)
        		strLoadingDataFromServer = new String(AppController.bundle.getString("LoadingDataFromServer").getBytes("ISO-8859-1"), "UTF-8");
        	else
        		strLoadingDataFromServer = new String(AppController.bundle.getString("FileProcessing").getBytes("ISO-8859-1"), "UTF-8");
       	 
		} catch (Exception e) {
			// TODO: handle exception
			strLoadingDataFromServer = "";
		}
		
       _progressDialog = ProgressDialog.show(thisClass, null, strLoadingDataFromServer);
	}
	
	public static void createExoFilesAdapter()
	{	
   		BaseAdapter test = new BaseAdapter() {
			
			  public View getView(int position, View convertView, ViewGroup parent) 
			    {
			    	LayoutInflater inflater = thisClass.getLayoutInflater();
			    	final View rowView = inflater.inflate(R.layout.fileitem, parent, false);
			    	final int pos = position;
			    	
			    	ImageView icon = (ImageView)rowView.findViewById(R.id.icon);
			    	icon.setOnClickListener(new View.OnClickListener() {
						
						public void onClick(View v) {
							// TODO Auto-generated method stub

							positionOfFileItem = pos;
							myFile = arrFiles.get(positionOfFileItem);
				        	 
				        	 if(!myFile.isFolder)
				        	 {
				        		 thisClass.runOnUiThread(fileItemClickRunnable);
				        	 }
				        	 else
				        	 {
				        		 Runnable loadingDataRunnable = new Runnable()
						         {
						         public void run()
						         {
						        	 
						        	 arrFiles = getPersonalDriveContent(myFile.urlStr);
						        	 
						        	 thisClass.runOnUiThread(fileItemClickRunnable);
						        	    	
						        	 thisClass.runOnUiThread(dismissProgressDialog);
						        	 }
						         };
					            
						         showProgressDialog(true);
					            
					            thread =  new Thread(loadingDataRunnable, "fileItemClickOnIcon"); 
					            thread.start();
				        	 }
							 
							
						}
					});
			    	
			    	TextView lb = (TextView)rowView.findViewById(R.id.label);
			    	lb.setOnClickListener(new View.OnClickListener() {
						
						public void onClick(View v) {
							// TODO Auto-generated method stub

							positionOfFileItem = pos;
							Runnable loadingDataRunnable = new Runnable()
					         {
					         public void run()
					         {
					        	 
					        	 myFile = arrFiles.get(positionOfFileItem);
					        	 if(myFile.isFolder)
					        		 arrFiles = getPersonalDriveContent(myFile.urlStr);
					        	thisClass.runOnUiThread(fileItemClickRunnable);
					        	    	
					        	 thisClass.runOnUiThread(dismissProgressDialog);
					        	 }
					         };
				            
					         
					        showProgressDialog(true);
				            
				            thread =  new Thread(loadingDataRunnable, "fileItemClickOnIcon"); 
				            thread.start();
							
				            
						}
					});
			    	
			    		
			    	Button btn = (Button) rowView.findViewById(R.id.Button_FileAction);
			    	btn.setOnClickListener(new View.OnClickListener() {
						
						public void onClick(View v) {
							// TODO Auto-generated method stub
							positionOfFileItem = pos;
//							myFile = arrFiles.get(positionOfFileItem);
							eXoFileActionDialog fileActionDialog = new eXoFileActionDialog(thisClass, arrFiles.get(positionOfFileItem));
							//fileAction.setTitle("User guide & language setting");
							fileActionDialog.show();
							
						}
					});
			    	bindView(rowView, arrFiles.get(position));
			        return(rowView);
			    }

			    private void bindView(View view, eXoFile file) 
			    {
			    	TextView label = (TextView)view.findViewById(R.id.label);
			    	
			    	label.setText(file.fileName.replace("%20", " "));
			    	
			    	ImageView icon = (ImageView)view.findViewById(R.id.icon);
			    	
			    	if(!file.isFolder)
			    	{
			    	
			    		icon.setImageBitmap(fileFolderIcon(file));
			    		//icon.setImageResource(.fileName));
			    	}
			    	else
			    	{
			    		icon.setImageResource(R.drawable.folder);
			    	}
			    	
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
				int count = arrFiles.size();
		    	return count;
			}
		};
		
		_lstvFiles.setAdapter(test);      
   		//_lstvFiles.setOnItemClickListener(test);
	}
	

	private static boolean saveFileToLocal(AuthScope auth, UsernamePasswordCredentials credential, String url, String path, String file, boolean isTakeImage)
	{
	    boolean returnValue = false;
	    	
	   	try {
	   		
	   		InputStream is;
	   		if(isTakeImage)
	   			is = thisClass.getContentResolver().openInputStream(_uri);
	   		else
	   			is = getInputStreamFromServer(auth, credential, url);
		   	
	   		File f = new File(path);
	   		if(!f.exists())
	   		{
	   			boolean isMade = f.mkdir();
	   			if(!isMade)
	   			{
	   				
	   			}
	   		}
	   		
	   		f = new File(path, file);
	   		if(!f.exists())
	   		{
	   			boolean isMade = f.createNewFile();
	   			if(!isMade)
	   			{
	   				
	   			}
	   		}
	   		
	   		FileOutputStream fos = new FileOutputStream(f);
//	   	 this.openFileOutput(path + file, MODE_PRIVATE);
	   		byte[] buffer = new byte[1024];
	   		
            int len1 = 0;
            while ( (len1 = is.read(buffer)) > 0 ) {
            	fos.write(buffer, 0, len1);
            	
            }
            
//	        f.close();
	            
	        returnValue = true;
	            
		} catch (Exception e) {
				// TODO: handle exception
			String msg = e.getMessage();
			String str = e.toString();
			Log.e(str, msg);
			
			thisClass.runOnUiThread(cannotAceesSDCard);
			
		}
	    	
		return returnValue;
	        
	}
	   
	public static InputStream getInputStreamFromServer(AuthScope auth, UsernamePasswordCredentials credential, String url)
	{
		InputStream is = null;
	    	
	    HttpResponse response = null;
	    DefaultHttpClient client = new DefaultHttpClient();
	    client.getCredentialsProvider().setCredentials(auth, credential);
	    HttpGet get = new HttpGet(url);
	    try {
	    		
	    	response = client.execute(get);
	    	is = response.getEntity().getContent();
	    		
	    } catch (Exception e) {
	    	// TODO: handle exception
	    }
//	    	client.getConnectionManager().shutdown();
	    	
	    return is;
	    	
	}
	    
	public static boolean deleteMethod(AuthScope auth, UsernamePasswordCredentials credential, String url)
	{
		boolean returnValue = false;
	    	
	    DefaultHttpClient client = new DefaultHttpClient();
	    client.getCredentialsProvider().setCredentials(auth, credential);
	    HttpDelete delete = new HttpDelete(url);
	    try {
	    	HttpResponse response = client.execute(delete);
	        int status = response.getStatusLine().getStatusCode();
	        if(status >= 200 && status < 300)
	        {
	        	returnValue = true;
	        }
	    } catch (Exception e) {
				// TODO: handle exception
		}
			
	    return returnValue;
	}
	    
	public static boolean putFileToServerFromLocal(AuthScope auth, UsernamePasswordCredentials credential, String url, String path, String file, String fileType)
	{
		boolean returnValue = false;
		
	    DefaultHttpClient client = new DefaultHttpClient();
	    client.getCredentialsProvider().setCredentials(auth, credential);
//	    try {
//	    	url = URLEncoder.encode(url, "UTF-8");	
//		} catch (Exception e) {
//			// TODO: handle exception
//		}
	    
	    HttpPut post = new HttpPut(url);
	        
	    File fileManager = new File(path + file);
	    FileEntity entity = new FileEntity(fileManager, fileType);
//	    binary/octet-stream
	        
	    post.setEntity(entity);
	        
	    try {
	    	HttpResponse response = client.execute(post);
	        int status = response.getStatusLine().getStatusCode();
	        if(status >= 200 && status < 300)
	        {
	        	returnValue = true;
	        }
	        	
	    } catch (Exception e) {
	    	// TODO: handle exception
			String msg = e.getMessage();
			String str = e.toString();
			Log.d(msg, str);
	    }
			
	    return returnValue;
	}
	
	public static boolean copyMethod(AuthScope auth, UsernamePasswordCredentials credential, String source, String destination)
	{
		boolean returnValue = false;
		
	    DefaultHttpClient client = new DefaultHttpClient();
	    client.getCredentialsProvider().setCredentials(auth, credential);
	   
	    HttpPut put = new HttpPut(destination);
		
		put.setHeader("Destination", source);
		put.setHeader("Overwrite", "T");

		try {
			
	    	HttpResponse response = client.execute(put);
	        int status = response.getStatusLine().getStatusCode();
	        if(status >= 200 && status < 300)
	        {
	        	returnValue = true;
	        }
	        	
	    } catch (Exception e) {
	    	// TODO: handle exception
			String msg = e.getMessage();
			String str = e.toString();
			Log.d(msg, str);
	    }
			
	    return returnValue;
	}

	public static boolean moveMethod(AuthScope auth, UsernamePasswordCredentials credential, String source, String destination)
	{
		boolean returnValue = false;
		returnValue = eXoFilesController.copyMethod(auth, credential, source, destination);
		if(returnValue)
			returnValue = eXoFilesController.deleteMethod(auth, credential, source);
		
		return returnValue;
	}
	
    private String getFolderNameFromUrl(String url)
    {
    	String folder = "";
    	int lastSlashIndex = url.lastIndexOf("/");
    	
    	folder = url.substring(lastSlashIndex + 1);
    	
    	return folder;
    }
   

    public String encodeUrl(String urlString)
    {
        
    	urlString.replace("&", "%26");
    	urlString.replace("+", "%2B");
    	urlString.replace(",", "%2C");
    	//urlString.replace("/", "%2F");
    	//urlString.replace(":", "%3A");
    	urlString.replace(";", "%3B");
    	urlString.replace("=", "%3D");
    	urlString.replace("?", "%3F");
    	urlString.replace("@", "%40");
    	urlString.replace(" ", "%20");
    	urlString.replace("\t", "%09");
    	urlString.replace("#", "%23");
    	urlString.replace("<", "%3C");
    	urlString.replace(">", "%3E");
    	urlString.replace("\"", "%22");
    	urlString.replace("\n", "%0A");
    	
    	
        return urlString;
    }
    
    public void changeLanguage(ResourceBundle resourceBundle)
    {
    	String strCloseBack = "";
    	String strUploadFile = "";
    	String strCancel = "";
    	String strEmptyPage = "";
    	try {
    		strCloseBack = new String(resourceBundle.getString("CloseButton").getBytes("ISO-8859-1"), "UTF-8");
    		strUploadFile = new String(resourceBundle.getString("Upload").getBytes("ISO-8859-1"), "UTF-8");
    		strCancel = new String(resourceBundle.getString("Cancel").getBytes("ISO-8859-1"), "UTF-8");
    		strEmptyPage = new String(resourceBundle.getString("EmptyPage").getBytes("ISO-8859-1"), "UTF-8");
        	strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage").getBytes("ISO-8859-1"), "UTF-8");
        	strDownloadFileIntoSDCard = new String(resourceBundle.getString("DownloadFileInToSDCard").getBytes("ISO-8859-1"), "UTF-8");
		} catch (Exception e) {
			// TODO: handle exception
		}
    	
		_btnCloseBack.setText(strCloseBack);
		_btnUploadImage.setText(strUploadFile);
		_btnCancelUploadImage.setText(strCancel);
		_textViewEmptyPage.setText(strEmptyPage);
    	
    	_delegate.changeLanguage(resourceBundle);
    	_delegate.createAdapter();
		
    }

}
