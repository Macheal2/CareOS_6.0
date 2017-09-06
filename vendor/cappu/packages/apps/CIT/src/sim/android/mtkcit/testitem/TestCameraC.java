/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package sim.android.mtkcit.testitem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import sim.android.mtkcit.CITActivity;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;
import sim.android.mtkcit.testitem.MikeTest.RecordThread;

public class TestCameraC extends TestBase implements SurfaceHolder.Callback {

	private int mMode;
	private boolean mIssdcardExist = false;
	private SurfaceView mCameraPreview;
	private int mPrvW = 640, mPrvH = 480;
	// private int mDispW = 640, mDispH = 480;
	// private int mPicW = 2560, mPicH = 1920;
	private int mPic8MW = 3264, mPic8MH = 2448;
	private Camera mCamera;
	private Camera.Parameters mCameraParam;
	private int mIsPreviewing = 0;
	private volatile int mIsFocused = 0;
	private volatile int mIsCapture = 0;
	private long mFocusStartTime;
	private long mFocusCallbackTime;
	private long mCaptureStartTime;
	private long mShutterCallbackTime;
	private long mRawPictureCallbackTime;
	private long mJpegPictureCallbackTime;
	private final AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
	private final ShutterCallback mShutterCallback = new ShutterCallback();
	private final RawPictureCallback mRawPictureCallback = new RawPictureCallback();
	private static final String CAMERA_IMAGE_BUCKET_NAME = Environment
			.getExternalStorageDirectory().toString()
			+ "/CITTest/Camera/";
	private String mModeName = "";
	private String mIsoName = "ISO";
	private int mAFEngMode = 0;
	private int mRawCaptureMode = 1;
	private int mRawCaptureType = 0;
	private String mAntiFlicker = "50";
	private String mISO = "AUTO";

	public static final int CAPTURE_ID = Menu.FIRST;

	private String TAG = "TestCamera";

	private final int DIALOG_PROGRESS = 1000;
	private ProgressDlgHandler mProgressDlgHandler = new ProgressDlgHandler();
	private final int EVENT_FULL_SCAN_START = 100;
	private final int EVENT_FULL_SCAN_COMPLETE = 101;
	private final int EVENT_COMPLETE_CAPTURE = 102;
	private final int EVENT_START_CAPTURE = 103;

	private final int EVENT_WAIT_FOCUES = 104;
	private final int EVENT_FOCUES_COMPLETE = 105;

	private boolean mIsTest = false;
	private boolean mProgressDlgExists = false;

	private boolean mIsRawCapture = false;
	private String mRawCaptureFileName;

	private boolean mIsOnPause = false;
	private int mPos = 0;
	private int mStep = 1; // jump steps each shot
	private int mStage = 0; // used for AF mode five
	private TextView mShotNum;
	private static final int MSG_AF_MODE1_EVENT = 1001;
	private static final int MSG_AF_MODE3_EVENT = 1003;
	private static final int MSG_AF_MODE4_EVENT = 1004;
	private static final int MSG_AF_MODE5_EVENT = 1005;
	private static final int MSG_RENEW_SHOTNUM = 1006;
	private static final int MSG_REPEAT_COMPLETED = 1007;
	private AFMode0Thread mode0Thread;
	private mAFMode1FirstThread threadFirst;
	private AFMode1Thread mode1Thread;
	private AFMode2Thread mode2Thread;
	private AFMode3Thread mode3Thread;
	private AFMode4Thread mode4Thread;
	private AFMode5Thread mode5Thread;
	// private RawCaptureThread mRawCaptureThread;
	private boolean mCanBack = true;

	private Button mCaptureBtn;
	private boolean mStoreImgaesFlag = false;
	private Rect mPreviewRect = new Rect();

	private boolean mFocusFlag = true; // avoid waiting for first time autofocus
	// (ky chen)
	private boolean mIsFocusKeyPress = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.v(TAG, "onCreate start 111");
		setContentView(R.layout.camera_preview);
		Log.v(TAG, "onCreate start after setContentView");

		mCaptureBtn = (Button) findViewById(R.id.capture_btn);
		mCaptureBtn.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					Log.v(TAG, "mCaptureBtn key down!");
					if (mIsTest) {
						return false;
					}
					mIsFocusKeyPress = false;
					if (null != mCamera) {
					} else {
						Log.v(TAG, "mCamera is null!");
					}

				}
				if (event.getAction() == MotionEvent.ACTION_UP) {
					Log.v(TAG, "mCaptureBtn key up!");
					mProgressDlgHandler.sendEmptyMessage(EVENT_WAIT_FOCUES);
				}
				return false;
			}
		});
		Log.v(TAG, "onCreate end");
		initResourceRefs();
	}

	private void initResourceRefs() {
		Window win = getWindow();
		win.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File file = new File(CAMERA_IMAGE_BUCKET_NAME);
			if (!file.isDirectory()) {
				file.mkdirs();
			}
			mIssdcardExist = true;
		} else {
			mIssdcardExist = false;
		}
		btn_success = (Button) findViewById(R.id.btn_success);
		btn_fail = (Button) findViewById(R.id.btn_fail);
		btn_success.setOnClickListener(this);
		btn_fail.setOnClickListener(this);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "onResume ");
		if (mProgressDlgExists) {
			return;
		}
		mIsPreviewing = 0;
		mIsFocused = 0;
		mIsCapture = 0;

		mCameraPreview = (SurfaceView) findViewById(R.id.camera_preview);

		SurfaceHolder holder = mCameraPreview.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		mIsTest = false;
		mIsOnPause = false;
	}

	@Override
	public void onPause() {
		Log.v(TAG, "super onPause.");
		if (mProgressDlgExists) {
			// mProgressDlgExists = false;
			mProgressDlgHandler.sendEmptyMessage(EVENT_FULL_SCAN_COMPLETE);
			// return;
		}
		mIsOnPause = true;
		if (1 == mIsPreviewing) {
			stopPreview();
		}
		closeCamera();
		mIsPreviewing = 0;
		mIsOnPause = false;
		this.finish();
		super.onPause();
		Log.v(TAG, "super onPause end.");
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_PROGRESS) {
			ProgressDialog dialog = new ProgressDialog(this);
			dialog.setMessage("It is in full scan, please wait......");
			dialog.setCancelable(false);
			return dialog;
		}
		return null;
	}

	private void setFocusRectangle(int isDraw) {
		if (null != mCameraParam && null != mCamera) {
			//mCameraParam.setFocusDrawMode(isDraw);//delete for build error by wangying for temp
			updatePreviewRectToCamera();
			mCamera.setParameters(mCameraParam);
			Log.v(TAG, "setFocusRectangle OK! isDraw = " + isDraw);
		} else {
			Log.v(TAG, "setFocusRectangle NG!");
		}
	}

	private void updatePreviewRectToCamera() {
		// get preview offset and update
		View preview = findViewById(R.id.camera_preview);
		int[] loc = new int[2];
		preview.getLocationOnScreen(loc);
		Log.v("fengxuanyang", "loc[0]="+loc[0]+" ---loc[1]="+ loc[1]);  //0  74

		mPreviewRect.set(loc[0], loc[1], loc[0] + preview.getWidth(), loc[1]
				+ preview.getHeight());
//		mPreviewRect.set(loc[0], loc[0],320, 480);

		// for FD
		Log.v(TAG, "preview: " + rectToCameraString(mPreviewRect));
		// Should be the value before rotate
		// if rotate, set to y,x,h,w
		// mParameters.setDisplayRegion(mPreviewRect.top, mPreviewRect.left,
		// mPreviewRect.height(),
		// mPreviewRect.width(), 1);
	//delete for build error by wangying for temp
	/*	mCameraParam.setDisplayRegion(mPreviewRect.left, mPreviewRect.top,
				mPreviewRect.width(), mPreviewRect.height(),
				getWindowManager().getDefaultDisplay().getRotation()
//				Surface.ROTATION_90
//				3
				);*/
	}

	private String rectToCameraString(Rect r) {
		StringBuilder sb = new StringBuilder(32);

		sb.append(r.left);
		sb.append("x");
		sb.append(r.top);
		sb.append("x");
		sb.append(r.right);
		sb.append("x");
		sb.append(r.bottom);
		return sb.toString();
	}

	private class ProgressDlgHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_FULL_SCAN_START:
				showDialog(DIALOG_PROGRESS);
				mProgressDlgExists = true;
				break;
			case EVENT_FULL_SCAN_COMPLETE:
				dismissDialog(DIALOG_PROGRESS);
				mProgressDlgExists = false;
				break;
			case EVENT_COMPLETE_CAPTURE:
				mCaptureBtn.setEnabled(true);
				Log.v(TAG, "Enabled mCaptureBtn");
				Log.v(TAG, "After Enabled mCaptureBtn mFocusFlag = "
						+ mFocusFlag);
				break;
			case EVENT_START_CAPTURE:
				mCaptureBtn.setEnabled(false);
				Log.v(TAG, "Disabled mCaptureBtn");
				break;
			case EVENT_WAIT_FOCUES:
				new WaitFocusThread().start();
				Log.v(TAG, "EVENT_WAIT_FOCUES");
				break;
			case EVENT_FOCUES_COMPLETE:
				Log.v(TAG, "After while mFocusFlag = " + mFocusFlag);
				CapturePicture();
				mFocusFlag = false;
				Log.v(TAG, "After CapturePicture = " + mFocusFlag);
				mProgressDlgHandler.sendEmptyMessage(EVENT_START_CAPTURE);
				Log.v(TAG, "EVENT_FOCUES_COMPLETE");
				break;
			}
		}
	}

	class WaitFocusThread extends Thread {

		@Override
		public void run() {
			int i = 0;
			Log.v(TAG, "Before while mFocusFlag = " + mFocusFlag);
			while (!mFocusFlag) {
				Log.v(TAG, "Waiting for focus!");
				Sleep(200);
				i++;
				if (i >= 20) {
					break;
				}
				Log.v(TAG, "Waiting for focus! i = " + i);
			}
			mProgressDlgHandler.sendEmptyMessage(EVENT_FOCUES_COMPLETE);
			Log.v(TAG, "WaitFocusThread");
		}

	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		if (mProgressDlgExists) {
			return;
		}
		// mDispW = w;
		// mDispH = h;
		Log.v(TAG, "surfaceChanged width is : " + w);
		Log.v(TAG, "surfaceChanged height is : " + h);

		// mCameraParam = mCamera.getParameters();
		// mCameraParam.set("focus-mode", "auto");
		// mCamera.setParameters(mCameraParam);
		Log.v(TAG, "before startPreview ");
		startPreview();

		Log.v(TAG, "after startPreview ");
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.v(TAG, "surfaceCreated start");
		if (mProgressDlgExists) {
			return;
		}
		openCamera();
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			closeCamera();
			Log.v(TAG, "surfaceCreated closeCamera ");
		}
		Log.v(TAG, "surfaceCreated end");
	}

	public void surfaceDestroyed(SurfaceHolder arg0) {
		Log.v(TAG, "enter surfaceDestroyed ");
		if (mProgressDlgExists) {
			mProgressDlgHandler.sendEmptyMessage(EVENT_FULL_SCAN_COMPLETE);
			// mProgressDlgExists = false;
			// return;
		}
		stopPreview();
		closeCamera();
		Log.v(TAG, "surfaceDestroyed closeCamera ");
	}

	private void openCamera() {
		if (mCamera == null) {
			Log.v("fengxuanyang", Camera.getNumberOfCameras()+"");
			mCamera=Camera.open(0);
//			mCamera = Camera.open(1);
			mCameraParam = mCamera.getParameters();
//			mCamera.setDisplayOrientation(270);


			// Added by Shaoying Han 2011-04-25
			Intent intent = getIntent();
			try {
				mISO = intent.getStringExtra("ISO");
				if (TextUtils.isEmpty(mISO)) {
					mISO = "AUTO";
				}
				Log.v(TAG, "intent mISO = " + mISO);
			} catch (Exception e) {
				mISO = "AUTO";
				Log.v(TAG, "User don't choice AntiFlicker or ISO!");
			}

			if (mISO.equals("AUTO") || mISO.equals("1600")) {
				mIsoName = mIsoName + mISO;
			} else {
				mIsoName = mIsoName + "0" + mISO;
			}

			if (null != mCameraParam && null != mCamera) {
				//mCameraParam.setISOSpeedEng(mISO);//delete for build error by wangying for temp
				mCamera.setParameters(mCameraParam);
			}
			Log.v(TAG, "Enter openCamera to init the mCamera.");
			if (null == mCamera) {
				Log.v(TAG, "init the mCamera is null.");
			}
		}
	}

	private boolean judgedSupportedSize(int width, int height) {
		List<Size> supprortSizeList = mCameraParam.getSupportedPictureSizes();
		if (supprortSizeList == null) {
			return false;
		} else if (supprortSizeList.isEmpty()) {
			return false;
		}
		for (Size size : supprortSizeList) {
			if (size.width == width && size.height == height) {
				return true;
			}
		}
		return false;
	}

	private void closeCamera() {
		Log.v(TAG, "closeCamera() start!");
		if (null != mCamera) {
			mCamera.cancelAutoFocus();
			mCamera.setZoomChangeListener(null);
			mCamera.release();
			mCamera = null;
		}
		Log.v(TAG, "closeCamera() end!");
	}

	private void startPreview() {
		Log.v(TAG, "startPreview() start!");
		if (null != mCamera) {
			mCameraParam = mCamera.getParameters();
			// GW616 Camera preview problem
			// Set a preview size that is closest to the viewfinder height and
			// has
			// the right aspect ratio.
			Size size = mCameraParam.getPictureSize();
			if (size != null) {
				Log.v(TAG, "Picturesize.width is " + size.width);
				Log.v(TAG, "Picturesize.height is " + size.height);
			}
			PreviewFrameLayout frameLayout = (PreviewFrameLayout) findViewById(R.id.frame_layout);
			frameLayout.setAspectRatio((double) size.width / size.height);

			List<Size> sizes = mCameraParam.getSupportedPreviewSizes();
			Size optimalSize = null;
			if (size != null && size.height != 0)
				optimalSize = getOptimalPreviewSize(sizes, (double) size.width
						/ size.height);
			if (optimalSize != null) {
				Log.v(TAG, "optimalSize.width is " + optimalSize.width);
				Log.v(TAG, "optimalSize.height is " + optimalSize.height);
				mCameraParam.setPreviewSize(optimalSize.width,
						optimalSize.height);
			} else {
				Log.v("TAG", "optimalSize == null");
			}

			// end
			// mCameraParam.setPreviewSize(mPrvW, mPrvH);
			mCameraParam.set("fps-mode", 0); // Frame rate is normal
			mCameraParam.set("cam-mode", 0); // Cam mode is preview
			Intent intent = getIntent();
			mRawCaptureType = intent.getIntExtra("RawType", 0);
			mCameraParam.set("isp-mode", mRawCaptureType);
			boolean isSupSize = judgedSupportedSize(mPic8MW, mPic8MH);
			if (isSupSize) {
				Log.v(TAG, "Support 8M picture size!");
				mCameraParam.setPictureSize(mPic8MW, mPic8MH);
			}
			if (mCamera == null) {
				return;
			}
			mCamera.setParameters(mCameraParam);
			Log.v(TAG, "startPreview width is : " + mPrvW);
			Log.v(TAG, "startPreview height is : " + mPrvH);

			if (null != mCameraParam) {
				int isDraw = 1;
				setFocusRectangle(isDraw);
				Log.v(TAG, "startPreview()mCameraParam.setFocusDrawMode(1)");

			}
			Log.v(TAG, "startPreview()after mCameraParam.setFocusDrawMode(1)");
			mCamera.startPreview();
			mIsPreviewing = 1;
		} else {
			Log.v(TAG, "when startPreview: mCamera is null.");
		}
		Log.v(TAG, "startPreview() end!");
	}

	private void stopPreview() {
		Log.v(TAG, "stopPreview() start!");
		if (null != mCamera) {
			mCamera.stopPreview();
		}
		mIsPreviewing = 0;
		Log.v(TAG, "stopPreview() end!");
		// clearFocusState();
		// int isDraw = 0;
		// setFocusRectangle(isDraw);
	}

	private Size getOptimalPreviewSize(List<Size> sizes, double targetRatio) {
		final double ASPECT_TOLERANCE = 0.05;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		// Because of bugs of overlay and layout, we sometimes will try to
		// layout the viewfinder in the portrait orientation and thus get the
		// wrong size of mSurfaceView. When we change the preview size, the
		// new overlay will be created before the old one closed, which causes
		// an exception. For now, just get the screen size

		Display display = getWindowManager().getDefaultDisplay();
		int targetHeight = Math.min(display.getHeight(), display.getWidth());

		if (targetHeight <= 0) {
			// We don't know the size of SurefaceView, use screen height
			WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
			targetHeight = windowManager.getDefaultDisplay().getHeight();
		}

		// try to find a size larger but closet to the desired preview size
		for (Size size : sizes) {
			if (targetHeight > size.height)
				continue;

			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// not found, apply origional policy.
		if (optimalSize == null) {

			// Try to find an size match aspect ratio and size
			for (Size size : sizes) {
				double ratio = (double) size.width / size.height;
				if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
					continue;
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			Log.i(TAG, "No preview size match the aspect ratio");
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// switch (keyCode) {
	// case KeyEvent.KEYCODE_CAMERA:
	// Log.v(TAG, "KEYCODE_CAMERA mIsTest = " + mIsTest);
	// if (mIsTest) {
	// return false;
	// }
	// if (!mStoreImgaesFlag) {
	// mIsFocusKeyPress = true;
	// this.CapturePicture();
	// } else {
	// Log.v(TAG, "It is in storing images!");
	// }
	//
	// return true;
	// case KeyEvent.KEYCODE_DPAD_CENTER:
	// return true;
	// case KeyEvent.KEYCODE_SEARCH:
	// return true;
	// case KeyEvent.KEYCODE_FOCUS:
	// return true;
	// }
	// return super.onKeyDown(keyCode, event);
	// }

	private boolean judgeSdcard() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			mIssdcardExist = true;
		} else {
			mIssdcardExist = false;
		}

		if (false == mIssdcardExist) {
			if (mIsTest) {
				mIsTest = false;
				mHandler.sendEmptyMessage(MSG_REPEAT_COMPLETED);
			}
			if (null != mCameraParam) {
				int isDraw = 0;
				setFocusRectangle(isDraw);
				Log.v(TAG, "judgeSdcard()  mCameraParam.setFocusDrawMode(0)");

			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("SD Card not available");
			builder.setMessage("Please insert an SD Card.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
			return false;
		} else {
			return true;
		}
	}

	private void CapturePicture() {

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_SHARED)) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("sdcard is busy");
			builder.setMessage("Sorry, your SD card is busy.");
			builder.setPositiveButton("OK", null);
			builder.create().show();
			return;
		}

		Log.v(TAG, "CapturePicture()->judgeSdcard()");
		boolean isSDCard = judgeSdcard();
		if (!isSDCard) {
			return;
		}
		if (true == mIsTest) {
			Toast.makeText(this, "It is in capturing, can not repeat capture.",
					Toast.LENGTH_LONG).show();
			if (true == mProgressDlgExists) {
				showDialog(DIALOG_PROGRESS);
			}
			return;
		}

		startPreview(); // set 3a parameter need after startPreview (ky chen)

		Intent intent = getIntent();
		// int TestMode = intent.getIntExtra("TestMode", 0);
		mRawCaptureMode = intent.getIntExtra("RawCaptureMode", 1);
		Log.v(TAG, "intent get Raw capture mode is " + mRawCaptureMode);
		mRawCaptureType = intent.getIntExtra("RawType", 0);
		Log.v(TAG, "intent get Raw Type  is " + mRawCaptureType);

		try {
			mAntiFlicker = intent.getStringExtra("AntiFlicker");
			if (TextUtils.isEmpty(mAntiFlicker)) {
				mAntiFlicker = "50"; 
			}
			Log.v(TAG, "intent get mAntiFlicker = " + mAntiFlicker);
			// mISO = intent.getStringExtra("ISO");
			// if (TextUtils.isEmpty(mISO)) {
			// mISO = "AUTO";
			// }
			// Log.v(TAG, "intent mISO = " + mISO);
		} catch (Exception e) {
			mAntiFlicker = "50";
			// mISO = "AUTO";
			Log.v(TAG, "User don't choice AntiFlicker!");
		}

		if (null != mCameraParam) {
			Log.v(TAG, "setAntibanding start!");
			mCameraParam.setAntibanding(mAntiFlicker);

		}
		//
		// Log.v(TAG, "The value of mRawCaptureMode is :" + mRawCaptureMode);
		// Log.v(TAG, "The value of mRawCaptureType is :" + mRawCaptureType);
		// if (1 == TestMode)// AF Mode
		// {
		mMode = intent.getIntExtra("AFMode", -1);
		mStep = intent.getIntExtra("AFStep", 1);

		Log.v(TAG, "The value of AFMode is :" + mMode);
		Log.v(TAG, "The value of AFStep is :" + mStep);

		if (mRawCaptureMode != 3) { // not JPEG Only
			mIsRawCapture = true;
			if (mRawCaptureMode == 1) {
				mModeName = "Preview";
			} else if (mRawCaptureMode == 2) {
				mModeName = "Image";
			}
			if (null != mCameraParam) {
				mCameraParam.set("rawsave-mode", mRawCaptureMode);
				mCameraParam.set("isp-mode", mRawCaptureType);
				// mCameraParam.setISOSpeedEng(mISO);
				long dateTaken = System.currentTimeMillis();
				mRawCaptureFileName = CAMERA_IMAGE_BUCKET_NAME + mModeName
						+ createName(dateTaken) + mIsoName;
				mCameraParam.set("rawfname", mRawCaptureFileName + ".raw");
				if (mCamera == null) {
					return;
				}
				mCamera.setParameters(mCameraParam);
				Log.v(TAG, "Set raw name success!");
			} else {
				Log.v(TAG, "mCameraParam is null when set rawsave-mode !");
			}
		}
		Log.v(TAG, "mMode = " + mMode);
//		switch (mMode) {
//		case 0:
//			captureMode0();
//			break;
//		case 1:
//			mShotNum.setText("");
//			mShotNum.setVisibility(View.VISIBLE);
//			captureMode1();
//			break;
//		case 2:
//			captureMode2();
//			break;
//		case 3:
//			mShotNum.setText("");
//			mShotNum.setVisibility(View.VISIBLE);
//			captureMode3();
//			break;
//		case 4:
//			mShotNum.setText("");
//			mShotNum.setVisibility(View.VISIBLE);
//			captureMode4();
//			break;
//		case 5:
//			mShotNum.setText("");
//			mShotNum.setVisibility(View.VISIBLE);
//			captureMode5();
//			break;
//		}
		// } else if (2 == TestMode) // RawCapture Mode
		// {
		// RawCapture();
		// } else // not select mode yet
		// {
		// // int isDraw = 0;
		// // setFocusRectangle(isDraw);
		// mProgressDlgHandler.sendEmptyMessage(EVENT_COMPLETE_CAPTURE);
		// mStoreImgaesFlag = false;
		// Toast.makeText(this, "Please select the test mode first!",
		// Toast.LENGTH_LONG).show();
		// }
	}

	private void captureMode0() {
		Log.v(TAG, "Enter captureMode0 function.");
		Log.v(TAG, "captureMode0()->judgeSdcard()");
		boolean isSDCard = judgeSdcard();
		if (!isSDCard) {
			return;
		}
		mode0Thread = new AFMode0Thread();
		mode0Thread.start();
	}

	class AFMode0Thread extends Thread {
		public void run() {
			Log.v(TAG, "AFMode0Thread");
			mIsTest = true;

			mCameraParam.set("focus-mode", "auto");
			mCameraParam.set("focus-meter", "spot");
			if (mCamera == null) {
				return;
			}
			mCamera.setParameters(mCameraParam);
			mFocusStartTime = System.currentTimeMillis();
			// if (mIsFocusKeyPress) {
			mIsFocused = 0;
			mCamera.autoFocus(mAutoFocusCallback);
			// }

			mCanBack = false;
			takePicture();

			startPreview();
			mIsTest = false;
			mCanBack = true;
			Sleep(2000);
			Log
					.v(TAG, "mAFMode1FirstThread after Sleep(2000) mMode = "
							+ mMode);

			mProgressDlgHandler.sendEmptyMessage(EVENT_COMPLETE_CAPTURE);
			mStoreImgaesFlag = false;

			Log.v(TAG, "mAFMode1FirstThread finish.");
			Log.v(TAG, "mAFMode1FirstThread mCanBack = " + mCanBack);
		}
	}

	private void captureMode1() {
		Log.v(TAG, "Enter captureMode1 function.");
		Log.v(TAG, "captureMode1()->judgeSdcard()");
		boolean isSDCard = judgeSdcard();
		if (!isSDCard) {
			return;
		}
//		if (mMode != 0) {
//			mShotNum.setText("One AF");
//		}
		threadFirst = new mAFMode1FirstThread();
		threadFirst.start();
	}

	class mAFMode1FirstThread extends Thread {
		public void run() {
			Log.v(TAG, "mAFMode1FirstThread");
			mIsTest = true;
			mAFEngMode = Camera.Parameters.FOCUS_ENG_MODE_NONE;
			// mCameraParam.set("afeng-mode", mAFEngMode);
			mCameraParam.setFocusEngMode(mAFEngMode);
			mCameraParam.set("focus-mode", "auto");
			mCameraParam.set("focus-meter", "spot");
			mCamera.setParameters(mCameraParam);
			mFocusStartTime = System.currentTimeMillis();
			// if (mIsFocusKeyPress) {
			mIsFocused = 0;
			if (mCamera == null) {
				return;
			}
			mCamera.autoFocus(mAutoFocusCallback);
			// }

			mCanBack = false;
			takePicture();
			mAFEngMode = Camera.Parameters.FOCUS_ENG_MODE_BRACKET;
			// mCameraParam.set("afeng-mode", mAFEngMode);
			mCameraParam.setFocusEngMode(mAFEngMode);
			mCameraParam.set("focus-mode", "manual");
			if (mCamera == null) {
				return;
			}
			mCamera.setParameters(mCameraParam);
			mPos = 0;
			// mCameraParam.set("afeng-pos", mPos);
			mCameraParam.setFocusEngStep(-24 * mStep);
			mCamera.setParameters(mCameraParam);
			startPreview();
			mCanBack = true;
			Sleep(2000);
			mHandler.sendEmptyMessage(MSG_AF_MODE1_EVENT);

			Log.v(TAG, "mAFMode1FirstThread finish.");
		}
	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			boolean isSDCard;
			switch (msg.what) {

			case MSG_AF_MODE1_EVENT:
				isSDCard = judgeSdcard();
				if (!isSDCard) {
					return;
				}
				mode1Thread = new AFMode1Thread();
				mode1Thread.start();
				break;
			case MSG_AF_MODE3_EVENT:
				isSDCard = judgeSdcard();
				if (!isSDCard) {
					return;
				}
				mode3Thread = new AFMode3Thread();
				mode3Thread.start();
				break;
			case MSG_AF_MODE4_EVENT:
				isSDCard = judgeSdcard();
				if (!isSDCard) {
					return;
				}
				mode4Thread = new AFMode4Thread();
				mode4Thread.start();
				break;
			case MSG_AF_MODE5_EVENT:
				isSDCard = judgeSdcard();
				if (!isSDCard) {
					return;
				}
				mode5Thread = new AFMode5Thread();
				mode5Thread.start();
				break;
			case MSG_RENEW_SHOTNUM:
				isSDCard = judgeSdcard();
				if (!isSDCard) {
					return;
				}
//				switch (mMode) {
//				case 1:
//					mShotNum.setText("Mode 1:" + (mPos + 1) + "/50");
//					break;
//				case 2:
//					break;
//				case 3:
//					mShotNum.setText("Mode 3:" + (mPos + 1) + "/50");
//					break;
//				case 4:
//					mShotNum.setText("Mode 4:" + (mPos + 1) + "/50");
//					break;
//				case 5:
//					if (mStage == 2) {
//						mShotNum.setText("Mode 5_Full:" + (mPos + 1) + "/50");
//					} else if (mStage == 1) {
//						mShotNum.setText("Mode 5_Auto:" + (mPos + 1) + "/50");
//					}
//					break;
//				default:
//					break;
//				}
				break;
//			case MSG_REPEAT_COMPLETED:
//				mShotNum.setText("");
//				mShotNum.setVisibility(View.GONE);
			default:
				break;
			}
		}
	};

	@Override
	public void onBackPressed() {
		Log.v(TAG, "onBackPressed() mCanBack = " + mCanBack);
		if (false == mCanBack) {
			return;
		}
		super.onBackPressed();
	}

	class AFMode1Thread extends Thread {
		public void run() {
			Log.v(TAG, "mAFMode1Thread");
			if (true == mIsOnPause) {
				mHandler.removeMessages(MSG_AF_MODE1_EVENT);
				return;
			}
			mCanBack = false;
			mHandler.sendEmptyMessage(MSG_RENEW_SHOTNUM);
			mIsFocused = 0;
			if (mCamera == null) {
				return;
			}
			mCamera.autoFocus(mAutoFocusCallback);
			takePicture();
			mPos++;
			// mCameraParam.set("afeng-pos", mPos);
			mCameraParam
					.setFocusEngMode(Camera.Parameters.FOCUS_ENG_MODE_BRACKET);
			mCameraParam.setFocusEngStep((mPos - 24) * mStep);
			if (mCamera == null) {
				return;
			}
			mCamera.setParameters(mCameraParam);
			startPreview();

			// change raw file name (ky chen)
			long dateTaken = System.currentTimeMillis();
			mRawCaptureFileName = CAMERA_IMAGE_BUCKET_NAME + mModeName
					+ createName(dateTaken) + mIsoName;
			mCameraParam.set("rawfname", mRawCaptureFileName + ".raw");
			if (mCamera == null) {
				return;
			}
			mCamera.setParameters(mCameraParam);

			mCanBack = true;
			Sleep(2000);
			if (false == mIsOnPause && mPos < 50) {
				mHandler.sendEmptyMessage(MSG_AF_MODE1_EVENT);
			}
			if (mPos >= 50) {
				mIsTest = false;
				mHandler.sendEmptyMessage(MSG_REPEAT_COMPLETED);
				mProgressDlgHandler.sendEmptyMessage(EVENT_COMPLETE_CAPTURE);
				mStoreImgaesFlag = false;
			}

		}
	}

	private void captureMode2() {
		Log.v(TAG, "Enter captureMode2 function.");
		Log.v(TAG, "captureMode2()->judgeSdcard()");
		boolean isSDCard = judgeSdcard();
		if (!isSDCard) {
			return;
		}
		mode2Thread = new AFMode2Thread();
		mode2Thread.start();
	}

	class AFMode2Thread extends Thread {
		public void run() {
			Log.v(TAG, "mAFMode2Thread");

			mIsTest = true;
			mCanBack = true;
			mAFEngMode = Camera.Parameters.FOCUS_ENG_MODE_FULLSCAN;
			// mCameraParam.set("afeng-mode", mAFEngMode);
			mCameraParam.setFocusEngMode(mAFEngMode);
			mCameraParam.setFocusEngStep(mStep); // variable step for fullscan
			// (ky chen)
			mCameraParam.set("focus-mode", "fullscan");
			mCamera.setParameters(mCameraParam);
			// if (mIsFocusKeyPress) {
			mIsFocused = 0;
			if (mCamera == null) {
				return;
			}
			mCamera.autoFocus(mAutoFocusCallback);
			// }
			mProgressDlgHandler.sendEmptyMessage(EVENT_FULL_SCAN_START);

			mCanBack = false;
			takePicture();
			startPreview();
			mCanBack = true;
			mIsTest = false;
			Sleep(2000);

			mProgressDlgHandler.sendEmptyMessage(EVENT_COMPLETE_CAPTURE);
			mStoreImgaesFlag = false;

		}
	}

	private void captureMode3() {
		Log.v(TAG, "Enter captureMode3 function.");
		Log.v(TAG, "captureMode3()->judgeSdcard()");
		boolean isSDCard = judgeSdcard();
		if (!isSDCard) {
			return;
		}
		mPos = 0;
		mAFEngMode = Camera.Parameters.FOCUS_ENG_MODE_FULLSCAN_REPEAT;
		// mCameraParam.set("afeng-mode", mAFEngMode);
		mCameraParam.setFocusEngMode(mAFEngMode);
		mCameraParam.setFocusEngStep(mStep); // variable step for fullscan (ky
		// chen)
		mCameraParam.set("focus-mode", "fullscan");
		if (mCamera == null) {
			return;
		}
		mCamera.setParameters(mCameraParam);
		mode3Thread = new AFMode3Thread();
		mode3Thread.start();
	}

	class AFMode3Thread extends Thread {
		public void run() {
			Log.v(TAG, "mAFMode3Thread");
			if (true == mIsOnPause) {
				mHandler.removeMessages(MSG_AF_MODE3_EVENT);
				return;
			}

			mIsTest = true;
			// if (mIsFocusKeyPress) {
			mIsFocused = 0;
			if (mCamera == null) {
				return;
			}
			mCamera.autoFocus(mAutoFocusCallback);
			// }
			mProgressDlgHandler.sendEmptyMessage(EVENT_FULL_SCAN_START);
			mHandler.sendEmptyMessage(MSG_RENEW_SHOTNUM);
			mCanBack = false;
			takePicture();
			mPos++;
			startPreview();

			// change raw file name (ky chen)
			long dateTaken = System.currentTimeMillis();
			mRawCaptureFileName = CAMERA_IMAGE_BUCKET_NAME + mModeName
					+ createName(dateTaken) + mIsoName;
			mCameraParam.set("rawfname", mRawCaptureFileName + ".raw");
			if (mCamera == null) {
				return;
			}
			mCamera.setParameters(mCameraParam);

			mCanBack = true;
			Sleep(2000);

			if (false == mIsOnPause && mPos < 50) {
				mHandler.sendEmptyMessage(MSG_AF_MODE3_EVENT);
			}
			if (mPos >= 50) {
				mIsTest = false;
				mHandler.sendEmptyMessage(MSG_REPEAT_COMPLETED);
				mProgressDlgHandler.sendEmptyMessage(EVENT_COMPLETE_CAPTURE);
				mStoreImgaesFlag = false;
			}

		}
	}

	private void captureMode4() {
		Log.v(TAG, "Enter captureMode4 function.");
		Log.v(TAG, "captureMode4()->judgeSdcard()");
		boolean isSDCard = judgeSdcard();
		if (!isSDCard) {
			return;
		}
		mPos = 0;
		mAFEngMode = Camera.Parameters.FOCUS_ENG_MODE_REPEAT;
		// mCameraParam.set("afeng-mode", mAFEngMode);
		mCameraParam.setFocusEngMode(mAFEngMode);
		// mCameraParam.setFocusEngStep(0);
		mCameraParam.set("focus-mode", "auto");
		mCameraParam.set("focus-meter", "spot");
		if (mCamera == null) {
			return;
		}
		mCamera.setParameters(mCameraParam);
		mode4Thread = new AFMode4Thread();
		mode4Thread.start();
	}

	class AFMode4Thread extends Thread {
		public void run() {
			Log.v(TAG, "mAFMode4Thread");
			if (true == mIsOnPause) {
				mHandler.removeMessages(MSG_AF_MODE4_EVENT);
				return;
			}

			Log.v(TAG, "mAFMode4Thread->judgeSdcard()");
			boolean isSDCard = judgeSdcard();
			if (!isSDCard) {
				Log.v(TAG, "No SdCard in AFMode4Thread!");
				this.stop();
			}
			mIsTest = true;
			// if (mIsFocusKeyPress) {
			mIsFocused = 0;
			if (mCamera == null) {
				return;
			}
			mCamera.autoFocus(mAutoFocusCallback);
			// }
			mCanBack = false;
			mHandler.sendEmptyMessage(MSG_RENEW_SHOTNUM);
			takePicture();
			mPos++;
			startPreview();

			// change raw file name (ky chen)
			long dateTaken = System.currentTimeMillis();
			mRawCaptureFileName = CAMERA_IMAGE_BUCKET_NAME + mModeName
					+ createName(dateTaken) + mIsoName;
			mCameraParam.set("rawfname", mRawCaptureFileName + ".raw");
			if (mCamera == null) {
				return;
			}
			mCamera.setParameters(mCameraParam);

			mCanBack = true;
			Sleep(2000);

			if (false == mIsOnPause && mPos < 50) {
				mHandler.sendEmptyMessage(MSG_AF_MODE4_EVENT);
			}
			if (mPos >= 50) {
				mIsTest = false;
				mHandler.sendEmptyMessage(MSG_REPEAT_COMPLETED);
				mProgressDlgHandler.sendEmptyMessage(EVENT_COMPLETE_CAPTURE);
				mStoreImgaesFlag = false;
			}

		}
	}

	private void captureMode5() {
		Log.v(TAG, "Enter captureMode5 function.");
		Log.v(TAG, "captureMode5()->judgeSdcard()");
		boolean isSDCard = judgeSdcard();
		if (!isSDCard) {
			return;
		}
		mPos = 0;
		mStage = 1;
		mAFEngMode = Camera.Parameters.FOCUS_ENG_MODE_REPEAT;
		// mCameraParam.set("afeng-mode", mAFEngMode);
		mCameraParam.setFocusEngMode(mAFEngMode);
		// mCameraParam.setFocusEngStep(0);
		mCameraParam.set("focus-mode", "auto");
		mCameraParam.set("focus-meter", "spot");
		if (mCamera == null) {
			return;
		}
		mCamera.setParameters(mCameraParam);
		mode5Thread = new AFMode5Thread();
		mode5Thread.start();
	}

	class AFMode5Thread extends Thread {
		public void run() {
			Log.v(TAG, "mAFMode5Thread");
			if (true == mIsOnPause) {
				mHandler.removeMessages(MSG_AF_MODE5_EVENT);
				return;
			}

			mIsTest = true;
			if (mStage == 1) {
				// if (mIsFocusKeyPress) {
				mIsFocused = 0;
				if (mCamera == null) {
					return;
				}
				mCamera.autoFocus(mAutoFocusCallback);
				// }
				mCanBack = false;
				mHandler.sendEmptyMessage(MSG_RENEW_SHOTNUM);
				takePicture();
				mPos++;
				startPreview();

				// change raw file name (ky chen)
				long dateTaken = System.currentTimeMillis();
				mRawCaptureFileName = CAMERA_IMAGE_BUCKET_NAME + mModeName
						+ createName(dateTaken) + mIsoName;
				mCameraParam.set("rawfname", mRawCaptureFileName + ".raw");
				if (mCamera == null) {
					return;
				}
				mCamera.setParameters(mCameraParam);

				mCanBack = true;
				Sleep(2000);
				if (false == mIsOnPause && mPos < 50) {
					mHandler.sendEmptyMessage(MSG_AF_MODE5_EVENT);
				}
				if (mPos >= 50) {
					mStage = 2;
					mPos = 0;
					mAFEngMode = Camera.Parameters.FOCUS_ENG_MODE_FULLSCAN_REPEAT;
					// mCameraParam.set("afeng-mode", mAFEngMode);
					mCameraParam.setFocusEngMode(mAFEngMode);
					mCameraParam.setFocusEngStep(mStep); // variable step for
					// fullscan (ky
					// chen)
					mCameraParam.set("focus-mode", "fullscan");
					if (mCamera == null) {
						return;
					}
					mCamera.setParameters(mCameraParam);
					mHandler.sendEmptyMessage(MSG_AF_MODE5_EVENT);// start mode
					// 5 stage
					// 2:Full
					// focus shot.
				}
			} else if (mStage == 2) {
				// if (mIsFocusKeyPress) {
				mIsFocused = 0;
				if (mCamera == null) {
					return;
				}
				mCamera.autoFocus(mAutoFocusCallback);
				// }
				mProgressDlgHandler.sendEmptyMessage(EVENT_FULL_SCAN_START);
				mHandler.sendEmptyMessage(MSG_RENEW_SHOTNUM);
				mCanBack = false;
				takePicture();
				mPos++;
				startPreview();

				// change raw file name (ky chen)
				long dateTaken = System.currentTimeMillis();
				mRawCaptureFileName = CAMERA_IMAGE_BUCKET_NAME + mModeName
						+ createName(dateTaken) + mIsoName;
				mCameraParam.set("rawfname", mRawCaptureFileName + ".raw");
				if (mCamera == null) {
					return;
				}
				mCamera.setParameters(mCameraParam);

				mCanBack = true;
				Sleep(2000);

				if (false == mIsOnPause && mPos < 50) {
					mHandler.sendEmptyMessage(MSG_AF_MODE5_EVENT);
				}

				if (mPos >= 50) {
					mIsTest = false;
					mStage = 0;
					mHandler.sendEmptyMessage(MSG_REPEAT_COMPLETED);
					mProgressDlgHandler
							.sendEmptyMessage(EVENT_COMPLETE_CAPTURE);
					mStoreImgaesFlag = false;
				}
			} else {/* Throw NotEx */
			}

		}
	}

	private final class AutoFocusCallback implements
			android.hardware.Camera.AutoFocusCallback {
		public void onAutoFocus(boolean focused, android.hardware.Camera camera) {
			mFocusCallbackTime = System.currentTimeMillis();
			mIsFocused = 1;
			Log.v(TAG, "mAFEngMode value is " + mAFEngMode);
			if (Camera.Parameters.FOCUS_ENG_MODE_FULLSCAN == mAFEngMode
					|| Camera.Parameters.FOCUS_ENG_MODE_FULLSCAN_REPEAT == mAFEngMode) {
				Log
						.v(TAG,
								"AutoFocusCallback send EVENT_FULL_SCAN_COMPLETE message ");
				mProgressDlgHandler.sendEmptyMessage(EVENT_FULL_SCAN_COMPLETE);
			}
			Log.v(TAG,
					"In mAutoFocusCallback before set CapturePicture mFocusFlag = "
							+ mFocusFlag);
			mFocusFlag = focused;
			Log.v(TAG,
					"In mAutoFocusCallback after set CapturePicture mFocusFlag = "
							+ mFocusFlag);
			// int isDraw = 1;
			// setFocusRectangle(isDraw);
		}
	}

	private final class ShutterCallback implements
			android.hardware.Camera.ShutterCallback {
		public void onShutter() {
			mShutterCallbackTime = System.currentTimeMillis();
			// int isDraw = 0;
			// setFocusRectangle(isDraw);
		}
	}

	private final class RawPictureCallback implements PictureCallback {
		public void onPictureTaken(byte[] rawData,
				android.hardware.Camera camera) {
			mRawPictureCallbackTime = System.currentTimeMillis();
		}
	}

	private final class JpegPictureCallback implements PictureCallback {
		public void onPictureTaken(byte[] jpegData,
				android.hardware.Camera camera) {
			mJpegPictureCallbackTime = System.currentTimeMillis();
			if (jpegData != null) {
				storeImage(jpegData);
			}
			mIsCapture = 0;
		}
	}

	private void takePicture() {
		Log.v(TAG, "takePicture() start");
		Log.v(TAG, "takePicture()-> judgeSdcard()");
		boolean isSDCard = judgeSdcard();
		if (!isSDCard) {
			Log.v(TAG, "No SdCard!");
			return;
		}

		while (mIsFocused == 0) {
			Sleep(100);
		}
		mIsCapture = 1;
		mCaptureStartTime = System.currentTimeMillis();
		if (mCamera == null) {
			return;
		}
		mCamera.takePicture(mShutterCallback, mRawPictureCallback,
				new JpegPictureCallback());
		while (mIsCapture == 1) {
			Sleep(100);
		}
		Log.v(TAG, "takePicture() end");
	}

	private void Sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void storeImage(byte[] jpegData) {
		Log.v(TAG, "storeImage()");
		mStoreImgaesFlag = true;
		// long time;
		long dateTaken = System.currentTimeMillis();

		String name = CAMERA_IMAGE_BUCKET_NAME;
		// mode 5,make different name for picture to distinguish between
		// fullscan and auto mode.
		if ((mMode == 5 && mStage == 1) || mMode == 4 || mMode == 0) {
			name += "AF_";
		} else if ((mMode == 5 && mStage == 2) || mMode == 2 || mMode == 3) {
			name += "Fullscan_";
		} else if (mMode == 1) {
			name += "Bracket_";
		}
		name = name + createNameJpeg(dateTaken) + ".jpg";

		if (true == mIsRawCapture) {
			name = mRawCaptureFileName + ".jpg";
		}
		Log.v(TAG, "Jpeg name is " + name);
		File fHandle = new File(name);
		try {
			OutputStream bos = new FileOutputStream(fHandle);
			bos.write(jpegData);
			bos.close();
			// time = System.currentTimeMillis();
		} catch (Exception ex) {
			fHandle.delete();
		}

	}

	private static String createName(long dateTaken) {
		return DateFormat.format("ddkkmmss", dateTaken).toString();
	}

	private static String createNameJpeg(long dateTaken) {
		return DateFormat.format("yyyy-MM-dd kk.mm.ss", dateTaken).toString();
	}

	@Override
	public void onClick(View button) {
		int id = button.getId();
		Bundle b = new Bundle();
		Intent intent = new Intent();

		if (id == R.id.btn_success) {
			b.putInt("test_result", 1);
		} else {
			b.putInt("test_result", 0);
		}
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();		
	}
}
