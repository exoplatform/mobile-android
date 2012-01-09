package org.exoplatform.model;

public class DashboardItem {

  public String id;   // Dashboard ID

  public String html; // Dashboard html

  public String link; // Dashboard link

  public String label; // Dashboard link

  // Constructor

  public DashboardItem(String _id, String _html, String _link, String _label) {

    id = _id;
    html = _html;
    link = _link;
    label = _label;

  }
}
