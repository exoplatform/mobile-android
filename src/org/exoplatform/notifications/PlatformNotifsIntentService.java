package org.exoplatform.notifications;

import org.exoplatform.R;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class PlatformNotifsIntentService extends IntentService {
	
	private static final String TAG = "PlatformNotifsIntentService";

	public PlatformNotifsIntentService()
	{
		super(TAG);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras(); // should contain Hello World
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String msgType = gcm.getMessageType(intent);
		
		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(msgType)) {
				String helloworld = extras.getString("param");
				Log.i(TAG, helloworld);
				if (!"".equals(helloworld)) {
					sendNotification(helloworld);
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
        PlatformNotifsBroadcastReceiver.completeWakefulIntent(intent);

	}
	
	// Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
    	final int NOTIFICATION_ID = 1;
        NotificationManager mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
        .setSmallIcon(R.drawable.application_icon)
        .setContentTitle("GCM Notification")
        .setStyle(new NotificationCompat.BigTextStyle()
        .bigText(msg))
        .setContentText(msg);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }


}
