package org.exoplatform.singleton;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/*
 * This class is for manage all the account information
 * 
 */
public class AccountSetting implements Parcelable {

  private static AccountSetting accountSetting = new AccountSetting();

  private String                userName;

  private String                password;

  /*
   * checked server url
   */
  private String                domainName;

  /*
   * the index of checked server
   */
  private String                domainIndex;

  public ArrayList<String>      cookiesList;

  private AccountSetting() {

  }

  public static AccountSetting getInstance() {
    return accountSetting;
  }

  public void setInstance(AccountSetting instance) {
    accountSetting = instance;
  }

  public String getUsername() {
    return userName;
  }

  public void setUsername(String name) {
    userName = name;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String pwd) {
    password = pwd;
  }

  public String getDomainName() {
    return domainName;
  }

  public void setDomainName(String url) {
    domainName = url;
  }

  public String getDomainIndex() {
    return domainIndex;
  }

  public void setDomainIndex(String index) {
    domainIndex = index;
  }

  private AccountSetting(Parcel in) {
    readFromParcel(in);
  }

  public void readFromParcel(Parcel in) {
    userName = in.readString();
    password = in.readString();
    domainName = in.readString();
    domainIndex = in.readString();
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
    par.writeString(userName);
    par.writeString(password);
    par.writeString(domainName);
    par.writeString(domainIndex);
    par.writeStringList(cookiesList);
  }

}
