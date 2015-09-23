/*
 * Copyright (C) 2003-2015 eXo Platform SAS.
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
package org.exoplatform.shareextension;

import org.exoplatform.R;
import org.exoplatform.model.ExoAccount;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Philippe Aristote paristote@exoplatform.com
 * @since Jun 10, 2015
 */
public class SignInFragment extends Fragment {

  private static SignInFragment instance;

  public static final String    SIGN_IN_FRAGMENT  = "sign_in_fragment";

  private TextView              tvUsername;

  private EditText              etPassword;

  private TextWatcher           passwordValidator = new TextWatcher() {
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
      enableDisableMainButton();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
  };

  private SignInFragment() {
  }

  public static SignInFragment getFragment() {
    if (instance == null) {
      instance = new SignInFragment();
    }
    return instance;
  }

  @Override
  public void onResume() {
    ExoAccount acc = getShareActivity().getPostInfo().ownerAccount;
    tvUsername.setText(acc.username);
    if (acc.isRememberEnabled)
      etPassword.setText(acc.password);
    getShareActivity().toggleMainButtonType(R.attr.share_button_type_signin);
    getShareActivity().getMainButton().setVisibility(View.VISIBLE);
    enableDisableMainButton();
    super.onResume();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.share_extension_sign_in_fragment, container, false);
    tvUsername = (TextView) layout.findViewById(R.id.share_signin_username);
    etPassword = (EditText) layout.findViewById(R.id.share_signin_password);
    etPassword.addTextChangedListener(passwordValidator);

    return layout;
  }

  @Override
  public void onDestroy() {
    Log.d(SIGN_IN_FRAGMENT, "Destroyed " + this);
    super.onDestroy();
  }

  /*
   * GETTERS & SETTERS
   */

  private void enableDisableMainButton() {
    if (isAdded()) {
      boolean passwordEmpty = "".equals(etPassword.getText().toString());
      getShareActivity().enableDisableMainButton(!passwordEmpty);
    }
  }

  public String getPassword() {
    return etPassword.getText().toString();
  }

  public ShareActivity getShareActivity() {
    if (getActivity() instanceof ShareActivity) {
      return (ShareActivity) getActivity();
    } else {
      throw new UnsupportedOperationException(new StringBuilder("This fragment is only valid in the activity")
          .append(ShareActivity.class.getName()).toString());
    }
  }
}
