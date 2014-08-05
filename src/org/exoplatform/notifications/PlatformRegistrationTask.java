package org.exoplatform.notifications;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.exoplatform.singleton.AccountSetting;
import org.exoplatform.utils.ExoConnectionUtils;
import org.exoplatform.utils.ExoConstants;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class PlatformRegistrationTask extends AsyncTask<Void, Void, Void> {
	
	private final String JSON_KEY_REGISTRATION_ID = "device_id";
	private final String JSON_KEY_DEVICE_PLATFORM = "platform";
	private final String JSON_KEY_USERNAME        = "username";
	private static final String TAG = "eXo____PlatformRegistrationTask____";
	
	private Context context;
	
	public PlatformRegistrationTask(Context ctx) {
		context = ctx;
	}

	/*
	 * TASK
	 */
	
	@Override
	protected Void doInBackground(Void... params) {
		String registrationId = getRegistrationId();
		if ("".equals(registrationId)) {
			// TODO should try to register from GCM
		} else {
			try {
				// Build the URL to request
				StringBuilder urlBuilder = new StringBuilder();
				urlBuilder.append(AccountSetting.getInstance().getDomainName()).append('/'); // starts with the server domain url
				urlBuilder.append(ExoConstants.ACTIVITY_PORTAL_CONTAINER).append('/'); // adds the portal container name (portal)
				urlBuilder.append(ExoConstants.ACTIVITY_REST_CONTEXT).append('/'); // adds the rest context (rest)
				urlBuilder.append(ExoConnectionUtils.REGISTRATION_BASE_URL).append('/'); // adds the registration base url
				urlBuilder.append(ExoConnectionUtils.REGISTRATION_ENDPOINT); // finishes with the name of the registration service
				
				// Build the JSON request entity
				
				JSONObject jsonParams = new JSONObject();
				jsonParams.put(JSON_KEY_REGISTRATION_ID, registrationId);
				jsonParams.put(JSON_KEY_DEVICE_PLATFORM, "android");
				jsonParams.put(JSON_KEY_USERNAME, AccountSetting.getInstance().getUsername());
				StringEntity stringEntity;
				stringEntity = new StringEntity(jsonParams.toString(), "UTF-8");
				
				// Build the request
				HttpPost postRequest = new HttpPost(urlBuilder.toString());
				postRequest.setHeader("Content-Type", "application/json");
				postRequest.setEntity(stringEntity);
				
				// Send the request and get the response
				HttpClient httpClient = ExoConnectionUtils.initHttpClient();
				HttpResponse response = httpClient.execute(postRequest);
				int statusCode = response.getStatusLine().getStatusCode();
				
				if (statusCode != 200) {
					Log.e(TAG, "Could not register device to current user. Error "+statusCode+": "+response.getStatusLine().getReasonPhrase());
				} else {
					Log.i(TAG, "Device registered successfully in Platform.");
				}
				
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		return null;
	}
	
	/*
	 * UTILS
	 */
	
	private String getRegistrationId() {
        final SharedPreferences prefs = context.getSharedPreferences(ExoConstants.EXO_PREFERENCE, Context.MODE_PRIVATE);
        String registrationId = prefs.getString(ExoConstants.GCM_REGISTRATION_ID, "");
        return registrationId;
    }

}
