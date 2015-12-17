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
package org.exoplatform.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.shareextension.service.ShareService.UploadInfo;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.utils.Utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Philippe Aristote paristote@exoplatform.com
 * @since Jun 16, 2015
 */
public class SocialPostInfo implements Parcelable {

  public final static String TYPE_DEFAULT = "activity_default";

  public final static String TYPE_DOC     = "activity_doc";

  public final static String TYPE_LINK    = "activity_link";

  public ExoAccount          ownerAccount;

  public String              postMessage;

  public List<String>        postAttachedFiles;

  public SocialSpaceInfo     destinationSpace;

  public Map<String, String> templateParams;

  public String              activityType = TYPE_DEFAULT;

  public SocialPostInfo() {
  }

  private SocialPostInfo(Parcel in) {
    readFromParcel(in);
  }

  public static final Parcelable.Creator<SocialPostInfo> CREATOR = new Parcelable.Creator<SocialPostInfo>() {
                                                                   public SocialPostInfo createFromParcel(Parcel in) {
                                                                     return new SocialPostInfo(in);
                                                                   }

                                                                   public SocialPostInfo[] newArray(int size) {
                                                                     return new SocialPostInfo[size];
                                                                   }
                                                                 };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel out, int flags) {
    out.writeParcelable(ownerAccount, flags);
    out.writeString(postMessage);
    out.writeStringList(postAttachedFiles);
    out.writeParcelable(destinationSpace, flags);
    out.writeMap(templateParams);
    out.writeString(activityType);

  }

  public void readFromParcel(Parcel in) {
    ownerAccount = in.readParcelable(SocialPostInfo.class.getClassLoader());
    postMessage = in.readString();
    postAttachedFiles = new ArrayList<String>();
    in.readStringList(postAttachedFiles);
    destinationSpace = in.readParcelable(SocialPostInfo.class.getClassLoader());
    in.readMap(templateParams, SocialPostInfo.class.getClassLoader());
    activityType = in.readString();
  }

  /*
   * 
   */

  /**
   * @return true if the post's destination space is null or empty, i.e. the
   *         post is public
   */
  public boolean isPublic() {
    return (destinationSpace == null || "".equals(destinationSpace));
  }

  /**
   * @return true if the post's attachments list is not null and not empty
   */
  public boolean hasAttachment() {
    return Utils.notEmpty(postAttachedFiles);
  }

  /**
   * Set the template param with the given name and value.<br/>
   * Create a new Map if necessary.
   * 
   * @param name
   * @param value
   */
  public void addTemplateParam(String name, String value) {
    if (templateParams == null)
      templateParams = new HashMap<String, String>(1);
    templateParams.put(name, value);
  }

  /**
   * Get the template param with the given name. <br/>
   * Return null if the Map is null or if no param is found under this name.
   * 
   * @param name
   * @return
   */
  public String getTemplateParam(String name) {
    if (templateParams == null)
      return null;
    return templateParams.get(name);
  }

  /**
   * Create TemplateParams for a DOC_ACTIVITY
   * 
   * @param uploadInfo Info about the uploaded DOC
   */
  public void buildTemplateParams(UploadInfo uploadInfo) {
    String docUrl = uploadInfo.getUploadedUrl();
    templateParams = new HashMap<String, String>();
    templateParams.put("WORKSPACE", uploadInfo.workspace);
    templateParams.put("REPOSITORY", uploadInfo.repository);
    String docLink = docUrl.substring(ownerAccount.serverUrl.length());
    templateParams.put("DOCLINK", docLink);
    StringBuffer beginPath = new StringBuffer(ExoConstants.DOCUMENT_JCR_PATH).append("/")
                                                                             .append(uploadInfo.repository)
                                                                             .append("/")
                                                                             .append(uploadInfo.workspace);
    String docPath = docLink.substring(beginPath.length());
    templateParams.put("DOCPATH", docPath);
    templateParams.put("DOCNAME", uploadInfo.fileToUpload.documentName);
    String mimeType = uploadInfo.fileToUpload.documentMimeType;
    if (mimeType != null && !mimeType.trim().isEmpty()) {
      templateParams.put("mimeType", uploadInfo.fileToUpload.documentMimeType);
    }
  }

}
