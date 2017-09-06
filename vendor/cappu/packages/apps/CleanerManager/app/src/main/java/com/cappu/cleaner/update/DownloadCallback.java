package com.cappu.cleaner.update;

import android.content.SharedPreferences;

public interface DownloadCallback {
    void downloadUpdate(SharedPreferences sharedPreferences, String key);
}
