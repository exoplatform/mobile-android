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
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

import org.exoplatform.ui.HomeActivity;
import org.exoplatform.ui.login.LoginProxy;

/**
 * Created by The eXo Platform SAS
 * Author : Philippe Aristote
 *          paristote@exoplatform.com
 * Sep 3, 2014  
 */
public class AccountSwitcherFragment extends DialogFragment implements LoginProxy.ProxyListener {
  
  /**
   * Fragment is opened in an activity
   */
  public static final int MODE_ACTIVITY = 0;
  /**
   * Fragment is opened in a dialog
   */
  public static final int MODE_DIALOG   = 1;
  /**
   * Whether this fragment is displayed in an activity (MODE_ACTIVITY) or a dialog (MODE_DIALOG) 
   */
  private int mOpenMode;
  /**
   * ListView that contains account items
   */
  private ListView mAccountListView;
  /**
   * TextView that displays the screen title, similar to an Action Bar
   */
  private TextView mAccountSwitcherTitle;
  /**
   * Listener that is called when an account item is tapped
   */
  private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
      if (AccountSetting.getInstance().getDomainIndex().equals(String.valueOf(position))) {
        // If the current account was selected, return to the Home screen
        dismissFragment();
      }
      else {
        // Otherwise, get the account at the given position and start the switching operation
        ExoAccount account = ServerSettingHelper.getInstance().getServerInfoList(getActivity()).get(position);
        if (account != null) switchToAccount(account);
      }
    }
  };
  
  public static final String DIALOG_TAG = "account_switcher_fragment_dialog";
  public static final String TAG = "eXo____AccountSwitcherFragment____";
  
  public static AccountSwitcherFragment newInstance(int mode) {
    AccountSwitcherFragment fragment = new AccountSwitcherFragment();
    if (mode == MODE_ACTIVITY || mode == MODE_DIALOG)
      fragment.mOpenMode = mode;
    return fragment;
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
    mAccountListView.setOnItemClickListener(mOnItemClickListener);

    return layout;
  }
  
  /**
   * Returns to the Home activity when the currently connected account is selected by the user.
   */
  private void dismissFragment() {
    switch (mOpenMode) {
    case MODE_ACTIVITY:
      dismiss();
      if (getActivity()!=null) 
        getActivity().finish();
      break;
    case MODE_DIALOG:
      dismiss();
      break;
    }
  }
  
  /**
   * Signs out the current account and sign in with the given account.<br/>
   * If the password is unknown, a dialog will open to let the user type it.
   * @param account The account to switch to.
   */
  private void switchToAccount(ExoAccount account) {
    // check if we have all the information to sign-in in the selected account
    if (account.serverUrl != null && !"".equals(account.serverUrl) &&
        account.username != null  && !"".equals(account.username)) {
      Log.i(TAG, "Switching to account "+account.accountName);
      if (account.password != null && !"".equals(account.password)) {
        // Logout action is done automatically in LoginTask.preExecute
        Bundle params = new Bundle();
        params.putString(LoginProxy.USERNAME, account.username);
        params.putString(LoginProxy.PASSWORD, account.password);
        params.putString(LoginProxy.DOMAIN,   account.serverUrl);
        params.putString(LoginProxy.ACCOUNT_NAME, account.accountName);
        LoginProxy login = new LoginProxy(getActivity(), LoginProxy.SWITCH_ACCOUNT, params);
        login.setListener(this);
        login.performLogin();
      } else {
        // TODO ask credentials
      }
    }
  }
  
  @Override
  public void onLoginFinished(boolean result) {
    if (result) {
      // Login successful, redirect to Home screen
      Intent home = new Intent(getActivity(), HomeActivity.class);
      home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(home);
    }
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
