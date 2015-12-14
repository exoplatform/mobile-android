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
  public ArrayList<GadgetInfo> getGadgetInTab(HttpResponse response, String tabName, String url) {
    InputStream input = ExoConnectionUtils.sendRequest(response);
    if (input == null) {
      addErrorDashboard(tabName);
      return null;
    } else {

      String result = ExoConnectionUtils.convertStreamToString(input);
      if (result == null) {
        addErrorDashboard(tabName);
        return null;
      } else {

        JSONArray array = (JSONArray) JSONValue.parse(result);
        if (array == null) {
          addErrorDashboard(tabName);
          return null;
        }
        int count = 0;
        ArrayList<GadgetInfo> gadgets = new ArrayList<GadgetInfo>();
        for (Object obj : array) {

          JSONObject json = (JSONObject) obj;
          if (json == null) {
            continue;
          }
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

          GadgetInfo gadget = new GadgetInfo(gadgetName, gadgetDescription, gadgetUrl, gadgetIcon, null, count);

          gadgets.add(gadget);

          count++;
        }
        return gadgets;
      }
    }
  }

  private void addErrorDashboard(String tabName) {
    if (dashboardsCannotBeRetrieved.length() == 0)
      dashboardsCannotBeRetrieved += tabName;
    else
      dashboardsCannotBeRetrieved = new StringBuilder(dashboardsCannotBeRetrieved).append(", ").append(tabName).toString();
  }

  public String getGadgetsErrorList() {
    return dashboardsCannotBeRetrieved;
  }
}
