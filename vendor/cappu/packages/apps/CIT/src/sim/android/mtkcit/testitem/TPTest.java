package sim.android.mtkcit.testitem;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.*;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.Toast;

import sim.android.mtkcit.R;
//import sim.android.mtkcit.TestActivity;


public class TPTest extends Activity {
	final Handler handler = new Handler();
	private static final int LCD_MENU_QUIT = 1;
	private static Paint mPaint;
	private static int screen_X;
	private static int screen_Y;
	class TouchView extends View {
		boolean[] begin_end = null;
		public int flag;
		public float percent = 0.98f;
		public int times;
		private float[] lines;
		private ArrayList<PointF> graphics;
		private int of = 0;
         final String TAG = "TPTest";
		int m_width = screen_X / 2;
		int m_height = screen_Y / 2;
		int m_middle = screen_X /10;
        float screen = ((float)screen_X)/ screen_Y;
		private void touch_move(float f, float f1) {
			graphics.add(new PointF(f, f1));
			float f2 = mX;
			float f3 = Math.abs(f - f2);
			float f4 = mY;
			float f5 = Math.abs(f1 - f4);
			if (f3 >= 4F || f5 >= 4F) {
				Path path = mPath;
				float f6 = mX;
				float f7 = mY;
				float f8 = (mX + f) / 2F;
				float f9 = (mY + f1) / 2F;
				path.quadTo(f6, f7, f8, f9);
				mX = f;
				mY = f1;
			}
		}

		private void touch_start(float f, float f1) {
			graphics.add(new PointF(f, f1));

			Log.v(TAG, "touch_start_times=" + times);
			Log.v(TAG, "touch_start_f=" + f + "touch_start_f111=" + f1);
			if(times<0) {
				times=0;
			}
			begin_end[times] = judgePoint(f, f1);

			times++;
			//
			mPath.reset();
			mPath.moveTo(f, f1);
			mX = f;
			mY = f1;
		}

		private void touch_up() {
			Path path = mPath;
			float f = mX;
			float f1 = mY;
			Log.v(TAG, "touch_up_MX=" + f + "touch_up_MY=" + f1);
			Log.v(TAG, "touch_up_times=" + times);
			if (times < 0) {
				times = 0;
			}
			begin_end[times] = judgePoint(f, f1);
			times++;
			if (times > 2) {
				judgePass(begin_end, times);
			}
			path.lineTo(f, f1);
			Canvas canvas = mCanvas;
			Path path1 = mPath;
			Paint paint = TPTest.mPaint;
			canvas.drawPath(path1, paint);
			mPath.reset();
		}

		protected void onDraw(Canvas canvas) {
			canvas.drawColor(-1);

			Paint pt = new Paint();
			pt.setARGB(255, 0, 0, 0);
			canvas.drawText(getResources().getString(R.string.touchname), 80,
					20, pt);
			// fengxuanyang
			lines = new float[] { m_middle, 0f, m_width, m_height - m_middle,
					screen_X - m_middle, 0f, m_width, m_height - m_middle,

					m_middle, 0f, 0f, m_middle,

					0f, m_middle, m_width - m_middle, m_height, 0f,
					screen_Y - m_middle, m_width - m_middle, m_height,

					0f, screen_Y - m_middle, m_middle, screen_Y,

					m_middle, screen_Y, m_width, m_height + m_middle,
					screen_X - m_middle, screen_Y, m_width,
					m_height + m_middle,

					screen_X - m_middle, screen_Y, screen_X,
					screen_Y - m_middle,

					screen_X, screen_Y - m_middle, m_width + m_middle,
					m_height, screen_X, m_middle, m_width + m_middle, m_height,

					screen_X, m_middle, screen_X - m_middle, 0f

			};
			canvas.drawLines(lines, pt);
			// Path ph1 = new Path();
			// ph1.moveTo(0f, 10f);
			// ph1.lineTo(150f, 150f);
			// ph1.lineTo(0f, 300f);
			// ph1.close();
			// canvas.drawPath(ph1, pt);
			// end
			Point p = new Point();
			Bitmap bitmap = mBitmap;
			Paint paint = mBitmapPaint;
			canvas.drawBitmap(bitmap, 0F, 0F, paint);
			Path path = mPath;
			Paint paint1 = TPTest.mPaint;
			canvas.drawPath(path, paint1);
		}

		protected void onSizeChanged(int i, int j, int k, int l) {
			super.onSizeChanged(i, j, k, l);
		}

		// fengxuanyang
		private boolean judgePoint(float f1, float f2) {
			// int m_width = screen_X / 2;
			// int m_height = screen_Y / 2;
			// int m_middle = 20;
			Log.v(TAG,"screen_x" + screen_X + "screen_Y="
					+ screen_Y);
			if (((0f <= f1 & f1 <= m_middle) | (screen_X- m_middle<= f1 & f1 <= screen_X))
					& ((0f <= f2 & f2 <= m_middle) | (screen_Y-m_middle <= f2 & f2 <= screen_Y))) {

				return true;
			}
			// Toast.makeText(this,
			// getResources().getString(R.string.touch_waring), 1).show();
			else {
				times -= 2;

			}
			Toast.makeText(this.getContext(), R.string.touch_waring, 0).show();
			refreshDrawableState();
			return false;
		}

		private void judgePass(boolean[] bs, int j) {
			Log.v(TAG, "screen="+screen);
			Log.v(TAG, "bs=BS.LENGTH=" + bs.length);
			times = 0;
			for (int i = 0; i < bs.length; i++) {
				if (!bs[i]) {
					Toast.makeText(this.getContext(), R.string.fail, 0).show();
					return;
				}
			}
			// if(graphics.size()>0){
			// // canvas.drawPoint(graphics.get(of).x, graphics.get(of).y,
			// paint);
			// of+=1;
			// if(of<graphics.size()){
			// if(of==graphics.size()-1){
			// mPath.reset();//移动完成后移除线条
			// }
			// invalidate();
			// }
			// }
			int mall = 0;
			for (int i = 0; i < graphics.size(); i++) {
				float mx = graphics.get(i).x;
				float my = graphics.get(i).y;

//				if (((-m_middle*3 <= (3 * mx - 2 * my)) & ((3 * mx - 2 * my) <= m_middle*3))
//						| ((3*(screen_X-m_middle) <= (3 * mx + 2 * my)) & ((3 * mx + 2 * my) <= 3*(screen_X+m_middle)))) {
//					mall++;
//				}
				if (((-m_middle <= (mx - screen*my)) & ((mx - screen*my) <= m_middle))
						| (((screen_X-m_middle) <= (mx + screen*my)) & ((mx + screen*my) <= (screen_X+m_middle)))) {
					mall++;
				}
				
//				if (((-m_middle*3 <= (3 * my - 2 * mx)) & ((3 * my - 2 * mx) <= m_middle*3))
//						| ((3*(screen_X-m_middle) <= (3 * my + 2 * mx)) & ((3 * my + 2 * mx) <= 3*(screen_X+m_middle)))) {
//					mall++;
//				}

			}
			Log.v(TAG, "graphics.size()==" + graphics.size());
			Log.v(TAG, "mall=" + mall + "pervent"
					+ ((float) mall / ((float) graphics.size())) * 100 + "%");
			if (((float) mall / ((float) graphics.size())) * 100 > 80) {
				AlertDialog.Builder builder = new AlertDialog.Builder(
						getContext()).setTitle(R.string.test_item_TP);
				builder.setMessage(R.string.tp_success);
				builder.setPositiveButton(R.string.alert_dialog_ok,
						new TPDilogListener()).create().show();
			} else {
				new Thread(new Runnable() {
					@Override
					public void run() {

						touchview.postInvalidate();
					}
				}).start();

				Toast.makeText(this.getContext(), R.string.tp_fail, 0).show();
				
			}

		}
		private class TPDilogListener implements
		android.content.DialogInterface.OnClickListener {

	public void onClick(DialogInterface dialoginterface, int i) {
		Bundle b = new Bundle();
		Intent intent = new Intent();
		b.putInt("test_result", 1);
		intent.putExtras(b);
		setResult(RESULT_OK, intent);
		finish();
	}
}
		public boolean onTouchEvent(MotionEvent motionevent) {
			float f;
			float f1;
			f = motionevent.getX();
			f1 = motionevent.getY();
			int action = motionevent.getAction();
			switch (action) {
			// return true;
			case MotionEvent.ACTION_DOWN:
				if (times == 0) {
					graphics = new ArrayList<PointF>();
				}

				touch_start(f, f1);
				invalidate();
				break;
			case MotionEvent.ACTION_MOVE:
				touch_move(f, f1);
				invalidate();
				break;
			case MotionEvent.ACTION_UP:
				touch_up();
				invalidate();
				break;

			}
			return true;
		}

		private static final float TOUCH_TOLERANCE = 4F;
		private Bitmap mBitmap;
		private Paint mBitmapPaint;
		private Canvas mCanvas;
		private Path mPath;
		private float mX;
		private float mY;

		public TouchView(Context context) {

			super(context);
			begin_end = new boolean[4];
			int i = TPTest.screen_X;
			int j = TPTest.screen_Y;
			android.graphics.Bitmap.Config config = android.graphics.Bitmap.Config.ARGB_8888;
			Bitmap bitmap = Bitmap.createBitmap(i, j, config);
			mBitmap = bitmap;
			Bitmap bitmap1 = mBitmap;
			Canvas canvas = new Canvas(bitmap1);
			mCanvas = canvas;
			Path path = new Path();
			mPath = path;
			Paint paint = new Paint(4);
			mBitmapPaint = paint;
		}

	}
	private void getScreenMetries() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screen_X = displaymetrics.widthPixels;
		screen_Y = displaymetrics.heightPixels;
		Log.v("TPTest", "widthPixels=" + displaymetrics.widthPixels
				+ "heightPixels" + displaymetrics.heightPixels);
	}

	private void setFullscreen() {
		requestWindowFeature(1);
		getWindow().setFlags(1024, 1024);
	}

	private TouchView touchview;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setFullscreen();
		getScreenMetries();
		touchview = new TouchView(this);
		setContentView(touchview);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
		mPaint.setColor(0xffff0000);
		Paint paint = mPaint;
		android.graphics.Paint.Style style = android.graphics.Paint.Style.STROKE;
		paint.setStyle(style);
		Paint paint1 = mPaint;
		android.graphics.Paint.Join join = android.graphics.Paint.Join.ROUND;
		paint1.setStrokeJoin(join);
		Paint paint2 = mPaint;
		android.graphics.Paint.Cap cap = android.graphics.Paint.Cap.ROUND;
		paint2.setStrokeCap(cap);
		mPaint.setStrokeWidth(1F);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, 1, 0, R.string.lcdmenuquit);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem menuitem) {

		switch (menuitem.getItemId()) {
		case 1:
			finish();
		}

		return true;

	}



}
