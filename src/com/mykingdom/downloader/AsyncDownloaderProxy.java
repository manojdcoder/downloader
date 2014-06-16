package com.mykingdom.downloader;

import java.util.Arrays;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.titanium.TiFileProxy;

import android.util.Log;

import com.mykingdom.downloader.DownloadFile;
import com.mykingdom.downloader.IAsyncFetchListener;

@Kroll.proxy(creatableInModule = DownloaderModule.class)
public class AsyncDownloaderProxy extends KrollProxy {

	// Standard Debugging variables
	private static final String TAG = "AsyncDownloaderProxy";

	// Static Properties
	public static final String PROPERTY_FILES_TO_DOWNLOAD = "filesToDownload";
	public static final String PROPERTY_OUTPUT_DIRECTORY = "outputDirectory";
	public static final String PROPERTY_ENABLE_NOTIFICATION = "enableNotification";
	public static final String PROPERTY_NOTIFICATION_ID = "notificationId";
	public static final String PROPERTY_NOTIFICATION_TITLE = "notificationTitle";

	private DownloadFile downloader;
	private Object[] filesToDownload;
	private TiFileProxy outputDirectory;
	private boolean isDownloading, enableNotification;
	private Integer notificationId;
	private String notificationTitle;

	public AsyncDownloaderProxy() {
		super();
	}

	@Override
	public void handleCreationDict(KrollDict options) {

		if (options.containsKey(PROPERTY_FILES_TO_DOWNLOAD)) {
			filesToDownload = (Object[]) options
					.get(PROPERTY_FILES_TO_DOWNLOAD);
		}

		if (options.containsKey(PROPERTY_OUTPUT_DIRECTORY)) {
			outputDirectory = (TiFileProxy) options
					.get(PROPERTY_OUTPUT_DIRECTORY);
		}

		if (options.containsKey(PROPERTY_ENABLE_NOTIFICATION)) {
			enableNotification = options
					.getBoolean(PROPERTY_ENABLE_NOTIFICATION);
		}

		if (options.containsKey(PROPERTY_NOTIFICATION_ID)) {
			notificationId = options.getInt(PROPERTY_NOTIFICATION_ID);
		}

		if (options.containsKey(PROPERTY_NOTIFICATION_TITLE)) {
			notificationTitle = options.getString(PROPERTY_NOTIFICATION_TITLE);
		}

		super.handleCreationDict(options);
	}

	@Kroll.getProperty
	@Kroll.method
	public Object[] getFilesToDownload() {
		return filesToDownload;
	}

	@Kroll.setProperty
	@Kroll.method
	public void setFilesToDownload(Object[] args) {
		filesToDownload = args;
	}

	@Kroll.getProperty
	@Kroll.method
	public TiFileProxy getOutputDirectory() {
		return outputDirectory;
	}

	@Kroll.setProperty
	@Kroll.method
	public void setOutputDirectory(TiFileProxy fileProxy) {
		outputDirectory = fileProxy;
	}

	@Kroll.getProperty
	@Kroll.method
	public boolean getEnableNotification() {
		return enableNotification;
	}

	@Kroll.setProperty
	@Kroll.method
	public void setEnableNotification(boolean enable) {
		enableNotification = enable;
	}

	@Kroll.getProperty
	@Kroll.method
	public int getNotificationId() {
		return notificationId;
	}

	@Kroll.setProperty
	@Kroll.method
	public void setNotificationId(int id) {
		notificationId = id;
	}

	@Kroll.getProperty
	@Kroll.method
	public String getNotificationTitle() {
		return notificationTitle;
	}

	@Kroll.setProperty
	@Kroll.method
	public void setNotificationTitle(String title) {
		notificationTitle = title;
	}

	@Kroll.method
	public boolean startDownload() {
		Log.d(TAG, "inside startDownload");
		if (isDownloading == false && filesToDownload.length > 0) {
			isDownloading = true;
			downloadFiles();
			return true;
		} else {
			return false;
		}
	}

	@Kroll.method
	public boolean stopDownload() {
		if (isDownloading == true) {
			downloader.cancel(true);
			return true;
		} else {
			return false;
		}
	}

	private void downloadFiles() {
		downloader = new DownloadFile(getActivity().getApplicationContext(),
				outputDirectory.getBaseFile().getNativeFile(),
				enableNotification, notificationId, notificationTitle);
		downloader.setListener(new IAsyncFetchListener() {
			@Override
			public void onError(String error, Integer currentIndex) {
				if (hasListeners("error")) {
					KrollDict errorKrollDict = new KrollDict();
					errorKrollDict.put("error", error);
					errorKrollDict.put("currentIndex", currentIndex);
					fireEvent("error", errorKrollDict);
				}
				isDownloading = false;
			}

			@Override
			public void onLoad(Integer progress) {
				if (hasListeners("onload")) {
					KrollDict progressKrollDict = new KrollDict();
					progressKrollDict.put("progress", progress);
					fireEvent("onload", progressKrollDict);
				}
			}

			@Override
			public void onCancel() {
				if (hasListeners("cancel")) {
					fireEvent("cancel", null);
				}
				isDownloading = false;
			}

			@Override
			public void onComplete() {
				if (hasListeners("success")) {
					fireEvent("success", null);
				}
				isDownloading = false;
			}
		});
		List<Object> filesList = Arrays.asList(filesToDownload);
		downloader.execute(filesList);
	}
}