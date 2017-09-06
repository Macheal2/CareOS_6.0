package com.cappu.magnifiter;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
  
public class MainActivity extends Activity implements SurfaceHolder.Callback {
	private SurfaceView mSurfaceView;
	private SeekBar mSeekBar;
	private int curZoomValue = -1;
	private int maxZoomValue = -1;
	private boolean isFlashOpened = false;
	private Camera.Parameters parameters;
	private Camera mCamera;
	private SurfaceHolder holder;
	private boolean mFlag = false;
	private int mCameraId = 0;
	private static final int BACK_CAMERA = 0;
	private static final int FRONT_CAMERA = 1;
	private static final int ROTATION = 90;
	private static final int REVERT = 180;
	private static final int PREVIEW_WIDTH = 720;
	private static final int PREVIEW_HEIGHT = 1280;

	private ImageView mReduce;
	private ImageView mIncrease;
	private ImageView mFlash;

	private final String dengyingTAG = "dengying";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setTitle(getResources().getText(R.string.app_name));
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		mReduce = (ImageView) findViewById(R.id.reduce);
		mReduce.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				curZoomValue = (curZoomValue == 0 ? curZoomValue : --curZoomValue);

				Log.e(dengyingTAG, "reduce curZoomValue=" + curZoomValue);

				mSeekBar.setProgress(curZoomValue);
				parameters.setZoom(curZoomValue);
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			}
		});

		mIncrease = (ImageView) findViewById(R.id.increase);
		mIncrease.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				curZoomValue = (curZoomValue == maxZoomValue ? curZoomValue : ++curZoomValue);

				Log.e(dengyingTAG, "increase curZoomValue=" + curZoomValue);

				mSeekBar.setProgress(curZoomValue);
				parameters.setZoom(curZoomValue);
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			}
		});

		mFlash = (ImageView) findViewById(R.id.flash);
		mFlash.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isFlashOpened) {
					parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
					mCamera.setParameters(parameters);

					mFlash.setImageResource(R.drawable.flash_open_press);
					isFlashOpened = true;
				} else {
					parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
					mCamera.setParameters(parameters);

					mFlash.setImageResource(R.drawable.flash_close_normal);
					isFlashOpened = false;
				}
			}
		});

		mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);

		mSeekBar = (SeekBar) findViewById(R.id.seekBar);
		// mSeekBar.setProgress(curZoomValue);

		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				parameters.setZoom(curZoomValue);
				mCamera.setParameters(parameters);
				mCamera.startPreview();
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				// TODO Auto-generated method stub
				Log.e(dengyingTAG, "onProgressChanged progress=" + progress);
				curZoomValue = progress;
			}
		});
	}

	@Override
	public void finish() {
		// TODO Auto-generated method stub
		super.finish();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		try {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		holder = mSurfaceView.getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		startCamera();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.e(dengyingTAG, "onTouchEvent");
		
		/*if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Log.e(dengyingTAG, "onTouchEvent ACTION_DOWN");
			//点击时自动对焦
			mCamera.autoFocus(new AutoFocusCallback() {
				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					if (success) {
						Log.e(dengyingTAG, "onTouchEvent onAutoFocus success=" + success);
						camera.cancelAutoFocus();
					}
				}

			});
		}*/
		return super.onTouchEvent(event);
	}

	private void startCamera() {
		// TODO Auto-generated method stub
		if (mFlag) {
			if (mCamera != null) {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}

		try {
			mCamera = Camera.open(mCameraId);
		} catch (RuntimeException e) {
			e.printStackTrace();
			mCamera = null;
		}

		if (mCamera != null) {
			mCamera.setDisplayOrientation(mCameraId == BACK_CAMERA ? ROTATION : ROTATION);
			parameters = mCamera.getParameters();
			parameters.setPictureFormat(PixelFormat.JPEG);
			parameters.set("orientation", "portrait");
			//parameters.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT);

			/*
             FOCUS_MODE_INFINITY 重点是无穷的
             FOCUS_MODE_MACRO 宏观（特写）对焦模式
             FOCUS_MODE_FIXED 焦点是固定的
             FOCUS_MODE_EDOF  扩展景深
			 FOCUS_MODE_AUTO 	自动对焦模式。
			 FOCUS_MODE_CONTINUOUS_PICTURE 	用于拍照的连续自动对焦模式。
			 FOCUS_MODE_CONTINUOUS_VIDEO 	用于视频记录的连续自动对焦模式。
			*/

	        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//用于拍照的连续自动对焦模式,加上它就能自动对焦了,自动对焦只需这一句代码
			parameters.setRotation(mCameraId == BACK_CAMERA ? ROTATION : REVERT + ROTATION);

			curZoomValue = parameters.getZoom();
			maxZoomValue = parameters.getMaxZoom();

			mSeekBar.setMax(maxZoomValue);
			mSeekBar.setProgress(curZoomValue);

			Log.e(dengyingTAG, "startCamera curZoomValue=" + curZoomValue + " MaxZoom=" + maxZoomValue);

			// parameters.setZoom(curZoomValue);
			mCamera.setParameters(parameters);
			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();
				mFlag = true;
			} catch (Exception e) {
				mCamera.release();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// TODO Auto-generated method stub

		Log.e(dengyingTAG, "surfaceChanged");
		
			//自动对焦
			/*mCamera.autoFocus(new AutoFocusCallback() {
						@Override
						public void onAutoFocus(boolean success, Camera camera) {
							if (success) {
								Log.e(dengyingTAG, "surfaceChanged onAutoFocus success=" + success);
								camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
							}
						}
			});*/
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		startCamera();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
	}
}
