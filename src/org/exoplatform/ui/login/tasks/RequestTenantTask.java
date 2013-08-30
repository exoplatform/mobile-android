package org.exoplatform.ui.login.tasks;

import android.os.AsyncTask;
import android.util.Log;
import org.apache.http.HttpResponse;
import org.exoplatform.utils.ExoConnectionUtils;

import java.io.IOException;

/**
 *
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
      mResult = ExoConnectionUtils.checkRequestTenant(response);
      if (mResult == null) return ExoConnectionUtils.SIGNIN_NO_TENANT_FOR_EMAIL;
      return ExoConnectionUtils.TENANT_OK;
    }
    catch (IOException e) {
      Log.d(TAG, "IOException: " + e.getLocalizedMessage());
      return ExoConnectionUtils.SIGNIN_SERVER_NAV;
    }
  }

  public void onPostExecute(Integer result) {
    Log.i(TAG, "result: " + result);
    if (mListener != null) mListener.onRequestingTenantFinished(result, mResult);
  }

  public void setListener(AsyncTaskListener listener) {
    mListener = listener;
  }

  public interface AsyncTaskListener {

    void onRequestingTenantFinished(int result, String[] userAndTenant);
  }
}
