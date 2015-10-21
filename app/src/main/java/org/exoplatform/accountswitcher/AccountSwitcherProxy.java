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

import org.exoplatform.model.ExoAccount;
import org.exoplatform.ui.login.LoginProxy;
import org.exoplatform.utils.SettingUtils;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com Sep 10, 2014
 */
public class AccountSwitcherProxy implements LoginProxy.ProxyListener {

    private Context                 mContext;

    private AccountSwitcherListener mListener;

    private boolean                 mIgnoreRememberMe = false;

    public static final String      TAG               = "eXo____AccountSwitcherController____";

    public AccountSwitcherProxy(Context c, AccountSwitcherListener l, boolean ignore) {
        mContext = c;
        mListener = l;
        mIgnoreRememberMe = ignore;
    }

    /**
     * Sign out the current account and sign in with the given account.<br/>
     * If the password is unknown, a dialog will open to let the user type it.
     * 
     * @param account the ExoAccount to switch to
     * @return true if the procedure has started, false if password is missing
     */
    public void switchToAccount(ExoAccount account) {
        if (account == null || account.serverUrl == null || account.serverUrl.isEmpty()) {
            // Invalid account object
            if (mListener != null)
                mListener.onAccountInvalid(account);
        } else if (account.username == null || account.username.isEmpty()
                || account.password == null || account.password.isEmpty()
                || (!mIgnoreRememberMe && !account.isRememberEnabled)) {
            // Credentials are missing or remember me is off, we inform the
            // fragment that is listening
            if (mListener != null)
                mListener.onMissingPassword(account);
        } else {
            Log.i(TAG, "Switching to account " + account.accountName);

            // We have all information to sign-in to the selected account
            Bundle params = new Bundle();
            params.putString(LoginProxy.USERNAME, account.username);
            params.putString(LoginProxy.PASSWORD, account.password);
            params.putString(LoginProxy.DOMAIN, account.serverUrl);
            params.putString(LoginProxy.ACCOUNT_NAME, account.accountName);
            // Logout is done automatically in LoginTask.preExecute started by
            // LoginProxy. Therefore we don't need to logout here, but we must
            // disable Auto Login.
            // Jul 21, 2015 should use application context here?
            SettingUtils.disableAutoLogin(mContext);
            LoginProxy login = new LoginProxy(mContext, LoginProxy.SWITCH_ACCOUNT, params);
            login.setListener(this);
            login.performLogin();
        }
    }

    /**
     * @inheritDoc
     */
    @Override
    public void onLoginFinished(boolean result) {
        if (mListener != null)
            mListener.onSwitchAccountFinished(result);
    }

    /**
     * An object that is listening to events sent by the Account Switcher Proxy.
     * 
     * @author paristote
     */
    public interface AccountSwitcherListener {
        /**
         * Called when the switch operation is finished, successful or not.
         * 
         * @param result true if switching is successful, false otherwise
         */
        public void onSwitchAccountFinished(boolean result);

        /**
         * Called when the switcher proxy detected that the password was missing
         * for the account
         * 
         * @param account the account whose password is missing
         */
        public void onMissingPassword(ExoAccount account);

        /**
         * Called when the account doesn't contain a URL.
         * 
         * @param account the invalid ExoAccount
         */
        public void onAccountInvalid(ExoAccount account);
    }

}
