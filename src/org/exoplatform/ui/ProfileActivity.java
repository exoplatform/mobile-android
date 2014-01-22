package org.exoplatform.ui;


import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;
import greendroid.widget.LoaderActionBarItem;

import org.exoplatform.R;
import org.exoplatform.controller.dashboard.DashboardLoadTask;
import org.exoplatform.controller.profile.ProfileLoadTask;
import org.exoplatform.controller.profile.UserProfile;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.widget.ConnectionErrorDialog;
import org.exoplatform.widget.MyActionBar;
import org.exoplatform.widget.ShaderImageView;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ProfileActivity extends MyActionBar implements OnTouchListener{
	private static final String TAG = "eXo____ProfileActivity____";
	private static final String CONFIRMED = "confirmed";
	private String                  title;

	LinearLayout homeView;
	LinearLayout btnView;
	RelativeLayout statusView;
	TextView fullName;
	TextView jobTitle;
	TextView connectStatus;
	TextView lastActivityLb;
	TextView lastActivity;
	ShaderImageView avatar;
	TextView e_mail;
	TextView phone;
	TextView skype;
	TextView phoneLb;
	TextView skypeLb;
	Button connectionBnt;
	Button requestConnectBtn;
	Button editBtn;
	String currentUser;
	ProfileLoadTask mLoadTask;
	LoaderActionBarItem loaderItem;
	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setActionBarContentView(R.layout.view_profile_layout);
		getActionBar().setType(greendroid.widget.ActionBar.Type.Normal);
		addActionBarItem(Type.Refresh);
		getActionBar().getItem(0).setDrawable(R.drawable.action_bar_icon_refresh);
		homeView = (LinearLayout) findViewById(R.id.profileView);
		btnView = (LinearLayout) findViewById(R.id.btnLayout);
		statusView = (RelativeLayout) findViewById(R.id.statusLayout);
		homeView.setVisibility(View.GONE);
		fullName = (TextView) findViewById(R.id.displayName);
		jobTitle = (TextView) findViewById(R.id.jobTitle);
		editBtn = (Button) findViewById(R.id.editBtn);
		requestConnectBtn = (Button) findViewById(R.id.connectBtn);
		connectStatus = (TextView) findViewById(R.id.con_status);
		lastActivityLb = (TextView) findViewById(R.id.lastActivityLb);
		connectionBnt = (Button) findViewById(R.id.update_connectBtn);
		lastActivity = (TextView) findViewById(R.id.lastActivity);

		avatar = (ShaderImageView) findViewById(R.id.displayImage);
		avatar.setDefaultImageResource(R.drawable.default_avatar);
		e_mail = (TextView) findViewById(R.id.emailTo);
		e_mail.setOnTouchListener(this);
		phoneLb = (TextView) findViewById(R.id.phoneNum);
		phone = (TextView) findViewById(R.id.phone);
		phone.setOnTouchListener(this);
		skypeLb = (TextView) findViewById(R.id.skypeName);
		skype = (TextView) findViewById(R.id.skypeid);
		skype.setOnTouchListener(this);
		if(getIntent().getExtras() !=null && getIntent().getExtras().get(UserProfile.USER_ID) != null && getIntent().getExtras().get(UserProfile.USER_ID).toString() != null)
		{
			currentUser = getIntent().getExtras().get(UserProfile.USER_ID).toString();
			loaderItem = (LoaderActionBarItem) getActionBar().getItem(0);
			mLoadTask = new ProfileLoadTask(this,  loaderItem);
			mLoadTask.execute(currentUser);
			onLoad(loaderItem);
			//findViewById(R.id.informationLayout).setVisibility(View.VISIBLE);
		} else {
			//findViewById(R.id.informationLayout).setVisibility(View.GONE);
		}
		changeLanguage();
	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (position) {

		case -1:
			finish();
			break;
		case 0:
			loaderItem = (LoaderActionBarItem) item;
			onLoad(loaderItem);

			break;

		default:

		}
		return true;

	}

	@Override
	public void finish() {
		super.finish();
	}

	@Override
	public void onBackPressed() {
		onCancelLoad();
		finish();
	}

	public void onLoad(LoaderActionBarItem loader) {
		if (ExoConnectionUtils.isNetworkAvailableExt(this)) {
			if (mLoadTask == null || mLoadTask.getStatus() == ProfileLoadTask.Status.FINISHED) {
				mLoadTask = (ProfileLoadTask) new ProfileLoadTask(this, loader).execute(currentUser);
			}
		} else {
			new ConnectionErrorDialog(this).show();
		}
	}

	public void onCancelLoad() {
		if (mLoadTask != null && mLoadTask.getStatus() == DashboardLoadTask.Status.RUNNING) {
			mLoadTask.cancel(true);
			mLoadTask = null;
		}
	}
	/*@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(ACCOUNT_SETTING, AccountSetting.getInstance());
	}*/

	public void changeLanguage() {
		Resources resource = getResources();
		title = resource.getString(R.string.Profile);
		editBtn.setText(resource.getString(R.string.editBtn));
		requestConnectBtn.setText(resource.getString(R.string.connectBtn));
		String currentRole = resource.getString(R.string.You);
		if(AccountSetting.getInstance().getUsername().equals(currentUser)) {
			currentRole = currentRole+" "+resource.getString(R.string.lastActivityLb) ;
		} else {
			currentRole = fullName.getText() + " " +resource.getString(R.string.lastActivityLb) ;
		}
		lastActivityLb.setText(currentRole);
		connectStatus.setText(resource.getString(R.string.connectStt));
		connectionBnt.setText(resource.getString(R.string.connectSttLb));
		phoneLb.setText(resource.getString(R.string.Phone));
		skypeLb.setText(resource.getString(R.string.Skype));
		setTitle(title);
	}

	private boolean isCallable() {
		return (((TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE)).getLine1Number()
				== null);
	}
	private void call(String phone) {
		try {
			Intent callIntent = new Intent(Intent.ACTION_CALL);
			callIntent.setData(Uri.parse("tel:"+phone));
			startActivity(callIntent);
		} catch (ActivityNotFoundException activityException) {
			Log.e(TAG, "Call failed", activityException);
		}
	}


	private void voiceCall(Context myContext, String mySkypeUri) {
		if (!isSkypeClientInstalled(myContext)) {
			goToMarket(myContext);
			return;
		}
		Uri skypeUri = Uri.parse(mySkypeUri);
		Intent myIntent = new Intent(Intent.ACTION_VIEW, skypeUri);
		myIntent.setComponent(new ComponentName("com.skype.raider", "com.skype.raider.Main"));
		myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		myContext.startActivity(myIntent);

		return;
	}

	/**
	 * Determine whether the Skype for Android client is installed on this device.
	 **/

	public boolean isSkypeClientInstalled(Context myContext) {
		PackageManager myPackageMgr = myContext.getPackageManager();
		try {
			myPackageMgr.getPackageInfo("com.skype.raider", PackageManager.GET_ACTIVITIES);
		}
		catch (PackageManager.NameNotFoundException e) {
			return (false);
		}
		return (true);
	}
	/**
	 * Install the Skype client through the market: URI scheme.
	 **/
	public void goToMarket(Context myContext) {
		Uri marketUri = Uri.parse("market://details?id=com.skype.raider");
		Intent myIntent = new Intent(Intent.ACTION_VIEW, marketUri);
		myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		myContext.startActivity(myIntent);

		return;
	}
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int source =	v.getId();
		switch (source) {
		case R.id.emailTo:
			Intent email =  new Intent(Intent.ACTION_SEND);
			email.putExtra(Intent.EXTRA_EMAIL, new String[]{e_mail.getText().toString()});	  
			email.putExtra(Intent.EXTRA_SUBJECT, "subject");
			email.putExtra(Intent.EXTRA_TEXT, "message");
			email.setType("message/rfc822");
			startActivity(Intent.createChooser(email, "Choose an Email client :"));
			break;
		case R.id.phone: if(isCallable())
			call(phone.getText().toString());
		break;
		case R.id.skypeid:
			voiceCall(this.getBaseContext(), skype.getText().toString());
			break;	

		default:
			break;
		}
		return false;
	}

	public void setInfor(UserProfile profile){

		if(profile != null){
			fullName.setText(profile.getFullName());
			jobTitle.setText(profile.getJobTitle());
			avatar.setUrl(AccountSetting.getInstance().getDomainName() + profile.getAvatarUrl());
			avatar.setVisibility(View.VISIBLE);
			if(profile.getLastActivity() != null){
				lastActivity.setText(Html.fromHtml(profile.getLastActivity()),TextView.BufferType.SPANNABLE);
				lastActivity.setVisibility(View.VISIBLE);
				lastActivityLb.setVisibility(View.VISIBLE);
			} else {
				lastActivity.setVisibility(View.GONE);
				lastActivityLb.setVisibility(View.GONE);
			}
			if(profile.getEmail() == null) {
				e_mail.setVisibility(View.GONE);  
			} else {
				e_mail.setText(profile.getEmail());
				e_mail.setVisibility(View.VISIBLE);
			}
			/*
			if("NoAction".equals(profile.getConnectionStt())){
				connectStatus.setVisibility(View.GONE);
				connectionBnt.setVisibility(View.GONE);
			} else {
				connectStatus.setVisibility(View.VISIBLE);
				connectionBnt.setVisibility(View.VISIBLE);
			}*/
			if(AccountSetting.getInstance().getUsername().equals(currentUser)) {
				//TODO Currently we disable button because we don't implement action then 
				btnView.setVisibility(View.GONE);
				statusView.setVisibility(View.GONE);
			} else {
				if(CONFIRMED.equals(profile.getConnectionStt())) connectStatus.setText(R.string.connectSttLb);
				else connectStatus.setText(R.string.disconnectSttLb);
				btnView.setVisibility(View.GONE);
				statusView.setVisibility(View.VISIBLE);
			}
			if(profile.getSkype() == null) {
				skypeLb.setVisibility(View.GONE);
				skype.setVisibility(View.GONE);
			} else {
				skypeLb.setVisibility(View.VISIBLE);
				skype.setVisibility(View.VISIBLE);
			}
			if(profile.getPhone() == null){
				phoneLb.setVisibility(View.GONE);
				phone.setVisibility(View.GONE);
			} else {
				phoneLb.setVisibility(View.VISIBLE);
				phone.setVisibility(View.VISIBLE);
			}
			homeView.setVisibility(View.VISIBLE);
		}
	}


}
