package org.exoplatform.controller.dashboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.exoplatform.model.DashBoardItem;
import org.exoplatform.model.GadgetInfo;
import org.exoplatform.model.GateInDbItem;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.ui.DashboardActivity;
import org.exoplatform.utils.ExoConnectionUtils;

import android.graphics.Bitmap;

public class DashboardController {

  private  String             _strContentForStandaloneURL;

  private DashboardActivity activity;

  private DashboardLoadTask mLoadTask;

  public DashboardController(DashboardActivity context) {
    activity = context;
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

  public void setAdapter(ArrayList<DashBoardItem> result) {
    activity.setListAdapter(new DashboardItemAdapter(activity, result));
  }
  
  

  // Get gadget list
  public List<GadgetInfo> getGadgetsList() {
    List<GadgetInfo> arrGadgets = new ArrayList<GadgetInfo>();
    String _strDomain = AccountSetting.getInstance().getDomainName();
    String strHomeUrl = _strDomain + "/portal/private/classic";
    String strContent = ExoConnectionUtils.sendRequestAndReturnString(strHomeUrl);

    String strGadgetMark = "eXo.gadget.UIGadget.createGadget";
    String title;
    String url;
    String description;
    int indexStart;
    int indexEnd;
    String tmpStr = strContent;
    indexStart = tmpStr.indexOf(strGadgetMark);

    while (indexStart >= 0) {
      tmpStr = tmpStr.substring(indexStart + 1);
      indexEnd = tmpStr.indexOf(strGadgetMark);
      String tmpStr2;

      if (indexEnd < 0)
        tmpStr2 = tmpStr;
      else
        tmpStr2 = tmpStr.substring(0, indexEnd);

      // Get title
      title = parseUrl(tmpStr2, "\"title\":\"", true, "\"");

      // Get description
      description = parseUrl(tmpStr2, "\"description\":\"", true, "\"");

      // Get url
      url = _strDomain + "/eXoGadgetServer/gadgets/ifr?container=default&mid=0&nocache=0";

      // Get country
      url += parseUrl(tmpStr2, "&country=", false, "&");

      // Get view
      url += parseUrl(tmpStr2, "&view=", false, "&");

      // Get language
      url += parseUrl(tmpStr2, "&lang=", false, "&");

      url += "&parent=" + _strDomain + "&st=";

      // Get token
      url += parseUrl(tmpStr2, "default:", false, "\"");

      // Get xml url
      url += parseUrl(tmpStr2, "&url=", false, "\"");

      // Get bitmap
      String bmpUrl = parseUrl(tmpStr2, "\"thumbnail\":\"", true, "\"");
      bmpUrl = bmpUrl.replace("localhost", _strDomain);

      GadgetInfo tempGadget = new GadgetInfo(title, description, url, bmpUrl, null, null);
      arrGadgets.add(tempGadget);

      indexStart = indexEnd;

    }

    return arrGadgets;
  }

  // Parser gadget string data
  private String getStringForGadget(String gadgetStr, String startStr, String endStr) {
    String returnValue = "";
    int index1;
    int index2;

    index1 = gadgetStr.indexOf(startStr);

    if (index1 > 0) {
      String tmpStr = gadgetStr.substring(index1 + startStr.length());
      index2 = tmpStr.indexOf(endStr);
      if (index2 > 0)
        returnValue = tmpStr.substring(0, index2);
    }

    return returnValue;
  }

  // Get gadget list with URL
  private List<GadgetInfo> listOfGadgetsWithURL(String url) {
    List<GadgetInfo> arrTmpGadgets = new ArrayList<GadgetInfo>();

    String strGadgetName;
    String strGadgetDescription;
    Bitmap imgGadgetIcon = null;

    AccountSetting acc = AccountSetting.getInstance();
    String domain = acc.getDomainName();
    String userName = acc.getUsername();
    String password = acc.getPassword();

    String strContent = "";

    int indexOfSocial = domain.indexOf("social");
    if (indexOfSocial > 0) {
      // dataReply = [[_delegate getConnection]
      // sendRequestToSocialToGetGadget:[url absoluteString]];
    } else {
      strContent = ExoConnectionUtils.sendRequestToGetGadget(url, userName, password);
    }

    _strContentForStandaloneURL = new String(strContent);

    int index1;
    int index2;

    index1 = strContent.indexOf("eXo.gadget.UIGadget.createGadget");

    do {
      if (index1 < 0)
        return null;
      strContent = strContent.substring(index1 + 32);
      index2 = strContent.indexOf("'/eXoGadgetServer/gadgets',");
      if (index2 < 0)
        return null;
      String tmpStr = strContent.substring(0, index2 + 45);

      strGadgetName = getStringForGadget(tmpStr, "\"title\":\"", "\",");
      strGadgetDescription = getStringForGadget(tmpStr, "\"description\":\"", "\",");
      String gadgetIconUrl = getStringForGadget(tmpStr, "\"thumbnail\":\"", "\",");
      String gadgetID = getStringForGadget(tmpStr, "'content-", "'");

      gadgetIconUrl = gadgetIconUrl.replace("http://localhost:8080", domain);

      try {
        if (imgGadgetIcon == null) {
          try {
          } catch (Exception e2) {

            imgGadgetIcon = null;
          }
        }

      } catch (Exception e) {

      }

      String gadgetUrl = domain;

      gadgetUrl += getStringForGadget(tmpStr, "'home', '", "',") + "/";
      gadgetUrl += "ifr?container=default&mid=1&nocache=0&lang="
          + getStringForGadget(tmpStr, "&lang=", "\",") + "&debug=1&st=default";

      String token = ":" + getStringForGadget(tmpStr, "\"default:", "\",");
      token = token.replace(":", "%3A");
      token = token.replace("/", "%2F");
      token = token.replace("+", "%2B");

      gadgetUrl += token + "&url=";

      String gadgetXmlFile = getStringForGadget(tmpStr, "\"url\":\"", "\",");
      gadgetXmlFile = gadgetXmlFile.replace(":", "%3A");
      gadgetXmlFile = gadgetXmlFile.replace("/", "%2F");

      gadgetUrl += gadgetXmlFile;

      GadgetInfo gadget = new GadgetInfo(strGadgetName,
                                       strGadgetDescription,
                                       gadgetUrl,
                                       gadgetIconUrl,
                                       null,
                                       gadgetID);

      arrTmpGadgets.add(gadget);

      strContent = strContent.substring(index2 + 35);
      index1 = strContent.indexOf("eXo.gadget.UIGadget.createGadget");

    } while (index1 > 0);

    return arrTmpGadgets;
  }

  // Get gadget tab list
  public ArrayList<GateInDbItem> listOfGadgets() {
    try {

      AccountSetting acc = AccountSetting.getInstance();
      String _strDomain = acc.getDomainName();

      ArrayList<GateInDbItem> gadgetList = new ArrayList<GateInDbItem>();

      String strContent = ExoConnectionUtils.getFirstLoginContent();
      if (strContent != null) {

        int index1;
        index1 = strContent.indexOf("DashboardIcon TBIcon");

        if (index1 < 0)
          return null;

        strContent = strContent.substring(index1 + 20);
        index1 = strContent.indexOf("TBIcon");
        if (index1 < 0)
          return null;

        strContent = strContent.substring(0, index1);

        String firstChunkForGadget = "<a class=\"ItemIcon DefaultPageIcon\" href=\"";
        String commaChar = "\"";
        String supChar = ">";
        String lastChunkForGadget = "</a>";

        do {

          // Search for each '<a class="ItemIcon DefaultPageIcon"' in the HTML
          int indexStartOfNextGadget = strContent.indexOf(firstChunkForGadget);

          if (indexStartOfNextGadget > 0) {
            // Remove the ***...<a class="ItemIcon DefaultPageIcon" from the
            // string
            String stringCutted = strContent.substring(indexStartOfNextGadget
                + firstChunkForGadget.length());

            // Search for the next '"' in the string
            int indexForComma = stringCutted.indexOf(commaChar);

            // Get the URL string
            String gadgetTabUrlStr = stringCutted.substring(0, indexForComma);

            // Search for the next '>' in the string
            int indexForSup = stringCutted.indexOf(supChar);

            // Remove the '>' from stringCutted
            stringCutted = stringCutted.substring(indexForSup + supChar.length());

            // Search for the '</a>'
            int indexForDashboardNameEnd = stringCutted.indexOf(lastChunkForGadget);

            // Get the TabName
            String gadgetTabName = stringCutted.substring(0, indexForDashboardNameEnd);

            List<GadgetInfo> arrTmpGadgetsInItem = listOfGadgetsWithURL(_strDomain + gadgetTabUrlStr);

            HashMap<String, String> mapOfURLs = listOfStandaloneGadgetsURL();

            if (arrTmpGadgetsInItem != null) {
              for (int i = 0; i < arrTmpGadgetsInItem.size(); i++) {
                GadgetInfo tmpGadget = arrTmpGadgetsInItem.get(i);

                String urlStandalone = mapOfURLs.get(tmpGadget._strGadgetID);

                if (urlStandalone != null) {
                  tmpGadget._strGadgetUrl = urlStandalone;
                }
              }

              GateInDbItem tmpGateInDbItem = new GateInDbItem(gadgetTabName,
                                                              gadgetTabUrlStr,
                                                              arrTmpGadgetsInItem);
              // arrTmpGadgets.add(tmpGateInDbItem);
              gadgetList.add(tmpGateInDbItem);

              // Prepare for the next iteration
              // Remove the last information about the current gadget
              String toRemoveFromContent = gadgetTabName + "</a>";
              int range3 = strContent.indexOf(toRemoveFromContent);
              strContent = strContent.substring(range3 + toRemoveFromContent.length());
              index1 = strContent.indexOf(firstChunkForGadget);

            }

          }

          else {
            // No gadgets so exit from the loop
            index1 = 0;
          }

        } while (index1 > 0);
      }
      return gadgetList;
    } catch (Exception e) {
      return null;
    }

  }

  // Get needed string
  private String parseUrl(String urlStr, String neededStr, boolean offset, String enddedStr) {
    String str;
    int idx = urlStr.indexOf(neededStr);
    String tmp = urlStr.substring(idx + neededStr.length());
    idx = tmp.indexOf(enddedStr);
    if (!offset)
      str = neededStr + tmp.substring(0, idx);
    else
      str = tmp.substring(0, idx);

    return str;
  }

  // Standalone gadgets
  private HashMap<String, String> listOfStandaloneGadgetsURL() {
    HashMap<String, String> mapOfURLs = new HashMap<String, String>();
    String strContent = _strContentForStandaloneURL;

    int index1;
    int index2;

    String[] arrParagraphs = strContent.split("<div class=\"UIGadget\" id=\"");

    for (int i = 1; i < arrParagraphs.length; i++) {
      String tmpStr1 = arrParagraphs[i];

      String idString = tmpStr1.substring(0, 36);

      if (this.isAGadgetIDString(idString)) {

        index1 = tmpStr1.indexOf("standalone");
        if (index1 >= 0) {
          index2 = tmpStr1.indexOf("<a style=\"display:none\" href=\"");
          String strStandaloneUrl = "";
          if (index2 >= 0) {
            int mark = 0;
            for (int j = index2 + 30; j < tmpStr1.length(); j++) {
              if (tmpStr1.charAt(j) == '"') {
                mark = j;
                break;
              }
            }
            strStandaloneUrl = tmpStr1.substring(index2 + 30, mark);
          }

          if (strStandaloneUrl.length() > 0) {
            mapOfURLs.put(idString, strStandaloneUrl);
          }
        }
      }
    }
    return mapOfURLs;
  }

  // Check if it is a standalone gadget
  private boolean isAGadgetIDString(String potentialIDString) {
    if ((potentialIDString.charAt(8) == '-') && (potentialIDString.charAt(13) == '-'))
      return true;
    return false;
  }

}
