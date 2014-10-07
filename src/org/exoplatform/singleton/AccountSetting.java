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

import java.util.ArrayList;

import org.exoplatform.model.ServerObjInfo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents as temporary instance of SharedPref, which is used to manage all
 * the account information Changes in account setting will be populated to
 * SharedPref only when user logs in successfully or launching app using custom
 * URL scheme
 */
public class AccountSetting implements Parcelable {

    private static AccountSetting accountSetting = new AccountSetting();

    /**
     * The index of checked server, in case no server selected, index = -1
     */
    private String                domainIndex;

    /*
     * SETTING_SOCIAL_FILTER
     */
    public String                 socialKey;

    /*
     * SETTING_SOCIAL_FILTER_INDEX
     */
    public String                 socialKeyIndex;

    /*
     * SETTING_DOCUMENT_SHOW_HIDDEN_FILE
     */
    public String                 documentKey;

    public ArrayList<String>      cookiesList;

    /** current server */
    private ServerObjInfo         mCurrentServer;

    private AccountSetting() {
    }

    public static AccountSetting getInstance() {
        return accountSetting;
    }

    public void setInstance(AccountSetting instance) {
        accountSetting = instance;
    }

    public ServerObjInfo getCurrentServer() {
        return mCurrentServer;
    }

    public void setCurrentServer(ServerObjInfo server) {
        mCurrentServer = server;
    }

    public String getUsername() {
        return (mCurrentServer != null) ? mCurrentServer.username : "";
    }

    public String getPassword() {
        return (mCurrentServer != null) ? mCurrentServer.password : "";
    }

    public String getDomainName() {
        return (mCurrentServer != null) ? mCurrentServer.serverUrl : "";
    }

    public String getServerName() {
        return (mCurrentServer != null) ? mCurrentServer.serverName : "";
    }

    /**
     * Whether auto-login is enabled or not if no server defined then disable
     * auto-login
     * 
     * @return
     */
    public boolean isAutoLoginEnabled() {
        return (mCurrentServer != null) ? mCurrentServer.isAutoLoginEnabled : false;
    }

    /**
     * Whether remember-me is enabled or not if no server defined then disable
     * remember-me
     * 
     * @return
     */
    public boolean isRememberMeEnabled() {
        return (mCurrentServer != null) ? mCurrentServer.isRememberEnabled : false;
    }

    public String getDomainIndex() {
        return (domainIndex == null) ? "-1" : domainIndex;
    }

    public void setDomainIndex(String index) {
        domainIndex = index;
    }

    private AccountSetting(Parcel in) {
        readFromParcel(in);
    }

    public void readFromParcel(Parcel in) {
        domainIndex = in.readString();
        mCurrentServer = in.readParcelable(ServerObjInfo.class.getClassLoader());
        cookiesList = new ArrayList<String>();
        in.readStringList(cookiesList);

    }

    public static final Parcelable.Creator<AccountSetting> CREATOR = new Parcelable.Creator<AccountSetting>() {
                                                                       public AccountSetting createFromParcel(Parcel in) {
                                                                           return new AccountSetting(in);
                                                                       }

                                                                       public AccountSetting[] newArray(int size) {
                                                                           return new AccountSetting[size];
                                                                       }
                                                                   };

    /*
     * (non-Javadoc)
     * @see android.os.Parcelable#describeContents()
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see android.os.Parcelable#writeToParcel(android.os.Parcel, int)
     */
    @Override
    public void writeToParcel(Parcel par, int flags) {
        par.writeString(domainIndex);
        par.writeParcelable(mCurrentServer, flags);
        par.writeStringList(cookiesList);
    }

}
