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
package org.exoplatform.ui;

import org.exoplatform.R;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.ui.login.LoginActivity;
import org.exoplatform.ui.login.LoginProxy;
import org.exoplatform.ui.login.LoginWarningDialog;
import org.exoplatform.utils.LaunchUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;

/**
 * Lightweight activity acts as entry point to the application
 */
public class LaunchActivity extends Activity implements LoginProxy.ProxyListener {

    private static final String TAG = "eXo____LaunchActivity____";

    private AccountSetting      mSetting;

    private LoginProxy          mLoginProxy;

    private Resources           mResources;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Crashlytics.start(this);

        new LaunchUtils(this);
        mSetting = AccountSetting.getInstance();
        mResources = getResources();

        redirect();
    }

    public void redirect() {
        /** no account configured - redirect to Welcome screen */
        if (mSetting.getCurrentAccount() == null) {
            Intent next = new Intent(this, WelcomeActivity.class);
            startActivityForResult(next, 0);
            overridePendingTransition(0, 0);
            return;
        }

        /** performs login */
        if (mSetting.isAutoLoginEnabled()) {
            setContentView(R.layout.launch);

            Bundle loginData = new Bundle();
            loginData.putString(LoginProxy.USERNAME, mSetting.getUsername());
            loginData.putString(LoginProxy.PASSWORD, mSetting.getPassword());
            loginData.putString(LoginProxy.DOMAIN, mSetting.getDomainName());
            loginData.putBoolean(LoginProxy.SHOW_PROGRESS, false);
            mLoginProxy = new LoginProxy(this, LoginProxy.WITH_EXISTING_ACCOUNT, loginData);
            mLoginProxy.setListener(this);

            /** if some errors raise up, we'll redirect to login screen */
            mLoginProxy.getWarningDialog()
                       .setTitle(mResources.getString(R.string.LoginWarningMsg))
                       .setButtonText(mResources.getString(R.string.RedirectToLogin))
                       .setViewListener(new LoginWarningDialog.ViewListener() {
                           @Override
                           public void onClickOk(LoginWarningDialog dialog) {
                               /** redirect to login screen */
                               Intent next = new Intent(LaunchActivity.this, LoginActivity.class);
                               startActivity(next);
                               /** don't come back to Launch */
                               finish();
                           }
                       });

            mLoginProxy.performLogin();

            return;
            // new LoginController(this, username, password);
        }

        /** redirect to login screen */
        Intent next = new Intent(this, LoginActivity.class);
        startActivityForResult(next, 0);
        overridePendingTransition(0, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_CANCELED) {
            /** back pressed from child activity */
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
            finish();
        }
    }

    @Override
    public void onLoginFinished(boolean result) {
        if (!result)
            return;

        /** Login ok, to Home screen!! */
        Intent next = new Intent(this, HomeActivity.class);
        // next.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(next, 0);
        overridePendingTransition(0, 0);
    }
}
