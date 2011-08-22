package org.exoplatform.social.image;

import java.util.ArrayList;

import android.graphics.Bitmap;

public class PhotoInfo {
  private String            albumsAvatar;

  private String            albumsName;

  private int               imageNumber;

  private ArrayList<String> imageList;

  public PhotoInfo() {

  }

//  public void setAlbumsAvatar(String avatar) {
//    albumsAvatar = avatar;
//  }

  public String getAlbumsAvatar() {
    return imageList.get(0);
  }

  public void setAlbumsName(String name) {
    albumsName = name;
  }

  public String getAlbumsName() {
    return albumsName;
  }

  // public void setImageNumber(int num) {
  // imageNumber = num;
  // }

  public int getImageNumber() {
    return imageList.size();
  }

  public void setImageList(ArrayList<String> list) {
    imageList = list;
  }

  public ArrayList<String> getImageList() {
    return imageList;
  }
}
