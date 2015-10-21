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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Philippe Aristote paristote@exoplatform.com
 * @since Apr 22, 2015
 */
public class SocialSpaceInfo implements Parcelable {

  public String id;

  public String avatarUrl;

  public String displayName;

  public String name;

  public String groupId;

  public SocialSpaceInfo() {
  }

  @Override
  public String toString() {
    return displayName;
  }

  /**
   * If the space has been renamed, it's still possible to retrieve the original
   * name from the groupId.<br/>
   * It works by extracting the last part of the groupId, e.g.
   * 
   * <pre>
   * name = new_name
   * groupId = /spaces/old_name
   * original name = old_name
   * </pre>
   * 
   * @return the original name, or the current name if extraction fails.
   */
  public String getOriginalName() {
    String origName = name;
    if (groupId != null) {
      int lastPart = groupId.lastIndexOf('/');
      if (lastPart >= 0 && lastPart < groupId.length()) {
        origName = groupId.substring(lastPart + 1);
      }
    }
    return origName;
  }

  /*
   * PARCEL
   */

  private SocialSpaceInfo(Parcel in) {
    readFromParcel(in);
  }

  public static final Parcelable.Creator<SocialSpaceInfo> CREATOR = new Parcelable.Creator<SocialSpaceInfo>() {
                                                                    public SocialSpaceInfo createFromParcel(Parcel in) {
                                                                      return new SocialSpaceInfo(in);
                                                                    }

                                                                    public SocialSpaceInfo[] newArray(int size) {
                                                                      return new SocialSpaceInfo[size];
                                                                    }
                                                                  };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(id);
    dest.writeString(name);
    dest.writeString(displayName);
    dest.writeString(groupId);
    dest.writeString(avatarUrl);
  }

  public void readFromParcel(Parcel in) {
    id = in.readString();
    name = in.readString();
    displayName = in.readString();
    groupId = in.readString();
    avatarUrl = in.readString();
  }

}
