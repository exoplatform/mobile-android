/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.mobile.tests;

import static org.fest.assertions.api.ANDROID.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import org.exoplatform.R;
import org.exoplatform.model.ServerObjInfo;
import org.exoplatform.singleton.ServerSettingHelper;
import org.exoplatform.ui.setting.ServerEditionActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

import android.content.Context;
import android.content.Intent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

@RunWith(ExoRobolectricTestRunner.class)
public class ServerEditionActivityTest extends ExoActivityTestUtils<ServerEditionActivity>{
	
	TextView mTitleTxt, mServerName, mServerURL, mUsername, mPassword;
	EditText mServerNameEditTxt, mServerUrlEditTxt, mUserEditTxt, mPassEditTxt;
	Button   mOkBtn, mDeleteBtn;
	
	ServerObjInfo thisServer = null;
	
	@Override
	@Before
	public void setup() {
		controller = Robolectric.buildActivity(ServerEditionActivity.class);
	}
	
	/**
	 * Create the activity with a server passed in extra and configured as the only and default server
	 */
	public void createWithDefaultServer() {
		thisServer = getServerWithDefaultValues();
		Context ctx = Robolectric.application.getApplicationContext();
		setDefaultServerInPreferences(ctx, thisServer);
		Intent i = new Intent(ctx, ServerEditionActivity.class);
		i.putExtra("EXO_SERVER_OBJ", thisServer);
		super.createWithIntent(i);
		init();
	}
	
	private void init() {
		mTitleTxt = (TextView)activity.findViewById(R.id.server_setting_title_txt);
		mServerName = (TextView)activity.findViewById(R.id.server_setting_server_name_txt);
		mServerURL = (TextView)activity.findViewById(R.id.server_setting_server_url_txt);
		mUsername = (TextView)activity.findViewById(R.id.server_setting_user_txt);
		mPassword = (TextView)activity.findViewById(R.id.server_setting_pass_txt);
		mServerNameEditTxt = (EditText)activity.findViewById(R.id.server_setting_server_name_edit_txt);
		mServerUrlEditTxt = (EditText)activity.findViewById(R.id.server_setting_server_url_edit_txt);
		mUserEditTxt = (EditText)activity.findViewById(R.id.server_setting_user_edit_txt);
		mPassEditTxt = (EditText)activity.findViewById(R.id.server_setting_pass_edit_txt);
		mOkBtn = (Button)activity.findViewById(R.id.server_setting_ok_btn);
		mDeleteBtn = (Button)activity.findViewById(R.id.server_setting_delete_btn);
	}
	
	@Test
	public void verifyDefaultLayout() {
		createWithDefaultServer();
		
		assertThat(mTitleTxt).containsText(thisServer.serverName);
		assertThat(mServerName).containsText(R.string.ServerName);
		assertThat(mServerNameEditTxt).containsText(thisServer.serverName);
		assertThat(mServerURL).containsText(R.string.ServerUrl);
		assertThat(mServerUrlEditTxt).containsText(thisServer.serverUrl);
		assertThat(mUsername).containsText(R.string.UserNameCellTitle);
		assertThat(mUsername).hasHint(null); // hint is null because username is set
		assertThat(mUserEditTxt).containsText(thisServer.username);
		assertThat(mPassword).containsText(R.string.PasswordCellTitle);
		assertThat(mPassword).hasHint(null); // hint is null because password is set 
		assertThat(mPassEditTxt).containsText(thisServer.password);
		assertThat(mOkBtn).isVisible().isClickable().containsText(R.string.OK);
		assertThat(mDeleteBtn).isVisible().isClickable().containsText(R.string.Delete);
		
	}
	
	@Test
	public void verifyEditOneAccount() {
		createWithDefaultServer();
		Context ctx = Robolectric.application.getApplicationContext();
		final String newName = TEST_SERVER_NAME+"_new";
		final String newURL  = TEST_SERVER_URL+".fr";
		final String newUser = TEST_USER_NAME+"_new";
		final String newPass = TEST_USER_PWD+"_new";
		
		assertThat("1 server should already exist", ServerSettingHelper.getInstance().getServerInfoList(ctx).size(), equalTo(1));
		
		mServerNameEditTxt.setText(newName);
		mServerUrlEditTxt.setText(newURL);
		mUserEditTxt.setText(newUser);
		mPassEditTxt.setText(newPass);
		
		Robolectric.clickOn(mOkBtn);
		
		assertThat("Only 1 server should exist", ServerSettingHelper.getInstance().getServerInfoList(ctx).size(), equalTo(1));
		
		ServerObjInfo srv = ServerSettingHelper.getInstance().getServerInfoList(ctx).get(0);
		
		assertThat("Account name should have been modified", srv.serverName, equalTo(newName));
		assertThat("Account server should have been modified", srv.serverUrl, equalTo(newURL));
		assertThat("Account username should have been modified", srv.username, equalTo(newUser));
		assertThat("Account password should have been modified", srv.password, equalTo(newPass));
	}
	
	@Test
	public void verifyDeleteOneAccount() {
		createWithDefaultServer();
		Context ctx = Robolectric.application.getApplicationContext();
		
		assertThat("1 server should already exist", ServerSettingHelper.getInstance().getServerInfoList(ctx).size(), equalTo(1));
		
		Robolectric.clickOn(mDeleteBtn);
		
		assertThat("Server should have been deleted", ServerSettingHelper.getInstance().getServerInfoList(ctx).size(), equalTo(0));
	}
	
//	@Test
	public void verifyCreateAndEditFailWithIncorrectAccountInfo() {
		createWithDefaultServer();
		// TODO
	}
	
//	@Test
	public void verifyAccountIsSelectedWhenOnlyOneExists() {
		createWithDefaultServer();
		// TODO
	}
	
}