package org.exoplatform.model;

import android.graphics.Bitmap;

public class HomeItem {
  public Bitmap _icon; // feature's icon

  public int    _index;

  public String _name; // feature's name

  public HomeItem() {

  }

  public HomeItem(Bitmap bm, int index, String name) {
    _icon = bm;
    _index = index;
    _name = name;
  }
}
