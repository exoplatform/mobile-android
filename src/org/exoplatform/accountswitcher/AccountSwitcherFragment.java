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

import java.util.Locale;

import org.exoplatform.R;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Sep 3, 2014  
 */
public class AccountSwitcherFragment extends DialogFragment {
  
  private ListView mAccountListView;
  private TextView mAccountSwitcherTitle;
  
  public static final String DIALOG_TAG = "account_switcher_fragment_dialog";
  
  public static AccountSwitcherFragment newInstance() {
    return new AccountSwitcherFragment();
  }

  
  
  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog d = super.onCreateDialog(savedInstanceState);
    d.requestWindowFeature(Window.FEATURE_NO_TITLE); // hides the dialog title bar because it's replaced by mAccountSwitcherTitle
    return d;
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    
    View layout = inflater.inflate(R.layout.account_switcher_fragment, container, false);
    
    mAccountSwitcherTitle = (TextView)layout.findViewById(R.id.account_switcher_fragment_title);
    String title = mAccountSwitcherTitle.getText().toString().toLowerCase(Locale.getDefault());
    title = Character.toUpperCase(title.charAt(0)) + title.substring(1);
    mAccountSwitcherTitle.setText(title);
    
    mAccountListView = (ListView)layout.findViewById(R.id.account_list_view);
    AccountListAdapter accountsAdapter = new AccountListAdapter(getActivity());
    mAccountListView.setAdapter(accountsAdapter);

    return layout;
  }

  @Override
  public void onDestroyView() {
    
    super.onDestroyView();
  }

  @Override
  public void onPause() {
    
    super.onPause();
  }

  @Override
  public void onResume() {
    
    super.onResume();
  }

  
  
}
