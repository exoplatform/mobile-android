package org.exoplatform.controller.dashboard;

import java.util.ArrayList;
import org.exoplatform.model.GadgetInfo;
import org.exoplatform.model.DashboardItem;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.ui.DashboardActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import android.widget.ListView;

public class DashboardController {

  private DashboardActivity activity;

  private DashboardLoadTask mLoadTask;

  private ListView          listView;
  
  private String dashboardsCannotBeRetrieved = "";

  public DashboardController(DashboardActivity context, ListView list) {
    activity = context;
    listView = list;
  }

  public void onLoad() {

    if (mLoadTask == null || mLoadTask.getStatus() == DashboardLoadTask.Status.FINISHED) {
      mLoadTask = (DashboardLoadTask) new DashboardLoadTask(activity, this).execute();
    }
  }

  public void onCancelLoad() {
    if (mLoadTask != null && mLoadTask.getStatus() == DashboardLoadTask.Status.RUNNING) {
      mLoadTask.onCancelled();
      mLoadTask.cancel(true);
      mLoadTask = null;
    }
  }

  public void setAdapter(ArrayList<GadgetInfo> result) {
    listView.setAdapter(new DashboardItemAdapter(activity, result));
  }

  // Get Dashboards
  public ArrayList<DashboardItem> getDashboards() {

    AccountSetting acc = AccountSetting.getInstance();
    String urlForDahboards = acc.getDomainName() + ExoConstants.DASHBOARD_PATH;
    String result = ExoConnectionUtils.convertStreamToString(ExoConnectionUtils.sendRequestWithAuthorization(urlForDahboards));
    ArrayList<DashboardItem> dashboards = new ArrayList<DashboardItem>();

    JSONArray array = (JSONArray) JSONValue.parse(result);

    for (Object obj : array) {
      JSONObject json = (JSONObject) obj;
      String id = "";
      String html = "";
      String link = "";
      String label = "";
      if (json.get("id") != null)
        id = json.get("id").toString();
      if (json.get("html") != null)
        html = json.get("html").toString();
      if (json.get("link") != null)
        link = json.get("link").toString();
      if (json.get("label") != null)
        label = json.get("label").toString();

      DashboardItem dashboardItem = new DashboardItem(id, html, link, label);

      dashboards.add(dashboardItem);
    }

    return dashboards;

  }

  // Get Gadget in Dashboard
  public ArrayList<GadgetInfo> getGadgetInTab(String tabName, String url) {

    try {
      
      String result = ExoConnectionUtils.convertStreamToString(ExoConnectionUtils.sendRequestWithAuthorization(url));
      ArrayList<GadgetInfo> gadgets = new ArrayList<GadgetInfo>();

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

        GadgetInfo gadget = new GadgetInfo(gadgetName, gadgetDescription,
                                           gadgetUrl, gadgetIcon, null, count);

        gadgets.add(gadget);

        count++;
      }

      return gadgets;
      
    } catch (Exception e) {

      if(dashboardsCannotBeRetrieved.length() == 0)
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
