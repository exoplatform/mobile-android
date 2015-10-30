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
import org.exoplatform.accountswitcher.AccountSwitcherProxy.AccountSwitcherListener;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.singleton.ServerSettingHelper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com Sep 3, 2014
 */
public class AccountSwitcherFragment extends Fragment implements AccountSwitcherListener {

  /**
   * ListView that contains account items
   */
  private ListView            mAccountListView;

  /**
   * Listener that is called when an account item is tapped
   */
  private OnItemClickListener mOnItemClickListener;

  public static final String  FRAGMENT_TAG = "account_switcher_fragment_dialog";

  public static final String  TAG          = "eXo____AccountSwitcherFragment____";

  public AccountSwitcherFragment() {
    mOnItemClickListener = new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (AccountSetting.getInstance().getDomainIndex().equals(String.valueOf(position))) {
          // If the current account was selected, return to the Home screen
          dismissFragment();
        } else {
          // Otherwise, get the account at the given position and start the
          // switching operation
          ExoAccount account = ServerSettingHelper.getInstance().getServerInfoList(getActivity()).get(position);
          if (account != null)
            switchToAccount(account);
        }
      }
    };
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

    View layout = inflater.inflate(R.layout.account_switcher_fragment, container, false);

    mAccountListView = (ListView) layout.findViewById(R.id.account_list_view);
    AccountListAdapter accountsAdapter = new AccountListAdapter(getActivity());
    mAccountListView.setAdapter(accountsAdapter);
    mAccountListView.setOnItemClickListener(mOnItemClickListener);

    return layout;
  }

  /**
   * Returns to the Home activity when the currently connected account is
   * selected by the user.
   */
  void dismissFragment() {
    getActivity().finish();
  }

  /**
   * Launches the switching procedure.<br/>
   * If the controller notifies that the password is missing, open the dialog to
   * ask the user to type it.
   * 
   * @param account
   */
  private void switchToAccount(ExoAccount account) {
    AccountSwitcherProxy controller = new AccountSwitcherProxy(getActivity(), this, false);
    controller.switchToAccount(account);
  }

  /**
   * Open the SignIn fragment to let the user type his password and sign in
   * again.
   * 
   * @param account
   */
  private void openSignInFragment(ExoAccount account) {

    Log.i(TAG, "Open the sign in form to get username and password of the account " + account.accountName);

    SignInFragment signInFragment = new SignInFragment(account);

    // TODO handle animation on returning from signin fragment to this fragment
    getActivity().getSupportFragmentManager()
                 .beginTransaction()
                 .setCustomAnimations(R.anim.fragment_enter_rtl, R.anim.fragment_exit_rtl)
                 .replace(R.id.share_extension_fragment, signInFragment, SignInFragment.FRAGMENT_TAG)
                 .addToBackStack(FRAGMENT_TAG)
                 .commit();
  }

  @Override
  public void onSwitchAccountFinished(boolean result) {
    // TODO surround with try..catch in case of ClassCastExc
    AccountSwitcherActivity parentActivity = (AccountSwitcherActivity) getActivity();
    if (result)
      // Login successful
      parentActivity.redirectToHomeScreenAndFinish();
    else
      // Login failed
      parentActivity.redirectToLoginScreenAndFinish();
  }

  @Override
  public void onMissingPassword(ExoAccount account) {
    // password is unknown, we must ask the user via the SignIn fragment
    openSignInFragment(account);
  }

  @Override
  public void onAccountInvalid(ExoAccount account) {
    /*
     * TODO possible actions: - open the account edition activity for this
     * account to enter a URL - show a message to the user - close the account
     * switcher
     */
  }

}
