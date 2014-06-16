var win = Ti.UI.createWindow({
	backgroundColor: "#fff",
	exitOnClose: true
});
var downloadBtn = Ti.UI.createButton({
	title: "Start Download"
});
downloadBtn.addEventListener("click", function(e){
	var DownloaderModule = require("com.mykingdom.downloader");
	var downloader = DownloaderModule.createAsyncDownloader({
		filesToDownload: [{
			url : "https://registry.npmjs.org/alloy/-/alloy-1.4.0-rc.tgz",
			name: "alloy.tgz"
		}, {
			url : "http://builds.appcelerator.com/mobile/3.3.0/mobilesdk-3.3.0.RC-osx.zip"
		}],
		outputDirectory: Ti.Filesystem.getFile(Ti.Filesystem.getExternalStorageDirectory()),
		enableNotification: true,
		notificationId: 1,
		notificationTitle: "Downloading Appcelerator SDK"
	});
	downloader.addEventListener("error", function(evt){
		alert(evt.error + " : While downloading file at (index 0 based) = " + evt.currentIndex);
	});
	downloader.addEventListener("onload", function(evt){
		Ti.API.info("Current Progress = " + evt.progress);
	});
	downloader.addEventListener("cancel", function(evt){
		Ti.API.info("Download canceled");
	});
	downloader.addEventListener("success", function(evt){
		Ti.API.info("Download completed successfully");
	});
	downloader.startDownload();
	//downloader.stopDownload();
});
win.add(downloadBtn);
win.open();