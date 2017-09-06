package sim.android.mtkcit.testitem;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;

public class MultiTouchActivity extends Activity {
	TouchView touchview;
	private static final int LCD_MENU_QUIT = 1;

	private static int screen_X;
	private static int screen_Y;
	private Bitmap mBitmap;
	private Canvas mCanvas;
	private final String TAG = "MultiTouchActivity";
	private Paint mPaint1;
	private Paint mPaint2;
	private Paint mPaint3;
	private float mRadius = 50f;
	private Paint paint;
	private int blcolor;
	private Circle[] circles;
	private boolean[] passFlags;
	private int toastTimes = 5;

	class TouchView extends View {

		TouchView(Context context) {
			super(context);

			circles = new Circle[4];
			mBitmap = Bitmap.createBitmap(screen_X, screen_Y,
					Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas();
			mCanvas.setBitmap(mBitmap);
			mPaint1 = new Paint();
			mPaint1.setAntiAlias(true);
			mPaint1.setDither(true);
			mPaint1.setStrokeWidth(5F);
			mPaint1.setStyle(android.graphics.Paint.Style.FILL);
			mPaint1.setColor(Color.RED);

			mPaint2 = new Paint();
			mPaint2.setAntiAlias(true);
			mPaint2.setDither(true);
			mPaint2.setStrokeWidth(5F);
			mPaint2.setStyle(android.graphics.Paint.Style.FILL);
			mPaint2.setColor(Color.GREEN);

			mPaint3 = new Paint();
			mPaint3.setAntiAlias(true);
			mPaint3.setDither(true);
			mPaint3.setStyle(android.graphics.Paint.Style.FILL_AND_STROKE);
			mPaint3.setColor(Color.WHITE);
			initCircels();

		}

		protected void onDraw(Canvas canvas) {
			canvas.drawBitmap(mBitmap, 0, 0, null);
		}

		protected void onSizeChanged(int i, int j, int k, int l) {
			super.onSizeChanged(i, j, k, l);
		}

		private void judgePass(PointF p1, PointF p2) {

			for (int i = 0; i < passFlags.length; i++) {
				if (!passFlags[i] && toastTimes > 0) {
					toastTimes--;
					Toast.makeText(this.getContext(), R.string.fail, 0).show();
					return;
				}
			}
			Toast.makeText(this.getContext(), R.string.success, 0).show();
			SystemClock.sleep(1000L);
			doAfterTest();

		}

		private void initCircels() {
			int n = 0;
			float x;
			float y;
			float xs;
			float y1;
			float xe;
			float y2;
			for (int i = 0; i < 2; i++) {
				for (int j = 0; j < 2; j++) {
					x = mRadius + i * (screen_X - 2 * mRadius);

					y = screen_Y / 3 + 3 * mRadius + 4 * j * mRadius;
					circles[n] = new Circle(new PointF(x, y), mRadius);
					mCanvas.drawCircle(x, y, mRadius, mPaint3);
					n++;
				}
			}
			mCanvas.drawLine(0, screen_Y / 3 + 2 * mRadius, screen_X, screen_Y
					/ 3 + 2 * mRadius, mPaint3);
			mCanvas.drawLine(0, screen_Y / 3 + 2 * mRadius + 2 * mRadius,
					screen_X, screen_Y / 3 + 2 * mRadius + 2 * mRadius, mPaint3);
			mCanvas.drawLine(0, screen_Y / 3 + 2 * mRadius + 2 * mRadius + 2
					* mRadius, screen_X, screen_Y / 3 + 2 * mRadius + 2
					* mRadius +2 * mRadius, mPaint3);
			mCanvas.drawLine(0, screen_Y / 3 + 2 * mRadius + 2 * mRadius + 2
					* mRadius + 2 * mRadius, screen_X, screen_Y / 3 + 2
					* mRadius + 2 * mRadius + 2 * mRadius + 2 * mRadius,
					mPaint3);

		}

		boolean mulFlag;
		private float x1;
		private float x2;
		private float y1;
		private float y2;
		private PointF startP;
		private PointF endP;
		Circle temCircle;

		public boolean onTouchEvent(MotionEvent event) {

			int count = event.getPointerCount();
			mulFlag = false;
			if (count == 2) {
				mulFlag = true;

			}
			if (mulFlag) {
				switch (event.getAction() & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					Log.v(TAG, "ACTION_DOWN");
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					passFlags = new boolean[4];
					x2 = event.getX(count - 1);
					y2 = event.getY(count - 1);
					startP = new PointF(x2, y2);

					break;
				case MotionEvent.ACTION_MOVE:
					mCanvas.drawLine(x1, y1, event.getX(), event.getY(),
							mPaint1);
					mCanvas.drawLine(x2, y2, event.getX(count - 1),
							event.getY(count - 1), mPaint2);

					break;
				case MotionEvent.ACTION_POINTER_UP:
					Log.v(TAG, "ACTION_POINTER_UP");
					x1 = event.getX();
					y1 = event.getY();
					endP = new PointF(x1, y1);
					toastTimes = 5;
					judgePass(startP, endP);
					initCircels();
					return true;
				case MotionEvent.ACTION_UP:
					Log.v(TAG, "ACTION_UP");
					break;
				}
				x1 = event.getX();
				y1 = event.getY();
				temCircle = pointInCirs(new PointF(x1, y1));
				drawCircle(temCircle);
				x2 = event.getX(count - 1);
				y2 = event.getY(count - 1);
				temCircle = pointInCirs(new PointF(x2, y2));
				drawCircle(temCircle);
				invalidate();
			}
			return true;
		}

		private void drawCircle(Circle tem) {
			if (tem != null) {
				mCanvas.drawCircle(tem.x, tem.y, mRadius, mPaint2);
				tem = null;

			} else
				initCircels();

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

	private void getScreenMetries() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screen_X = displaymetrics.widthPixels;
		screen_Y = displaymetrics.heightPixels;
		Log.v("CIT", "widthPixels=" + displaymetrics.widthPixels
				+ "heightPixels" + displaymetrics.heightPixels);
	}

	public class Circle {
		private float x;
		private float y;
		private float radius;

		Circle(PointF point, float r) {
			this.x = point.x;
			this.y = point.y;
			this.radius = r;
		}

		// private boolean pointInCir(PointF point) {
		// if (point.x > this.x - this.radius
		// && point.x < this.x + this.radius
		// && point.y > this.y - this.radius
		// && point.y < this.y + this.radius) {
		// return true;
		// }
		// return false;
		// }
	}

	private Circle pointInCirs(PointF point) {
		for (int i = 0; i < circles.length; i++) {
			if (point.x > circles[i].x - circles[i].radius
					&& point.x < circles[i].x + circles[i].radius
					&& point.y > circles[i].y - circles[i].radius
					&& point.y < circles[i].y + circles[i].radius) {
				passFlags[i] = true;
				Log.v(TAG, "pointInCirspassFlags[" + i + "]=" + true);
				return circles[i];
			}
		}

		return null;
	}

	// private boolean judgePoint(PointF p , Circle[] cs) {
	// for (int i = 0 ;i <cs.length ; i ++) {
	//
	// }
	// return false;
	// }

	private void setFullscreen() {
		requestWindowFeature(1);
		getWindow().setFlags(1024, 1024);
	}

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setFullscreen();
		getScreenMetries();
		touchview = new TouchView(this);
		setContentView(touchview);

	}

}
