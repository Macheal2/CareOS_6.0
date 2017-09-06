package com.joy.network.handler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import com.joy.util.Constants;
import com.joy.util.Logger;
import com.joy.util.Util;

/**
 * ��������ͼƬ
 * 
 * @author wanghao
 * 
 */
public class BitmapHandler {

	/**
	 * Ĭ���ǻ�ȡpng��ʽͼƬ
	 * 
	 * @param in
	 * @param url
	 * @param option
	 * @return
	 */
	public Bitmap getBitmapByUrl(InputStream in, String url) {
		return getBitmapByUrl(in, url, ".png");
	}

	/**
	 * �������ϸ��url��ȡ�����ָ����׺��suffix��ͼƬ
	 * 
	 * @param in
	 * @param url
	 * @param suffix
	 * @param option
	 * @return
	 */
	public Bitmap getBitmapByUrl(InputStream in, String url, String suffix) {
		InputStream is = in;
		if (is == null) {
			return null;
		}
		byte[] b = Util.getBytes(is);
		if (b == null) {
			return null;
		}
		Bitmap bm = BitmapFactory.decodeByteArray(b, 0, b.length);

		return bm;
	}

	public Bitmap getBitmapByUrl(InputStream in) {
		Bitmap bm = null;
		FilterInputStream fit = null;
		try {
			fit = new FlushedInputStream(in);
			bm = BitmapFactory.decodeStream(fit);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
				if (fit != null)
					fit.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			in = null;
		}
		return bm;
	}

	public Bitmap getBitmapByUrl(InputStream is, int width) {
		Bitmap bm = null;
		Bitmap originBm = getBitmapByUrl(is);
		try {
			if (originBm != null) {
				int originWidth = originBm.getWidth();
				if (originWidth == width) {
					bm = originBm;
				} else {
					int originHeight = originBm.getHeight();
					float ratio = ((float) width) / originWidth;
					Matrix matrix = new Matrix();
					matrix.postScale(ratio, ratio);
					bm = Bitmap.createBitmap(originBm, 0, 0, originWidth, originHeight, matrix, true);
					if (!originBm.isRecycled())
						originBm.recycle();
					originBm = null;
				}
			}
		} catch (Exception e) {
		}
		return bm;
	}

	class FlushedInputStream extends FilterInputStream {
		public FlushedInputStream(InputStream inputStream) {
			super(inputStream);
		}

		@Override
		public long skip(long n) throws IOException {
			long totalBytesSkipped = 0L;
			while (totalBytesSkipped < n) {
				long bytesSkipped = in.skip(n - totalBytesSkipped);
				if (bytesSkipped == 0L) {
					int b = read();
					if (b < 0) {
						break; // we reached EOF
					} else {
						bytesSkipped = 1; // we read one byte
					}
				}
				totalBytesSkipped += bytesSkipped;
			}
			return totalBytesSkipped;
		}
	}
}
