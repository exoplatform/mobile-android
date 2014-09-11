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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com Sep 10, 2014
 */
public class SignInFragment extends Fragment implements AccountSwitcherListener {

    private ExoAccount         mCurrentAccount;

    private TextView           mPassword;

    private final String       EXO_ACCOUNT     = "exo_account";

    private OnClickListener    mSignInListener = new OnClickListener() {
                                                   @Override
                                                   public void onClick(View v) {
                                                       String password = mPassword.getText()
                                                                                  .toString();
                                                       if (!password.isEmpty()) {
                                                           mCurrentAccount.password = password;
                                                           switchToAccount();
                                                       } else {
                                                           Toast.makeText(getActivity(),
                                                                          R.string.NoPasswordEnter,
                                                                          Toast.LENGTH_LONG).show();
                                                       }
                                                   }
                                               };

    public static final String FRAGMENT_TAG    = "account_switcher_signin_fragment";

    public static final String TAG             = "eXo____AccountSwitcherSignInFragment____";

    public SignInFragment() {
        super();
    }

    public SignInFragment(ExoAccount account) {
        mCurrentAccount = account;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey(EXO_ACCOUNT)) {
            mCurrentAccount = (ExoAccount) savedInstanceState.getParcelable(EXO_ACCOUNT);
        }

        View layout = inflater.inflate(R.layout.account_switcher_signin_fragment, container, false);

        Button signInBtn = (Button) layout.findViewById(R.id.account_switcher_signin_btn);
        signInBtn.setOnClickListener(mSignInListener);

        TextView username = (TextView) layout.findViewById(R.id.account_switcher_signin_user_edit_txt);
        username.setText(mCurrentAccount.username);

        mPassword = (TextView) layout.findViewById(R.id.account_switcher_signin_pass_edit_txt);

        return layout;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mCurrentAccount != null)
            outState.putParcelable(EXO_ACCOUNT, mCurrentAccount);
        super.onSaveInstanceState(outState);
    }

    /**
     * Launches the switching procedure.
     */
    private void switchToAccount() {
        AccountSwitcherProxy controller = new AccountSwitcherProxy(getActivity(), this, true);
        controller.switchToAccount(mCurrentAccount);
    }

    @Override
    public void onSwitchAccountFinished(boolean result) {
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
        // nothing to do, the screen should remain the same
    }

    @Override
    public void onAccountInvalid(ExoAccount account) {
        //
    }
}
