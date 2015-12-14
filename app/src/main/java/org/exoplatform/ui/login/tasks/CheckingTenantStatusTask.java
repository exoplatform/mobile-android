/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.ui.login.tasks;

import java.io.IOException;

import org.exoplatform.utils.ExoConnectionUtils;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Task performing checking status of tenant
 */
public class CheckingTenantStatusTask extends AsyncTask<String, Void, Integer> {

  private AsyncTaskListener   mListener;

  private static final String TAG = CheckingTenantStatusTask.class.getName();

  @Override
  protected Integer doInBackground(String... params) {
    String tenant = params[0];
    String email = params[1];
    Log.d(TAG, "Checking tenant status: " + tenant + " for " + email);

    try {
      int status = ExoConnectionUtils.requestTenantStatus(tenant);
      if (status == ExoConnectionUtils.LOGIN_SERVER_RESUMING)
        return ExoConnectionUtils.LOGIN_SERVER_RESUMING;

      else if (status == ExoConnectionUtils.SIGNIN_SERVER_SUSPENDED) {
        /** stimulate a sign up request to force restoring server */
        ExoConnectionUtils.makeCloudSignUpRequest(email);
        return ExoConnectionUtils.LOGIN_SERVER_RESUMING;
      } else if (status == ExoConnectionUtils.SIGNIN_SERVER_ONLINE)
        return ExoConnectionUtils.TENANT_OK;

      return status;
    } catch (IOException e) {
      Log.d(TAG, "IOException: " + e.getLocalizedMessage());
      return ExoConnectionUtils.SIGNIN_SERVER_NAV;
    }
  }

  public void setListener(AsyncTaskListener listener) {
    mListener = listener;
  }

  @Override
  protected void onPostExecute(Integer result) {
    if (mListener != null)
      mListener.onCheckingTenantStatusFinished(result);
  }

  public interface AsyncTaskListener {

    void onCheckingTenantStatusFinished(int result);
  }
}
