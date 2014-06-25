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

import org.exoplatform.utils.ExoConnectionUtils;

import android.os.AsyncTask;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Jun 25, 2014  
 */
public class CheckAccountExistsTask extends AsyncTask<String, Void, Boolean> {

  private AsyncTaskListener mListener; 
  
  private static final String TAG = "eXo____CheckAccountExistsTask____";

  @Override
  protected Boolean doInBackground(String... params) {
    
//    if (params.length != 2)
          
    String username = params[0];
    String tenant = params[1];
    
    Boolean result = Boolean.valueOf(ExoConnectionUtils.requestAccountExistsForUser(username, tenant));
    
    return result;
  }
  
 
  @Override
  protected void onPostExecute(Boolean result) {
    if (mListener!= null) mListener.onCheckAccountExistsFinished(result.booleanValue());
  }
  
  
  public void setListener(AsyncTaskListener listener) {
    mListener = listener;
  }
  
  public interface AsyncTaskListener {

    void onCheckAccountExistsFinished(boolean result);
  }

}
