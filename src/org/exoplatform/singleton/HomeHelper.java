package org.exoplatform.singleton;

import java.util.ArrayList;

import org.exoplatform.model.HomeItem;

public class HomeHelper {
  private static HomeHelper   homeHelper = new HomeHelper();

  private ArrayList<HomeItem> homeItemList; // list of item.
  
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
