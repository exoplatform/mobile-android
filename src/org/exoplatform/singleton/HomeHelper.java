package org.exoplatform.singleton;

import java.util.ArrayList;

import org.apache.http.impl.client.DefaultHttpClient;
import org.exoplatform.model.HomeItem;
/*
 * The singleton for management the list of application item like Activity item, Dashboard item etc.
 */

public class HomeHelper {
  private static HomeHelper   homeHelper = new HomeHelper();

  private ArrayList<HomeItem> homeItemList; // list of item.
  
  public DefaultHttpClient httpClient;

  private HomeHelper() {

  }

  public static HomeHelper getInstance() {
    return homeHelper;
  }

  public void setHomeItemList(ArrayList<HomeItem> list) {
    homeItemList = list;
  }

  public ArrayList<HomeItem> getHomeItemList() {
    return homeItemList;
  }

}
