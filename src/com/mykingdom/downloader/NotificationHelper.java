package com.mykingdom.downloader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

@SuppressWarnings("deprecation")
public class NotificationHelper {

	private Context mContext;
	private NotificationCompat.Builder mBuilder;
	private Intent notifyIntent;
	private PendingIntent notifyPendingIntent;
	private NotificationManager mNotificationManager;

	private Integer notificationId = 0, lastPercent = 0;
	private String notificationTitle;

	public NotificationHelper(Context context, int id, String title) {
		mContext = context;
		notificationId = id;
		notificationTitle = title;
	}

	public void createNotification() {

		mBuilder = new NotificationCompat.Builder(mContext);

		notifyIntent = new Intent(mContext, NotificationHelper.class);
		notifyIntent.setFlags(PendingIntent.FLAG_UPDATE_CURRENT);

		notifyPendingIntent = PendingIntent.getActivity(mContext, 0,
				notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		mBuilder.setContentIntent(notifyPendingIntent);

		mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);

		mBuilder.setContentTitle(notificationTitle).setProgress(100, 0, false)
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.setAutoCancel(false).setOngoing(true)
				.setWhen(System.currentTimeMillis());
		mNotificationManager.notify(notificationId, mBuilder.build());
	}

	public void progressUpdate(Integer percentageComplete) {
		if (percentageComplete > lastPercent) {
			mBuilder.setContentText(percentageComplete + "%").setProgress(100,
					percentageComplete, false);
			mNotificationManager.notify(notificationId, mBuilder.build());
			lastPercent = percentageComplete;
		}
	}

	public boolean completed(String title, String description) {
		if (mNotificationManager != null) {
			mBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done)
					.setProgress(0, 0, false).setContentTitle(title)
					.setContentText(description).setAutoCancel(true)
					.setOngoing(false);
			mNotificationManager.notify(notificationId, mBuilder.build());
			mNotificationManager = null;
			return true;
		}
		return false;
	}

	public void notcompleted(String title, String description) {
		if (mNotificationManager != null) {
			mBuilder.setSmallIcon(android.R.drawable.stat_sys_download_done)
					.setProgress(0, 0, false).setContentTitle(title)
					.setContentText(description).setAutoCancel(true)
					.setOngoing(false);
			mNotificationManager.notify(notificationId, mBuilder.build());
		}
	}

	public void cancelnotification() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(notificationId);
		}
	}
}
