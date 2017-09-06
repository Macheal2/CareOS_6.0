package sim.android.mtkcit.testitem;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import sim.android.mtkcit.cittools.CITTools;

import android.util.Log;
import android.graphics.Path; // added by bruce

public class TouchTest extends Activity {
	private static final String TAG = "TouchpanelTest";
	private static final boolean mDebug = false;
	private TPTestView tpView;
	private ArrayList<Rect> rectList;
	private static final int numX = 9;
	private static final int numY = 13;
	private int mDisplayWidth = 480;
	private int mDisplayHeight = 800;
	private int absX;
	private int absY;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (mDebug)
			Log.v(TAG, "start TouchpanelTest");
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		initRec();
		tpView = new TPTestView(this);
		setContentView(tpView);
	}

	private void initRec() {

		Display myDisplay = getWindowManager().getDefaultDisplay();
		mDisplayHeight = myDisplay.getHeight();

		absY = mDisplayHeight / numY;
		mDisplayWidth = myDisplay.getWidth();
		absX = mDisplayWidth / numX;
		Log.i("bruce_nan", "TouchTest_initRec_01: mDisplayHeight = " + mDisplayHeight + "; mDisplayWidth = " + mDisplayWidth);
		Log.i("bruce_nan", "TouchTest_initRec_02: absY = " + absY + "; absX = " + absX);
		rectList = new ArrayList<TouchTest.Rect>(numX * numY);
		for (int i = 0; i < numX; i++) {
			for (int j = 0; j < numY; j += (numY - 1) / 2) {
				Rect r = new Rect(i * absX, j * absY, absX, absY);

				rectList.add(r);
			}
		}

		for (int i = 0; i < numX; i += (numX - 1) / 2) {
			for (int j = 1; j < numY - 1; j++) {
				if (j == (numY - 1) / 2)
					continue;

				Rect r = new Rect(i * absX, j * absY, absX, absY);

				rectList.add(r);
			}
		}

		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				Rect r = new Rect(absX + i * absX * (numX - 1) / 2, absY + j
						* absY * (numY - 1) / 2, (numX - 3) / 2 * absX,
						(numY - 3) / 2 * absY);

				rectList.add(r);

			}
		}
	}

	private void doAfterTest() {
		Bundle b = new Bundle();
		Intent intent = new Intent();
		b.putInt("test_result", 1);

		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();

	}

	private class Rect {
		int x;
		int y;
		int dx;
		int dy;

		Rect(int x, int y, int dx, int dy) {
			this.x = x;
			this.y = y;
			this.dx = dx;
			this.dy = dy;
		}

		public boolean isInRect(int x, int y) {
			if (x >= this.x && x <= this.x + this.dx && y >= this.y
					&& y <= this.y + this.dy) {
				return true;
			} else {
				return false;
			}
		}
	}

	private class TPTestView extends View {
		private Bitmap bitmap;
		private Canvas canvas;
		private Paint paint;
		private Rect tempRect;

        // added by bruce for draw line begin
		private Paint mPaint;
		private Path mPath;
		private float mPosX,mPosY;
        // added by bruce for draw line end
        
		public TPTestView(Context context) {
			super(context);
			paint = new Paint(Paint.DITHER_FLAG);
			bitmap = Bitmap.createBitmap(mDisplayWidth, mDisplayHeight,
					Bitmap.Config.ARGB_8888);
			canvas = new Canvas();
			canvas.setBitmap(bitmap);

			paint.setStyle(Style.FILL);
			paint.setStrokeWidth(5);
			paint.setColor(Color.WHITE);
			paint.setAntiAlias(true);

			Iterator<Rect> iterator = rectList.iterator();
			while (iterator.hasNext()) {
				Rect rect = iterator.next();
				canvas.drawRect(rect.x, rect.y, rect.x + rect.dx - 1, rect.y
						+ rect.dy - 1, paint);
			}
			invalidate();

			paint.setColor(Color.GREEN);

            // added by bruce for draw line begin
            mPaint = new Paint();
            mPaint.setAntiAlias(true);
		    mPaint.setARGB(255,255,0, 0);
		    mPaint.setStyle(Paint.Style.STROKE);
		    mPaint.setStrokeWidth(3);

		    mPath = new Path();
		    // added by bruce for draw line end
		}

		protected void onDraw(Canvas canvas) {
			canvas.drawBitmap(bitmap, 0, 0, null);
			//canvas.drawPath(mPath, mPaint);
		}

		public boolean onTouchEvent(MotionEvent event) {
			int action = event.getAction();
			float x = event.getX();
			float y = event.getY();
			if (mDebug)
				Log.v(TAG, "onTouchEvent" + " x=" + x + " y=" + y);

			if (action == MotionEvent.ACTION_DOWN
					|| action == MotionEvent.ACTION_MOVE) {
				if (rectList.size() == 0) {
					doAfterTest();
				}
				boolean bNeedFindRect = false;
				if (tempRect == null) {
					bNeedFindRect = true;
				} else {
					if (!tempRect.isInRect((int) x, (int) y)) {
						bNeedFindRect = true;
					} else {
						bNeedFindRect = false;
					}

				}
				if (mDebug)
					Log.v(TAG, "onTouchEvent" + " bNeedFindRect="
							+ bNeedFindRect);
				if (bNeedFindRect) {
					Iterator<Rect> iterator = rectList.iterator();
					while (iterator.hasNext()) {
						Rect rect = iterator.next();
						if (rect.isInRect((int) x, (int) y)) {
							tempRect = rect;
							canvas.drawRect(rect.x, rect.y, rect.x + rect.dx,
									rect.y + rect.dy, paint);
							invalidate();
							rectList.remove(rect);
							break;
						}
					}
				}

                // added by bruce for draw line begin
				if (action == MotionEvent.ACTION_DOWN){
                    mPath.moveTo(x, y);
                }else if (action == MotionEvent.ACTION_MOVE){
                    mPath.quadTo(mPosX, mPosY, x, y);
                    canvas.drawPath(mPath, mPaint);
                    invalidate();
                }
                mPosX = x;
                mPosY = y;
                // added by bruce for draw line end
			}

			
			return true;
		}
	}


}
