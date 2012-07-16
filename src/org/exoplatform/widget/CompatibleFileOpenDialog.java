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
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.ExoDocumentUtils;
import org.exoplatform.utils.image.FileCache;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class CompatibleFileOpenDialog extends Dialog implements android.view.View.OnClickListener {

  private Button           okButton;

  private Button           cancelButton;

  private Context          mContext;

  private String           fileType;

  private String           filePath;

  private String           fileName;

  private ProgressDialog   mProgressDialog;

  public static final int  DIALOG_DOWNLOAD_PROGRESS = 0;

  private FileDownloadTask mLoadTask;

  private String           downLoadingFile;

  private String           noAppFound;

  private String           fileNotSupport;

  private String           cannotOpenFile;

  private String           fileNotFound;

  private Resources        resource;

  private FileCache        fileCache;

  public CompatibleFileOpenDialog(Context context, String fType, String fPath, String fName) {
    super(context);
    mContext = context;
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.compatible_open_file_dialog_layout);
    changeLanguage();
    fileCache = new FileCache(context, ExoConstants.DOCUMENT_FILE_CACHE);
    TextView titleView = (TextView) findViewById(R.id.com_dialog_title_view);
    titleView.setText(fName);
    TextView contentView = (TextView) findViewById(R.id.com_warning_content);
    contentView.setText(context.getResources().getString(R.string.CompatibleFileSuggest));
    okButton = (Button) findViewById(R.id.com_ok_button);
    okButton.setOnClickListener(this);
    cancelButton = (Button) findViewById(R.id.com_cancel_button);
    cancelButton.setOnClickListener(this);
    ImageView iconView = (ImageView) findViewById(R.id.com_warning_image);
    iconView.setBackgroundResource(ExoDocumentUtils.getIconFromType(fType));
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

  private void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == FileDownloadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  private void changeLanguage() {
    resource = mContext.getResources();
    downLoadingFile = resource.getString(R.string.DownloadingFile);
    noAppFound = resource.getString(R.string.NoAppFound);
    cannotOpenFile = resource.getString(R.string.CannotOpenFile);
    fileNotFound = resource.getString(R.string.FileNotFound);
    fileNotSupport = resource.getString(R.string.FileNotSupported);
  }

  private class FileDownloadTask extends AsyncTask<String, String, File> {

    private File file;

    @Override
    protected void onPreExecute() {
      mProgressDialog = (ProgressDialog) onCreateDialog(DIALOG_DOWNLOAD_PROGRESS);
      mProgressDialog.show();

    }

    @Override
    protected File doInBackground(String... params) {

      /*
       * If file exists return file else download from url
       */
      file = fileCache.getFileFromName(fileName);
      if (file.exists()) {
        return file;
      }

      String url = params[0].replaceAll(" ", "%20");
      HttpParams httpParameters = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
      HttpConnectionParams.setSoTimeout(httpParameters, 10000);
      HttpConnectionParams.setTcpNoDelay(httpParameters, true);
      DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
      httpClient.setCookieStore(ExoConnectionUtils.cookiesStore);

      try {

        HttpGet getRequest = new HttpGet(url);
        HttpResponse response = httpClient.execute(getRequest);
        HttpEntity entity = response.getEntity();
        if (entity != null) {

          // lenghtOfFile is used for calculating download progress
          long lenghtOfFile = entity.getContentLength();
          mProgressDialog.setMax((int) lenghtOfFile);
          InputStream is = entity.getContent();
          OutputStream os = new FileOutputStream(file);

          // here's the downloading progress
          byte[] buffer = new byte[1024];
          int len = 0;
          long total = 0;

          while ((len = is.read(buffer)) > 0) {
            total += len; // total = total + len
//            publishProgress("" + (int) ((total * 100) / lenghtOfFile));
            publishProgress("" + total);
            os.write(buffer, 0, len);
          }

          os.close();
        }
        return file;
      } catch (IOException e) {
        if (file != null) {
          file.delete();
        }
        return null;
      } finally {
        httpClient.getConnectionManager().shutdown();
      }

    }

    /*
     * If cancelled, delete the downloading file
     */
    @Override
    protected void onCancelled() {
      if (file.exists()) {
        file.delete();
      }
      mProgressDialog.dismiss();
      super.onCancelled();
    }

    @Override
    protected void onProgressUpdate(String... values) {
      mProgressDialog.setProgress(Integer.parseInt(values[0]));
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
        } else if (fileType.startsWith(ExoDocumentUtils.IMAGE_TYPE)) {
          docFileType = ExoDocumentUtils.ALL_IMAGE_TYPE;
        } else if (fileType.startsWith(ExoDocumentUtils.TEXT_TYPE)) {
          docFileType = ExoDocumentUtils.ALL_TEXT_TYPE;
        }

        if (docFileType != null) {
          Uri path = Uri.fromFile(result);
          Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setDataAndType(path, docFileType);
          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          try {
            mContext.startActivity(intent);
          } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, noAppFound, Toast.LENGTH_SHORT).show();
          }

        } else {
          Toast.makeText(mContext, cannotOpenFile, Toast.LENGTH_SHORT).show();
        }

      } else {
        Toast.makeText(mContext, fileNotFound, Toast.LENGTH_SHORT).show();
      }
      mProgressDialog.dismiss();
    }

  }

  private Dialog onCreateDialog(int id) {
    switch (id) {
    case DIALOG_DOWNLOAD_PROGRESS: // we set this to 0
      mProgressDialog = new ProgressDialog(mContext);
      mProgressDialog.setMessage(downLoadingFile);
      mProgressDialog.setIndeterminate(false);
      mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
      mProgressDialog.setCancelable(true);
      mProgressDialog.show();
      return mProgressDialog;
    default:
      return null;
    }
  }

  @Override
  public void onBackPressed() {
    onCancelLoad();
    dismiss();
    super.onBackPressed();
  }

}
