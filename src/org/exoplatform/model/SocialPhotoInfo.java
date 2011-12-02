package org.exoplatform.model;

import java.util.ArrayList;

public class SocialPhotoInfo {
  private String            albumsName;

  private ArrayList<String> imageList;

  public SocialPhotoInfo() {

  }


  public String getAlbumsAvatar() {
    return imageList.get(0);
  }

  public void setAlbumsName(String name) {
    albumsName = name;
  }

  public String getAlbumsName() {
    return albumsName;
  }

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
