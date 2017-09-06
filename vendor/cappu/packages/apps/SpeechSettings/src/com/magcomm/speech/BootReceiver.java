package com.magcomm.speech;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.os.Environment;
import android.content.res.AssetManager;
import android.text.TextUtils;

public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = "BootReceiver";
	private static final String OUT_API_PATH = ".Speechcloud";
	private SharedPreferences mSharedPreferences;
	private static final String PESET_PATH_API = "tts";
	private Context mContext;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		new Thread() {
			@Override
			public void run() {
				try {
					Log.i(TAG, "Entry_BootBroadcastReceiver_onReceive");
					if (Environment.getExternalStorageState().equals(
							Environment.MEDIA_MOUNTED)) {
						mSharedPreferences = mContext.getSharedPreferences(
								"user_info", 0);
						Boolean isExists = mSharedPreferences.getBoolean(
								"isExists", false);
						Log.i(TAG,
								"Entry_BootBroadcastReceiver_onReceive isExists = "
										+ isExists);
						if (isExists) {
							return;
						}
						saveVoice();
						mSharedPreferences = mContext.getSharedPreferences(
								"user_info", 0);
						mSharedPreferences.edit().putBoolean("isExists", true)
								.commit();
					}
				} catch (Exception e) {
					Log.i(TAG, "Entry_BootBroadcastReceiver has exception = "
							+ e);
					e.printStackTrace();
				}
			}
		}.start();
	}

	private void saveVoice() throws Exception {
		AssetManager asset = mContext.getAssets();
		String[] voices = asset.list(PESET_PATH_API);
		for (int i = 0; i < voices.length; i++) {
			String name = voices[i];
			String tag = plusString(PESET_PATH_API, name);
			Boolean isExists = mSharedPreferences.getBoolean(tag, false);
			Log.i(TAG, "input name = " + name + " , old isExists = " + isExists);
			if (!isExists) {
				InputStream inputStream = getClass().getResourceAsStream(
						"/assets/" + PESET_PATH_API + "/" + voices[i]);
				saveToSDCard(OUT_API_PATH, name, inputStream);
				mSharedPreferences.edit().putBoolean(tag, true).commit();
			}
			Log.i(TAG, "++++++++++++++++++++++++++++++++++++++");
		}
	}

	// save to SDcard
	public void saveToSDCard(String outpath, String name, InputStream input)
			throws Exception {
		File out = new File(Environment.getExternalStoragePublicDirectory(
				OUT_API_PATH).toString());
		out.mkdirs();
		Log.i(TAG, "path = " + out.getPath());
		saveToSDCard(out, name, input);
	}

	// save to SDcard
	public void saveToSDCard(File outpath, String name, InputStream input)
			throws Exception {
		Log.i(TAG, "start saveToSDCard path = " + outpath.getPath());
		File file = new File(outpath, name);
		FileOutputStream output = new FileOutputStream(file);
		if (!file.exists()) {
			Log.i(TAG, "file is not exists create");
			file.createNewFile();
		}

		Log.i(TAG, "saveToSDCard_startwrite output = " + output);
		Log.i(TAG, "saveToSDCard_startwrite input = " + input);

		byte[] buffer = new byte[1024 * 16];
		int len = 0;

		while ((len = input.read(buffer)) != -1) {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				output.write(buffer, 0, len);
			}
		}
		output.flush();
		Log.i(TAG, "saveToSDCard_write_over");

		output.close();
		input.close();
	}

	public static String plusString(String... strs) {
		StringBuilder builder = new StringBuilder();
		for (String str : strs) {
			if (!TextUtils.isEmpty(str)) {
				builder.append(str);
			}
		}
		return builder.toString();
	}
}
