package org.exoplatform.controller.dashboard;

//import greendroid.util.Config;
//import greendroid.widget.LoaderActionBarItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.exoplatform.R;
import org.exoplatform.model.DashboardItem;
import org.exoplatform.model.GadgetInfo;
import org.exoplatform.model.SocialActivityInfo;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.ui.DashboardActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.exoplatform.widget.ConnTimeOutDialog;
import org.exoplatform.widget.WarningDialog;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;


public class DashboardLoadTask extends AsyncTask<Void, Void, Integer> {


  //private DashboardController      dashboardController;
  //private DashboardActivity        dashboardActivity;
  //private String                   okString;
  //private String                   titleString;
  //private String                   contentString;
  //private LoaderActionBarItem      loaderItem;

  private ArrayList<DashboardItem> mDashboardList;
  
  private ArrayList<GadgetInfo>    mGadgetInfoList = new ArrayList<GadgetInfo>();

  private String mDashboardError = "";

  private AsyncTaskListener mListener;

  //public DashboardLoadTask(DashboardActivity context) {
      //, LoaderActionBarItem loader) {

  public DashboardLoadTask() {

    //dashboardActivity = context;
    //dashboardController = new DashboardController();
    //loaderItem = loader;
    //changeLanguage();
  }

  @Override
  public void onPreExecute() {
    //loaderItem.setLoading(true);
  }

  @Override
  public Integer doInBackground(Void... params) {
    HttpResponse response;
    String dashboardUrl = AccountSetting.getInstance().getDomainName() + ExoConstants.DASHBOARD_PATH;

    try {
      /**
       * Checking the session status each time we retrieve dashboard item list.
       * If time out, re logging in
       */
      if (ExoConnectionUtils.checkTimeout(dashboardUrl) != ExoConnectionUtils.LOGIN_SUSCESS)
        return DashboardActivity.RESULT_TIMEOUT;

      mGadgetInfoList.clear();
      response = ExoConnectionUtils.getRequestResponse(dashboardUrl);
      mDashboardList = getDashboards(response);
      for (int i = 0; i < mDashboardList.size(); i++) {
        DashboardItem gadgetTab = mDashboardList.get(i);
        try {
      	  HttpResponse response2 = ExoConnectionUtils.getRequestResponse(gadgetTab.link);
          List<GadgetInfo> gadgets = getGadgetInTab(response2, gadgetTab.label, gadgetTab.link);
          if (gadgets != null && gadgets.size() > 0) {
            mGadgetInfoList.add(new GadgetInfo(gadgetTab.label));
            mGadgetInfoList.addAll(gadgets);
          }
        } catch (IOException e) {
          //if (Config.GD_ERROR_LOGS_ENABLED)
          Log.e("DashboardLoadTask", e.getMessage());
        }
      }
      return DashboardActivity.RESULT_OK;
    } catch (IOException e) {
      return DashboardActivity.RESULT_ERROR;
    }
  }


  public ArrayList<DashboardItem> getDashboards(HttpResponse response) {
    InputStream input = ExoConnectionUtils.sendRequest(response);
    ArrayList<DashboardItem> dashboards = new ArrayList<DashboardItem>();
    if (input != null) {

      String result = ExoConnectionUtils.convertStreamToString(input);
      JSONArray array = (JSONArray) JSONValue.parse(result);

      for (Object obj : array) {
        JSONObject json = (JSONObject) obj;
        String id    = json.get("id")    != null ? json.get("id").toString().trim()    : "";
        String html  = json.get("html")  != null ? json.get("html").toString().trim()  : "";
        String link  = json.get("link")  != null ? json.get("link").toString().trim()  : "";
        String label = json.get("label") != null ? json.get("label").toString().trim() : "";
        //if (json.get("id") != null) id = json.get("id").toString().trim();
        //if (json.get("html") != null) html = json.get("html").toString().trim();
        // if (json.get("link") != null) link = json.get("link").toString().trim();
        // if (json.get("label") != null) label = json.get("label").toString().trim();
        dashboards.add(new DashboardItem(id, html, link, label));
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

          String gadgetIcon = json.get("gadgetIcon") != null ? json.get("gadgetIcon").toString() : "";
          String gadgetUrl  = json.get("gadgetUrl") != null ? json.get("gadgetUrl").toString() : "";
          String gadgetName = json.get("gadgetName") != null ? json.get("gadgetName").toString() : "";
          String gadgetDes  = json.get("gadgetDescription") != null ? json.get("gadgetDescription").toString() : "";

          // if (json.get("gadgetIcon") != null) gadgetIcon = json.get("gadgetIcon").toString();
          // if (json.get("gadgetUrl") != null) gadgetUrl = json.get("gadgetUrl").toString();
          //if (json.get("gadgetName") != null) gadgetName = json.get("gadgetName").toString();
          // if (json.get("gadgetDescription") != null) gadgetDescription = json.get("gadgetDescription").toString();
          gadgets.add(new GadgetInfo(gadgetName, gadgetDes, gadgetUrl, gadgetIcon, null, count));
          count++;
        }
      }

      return gadgets;
    } catch (Exception e) {

      if (mDashboardError.length() == 0)
        mDashboardError += tabName;
      else
        mDashboardError += ", " + tabName;

      return null;
    }

  }

  @Override
  protected void onCancelled() {
    //loaderItem.setLoading(false);
  }

  @Override
  public void onPostExecute(Integer result) {

    if (mListener != null) mListener.onLoadingAppsFinished(result, mDashboardList.size(), mGadgetInfoList, mDashboardError);
  }


  /**
  private void changeLanguage() {
    Resources resource = dashboardActivity.getResources();
    okString = resource.getString(R.string.OK);
    titleString = resource.getString(R.string.Warning);
    contentString = resource.getString(R.string.GadgetsCannotBeRetrieved);
  }
   **/

  public void setListener(AsyncTaskListener listener) {
    mListener = listener;
  }

  public interface AsyncTaskListener {

    void onLoadingAppsFinished(int result, int dashboardSize, ArrayList<GadgetInfo> gadgetInfos, String dashboardError);
  }
}
