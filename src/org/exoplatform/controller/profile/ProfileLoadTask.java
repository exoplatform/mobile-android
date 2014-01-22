package org.exoplatform.controller.profile;

import greendroid.widget.LoaderActionBarItem;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.ui.ProfileActivity;
import org.exoplatform.utils.ExoConnectionUtils;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import android.os.AsyncTask;
import android.util.Log;

/**
 * The asynchronous task that loads user profile from the Social REST service.
 */
public class ProfileLoadTask extends AsyncTask<String, Void, Void>{
	private ProfileActivity               activity;
	private UserProfile profile;
	private LoaderActionBarItem           loaderItem;
	private static String GET_PROFILE = "/rest/private/social/people/getPeopleInfo/";
	private static String JSON_FORMAT = ".json";
	private static final String TAG = "eXo____ProfileLoadTask____";

	public ProfileLoadTask(ProfileActivity activity, LoaderActionBarItem loader){
		this.activity = activity;
		loaderItem = loader;
	}

	@Override
	protected Void doInBackground(String...params) {
		String url = AccountSetting.getInstance().getDomainName() + GET_PROFILE ;
		String uid = params[0];
		if(uid != null && !url.isEmpty()) url = url + uid + JSON_FORMAT; 
		try {
			UserProfile profile = null ;
			HttpResponse response = ExoConnectionUtils.getRequestResponse(url);
			//int responseCode = response.getStatusLine().getStatusCode();
			InputStream input = ExoConnectionUtils.sendRequest(response);
			if (input != null) {
				String result = ExoConnectionUtils.convertStreamToString(input);
				JSONObject json = (JSONObject) JSONValue.parse(result);
				profile = new UserProfile(json); 
			}
			this.profile = profile;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onCancelled() {
		loaderItem.setLoading(false);
	}
	@Override
	public void onPreExecute() {
		loaderItem.setLoading(true);
	}

	@Override
	public void onPostExecute(Void result) {
		activity.setInfor(this.profile);
		loaderItem.setLoading(false);
		changeLanguage();
	}

	private void changeLanguage() {
		activity.changeLanguage();
	}

}
