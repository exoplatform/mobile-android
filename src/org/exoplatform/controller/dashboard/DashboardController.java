package org.exoplatform.controller.dashboard;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.exoplatform.model.DashboardItem;
import org.exoplatform.model.GadgetInfo;
import org.exoplatform.utils.ExoConnectionUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class DashboardController {

  private String dashboardsCannotBeRetrieved = "";

  public DashboardController() {
  }

  // Get Dashboards
  public ArrayList<DashboardItem> getDashboards(HttpResponse response) {

   
    InputStream input = ExoConnectionUtils.sendRequest(response);
    ArrayList<DashboardItem> dashboards = new ArrayList<DashboardItem>();
    if (input != null) {

      String result = ExoConnectionUtils.convertStreamToString(input);

      JSONArray array = (JSONArray) JSONValue.parse(result);

      for (Object obj : array) {
        JSONObject json = (JSONObject) obj;
        String id = "";
        String html = "";
        String link = "";
        String label = "";
        if (json.get("id") != null)
          id = json.get("id").toString().trim();
        if (json.get("html") != null)
          html = json.get("html").toString().trim();
        if (json.get("link") != null)
          link = json.get("link").toString().trim();
        if (json.get("label") != null)
          label = json.get("label").toString().trim();

        DashboardItem dashboardItem = new DashboardItem(id, html, link, label);

        dashboards.add(dashboardItem);
      }
    }

    return dashboards;

  }

  // Get Gadget in Dashboard
  public ArrayList<GadgetInfo> getGadgetInTab(HttpResponse response,String tabName, String url) {

    try {

      InputStream input = ExoConnectionUtils.sendRequest(response);
      ArrayList<GadgetInfo> gadgets = new ArrayList<GadgetInfo>();
      if (input != null) {

        String result = ExoConnectionUtils.convertStreamToString(input);

        JSONArray array = (JSONArray) JSONValue.parse(result);

        int count = 0;

        for (Object obj : array) {

          JSONObject json = (JSONObject) obj;

          String gadgetIcon = "";
          String gadgetUrl = "";
          String gadgetName = "";
          String gadgetDescription = "";

          if (json.get("gadgetIcon") != null)
            gadgetIcon = json.get("gadgetIcon").toString();
          if (json.get("gadgetUrl") != null)
            gadgetUrl = json.get("gadgetUrl").toString();
          if (json.get("gadgetName") != null)
            gadgetName = json.get("gadgetName").toString();

          if (json.get("gadgetDescription") != null)
            gadgetDescription = json.get("gadgetDescription").toString();

          GadgetInfo gadget = new GadgetInfo(gadgetName,
                                             gadgetDescription,
                                             gadgetUrl,
                                             gadgetIcon,
                                             null,
                                             count);

          gadgets.add(gadget);

          count++;
        }
      }

      return gadgets;

    } catch (Exception e) {

      if (dashboardsCannotBeRetrieved.length() == 0)
        dashboardsCannotBeRetrieved += tabName;
      else
        dashboardsCannotBeRetrieved += ", " + tabName;

      return null;
    }

  }

  public String getGadgetsErrorList() {
    return dashboardsCannotBeRetrieved;
  }
}
