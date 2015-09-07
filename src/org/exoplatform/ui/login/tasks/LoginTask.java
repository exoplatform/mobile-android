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
import java.net.URI;

import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.protocol.HttpContext;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.Log;

import android.os.AsyncTask;

/**
 * Performs login
 */
public class LoginTask extends AsyncTask<String, Void, Integer> {

  private AsyncTaskListener   mListener;

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
    String domain = params[2];

    if (Log.LOGD)
      Log.d(TAG, "Logging in with " + username + " at " + domain);

    try {
      String versionUrl = domain + ExoConstants.DOMAIN_PLATFORM_VERSION;
      HttpResponse response = ExoConnectionUtils.getPlatformResponse(username, password, versionUrl);

      setRedirectResponseInterceptor();
      int statusCode = response.getStatusLine().getStatusCode();

      if (Log.LOGD)
        Log.d(TAG, "response code: " + statusCode);

      if (statusCode == HttpStatus.SC_NOT_FOUND)
        return ExoConnectionUtils.SIGNIN_SERVER_NAV;

      /** Login OK - check mobile compatibility */
      if (statusCode >= HttpStatus.SC_OK && statusCode < HttpStatus.SC_MULTIPLE_CHOICES) {
        boolean isCompliant = ExoConnectionUtils.checkPLFVersion(response, domain, username);
        if (!isCompliant)
          return ExoConnectionUtils.LOGIN_INCOMPATIBLE;
      }

      return ExoConnectionUtils.checkPlatformRespose(response);
    } catch (HttpHostConnectException e) {
      Log.d(TAG, "HttpHostConnectException: " + e.getLocalizedMessage());
      return ExoConnectionUtils.SIGNIN_SERVER_NAV;
    } catch (IOException e) {
      Log.d(TAG, "IOException: " + e.getLocalizedMessage());
      return ExoConnectionUtils.SIGNIN_SERVER_NAV;
    } finally {
      if (ExoConnectionUtils.httpClient != null) {
        ExoConnectionUtils.httpClient.clearResponseInterceptors();
      }
    }
  }

  @Override
  protected void onCancelled() {
    super.onCancelled();
    if (mListener != null)
      mListener.onCanceled();
  }

  @Override
  public void onPostExecute(Integer result) {
    if (Log.LOGD)
      Log.d(TAG, "Login result: " + result);

    if (mListener != null)
      mListener.onLoggingInFinished(result);
  }

  private void setRedirectResponseInterceptor() {
    if (ExoConnectionUtils.httpClient == null)
      return;

    ExoConnectionUtils.httpClient.addResponseInterceptor(new HttpResponseInterceptor() {

      @Override
      public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY) {
          if (mListener != null) {
            Header location = response.getFirstHeader("Location");
            if (location != null) {
              URI newDomain = URI.create(location.getValue());
              mListener.onUpdateDomain(newDomain.getScheme() + "://" + newDomain.getHost()
                  + (newDomain.getPort() == -1 ? "" : ":" + newDomain.getPort()));
            }
          }
        }
      }
    });
  }

  public void setListener(AsyncTaskListener listener) {
    mListener = listener;
  }

  public interface AsyncTaskListener {

    /**
     * Called when the task has finished to return the result.
     * 
     * @param result (1) for a successful login
     */
    void onLoggingInFinished(int result);

    /**
     * Called when the task is canceled.
     */
    void onCanceled();

    /**
     * Called when the original domain was redirected to a new one. <br/>
     * 
     * @param newDomain the new domain URL (scheme + host + optional port)
     */
    void onUpdateDomain(String newDomain);
  }
}
