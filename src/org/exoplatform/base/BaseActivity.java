/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.base;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashSet;

import org.exoplatform.utils.Log;

import android.support.v4.app.FragmentActivity;

/**
 * Created by The eXo Platform SAS
 * Author :  MinhTDH
 *           MinhTDH@exoplatform.com
 * Jul 20, 2015  
 */
public class BaseActivity extends FragmentActivity {
  /**
   * same purpose with ActivityLifecycleCallbacks in API 14, 
   * but only interested in onPaused and onResume.
   * @author  MinhTDH
   *
   */
  public interface ActivityLifecycleCallbacks {
    void onResume(BaseActivity act);
    void onPause(BaseActivity act);
  }

  public static class BasicActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

    @Override
    public void onResume(BaseActivity act) {
      
    }

    @Override
    public void onPause(BaseActivity act) {
      
    }
    
  }
  
  private HashSet<WeakReference<BasicActivityLifecycleCallbacks>> mLifeCycleCallbackSet = new HashSet<WeakReference<BasicActivityLifecycleCallbacks>>();

  public void addLifeCycleObserverRef(BasicActivityLifecycleCallbacks cbRef) {
    mLifeCycleCallbackSet.add(new WeakReference<BaseActivity.BasicActivityLifecycleCallbacks>(cbRef));
  }

  public void removeLifeCycleObserver(BasicActivityLifecycleCallbacks removeCb) {
    synchronized (mLifeCycleCallbackSet) {
      ArrayList<WeakReference<BasicActivityLifecycleCallbacks>> removeList = new ArrayList<WeakReference<BasicActivityLifecycleCallbacks>>();
      for (WeakReference<BasicActivityLifecycleCallbacks> cbRef : mLifeCycleCallbackSet) {
        BasicActivityLifecycleCallbacks cb = cbRef == null ? null : cbRef.get();
        if (cb == removeCb) {
          removeList.add(cbRef);
        }
      }
      for (WeakReference<BasicActivityLifecycleCallbacks> cbRef : removeList) {
        mLifeCycleCallbackSet.remove(cbRef);
      }
    }
  }
  
  @Override
  protected void onResume() {
    super.onResume();
    for (WeakReference<BasicActivityLifecycleCallbacks> cbRef : mLifeCycleCallbackSet) {
      BasicActivityLifecycleCallbacks cb = cbRef == null ? null : cbRef.get();
      if (cb != null) {
       cb.onResume(this);
      }
    } 
  }
  
  @Override
  protected void onPause() {
    for (WeakReference<BasicActivityLifecycleCallbacks> cbRef : mLifeCycleCallbackSet) {
      BasicActivityLifecycleCallbacks cb = cbRef == null ? null : cbRef.get();
      if (cb != null) {
       cb.onPause(this);
      }
    } 
    // clear release reference
    removeLifeCycleObserver(null);
    if (Log.LOGD)
      Log.d(getClass().getSimpleName(), "onPause isFinishing=", isFinishing(), " ", this);
    super.onPause();
  }
}
