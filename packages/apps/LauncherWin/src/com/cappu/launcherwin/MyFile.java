package com.cappu.launcherwin;

import java.io.FileInputStream;
import java.io.FileOutputStream;


import android.content.Context;

//dengying speaker
public class MyFile {
	private static final String FILENAME = "Speaker_OnOff.txt";

	private static final String ENCODING = "UTF-8";
	private static final String OPEN = "1";
	private static final String CLOSE = "0";

	// 读文件方法
	// ON:open, OFF:close
	public static String read(Context c) {
		try {
			FileInputStream inputStream = c.openFileInput(FILENAME);
			byte[] b = new byte[inputStream.available()];
			inputStream.read(b);
			String result = new String(b, ENCODING);
			return result;
		} catch (Exception e) {
			return OPEN;
		}
	}

	// 写文件
	public static void write(Context c, String str) {
		try {
			FileOutputStream fos = c.openFileOutput(FILENAME,
					Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
			fos.write(str.getBytes(ENCODING));
			fos.close();
		} catch (Exception e) {
		}
	}

	public static void write(String path, Context c, String str) {
		try {
			FileOutputStream fos = c.openFileOutput(path,
					Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
			fos.write(str.getBytes(ENCODING));
			fos.close();
		} catch (Exception e) {
		}
	}

	public static void OpenSpeaker(Context c) {
		write(c, OPEN);
	}

	public static void CloseSpeaker(Context c) {
		write(c, CLOSE);
	}

	public static boolean getSpeakerOnOFF(Context c) {
		if (read(c).equals(CLOSE)) {
			return false;
		} else {
			return true;
		}
	}
}
