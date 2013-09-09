package org.exoplatform.ui.login.tasks;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.exoplatform.utils.ExoConnectionUtils;

import java.io.IOException;

/**
 * Request tenant for a given email address
 */
public class RequestTenantTask extends AsyncTask<String, Void, Integer> {

  private static final String TAG = "eXo____RequestTenantTask____";

  private AsyncTaskListener mListener;

  private String[]          mResult;

  @Override
  protected Integer doInBackground(String... params) {
    Log.i(TAG, "request tenant for email: " + params[0]);
    String email = params[0];
    try {
      HttpResponse response  = ExoConnectionUtils.requestTenantForEmail(email);
      int responseCode = response.getStatusLine().getStatusCode();
      Log.d(TAG, "status: " + responseCode);
      mResult = ExoConnectionUtils.checkRequestTenant(response);

      /** check error */
      if (mResult == null) {
        if (responseCode == HttpStatus.SC_SERVICE_UNAVAILABLE
            || responseCode == HttpStatus.SC_NOT_FOUND)
          return ExoConnectionUtils.SIGNIN_SERVER_NAV;

        return ExoConnectionUtils.SIGNIN_NO_TENANT_FOR_EMAIL;
      }

      return ExoConnectionUtils.TENANT_OK;
    }
    catch (IOException e) {
      Log.d(TAG, "IOException: " + e.getLocalizedMessage());
      return ExoConnectionUtils.SIGNIN_SERVER_NAV;
    }
  }

  public void onPostExecute(Integer result) {
    if (mListener != null) mListener.onRequestingTenantFinished(result, mResult);
  }

  public void setListener(AsyncTaskListener listener) {
    mListener = listener;
  }

  public interface AsyncTaskListener {

    void onRequestingTenantFinished(int result, String[] userAndTenant);
  }
}
