package org.exoplatform.notifications;

import org.exoplatform.R;
import org.exoplatform.singleton.AccountSetting;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class PlatformNotifsIntentService extends IntentService {
	
	private static final String TAG = "eXo____PlatformNotifsIntentService____";

	public PlatformNotifsIntentService()
	{
		super(TAG);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras(); // should contain as follows:
		// - user    : username of the user targeted by this notification
		// - title   : title of the notification
		// - message : message/content of the notification
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String msgType = gcm.getMessageType(intent);
		
		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(msgType)) {
				String user = extras.getString("user");
				if (AccountSetting.getInstance().getUsername().equals(user)) { // show the notification only if the targeted user is currently logged-in
					String title = extras.getString("title");
					String message = extras.getString("message");
					if (!"".equals(title)) {
						sendNotification(title, message);
					}
				} else {
					Log.i(TAG, "Notification for user '"+user+"' was dismissed.");
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
        PlatformNotifsBroadcastReceiver.completeWakefulIntent(intent);

	}
	
	// Put the title and message into a notification and post it.
    private void sendNotification(String title, String message) {
    	final int NOTIFICATION_ID = 1;
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.application_icon)
        .setContentTitle(title)
        .setStyle(new NotificationCompat.BigTextStyle());
        if (message != null && !message.equals("")) {
        	mBuilder.setContentText(message).setStyle(new NotificationCompat.BigTextStyle().bigText(message));
        }

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


}
