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

import greendroid.app.GDListActivity;
import greendroid.widget.ItemAdapter;
import greendroid.widget.item.DescriptionItem;
import greendroid.widget.item.DrawableItem;
import greendroid.widget.item.Item;
import greendroid.widget.item.SeparatorItem;
import greendroid.widget.item.TextItem;
import greendroid.widget.item.ThumbnailItem;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.Window;
import android.webkit.CookieSyncManager;

public class eXoDashboard extends GDListActivity {
    
  public static eXoDashboard eXoDashboardInstance;
  List<GateInDbItem> arrGadgets;
    
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        eXoDashboardInstance = this;
        
        CookieSyncManager.createInstance(this);
        
        arrGadgets = eXoApplicationsController2.eXoApplicationsController2Instance.arrGadgets;
        List<Item> items = new ArrayList<Item>();
        
        for(int i = 0; i < arrGadgets.size(); i++)
        {
          GateInDbItem gadgetTab = arrGadgets.get(i);
          items.add(new SeparatorItem(gadgetTab._strDbItemName));
          for(int j = 0; j < gadgetTab._arrGadgetsInItem.size(); j++)
          {
            eXoGadget gadget = gadgetTab._arrGadgetsInItem.get(j);
            items.add(new ThumbnailItem(gadget._strGadgetName, gadget._strGadgetDescription, gadget._btmGadgetIcon));
          }
           
        }
        
        final ItemAdapter adapter = new ItemAdapter(this, items);
        setListAdapter(adapter);
    }
        

    public static void finishMe() {
      eXoDashboardInstance.finish();
      eXoDashboardInstance = null;
    }
}
