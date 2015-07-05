var DownloaderModule = require("com.mykingdom.downloader");
var downloader = DownloaderModule.createAsyncDownloader({
	filesToDownload : [{
		url : "http://api.appcelerator.com/p/v1/release-download?token=G4KZpgw4",
		name : "3.5.1.zip"
	}],
	outputDirectory : Ti.Filesystem.getFile(Ti.Filesystem.getExternalStorageDirectory()),
	useCache : false,
	enableNotification : true,
	notificationId : 1,
	notificationTitle : "Custom Title",
	notificationSuccessTitle : "Custom Success Title",
	notificationSuccessDescription : "Custom Success Description",
	notificationFailureTitle : "Custom Failure Title",
	notificationFailureDescription : "Custom Failure Description"
});
downloader.addEventListener("error", function(evt) {
	alert(evt.error + " : While downloading file at (index 0 based) = " + evt.currentIndex);
});
downloader.addEventListener("onload", function(evt) {
	Ti.API.info("Current Progress = " + evt.progress);
});
downloader.addEventListener("cancel", function(evt) {
	Ti.API.info("Download canceled");
});
downloader.addEventListener("success", function(evt) {
	Ti.API.info("Download completed successfully");
});

var win = Ti.UI.createWindow({
	backgroundColor : "#fff",
	layout : "vertical",
	exitOnClose : true
});
var startBtn = Ti.UI.createButton({
	top : 20,
	title : "Start Download",
	tocuhEnabled : true
});
startBtn.addEventListener("click", function(e) {
	startBtn.tocuhEnabled = false;
	stopBtn.tocuhEnabled = true;
	downloader.startDownload();
});
win.add(startBtn);
var stopBtn = Ti.UI.createButton({
	top : 20,
	title : "Stop Download",
	tocuhEnabled : false
});
stopBtn.addEventListener("click", function(e) {
	stopBtn.tocuhEnabled = false;
	downloader.stopDownload();
});
win.add(stopBtn);
win.open();
