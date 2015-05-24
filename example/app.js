var DownloaderModule = require("com.mykingdom.downloader");
var win = Ti.UI.createWindow({
	backgroundColor : "#fff",
	exitOnClose : true
});
var downloadBtn = Ti.UI.createButton({
	title : "Start Download"
});
downloadBtn.addEventListener("click", function(e) {
	var downloader = DownloaderModule.createAsyncDownloader({
		filesToDownload : [{
			url : "https://ebinders.etcconnect.com/assets/uploads/files/en-us/63a26-White_Paper_Control_Philosophy_revA.pdf",
			name : "White_Paper_Control_Philosophy_revA1.pdf"
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
	downloader.startDownload();
	//downloader.stopDownload();
});
win.add(downloadBtn);
win.open();
