package com.cappu.downloadcenter.common.cache;

import android.graphics.Bitmap;

public interface ImageDownLoadCallback {
	public void imageDownLoaded(int status, Bitmap bm);
}
