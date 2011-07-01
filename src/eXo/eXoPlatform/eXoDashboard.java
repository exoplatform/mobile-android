/*
 * Copyright (C) 2010 Cyril Mottier (http://www.cyrilmottier.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eXo.eXoPlatform;

import greendroid.app.GDActivity;
import greendroid.app.GDListActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.item.Item;
import greendroid.widget.item.SeparatorItem;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import com.cyrilmottier.android.greendroid.R;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

public class eXoDashboard extends MyListActivity {

  public static eXoDashboard       eXoDashboardInstance;

  public static List<GateInDbItem> arrGadgets;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);

    eXoDashboardInstance = this;

    setTitle("Dashboard");
    
    getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);

    CookieSyncManager.createInstance(this);

    arrGadgets = eXoApplicationsController2.eXoApplicationsController2Instance.arrGadgets;
    List<Item> items = new ArrayList<Item>();

    for (int i = 0; i < arrGadgets.size(); i++) {
      GateInDbItem gadgetTab = arrGadgets.get(i);

      items.add(new SeparatorItem(gadgetTab._strDbItemName));
      for (int j = 0; j < gadgetTab._arrGadgetsInItem.size(); j++) {
        eXoGadget gadget = gadgetTab._arrGadgetsInItem.get(j);
//        items.add(new ThumbnailItem(gadget._strGadgetName,
//                                    gadget._strGadgetDescription,
//                                    gadget._btmGadgetIcon));
      }

    }

//    final ItemAdapter adapter = new ItemAdapter(this, items);
//
//    setListAdapter(adapter);
  }


  @Override
  public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
    switch (position) {

    case -1:
      finish();
      break;
    case 0:

      break;

    default:

    }
    return true;

  }

  
  // Key down listener
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    // Save data to the server once the user hits the back button
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      // Toast.makeText(AppController.this, strCannotBackToPreviousPage,
      // Toast.LENGTH_LONG).show();

    }

    return false;
  }

  public void showGadget(eXoGadget gadget) {

    eXoApplicationsController2.webViewMode = 0;
    DefaultHttpClient client = new DefaultHttpClient();

    HttpGet get = new HttpGet(gadget._strGadgetUrl);
    try {
      HttpResponse response = client.execute(get);
      int status = response.getStatusLine().getStatusCode();
      if (status < 200 || status >= 300) {
        Toast.makeText(this, "Connection timed out", Toast.LENGTH_LONG).show();
        return;
      }
    } catch (Exception e) {

      return;
    }

    eXoWebViewController._titlebar = gadget._strGadgetName;
    eXoWebViewController._url = gadget._strGadgetUrl;

    Intent next = new Intent(this, eXoWebViewController.class);
    startActivity(next);

  }

  public void finishMe() {

    Intent next = new Intent(eXoDashboard.this, eXoApplicationsController2.class);
    startActivity(next);

    eXoDashboardInstance = null;
//    GDActivity.TYPE = 0;
  }

}
