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
import org.exoplatform.base.BaseActivity;
import org.exoplatform.ui.HomeActivity;
import org.exoplatform.ui.login.LoginActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.IntentCompat;

/**
 * Created by The eXo Platform SAS Author : Philippe Aristote
 * paristote@exoplatform.com Sep 3, 2014
 */
public class AccountSwitcherActivity extends BaseActivity {

  private static final String TAG = "eXo____AccountSwitcherActivity____";

  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.account_switcher_activity);

    setTitle(R.string.Server);

    getSupportFragmentManager().beginTransaction()
                               .add(R.id.share_extension_fragment,
                                    new AccountSwitcherFragment(),
                                    AccountSwitcherFragment.FRAGMENT_TAG)
                               .commit();
  }

  /**
   * Redirect to the HomeActivity (Home screen)
   */
  public void redirectToHomeScreenAndFinish() {
    Intent home = new Intent(this, HomeActivity.class);
    // IntentCompat.FLAG_ACTIVITY_CLEAR_TASK works only from API_VERSION>=11
    home.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(home);
    finish();
  }

  /**
   * Redirect to the LoginActivity (Authenticate screen)
   */
  public void redirectToLoginScreenAndFinish() {
    Intent login = new Intent(this, LoginActivity.class);
    // IntentCompat.FLAG_ACTIVITY_CLEAR_TASK works only from API_VERSION>=11
    login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(login);
    finish();
  }

}
