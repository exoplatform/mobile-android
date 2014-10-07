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
package org.exoplatform.singleton;

import org.exoplatform.utils.image.SocialImageLoader;

import android.os.Parcel;
import android.os.Parcelable;

public class SocialDetailHelper implements Parcelable {
    private String                    activityId;

    private boolean                   liked;

    private String                    attachImageUrl;

    public SocialImageLoader          socialImageLoader;

    private static SocialDetailHelper detailHelper = new SocialDetailHelper();

    private SocialDetailHelper() {

    }

    public static SocialDetailHelper getInstance() {
        return detailHelper;
    }

    public void setActivityId(String id) {
        activityId = id;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setLiked(boolean is) {
        liked = is;
    }

    public boolean getLiked() {
        return liked;
    }

    public void setAttachedImageUrl(String url) {
        attachImageUrl = url;
    }

    public String getAttachedImageUrl() {
        return attachImageUrl;
    }

    private SocialDetailHelper(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        activityId = in.readString();
        in.readBooleanArray(new boolean[] { liked });
        attachImageUrl = in.readString();
    }

    public static final Parcelable.Creator<SocialDetailHelper> CREATOR = new Parcelable.Creator<SocialDetailHelper>() {
                                                                           public SocialDetailHelper createFromParcel(Parcel in) {
                                                                               return new SocialDetailHelper(in);
                                                                           }

                                                                           public SocialDetailHelper[] newArray(int size) {
                                                                               return new SocialDetailHelper[size];
                                                                           }
                                                                       };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(activityId);
        dest.writeBooleanArray(new boolean[] { liked });
        dest.writeString(attachImageUrl);
    }

}
