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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.R;
import org.exoplatform.model.ExoAccount;
import org.exoplatform.singleton.ServerSettingHelper;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * Created by The eXo Platform SAS
 * 
 * @author Philippe Aristote paristote@exoplatform.com
 * @since Jun 9, 2015
 */
public class AccountsFragment extends ListFragment implements SimpleAdapter.ViewBinder {

  public static final String      ACCOUNTS_FRAGMENT = "accounts_fragment";

  private static AccountsFragment instance;

  List<ExoAccount>                accounts;

  private AccountsFragment() {
    accounts = ServerSettingHelper.getInstance().getServerInfoList(getActivity());
  }

  public static AccountsFragment getFragment() {
    if (instance == null) {
      instance = new AccountsFragment();
    }
    return instance;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    ArrayList<Map<String, ExoAccount>> data = new ArrayList<Map<String, ExoAccount>>(accounts.size());
    for (ExoAccount account : accounts) {
      Map<String, ExoAccount> map = new HashMap<String, ExoAccount>();
      map.put("ACCOUNT_DATA", account);
      data.add(map);
    }
    String[] from = { "ACCOUNT_DATA" };
    int[] to = new int[] { R.id.share_account_item_layout };
    SimpleAdapter adapter = new SimpleAdapter(activity, data, R.layout.share_extension_account_item, from, to);
    adapter.setViewBinder(this);
    setListAdapter(adapter);
  }

  @Override
  public boolean setViewValue(View view, Object data, String textRepresentation) {
    ExoAccount acc = (ExoAccount) data;
    TextView name = (TextView) view.findViewById(R.id.share_account_item_name);
    name.setText(acc.accountName);
    TextView username = (TextView) view.findViewById(R.id.share_account_item_username);
    username.setText(acc.username + " ");
    TextView server = (TextView) view.findViewById(R.id.share_account_item_server_url);
    server.setText(acc.serverUrl);
    ImageView icon = (ImageView) view.findViewById(R.id.share_account_item_icon);
    if (getShareActivity().getPostInfo().ownerAccount.equals(acc)) {
      Picasso.with(getActivity()).load(R.drawable.icon_check_circle_grey).into(icon);
    } else {
      Picasso.with(getActivity()).load(R.drawable.empty_drawable).into(icon);
    }
    return true;
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    if (accounts != null && position >= 0 && position < accounts.size()) {
      ExoAccount acc = accounts.get(position);
      if (acc.isRememberEnabled && acc.isAutoLoginEnabled && acc.password != null && !"".equals(acc.password)) {
        getShareActivity().onAccountSelected(acc);
        getShareActivity().openFragment(ComposeFragment.getFragment(),
                                        ComposeFragment.COMPOSE_FRAGMENT,
                                        ShareActivity.Anim.FROM_LEFT);
      } else {
        getShareActivity().getPostInfo().ownerAccount = acc;
        SignInFragment signIn = SignInFragment.getFragment();
        getShareActivity().openFragment(signIn, SignInFragment.SIGN_IN_FRAGMENT, ShareActivity.Anim.FROM_RIGHT);
      }
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.share_extension_accounts_fragment, container, false);
    return layout;

  }

  @Override
  public void onResume() {
    getShareActivity().toggleMainButtonType(ShareActivity.BUTTON_TYPE_INVISIBLE);
    super.onResume();
  }

  @Override
  public void onDestroy() {
    Log.d(ACCOUNTS_FRAGMENT, "Destroyed " + this);
    super.onDestroy();
  }

  /*
   * GETTERS & SETTERS
   */

  public ShareActivity getShareActivity() {
    if (getActivity() instanceof ShareActivity) {
      return (ShareActivity) getActivity();
    } else {
      throw new RuntimeException("This fragment is only valid in the activity org.exoplatform.shareextension.ShareActivity");
    }
  }

  public void setAccountList(List<ExoAccount> list) {
    accounts = list;
  }

}
