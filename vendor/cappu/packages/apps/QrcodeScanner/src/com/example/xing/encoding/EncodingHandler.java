package com.example.xing.encoding;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Hashtable;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * @author Ryan Tang
 * 
 */
public final class EncodingHandler {
	private static final int BLACK = 0xff000000;

	public static Bitmap createQRCode(String str, int widthAndHeight)
			throws WriterException {
		
		int width = 200, height = 200;

		
		if (str.length() < 1) {
			return null;
		}

		Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
		BitMatrix bitMatrix = new QRCodeWriter().encode(str,
				BarcodeFormat.QR_CODE, width, height, hints);
		int[] pixels = new int[width * height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (bitMatrix.get(x, y)) {
					pixels[y * width + x] = 0xffff0000;
				} else {
					pixels[y * width + x] = 0xffffffff;
				}
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);

		return bitmap;
	}
}
