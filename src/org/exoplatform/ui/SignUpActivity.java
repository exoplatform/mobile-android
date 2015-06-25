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

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.ViewGroup;
import android.widget.TextView;

import org.exoplatform.R;
import org.exoplatform.utils.AssetUtils;

public class SignUpActivity extends FragmentActivity {

  private static final String TAG = "eXoSignUpActivity";

  public void onCreate(Bundle savedInstanceState) {
    if (!WelcomeActivity.mIsTablet) setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

    super.onCreate(savedInstanceState);

    setContentView(R.layout.signup);

    getSupportFragmentManager()
        .beginTransaction()
        .add(R.id.share_extension_fragment, new CreationAccountFragment(this))
        .commit();
  }

  public void flipToGreetingsPanel() {
    getSupportFragmentManager()
        .beginTransaction()
        .setCustomAnimations(
            R.anim.grow_from_middle, R.anim.shrink_to_middle)
        .replace(R.id.share_extension_fragment, new GreetingsFragment())
        .commit();
  }

}