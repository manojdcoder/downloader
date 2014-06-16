package com.mykingdom.downloader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.appcelerator.kroll.KrollDict;
import org.appcelerator.titanium.TiFileProxy;

import android.content.Context;
import android.os.AsyncTask;

class DownloadFile extends AsyncTask<List<Object>, Integer, String> {

	private File outputDirectory;
	private boolean enableNotification = false;
	private Integer notificationId;
	private String notificationTitle;
	private Context mContext;
	private NotificationHelper notificationHelper;
	IAsyncFetchListener fetchListener = null;

	public DownloadFile(Context context, File direcory, boolean enable,
			Integer id, String title) {
		mContext = context;
		outputDirectory = direcory;
		enableNotification = enable;
		notificationId = id;
		notificationTitle = title;
	}

	public void setListener(IAsyncFetchListener listener) {
		this.fetchListener = listener;
	}

	@Override
	protected void onPreExecute() {
		if (enableNotification) {
			notificationHelper = new NotificationHelper(mContext,
					notificationId, notificationTitle);
			notificationHelper.createNotification();
		}
	}

	@Override
	protected String doInBackground(List<Object>... fileToDownload) {

		Integer filesCount = fileToDownload[0].size();
		Integer i = 0;
		File fileObj = null;

		try {

			for (i = 0; i < filesCount; i++) {

				HashMap<String, String> hashMap = (HashMap<String, String>) fileToDownload[0]
						.get(i);

				int count;

				String strUrl = hashMap.get("url");
				URL url = new URL(strUrl);
				URLConnection connection = url.openConnection();
				connection.connect();

				int lengthOfFile = connection.getContentLength();
				long total = 0;
				String name;
				if (hashMap.containsKey("name")) {
					name = hashMap.get("name");
				} else {
					name = strUrl.substring(strUrl.lastIndexOf('/'));
				}
				fileObj = new File(outputDirectory, name);
				InputStream input = new BufferedInputStream(url.openStream());
				OutputStream output = new FileOutputStream(fileObj, false);
				byte data[] = new byte[1024];
				while ((count = input.read(data)) != -1) {
					total += count;
					int fileProgress = (int) ((total * 100) / lengthOfFile);
					publishProgress((int) ((fileProgress + (i * 100)) / filesCount));
					output.write(data, 0, count);
				}
				output.flush();
				output.close();
				input.close();

				if (isCancelled()) {
					deleteUnCompletedFile(fileObj);
					break;
				}
			}

		} catch (Exception e) {

			deleteUnCompletedFile(fileObj);
			if (enableNotification) {
				notificationHelper.notcompleted();
			}
			this.fetchListener.onError(e.toString(), i);

		}

		return null;
	}

	@Override
	protected void onProgressUpdate(Integer... progress) {
		if (enableNotification) {
			notificationHelper.progressUpdate(progress[0]);
		}
		this.fetchListener.onLoad(progress[0]);
	}

	@Override
	protected void onPostExecute(String result) {
		if (!enableNotification || notificationHelper.completed()) {
			this.fetchListener.onComplete();
		}
	}

	@Override
	protected void onCancelled() {
		if (enableNotification) {
			notificationHelper.cancelnotification();
		}
		this.fetchListener.onCancel();
	}

	private void deleteUnCompletedFile(File file) {
		try {
			file.delete();
		} catch (Exception err) {
			// error
		}
	}
}