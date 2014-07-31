package org.exoplatform.notifications;

import java.io.IOException;

import org.exoplatform.utils.ExoConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GCMRegistrationTask extends AsyncTask<Void, Void, Void> {
	
	private static final String TAG = "eXo____GCMRegistrationTask____";
	
	private final String SENDER_ID = "820134396909";
	private GoogleCloudMessaging gcm;
	private Context context;
	private String registrationId;

	/*
	 * TASK
	 */
	
	public GCMRegistrationTask(Context ctx) {
		context = ctx;
		if (gcm == null) {
			  gcm = GoogleCloudMessaging.getInstance(context);
		  }
	}

	@Override
	protected Void doInBackground(Void... params) {
		if ("".equals(getRegistrationId())) {
			// if the registration ID is not stored, we must get one from GCM
			try {
				registrationId = gcm.register(SENDER_ID);
				// Persist the registration ID - no need to register again.
                storeRegistrationId(registrationId);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	@Override
	protected void onCancelled() {
		super.onCancelled();
	}
	
	/*
	 * READ / WRITE IN SHARED PREFERENCES
	 */
	
	/**
     * Gets the current registration ID for application on GCM service, if there is one.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId() {
        final SharedPreferences prefs = getGcmPreferences();
        String registrationId = prefs.getString(ExoConstants.GCM_REGISTRATION_ID, "");
        if (registrationId.isEmpty()) {
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing ID is not guaranteed to work with the new app version.
        int registeredVersion = prefs.getInt(ExoConstants.APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            return "";
        }
        return registrationId;
    }

	
	/**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param regId registration ID
     */
    private void storeRegistrationId(String regId) {
        final SharedPreferences prefs = getGcmPreferences();
        int appVersion = getAppVersion();
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ExoConstants.GCM_REGISTRATION_ID, regId);
        editor.putInt(ExoConstants.APP_VERSION, appVersion);
        editor.commit();
    }

	
	/*
	 * UTILS
	 */
	
	/**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGcmPreferences() {
        return context.getSharedPreferences(ExoConstants.EXO_PREFERENCE, Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private int getAppVersion() {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

	
}
