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
package org.exoplatform.shareextension.service;

import org.exoplatform.model.SocialPostInfo;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Philippe Aristote paristote@exoplatform.com
 * @since Jun 17, 2015
 */
public abstract class Action {

  protected final String   LOG_TAG = "____eXo_Action_" + this.getClass().getName() + "____";

  protected SocialPostInfo postInfo;

  protected ActionListener listener;

  protected void check() {
    if (postInfo == null)
      throw new IllegalArgumentException("Cannot pass null as the SocialPostInfo argument");
    if (listener == null)
      throw new IllegalArgumentException("Cannot pass null as the ActionListener argument");
  }

  protected abstract void doExecute();

  protected void execute() {
    check();
    doExecute();
  }

  public static interface ActionListener {

    public void onSuccess(String message);

    public void onError(String error);

  }

}
