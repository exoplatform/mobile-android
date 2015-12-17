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
import org.exoplatform.shareextension.service.ShareService.UploadInfo;
import org.exoplatform.utils.ExoDocumentUtils;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Philippe Aristote paristote@exoplatform.com
 * @since Jun 17, 2015
 */
public class CreateFolderAction extends Action {

  private UploadInfo uploadInfo;

  @Override
  protected void check() {
    if (uploadInfo == null)
      throw new IllegalArgumentException("Cannot pass null as the UploadInfo argument");
    super.check();
  }

  // create and execute create folder request, wait for result
  public static boolean execute(SocialPostInfo post, UploadInfo upload, ActionListener listener) {

    CreateFolderAction action = new CreateFolderAction();
    action.postInfo = post;
    action.listener = listener;
    action.uploadInfo = upload;
    return action.execute();

  }

  @Override
  protected boolean doExecute() {

    String folderUrl = uploadInfo.jcrUrl + "/" + uploadInfo.folder;
    boolean createFolder = ExoDocumentUtils.createFolder(folderUrl);
    boolean ret = false;
    if (createFolder) {
      ret = listener.onSuccess("Destination folder ready");
    } else {
      ret = listener.onError("Could not create the destination folder");
    }
    return ret;
  }

}
