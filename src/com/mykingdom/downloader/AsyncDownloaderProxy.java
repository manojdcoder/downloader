package com.mykingdom.downloader;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.kroll.KrollProxy;
import org.appcelerator.kroll.annotations.Kroll;
import org.appcelerator.kroll.annotations.Kroll.proxy;
import org.appcelerator.titanium.TiFileProxy;
import org.appcelerator.titanium.util.TiConvert;

import android.os.Environment;
import android.util.Log;

import com.mykingdom.downloader.DownloadFile;
import com.mykingdom.downloader.IAsyncFetchListener;

@Kroll.proxy(creatableInModule = DownloaderModule.class, propertyAccessors = {
		DownloaderModule.PROPERTY_FILES_TO_DOWNLOAD,
		DownloaderModule.PROPERTY_OUTPUT_DIRECTORY,
		DownloaderModule.PROPERTY_USE_CACHE,
		DownloaderModule.PROPERTY_ENABLE_NOTIFICATION,
		DownloaderModule.PROPERTY_NOTIFICATION_ID,
		DownloaderModule.PROPERTY_NOTIFICATION_TITLE })
public class AsyncDownloaderProxy extends KrollProxy {

	// Standard Debugging variables
	private static final String TAG = "AsyncDownloaderProxy";

	private DownloadFile downloader;
	private boolean isDownloading = false;

	public AsyncDownloaderProxy() {
		super();
	}

	@Override
	public void handleCreationDict(KrollDict options) {
		super.handleCreationDict(options);
	}

	@Kroll.method
	public boolean startDownload() {
		Log.d(TAG, "inside startDownload");
		if (isDownloading == false
				&& hasProperty(DownloaderModule.PROPERTY_FILES_TO_DOWNLOAD)) {
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

		File outputDirectory;
		if (hasProperty(DownloaderModule.PROPERTY_OUTPUT_DIRECTORY)) {
			outputDirectory = ((TiFileProxy) getProperty(DownloaderModule.PROPERTY_OUTPUT_DIRECTORY))
					.getBaseFile().getNativeFile();
		} else {
			outputDirectory = new File(Environment.getDataDirectory()
					.getAbsolutePath());
		}

		downloader = new DownloadFile(
				getActivity().getApplicationContext(),
				outputDirectory,
				TiConvert
						.toBoolean(
								getProperty(DownloaderModule.PROPERTY_ENABLE_NOTIFICATION),
								true),
				TiConvert.toInt(
						getProperty(DownloaderModule.PROPERTY_NOTIFICATION_ID),
						1),
				TiConvert
						.toString(
								getProperty(DownloaderModule.PROPERTY_NOTIFICATION_TITLE),
								"Download"),
				TiConvert
						.toBoolean(
								getProperty(DownloaderModule.PROPERTY_USE_CACHE),
								true));

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
		Object[] filesToDownload = (Object[]) getProperty(DownloaderModule.PROPERTY_FILES_TO_DOWNLOAD);
		List<Object> filesList = Arrays.asList(filesToDownload);
		downloader.execute(filesList);
	}
}