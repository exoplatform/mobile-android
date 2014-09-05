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
package org.exoplatform.accountswitcher;

import org.exoplatform.R;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Sep 3, 2014  
 */
public class AccountSwitcherActivity extends FragmentActivity {
  
  private static final String TAG = "eXo____AccountSwitcherActivity____";

  public void onCreate(Bundle savedInstanceState) {
    
    super.onCreate(savedInstanceState);
    
    setContentView(R.layout.account_switcher_activity);
    
    getSupportFragmentManager()
     .beginTransaction()
     .add(R.id.fragment_panel, AccountSwitcherFragment.newInstance(AccountSwitcherFragment.MODE_ACTIVITY), AccountSwitcherFragment.DIALOG_TAG)
     .commit();
  }
  
}
