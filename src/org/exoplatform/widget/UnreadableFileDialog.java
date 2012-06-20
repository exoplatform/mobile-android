package org.exoplatform.widget;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.exoplatform.R;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.PhotoUtils;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class UnreadableFileDialog extends Dialog implements android.view.View.OnClickListener {

  private Button           okButton;

  private Button           cancelButton;

  private Context          mContext;

  private String           fileType;

  private String           filePath;

  private String           fileName;

  private WaitingDialog    _progressDialog;

  private FileDownloadTask mLoadTask;

  private String           downLoadingData;

  private String           noAppFound;

  private String           fileNotSupport;

  private String           cannotOpenFile;

  private String           fileNotFound;

  private Resources        resource;

  public UnreadableFileDialog(Context context, String fType, String fPath, String fName) {
    super(context);
    mContext = context;
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.unreadable_file_dialog_layout);
    changeLanguage();
    TextView titleView = (TextView) findViewById(R.id.dialog_title_view);
    titleView.setText(context.getResources().getString(R.string.UnreachableFileTitle));
    TextView contentView = (TextView) findViewById(R.id.warning_content);
    contentView.setText(context.getResources().getString(R.string.UnreadableFileSuggest));
    okButton = (Button) findViewById(R.id.warning_ok_button);
    okButton.setOnClickListener(this);
    cancelButton = (Button) findViewById(R.id.warning_cancel_button);
    cancelButton.setOnClickListener(this);
    fileType = fType;
    filePath = fPath;
    fileName = fName;
  }

  public void onClick(View view) {
    if (view.equals(cancelButton)) {
      dismiss();
    }
    if (view.equals(okButton)) {
      if (ExoDocumentUtils.isFileReadable(fileType)) {
        onLoad(filePath);
      } else {
        Toast.makeText(mContext, fileNotSupport, Toast.LENGTH_SHORT).show();
      }

      dismiss();
    }
  }

  private void onLoad(String path) {
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mLoadTask == null || mLoadTask.getStatus() == FileDownloadTask.Status.FINISHED) {
        mLoadTask = (FileDownloadTask) new FileDownloadTask().execute(path);
      }
    } else {
      new ConnectionErrorDialog(mContext).show();
    }
  }

  private void changeLanguage() {
    resource = mContext.getResources();
    downLoadingData = resource.getString(R.string.DownloadingData);
    noAppFound = resource.getString(R.string.NoAppFound);
    cannotOpenFile = resource.getString(R.string.CannotOpenFile);
    fileNotFound = resource.getString(R.string.FileNotFound);
    fileNotSupport = resource.getString(R.string.FileNotSupported);
  }

  private class FileDownloadTask extends AsyncTask<String, Void, File> {

    @Override
    protected void onPreExecute() {
      _progressDialog = new WaitingDialog(mContext, null, downLoadingData);
      _progressDialog.show();
    }

    @Override
    protected File doInBackground(String... params) {

      String url = params[0].replaceAll(" ", "%20");
      HttpParams httpParameters = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
      HttpConnectionParams.setSoTimeout(httpParameters, 10000);
      HttpConnectionParams.setTcpNoDelay(httpParameters, true);
      DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
      httpClient.setCookieStore(ExoConnectionUtils.cookiesStore);
      try {
        String parentPath = Environment.getExternalStorageDirectory() + "/eXo/";
        File file = new File(parentPath, fileName);
        HttpGet getRequest = new HttpGet(url);
        HttpResponse response = httpClient.execute(getRequest);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
          InputStream is = entity.getContent();
          OutputStream os = new FileOutputStream(file);
          PhotoUtils.copyStream(is, os);
          os.close();
        }
        return file;
      } catch (IOException e) {
        return null;
      } finally {
        httpClient.getConnectionManager().shutdown();
      }

    }

    @Override
    protected void onPostExecute(File result) {
      if (result != null) {
        /*
         * get exactly document type from content type and open it with
         * compatible intent
         */
        String docFileType = null;
        if (fileType.equals(ExoDocumentUtils.PDF_TYPE)) {
          docFileType = ExoDocumentUtils.PDF_TYPE;
        } else if (fileType.equals(ExoDocumentUtils.MSWORD_TYPE)) {
          docFileType = ExoDocumentUtils.MSWORD_TYPE;
        } else if (fileType.equals(ExoDocumentUtils.XLS_TYPE)) {
          docFileType = ExoDocumentUtils.XLS_TYPE;
        } else if (fileType.equals(ExoDocumentUtils.POWERPOINT_TYPE)) {
          docFileType = ExoDocumentUtils.POWERPOINT_TYPE;
        } else if (fileType.equals(ExoDocumentUtils.OPEN_MSWORD_TYPE)) {
          docFileType = ExoDocumentUtils.OPEN_MSWORD_TYPE;
        } else if (fileType.equals(ExoDocumentUtils.OPEN_XLS_TYPE)) {
          docFileType = ExoDocumentUtils.OPEN_XLS_TYPE;
        } else if (fileType.equals(ExoDocumentUtils.OPEN_POWERPOINT_TYPE)) {
          docFileType = ExoDocumentUtils.OPEN_POWERPOINT_TYPE;
        } else if (fileType.startsWith(ExoDocumentUtils.AUDIO_TYPE)) {
          docFileType = ExoDocumentUtils.ALL_AUDIO_TYPE;
        } else if (fileType.startsWith(ExoDocumentUtils.VIDEO_TYPE)) {
          docFileType = ExoDocumentUtils.ALL_VIDEO_TYPE;
        }

        if (docFileType != null) {
          Uri path = Uri.fromFile(result);
          Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
          pdfIntent.setDataAndType(path, docFileType);
          pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          try {
            mContext.startActivity(pdfIntent);
          } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, noAppFound, Toast.LENGTH_SHORT).show();
          }
        } else {
          Toast.makeText(mContext, cannotOpenFile, Toast.LENGTH_SHORT).show();
        }

      } else {
        Toast.makeText(mContext, fileNotFound, Toast.LENGTH_SHORT).show();
      }
      _progressDialog.dismiss();
    }

  }

}
