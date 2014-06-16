package com.mykingdom.downloader;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

@SuppressWarnings("deprecation")
public class NotificationHelper {

	private Context mContext;
	private Integer notificationId = 0, lastPercent = 0;
	private Notification mNotification;
	public static NotificationManager mNotificationManager;
	private PendingIntent contentIntent;
	private Intent notificationIntent;
	private String notificationTitle;

	public NotificationHelper(Context context, int id, String title) {
		mContext = context;
		notificationId = id;
		notificationTitle = title;
	}

	public void createNotification() {

		mNotificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationIntent = new Intent(mContext, NotificationHelper.class);
		contentIntent = PendingIntent.getActivity(mContext, notificationId,
				notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		Integer icon = android.R.drawable.stat_sys_download;
		Long when = System.currentTimeMillis();
		mNotification = new Notification(icon, notificationTitle, when);
		mNotification.flags = mNotification.flags
				| Notification.FLAG_ONGOING_EVENT;
		mNotification.contentView = new RemoteViews(mContext.getPackageName(),
				mContext.getResources().getIdentifier("notification_progress",
						"layout", mContext.getPackageName()));
		mNotification.contentIntent = contentIntent;
		mNotification.contentView.setImageViewResource(mContext.getResources()
				.getIdentifier("icon", "id", mContext.getPackageName()), icon);
		mNotification.contentView.setTextViewText(mContext.getResources()
				.getIdentifier("title", "id", mContext.getPackageName()),
				notificationTitle != "" ? notificationTitle
						: "Downloading Files");
		mNotification.contentView.setTextViewText(mContext.getResources()
				.getIdentifier("percentage", "id", mContext.getPackageName()),
				"0%");
		mNotification.contentView.setProgressBar(mContext.getResources()
				.getIdentifier("progressbar", "id", mContext.getPackageName()),
				100, 0, false);

		mNotificationManager.notify(notificationId, mNotification);
	}

	public void progressUpdate(Integer percentageComplete) {
		if (percentageComplete > lastPercent) {
			mNotification.contentView.setProgressBar(
					mContext.getResources().getIdentifier("progressbar", "id",
							mContext.getPackageName()), 100,
					percentageComplete, false);
			mNotification.contentView.setTextViewText(
					mContext.getResources().getIdentifier("percentage", "id",
							mContext.getPackageName()), percentageComplete
							+ "%");
			mNotificationManager.notify(notificationId, mNotification);
			lastPercent = percentageComplete;
		}
	}

	private void beforeComplete() {
		Integer icon = android.R.drawable.stat_sys_download_done;
		mNotification.icon = icon;
		mNotification.contentView.setImageViewResource(mContext.getResources()
				.getIdentifier("icon", "id", mContext.getPackageName()), icon);
	}

	public boolean completed() {
		if (mNotificationManager != null) {
			beforeComplete();
			mNotification.setLatestEventInfo(mContext, "Download completed",
					"Download completed successfully.", contentIntent);
			mNotification.flags = Notification.FLAG_AUTO_CANCEL;
			mNotificationManager.notify(notificationId, mNotification);
			mNotificationManager = null;
			return true;
		}
		return false;
	}

	public void notcompleted() {
		if (mNotificationManager != null) {
			beforeComplete();
			mNotification.setLatestEventInfo(mContext, "Download failed",
					"Please try again later!", contentIntent);
			mNotification.flags = Notification.FLAG_AUTO_CANCEL;
			mNotificationManager.notify(notificationId, mNotification);
			mNotificationManager = null;
		}
	}

	public void cancelnotification() {
		if (mNotificationManager != null) {
			mNotificationManager.cancel(notificationId);
			mNotificationManager = null;
		}
	}
}
