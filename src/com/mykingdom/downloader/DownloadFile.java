package com.mykingdom.downloader;

import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.appcelerator.kroll.common.Log;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.AsyncTask;

class DownloadFile extends AsyncTask<List<Object>, Integer, String> {

	private File outputDirectory;
	private boolean enableNotification, useCache;
	private Integer notificationId;
	private String notificationTitle;
	private Context mContext;
	private NotificationHelper notificationHelper;
	IAsyncFetchListener fetchListener = null;

	public DownloadFile(Context context, File direcory, boolean enable,
			Integer id, String title, boolean cache) {
		mContext = context;
		outputDirectory = direcory;
		enableNotification = enable;
		notificationId = id;
		notificationTitle = title;
		useCache = cache;
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
		InputStream input = null;
		OutputStream output = null;
		HttpURLConnection connection = null;

		try {

			for (i = 0; i < filesCount; i++) {

				HashMap<String, String> hashMap = (HashMap<String, String>) fileToDownload[0]
						.get(i);

				String strUrl = hashMap.get("url");
				String name;
				if (hashMap.containsKey("name")) {
					name = hashMap.get("name");
				} else {
					name = strUrl.substring(strUrl.lastIndexOf('/'));
				}
				fileObj = new File(outputDirectory, name);
				deleteUnCompletedFile(fileObj);

				URL url = new URL(strUrl);
				connection = (HttpURLConnection) url.openConnection();
				connection.setUseCaches(useCache);
				connection.connect();

				if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
					Log.e("Server returned HTTP ", connection.getResponseCode()
							+ " - " + connection.getResponseMessage());
				}

				int fileLength = connection.getContentLength();

				input = connection.getInputStream();
				output = new FileOutputStream(fileObj);

				byte data[] = new byte[4096];
				long total = 0;
				int count;
				while ((count = input.read(data)) != -1) {
					total += count;
					if (fileLength > 0) {
						int fileProgress = (int) ((total * 100) / fileLength);
						publishProgress((int) ((fileProgress + (i * 100)) / filesCount));
					}
					output.write(data, 0, count);
				}

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

		} finally {

			try {

				if (output != null) {
					output.flush();
					output.close();
				}

				if (input != null)
					input.close();

			} catch (IOException ignored) {

			}

			if (connection != null)
				connection.disconnect();

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

	private static SSLSocketFactory createSslSocketFactory() throws Exception {
		TrustManager[] byPassTrustManagers = new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}

			public void checkClientTrusted(X509Certificate[] chain,
					String authType) {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) {
			}
		} };
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, byPassTrustManagers, new SecureRandom());
		return sslContext.getSocketFactory();
	}

	private void deleteUnCompletedFile(File file) {
		try {
			if (file.exists())
				file.delete();
		} catch (Exception err) {
			// error
		}
	}
}