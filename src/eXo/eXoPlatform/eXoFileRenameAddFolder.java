package eXo.eXoPlatform;

import java.util.ResourceBundle;

import eXo.eXoPlatform.AppController;

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
public class eXoFileRenameAddFolder extends Dialog implements OnClickListener {
	
	eXoFile myFile;
	Button btnOK;
	Button btnCancel;
	EditText edtNewFileFolderName;
	TextView txtTittle; 
	boolean isRenameFile;
	
	public eXoFileRenameAddFolder(Context context, eXoFile file, boolean renameFile) {
		super(context);
		
		myFile = file;
		isRenameFile = renameFile;
		
		/** 'Window.FEATURE_NO_TITLE' - Used to hide the title */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		/** Design the dialog in main.xml file */
		setContentView(R.layout.exorenameaddfolder);
		
		btnOK = (Button) findViewById(R.id.Button_OK);
		btnOK.setOnClickListener(this);
		
		btnCancel = (Button) findViewById(R.id.Button_Cancel);
		btnCancel.setOnClickListener(this);
		
		edtNewFileFolderName = (EditText) findViewById(R.id.EditText_RenameAddFolder);
		
		txtTittle = (TextView) findViewById(R.id.TextView_RenameAddFolder);
	     
	    changeLanguage(AppController.bundle);
	     
	}

	
	public void onClick(View v) {
		/** When OK Button is clicked, dismiss the dialog */
		if (v == btnOK)
		{
			String newFileNameStr = edtNewFileFolderName.getText().toString();
			if(newFileNameStr == null || newFileNameStr.equalsIgnoreCase(""))
			{
				return;
			}
    		
    		if(isRenameFile)
    		{
    			String url = myFile.fatherUrl + "/" + newFileNameStr;
        		url = url.replace(" ", "%20");
        		url = url.replace("+", "%2B");
        		
    			eXoFilesController.MoveFileFromServerToOtherServer(AppController.auth, AppController.credential, myFile, url);
    			
    			eXoFilesController.arrFiles = eXoFilesController.getPersonalDriveContent();
       			eXoFilesController.createExoFilesAdapter();
    		}
    		else
    		{
    			String url = myFile.fatherUrl + "/" + myFile.fileName;
        		url = url.replace(" ", "%20");
        		url = url.replace("+", "%2B");
        		
    			myFile.fatherUrl = url;
    			myFile.fileName = newFileNameStr;
    			eXoFilesController.addNewFolder(AppController.auth, AppController.credential, myFile, newFileNameStr);
    		}
			
   			
		}
		else if(v == btnCancel)
		{
			
		}
		
		dismiss();
	}
	   
	public  void changeLanguage(ResourceBundle resourceBundle)
	   {
		   
		   	String strTittle = "";
	   		String strOK = "";
	   		String strCancel = "";
	   		
	   		try {
	   			strTittle = new String(resourceBundle.getString("Language").getBytes("ISO-8859-1"), "UTF-8");
	   			strOK = new String(resourceBundle.getString("OK").getBytes("ISO-8859-1"), "UTF-8");
	   			strCancel = new String(resourceBundle.getString("Cancel").getBytes("ISO-8859-1"), "UTF-8");
	       		
			} catch (Exception e) {
				// TODO: handle exception
			}
	   	
			txtTittle.setText(strTittle);
			btnOK.setText(strOK);
			btnCancel.setText(strCancel);
		   
	   }

}
