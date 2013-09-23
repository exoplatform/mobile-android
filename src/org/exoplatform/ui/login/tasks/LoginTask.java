package org.exoplatform.ui.login.tasks;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.conn.HttpHostConnectException;
import org.exoplatform.utils.*;

import java.io.IOException;

/**
 * Performs login
 */
public class LoginTask extends AsyncTask<String, Void, Integer> {

  private AsyncTaskListener mListener;

  private static final String TAG = "eXo____LoginTask____";


  @Override
  public void onPreExecute() {
    /** need to log out first */
    ExoConnectionUtils.loggingOut();
  }

  @Override
  public Integer doInBackground(String... params) {
    String username = params[0];
    String password = params[1];
    String domain   = params[2];
    Log.d(TAG, "logging in with " + username + " at " + domain);

    try {
      String versionUrl     = domain + ExoConstants.DOMAIN_PLATFORM_VERSION;
      HttpResponse response = ExoConnectionUtils.getPlatformResponse(username, password, versionUrl);
      int statusCode        = response.getStatusLine().getStatusCode();

      Log.d(TAG, "response code: " + statusCode);
      if (statusCode == HttpStatus.SC_NOT_FOUND) return ExoConnectionUtils.SIGNIN_SERVER_NAV;

      /** Login OK - check mobile compatibility */
      if (statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
        boolean isCompliant = ExoConnectionUtils.checkPLFVersion(response);
        if (!isCompliant) return ExoConnectionUtils.LOGIN_INCOMPATIBLE;
      }

      ExoDocumentUtils.setRepositoryHomeUrl(username, domain);
      return ExoConnectionUtils.checkPlatformRespose(response);
    } catch(HttpHostConnectException e) {
      Log.d(TAG, "HttpHostConnectException: " + e.getLocalizedMessage());
      return ExoConnectionUtils.SIGNIN_SERVER_NAV;
    } catch (IOException e) {
      Log.d(TAG, "IOException: " + e.getLocalizedMessage());
      return ExoConnectionUtils.SIGNIN_SERVER_NAV;
    }
  }

  @Override
  public void onPostExecute(Integer result) {
    Log.d(TAG, "onPostExecute - login result: " + result);

    if (mListener != null) mListener.onLoggingInFinished(result);
  }

  public void setListener(AsyncTaskListener listener) {
    mListener = listener;
  }

  public interface AsyncTaskListener {

    void onLoggingInFinished(int result);
  }
}