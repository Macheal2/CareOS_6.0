package com.cappu.launcherwin.downloadUI.celllayout;

import java.io.File;

public interface DownloadListener {

    void onStart(int fileByteSize);

    void onPause();

    void onResume();

    void onProgress(int receivedBytes);

    void onFail();

    void onSuccess(File file);

    void onCancel();
}
