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

import android.util.Log;
//import greendroid.widget.LoaderActionBarItem;

import java.util.ArrayList;

import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.SocialServiceHelper;
import org.exoplatform.social.client.api.SocialClientLibException;
import org.exoplatform.social.client.api.common.RealtimeListAccess;
import org.exoplatform.social.client.api.model.RestActivity;
import org.exoplatform.social.client.api.model.RestIdentity;
import org.exoplatform.social.client.api.service.QueryParams;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.widget.ConnectionErrorDialog;

import android.content.Context;

/**
 * Created by The eXo Platform SAS Author : eXoPlatform exo@exoplatform.com May
 * 17, 2012
 */
public class HomeController {
  private Context                mContext;

  public  SocialServiceLoadTask  mServiceLoadTask;

  private SocialLoadTask         mLoadTask;

  //public  LoaderActionBarItem    loader;

  public  static final int     FLIPPER_VIEW = 10;

  private static final String  TAG = "eXo____HomeController____";

  /**
  public HomeController(Context context, LoaderActionBarItem loaderItem) {
    mContext = context;
    loader = loaderItem;
  }
   **/

  public void finishService() {
    onCancelLoadNewsService();
    onCancelLoad();
  }

  public void launchNewsService() {
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mServiceLoadTask == null || mServiceLoadTask.getStatus() == SocialServiceLoadTask.Status.FINISHED) {
        //mServiceLoadTask = (SocialServiceLoadTask) new SocialServiceLoadTask(mContext, this, loader).execute();
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

  /**
   * Load a number of activities with specific type
   *
   * @param number
   * @param type
   */
  public void onLoad(int number, int type) {
    if (ExoConnectionUtils.isNetworkAvailableExt(mContext)) {
      if (mLoadTask == null || mLoadTask.getStatus() == SocialLoadTask.Status.FINISHED) {

        /**
        mLoadTask = (SocialLoadTask) new SocialLoadTask(mContext, loader) {

          @Override
          protected ArrayList<SocialActivityInfo> getSocialActivityList() {
            return SocialServiceHelper.getInstance().socialInfoList;
          }

          @Override
          protected RealtimeListAccess<RestActivity> getRestActivityList(RestIdentity identity, QueryParams params) throws SocialClientLibException {
            return activityService.getFeedActivityStream(identity, params);
          }
        }.execute(number, type);

         **/
      }
    } else {
      new ConnectionErrorDialog(mContext).show();
    }
  }

  public boolean isLoadingTask() {
    return (mServiceLoadTask != null
        && mServiceLoadTask.getStatus() == SocialServiceLoadTask.Status.RUNNING);
  }

  private void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == SocialLoadTask.Status.RUNNING) {
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

}
