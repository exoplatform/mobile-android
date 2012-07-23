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
package org.exoplatform.controller.home;

import greendroid.widget.LoaderActionBarItem;

import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.SocialWaitingDialog;

import android.content.Context;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com May
 * 17, 2012
 */
public class HomeController {
  public SocialWaitingDialog    _progressDialog;

  private Context               mContext;

  private SocialServiceLoadTask mServiceLoadTask;

  private SocialLoadTask        mLoadTask;

  public HomeController(Context context) {
    mContext = context;
  }

  public void finishService() {
    onCancelLoadNewsService();
    onCancelLoad();
  }

  public void launchNewsService(LoaderActionBarItem loader) {

    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mServiceLoadTask == null
          || mServiceLoadTask.getStatus() == SocialServiceLoadTask.Status.FINISHED) {
        mServiceLoadTask = (SocialServiceLoadTask) new SocialServiceLoadTask(mContext, this, loader).execute();
      }
    } else {
      new ConnectionErrorDialog(mContext).show();
    }

  }

  private void onCancelLoadNewsService() {
    if (mServiceLoadTask != null
        && mServiceLoadTask.getStatus() == SocialServiceLoadTask.Status.RUNNING) {
      mServiceLoadTask.cancel(true);
      mServiceLoadTask = null;
    }
  }

  public void onLoad(int number, LoaderActionBarItem loader) {
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mLoadTask == null || mLoadTask.getStatus() == SocialLoadTask.Status.FINISHED) {
        mLoadTask = (SocialLoadTask) new SocialLoadTask(mContext, loader).execute(number);
      }
    } else {
      new ConnectionErrorDialog(mContext).show();
    }
  }

  public boolean isLoadingTak() {
    if (mServiceLoadTask != null
        && mServiceLoadTask.getStatus() == SocialServiceLoadTask.Status.RUNNING) {
      return true;
    }
    if (mLoadTask != null && mLoadTask.getStatus() == SocialLoadTask.Status.RUNNING) {
      return true;
    }

    return false;

  }

  private void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == SocialLoadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

}
