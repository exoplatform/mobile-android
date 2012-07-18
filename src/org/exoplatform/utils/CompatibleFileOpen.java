/*
 * Copyright (C) 2003-2012 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.utils;

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
import org.exoplatform.utils.image.FileCache;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.UnreadableFileDialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com Jul
 * 6, 2012
 */
public class CompatibleFileOpen {
  private static final int       RESULT_OK                = 0;

  private static final int       RESULT_ERROR             = 1;

  private static final int       RESULT_CANCEL            = 2;

  private Context                mContext;

  private String                 fileType;

  private String                 filePath;

  private String                 fileName;

  private DownloadProgressDialog mProgressDialog;

  public static final int        DIALOG_DOWNLOAD_PROGRESS = 0;

  private FileDownloadTask       mLoadTask;

  private String                 downLoadingFile;

  private String                 noAppFound;

  private String                 fileNotSupport;

  private String                 cannotOpenFile;

  private String                 fileNotFound;

  private String                 memoryWarning;

  private Resources              resource;

  private FileCache              fileCache;

  public CompatibleFileOpen(Context context, String fType, String fPath, String fName) {
    mContext = context;
    changeLanguage();
    fileCache = new FileCache(context, ExoConstants.DOCUMENT_FILE_CACHE);
    fileType = fType;
    filePath = fPath;
    fileName = fName;
    if (ExoDocumentUtils.isFileReadable(fileType)) {
      onLoad(filePath);
    } else {
      new UnreadableFileDialog(mContext, fileNotSupport).show();
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
    memoryWarning = resource.getString(R.string.FileNotSupported);
  }

  private class FileDownloadTask extends AsyncTask<String, String, Integer> {

    private File file;

    @Override
    protected void onPreExecute() {
      mProgressDialog = (DownloadProgressDialog) onCreateDialog(DIALOG_DOWNLOAD_PROGRESS);
      mProgressDialog.show();

    }

    @Override
    protected Integer doInBackground(String... params) {

      /*
       * If file exists return file else download from url
       */
      file = fileCache.getFileFromName(fileName);
      if (file.exists()) {
        return RESULT_OK;
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
          /*
           * Compare the file size and free sdcard memory
           */
          if (!ExoDocumentUtils.isEnoughMemory((int) lenghtOfFile)) {
            return RESULT_CANCEL;
          }
          mProgressDialog.setMax((int) lenghtOfFile);
          InputStream is = entity.getContent();
          OutputStream os = new FileOutputStream(file);

          // here's the downloading progress
          byte[] buffer = new byte[1024];
          int len = 0;
          long total = 0;

          while ((len = is.read(buffer)) > 0) {
            total += len; // total = total + len
            publishProgress("" + total);
            os.write(buffer, 0, len);
          }

          os.close();
        }
        return RESULT_OK;
      } catch (IOException e) {
        if (file != null) {
          file.delete();
        }
        return RESULT_ERROR;
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
    protected void onPostExecute(Integer result) {
      if (result == RESULT_OK) {
        /*
         * get exactly document type from content type and open it with
         * compatible intent
         */
        String docFileType = ExoDocumentUtils.getFullFileType(fileType);

        if (docFileType != null) {
          Uri path = Uri.fromFile(file);
          Intent intent = new Intent(Intent.ACTION_VIEW);
          intent.setDataAndType(path, docFileType);
          intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          try {
            mContext.startActivity(intent);
          } catch (ActivityNotFoundException e) {
            new UnreadableFileDialog(mContext, noAppFound).show();
          }

        } else {
          new UnreadableFileDialog(mContext, cannotOpenFile).show();
        }

      } else if (result == RESULT_ERROR) {
        new UnreadableFileDialog(mContext, fileNotFound).show();
      } else if (result == RESULT_CANCEL) {
        new UnreadableFileDialog(mContext, memoryWarning).show();
      }
      mProgressDialog.dismiss();
    }

  }

  private Dialog onCreateDialog(int id) {
    switch (id) {
    case DIALOG_DOWNLOAD_PROGRESS: // we set this to 0
      mProgressDialog = new DownloadProgressDialog(mContext);
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

  private class DownloadProgressDialog extends ProgressDialog {

    public DownloadProgressDialog(Context context) {
      super(context);
    }

    /*
     * Override onBackPressed() method to call onCancelLoad() to cancel
     * downloading file task and delete the downloading file
     */

    @Override
    public void onBackPressed() {
      onCancelLoad();
      super.onBackPressed();
    }
  }

}
