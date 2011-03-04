package eXo.eXoPlatform;

import java.util.ResourceBundle;

import android.app.Dialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class eXoFileActionDialog extends Dialog implements OnClickListener {
	
	public class FileActionDescription extends Object
	{
		
		public String actionName;
		public int imageID;
		public FileActionDescription(String name, int image)
		{
			actionName = name;
			imageID = image;
		}
		
		public String getActionName()
		{
			return actionName;
		}
		
		public int getImageID()
		{
			return imageID;
		}
		
	}
	
	static Thread thread;
	Button _btnClose;
	ListView _lvFileAction;
	TextView _txtvFileName;
	Context thisClass;
	private eXoFile myFile;
	//copyMoveFileMode: 1-copy, 2-move;
	public static short copyMoveFileMode = 0;
	public static eXoFile copyMoveFile = null;
	
	String strClose = "";
	String strTakePicture = "";
	String strCopy = "";
	String strMove = "";
	String strDelete = "";
	String strRename = "";
	String strPaste = "";
	 
	FileActionDescription[] fileActionList = null;
	String strCannotBackToPreviousPage;

	public eXoFileActionDialog(Context context, eXoFile file) {
		super(context);
	
		thisClass = context;

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.exofileaction);
        
        myFile = file;
		
        _btnClose = (Button) findViewById(R.id.Button_Close);
        _btnClose.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				dismiss();
			}
		});
        
        _txtvFileName = (TextView) findViewById(R.id.TextView_FileName);
        _txtvFileName.setText(myFile.fileName.replace("%20", " "));
        
        changeLanguage(AppController.bundle);
        
        _lvFileAction = (ListView) findViewById(R.id.ListView0_FileAction);
        
        BaseAdapter test = new BaseAdapter() {
			
        	public View getView(int position, View convertView, ViewGroup parent) 
    	    {
    	    	LayoutInflater inflater = getLayoutInflater();
    	    	View rowView = inflater.inflate(R.layout.fileactionitem, parent, false);
    	    	final int pos = position;
    	    	rowView.setOnClickListener(new View.OnClickListener() {
					
					public void onClick(View v) {
						// TODO Auto-generated method stub
						
						eXoFilesController.showProgressDialog(false);
						
						Runnable loadingDataRunnable = new Runnable()
				         {
				         public void run()
				         {
				        	 if(pos == 0)//Take picture
								{
									eXoFilesController.takePicture();
								}
								else if(pos == 1)//Copy file
								{
									copyMoveFileMode = 1;
									copyMoveFile = myFile;
								}
								else if(pos == 2)//move file
								{
									copyMoveFileMode = 2;
									copyMoveFile = myFile;
								}
								else if(pos == 3)//Delete file, folder
								{
						    		eXoFilesController.deleteMethod(AppController.auth, AppController.credential, myFile.urlStr);
						    		//Files List
						    		int index = myFile.urlStr.lastIndexOf("/");
						    		myFile.urlStr = myFile.urlStr.substring(0, index);
						    		eXoFilesController.arrFiles = eXoFilesController.getPersonalDriveContent(myFile.urlStr);
						    		eXoFilesController.thisClass.runOnUiThread(reloadFileAdapter);
						   			
								}
								else if(pos == 4)//Paste copy, move file 
								{
									if(copyMoveFileMode == 1 || copyMoveFileMode == 2)
									{
										//Copy file
										if(copyMoveFileMode == 1)
										{
											int index = copyMoveFile.urlStr.lastIndexOf("/");
											String tmpUrl = copyMoveFile.urlStr;
											String lastPathComponent = copyMoveFile.urlStr.substring(index, copyMoveFile.urlStr.length());
											eXoFilesController.copyMethod(AppController.auth, AppController.credential, copyMoveFile.urlStr, myFile.urlStr.concat(lastPathComponent));
											copyMoveFile.urlStr = tmpUrl;
											
											copyMoveFileMode = 0;
										}
										else if(copyMoveFileMode == 2)
										{
											if(!copyMoveFile.urlStr.equalsIgnoreCase(myFile.urlStr))
											{
												int index = copyMoveFile.urlStr.lastIndexOf("/");
												String tmpUrl = copyMoveFile.urlStr;
												String lastPathComponent = copyMoveFile.urlStr.substring(index, copyMoveFile.urlStr.length());
												eXoFilesController.moveMethod(AppController.auth, AppController.credential, copyMoveFile.urlStr, myFile.urlStr.concat(lastPathComponent));
												copyMoveFile.urlStr = tmpUrl;
												
												copyMoveFileMode = 0;
											}
										}
									}
								
								}
								else//Rename file 
								{
									
								}
				        	 
				        	 dismiss();
				        	 eXoFilesController.thisClass.runOnUiThread(dismissProgressDialog);
				        	 
				        	 }
				         };
			            
			            thread =  new Thread(loadingDataRunnable, "fileActionProcessing"); 
			            thread.start();
			            	
					}
					
				});
    	    	
    	    	bindView(rowView, fileActionList[position]);
    	    	return(rowView);
    	    }
    	    
    	    private void bindView(View view, FileActionDescription fileAction) 
    	    {
    	    	TextView label = (TextView)view.findViewById(R.id.label);
    	    	label.setText(fileAction.actionName.replace("%20", " "));
    	    	ImageView icon = (ImageView)view.findViewById(R.id.icon);
    	    	icon.setImageResource(fileAction.imageID);
    	    	
    	    	 if(myFile.isFolder)
    	         {
    	    		 if(fileAction.actionName.equalsIgnoreCase(strCopy) || fileAction.actionName.equalsIgnoreCase(strMove) ||
    	    				 (fileAction.actionName.equalsIgnoreCase(strPaste) && copyMoveFileMode == 0))
    	    		 {
    	    			 //view.setBackgroundResource(R.drawable.disable);
    	    			 label.setTextColor(android.graphics.Color.DKGRAY);
    	    			 view.setEnabled(false);
    	    		 }
    	         }
    	         else
    	         {
    	        	 if(fileAction.actionName.equalsIgnoreCase(strTakePicture) || fileAction.actionName.equalsIgnoreCase(strPaste))
    	        	 {
    	        		 //view.setBackgroundResource(R.drawable.disable);
    	        		 label.setTextColor(android.graphics.Color.DKGRAY);
    	    			 view.setEnabled(false);
    	        	 }
    	        		 
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
				return 5;
			}
		};
        
		_lvFileAction.setAdapter(test);
        
	}
	
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
	      //Save data to the server once the user hits the back button
	      if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	          Toast.makeText(thisClass, strCannotBackToPreviousPage ,Toast.LENGTH_LONG).show();
	      }
	      return false;
	  }
	 
	 static Runnable reloadFileAdapter = new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				eXoFilesController.createExoFilesAdapter();
				
			}
		};
		
		
		static Runnable dismissProgressDialog = new Runnable() {
			
			public void run() {
				// TODO Auto-generated method stub
				eXoFilesController._progressDialog.dismiss();
				thread.stop();
			}
		};
		
//	 protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//	        if (requestCode== 0 && resultCode == Activity.RESULT_OK)
//	        {
//	        	eXoFilesController.setVieweXoImage(true);
//	        	eXoFilesController._uri = data.getData();
//	        	Bitmap bmp = (Bitmap) data.getExtras().get("data");
//	        	eXoFilesController.imgView.setImageBitmap(bmp);	    		
//	        }
//		}
	 
	 public  void changeLanguage(ResourceBundle resourceBundle)
	 {

		 try {
			 strClose = new String(resourceBundle.getString("CloseButton").getBytes("ISO-8859-1"), "UTF-8");
			 strTakePicture = new String(resourceBundle.getString("TakePicture").getBytes("ISO-8859-1"), "UTF-8");
			 strCopy = new String(resourceBundle.getString("Copy").getBytes("ISO-8859-1"), "UTF-8");
			 strMove = new String(resourceBundle.getString("Move").getBytes("ISO-8859-1"), "UTF-8");
			 strDelete = new String(resourceBundle.getString("Delete").getBytes("ISO-8859-1"), "UTF-8");
			 strRename = new String(resourceBundle.getString("Rename").getBytes("ISO-8859-1"), "UTF-8");
			 strPaste = new String(resourceBundle.getString("Paste").getBytes("ISO-8859-1"), "UTF-8");
			 strCannotBackToPreviousPage = new String(resourceBundle.getString("CannotBackToPreviousPage").getBytes("ISO-8859-1"), "UTF-8");
		 } catch (Exception e) {
			 // TODO: handle exception
		 }
		 
		 _btnClose.setText(strClose);
		 
		 fileActionList = new FileActionDescription[] {new FileActionDescription(strTakePicture, R.drawable.takephoto)
		 , new FileActionDescription(strCopy, R.drawable.copy)
		 , new FileActionDescription(strMove, R.drawable.move)
		 , new FileActionDescription(strDelete, R.drawable.delete)
		 , new FileActionDescription(strPaste, R.drawable.paste)};
		 
	       
	 }

	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}

}
