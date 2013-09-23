package org.exoplatform.ui.login.tasks;

import android.os.AsyncTask;
import android.util.Log;
import org.exoplatform.utils.ExoConnectionUtils;

import java.io.IOException;

/**
 * Task performing checking status of tenant
 */
public class CheckingTenantStatusTask extends AsyncTask<String, Void, Integer> {

  private AsyncTaskListener   mListener;

  private static final String TAG = "eXo____CheckingTenantStatusTask____";

  @Override
  protected Integer doInBackground(String... params) {
    String tenant = params[0];
    String email  = params[1];
    Log.d(TAG, "checking tenant status: " + tenant + " for " + email);

    try {
      int status = ExoConnectionUtils.requestTenantStatus(tenant);
      if (status == ExoConnectionUtils.LOGIN_SERVER_RESUMING)
        return ExoConnectionUtils.LOGIN_SERVER_RESUMING;

      else if (status == ExoConnectionUtils.SIGNIN_SERVER_SUSPENDED) {
        /** stimulate a sign up request to force restoring server */
        ExoConnectionUtils.makeCloudSignUpRequest(email);
        return ExoConnectionUtils.LOGIN_SERVER_RESUMING;
      }
      else if (status == ExoConnectionUtils.SIGNIN_SERVER_ONLINE)
        return ExoConnectionUtils.TENANT_OK;

      return status;
    }
    catch (IOException e) {
      Log.d(TAG, "IOException: " + e.getLocalizedMessage());
      return ExoConnectionUtils.SIGNIN_SERVER_NAV;
    }
  }

  public void setListener(AsyncTaskListener listener) {
    mListener = listener;
  }

  @Override
  protected void onPostExecute(Integer result) {
    if (mListener!= null) mListener.onCheckingTenantStatusFinished(result);
  }


  public interface AsyncTaskListener {

    void onCheckingTenantStatusFinished(int result);
  }
}


