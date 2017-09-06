package com.joy.network.util;

import android.os.Handler;
import android.os.Message;

public class AsyncTask {

	static AsyncTask task;

	private AsyncTask() {
	}

	public static AsyncTask getInstance() {
		if (task == null) {
			task = new AsyncTask();
		}
		return task;
	}

	public void run(final CallBack callBack) {

		final Handler handler = new Handler() {
			public void handleMessage(Message message) {

				int what = message.what;
				switch (what) {
				case 0:
					callBack.onPreExecute();
					break;
				case 1:
					callBack.onPostExecute();
					break;
				}
			}
		};
		new Thread(new Runnable() {

			@Override
			public void run() {
				handler.sendEmptyMessage(0);
				callBack.doInBackground();
				handler.sendEmptyMessage(1);
			}
		}).start();
		;
	}

	public interface CallBack {

		public void onPreExecute();

		public void onPostExecute();

		public void doInBackground();
	}
}
