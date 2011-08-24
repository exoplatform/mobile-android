package org.exoplatform.model;

import android.graphics.Bitmap;

public class ExoAppItem {
 public Bitmap _icon;      // feature's icon

 public int    _badgeCount; // number of notification

 public String _name;      // feature's name

  public ExoAppItem() {

  }

  public ExoAppItem(Bitmap bm, String name) {
    _icon = bm;
    _name = name;
  }
}
