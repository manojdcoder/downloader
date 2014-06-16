package com.mykingdom.downloader;

import java.util.EventListener;

public interface IAsyncFetchListener extends EventListener {
	void onError(String error, Integer currentIndex);

	void onLoad(Integer progress);

	void onCancel();
	
	void onComplete();
}