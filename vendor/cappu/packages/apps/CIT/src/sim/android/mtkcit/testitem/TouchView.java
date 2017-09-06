package sim.android.mtkcit.testitem;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import sim.android.mtkcit.R;

public class TouchView extends View {

	private Canvas mCanvas;
	private Bitmap mBitmap;
	private Paint mBitmapPaint;
	private float click_x = 0;
	private float click_y = 0;
	private static int Rectx = 0;
	private static int Recty = 0;
	static int screen_X;
	static int screen_Y;

	public TouchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mBitmap = Bitmap.createBitmap(screen_X, screen_Y,
				Bitmap.Config.ARGB_8888);
		mCanvas = new Canvas(mBitmap);
		mBitmapPaint = new Paint(Paint.DITHER_FLAG);
		Rectx = screen_X / 3;
		Recty = screen_Y / 4;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawColor(Color.WHITE);
		canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
		DrawSquare(canvas);
	}

	public void ShowArea(Canvas canvas) {
		Paint pt = new Paint();
		pt.setColor(Color.RED);
		if (click_x < Rectx && click_x > 0) {
			if (click_y < Recty && click_y > 0) {
				canvas.drawRect(0, 0, Rectx, Recty, pt);
			} else if (click_y < Recty * 2 && click_y > Recty) {
				canvas.drawRect(0, Recty, Rectx, Recty * 2, pt);
			} else if (click_y < Recty * 3 && click_y > Recty * 2) {
				canvas.drawRect(0, Recty*2, Rectx, Recty * 3, pt);
			} else if (click_y < Recty * 4 && click_y > Recty * 3) {
				canvas.drawRect(0, Recty*3, Rectx, Recty * 4, pt);
			} else if (click_y < Recty * 5 && click_y > Recty * 4) {
				canvas.drawRect(0, Recty*4, Rectx, Recty * 5, pt);
			}
		} else if (click_x < Rectx * 2 && click_x > Rectx) {
			if (click_y < Recty && click_y > 0) {
				canvas.drawRect(Rectx, 0, Rectx*2, Recty, pt);
			} else if (click_y < Recty * 2 && click_y > Recty) {
				canvas.drawRect(Rectx, Recty, Rectx*2, Recty * 2, pt);
			} else if (click_y < Recty * 3 && click_y > Recty * 2) {
				canvas.drawRect(Rectx, Recty*2, Rectx*2, Recty * 3, pt);
			} else if (click_y < Recty * 4 && click_y > Recty * 3) {
				canvas.drawRect(Rectx, Recty*3, Rectx*2, Recty * 4, pt);
			} else if (click_y < Recty * 5 && click_y > Recty * 4) {
				canvas.drawRect(Rectx, Recty*4, Rectx*2, Recty * 5, pt);
			}
		} else if (click_x < Rectx * 3 && screen_X > Rectx * 2) {
			if (click_y < Recty && click_y > 0) {
				canvas.drawRect(Rectx*2, 0, Rectx*3, Recty, pt);
			} else if (click_y < Recty * 2 && click_y > Recty) {
				canvas.drawRect(Rectx*2, Recty, Rectx*3, Recty * 2, pt);
			} else if (click_y < Recty * 3 && click_y > Recty * 2) {
				canvas.drawRect(Rectx*2, Recty*2, Rectx*3, Recty * 3, pt);
			} else if (click_y < Recty * 4 && click_y > Recty * 3) {
				canvas.drawRect(Rectx*2, Recty*3, Rectx*3, Recty * 4, pt);
			} else if (click_y < Recty * 5 && click_y > Recty * 4) {
				canvas.drawRect(Rectx*2, Recty*4, Rectx*3, Recty * 5, pt);
			}
		} else if (click_x < Rectx * 4 && screen_X > Rectx * 3) {
			if (click_y < Recty && click_y > 0) {
				canvas.drawRect(Rectx*3, 0, Rectx*4, Recty, pt);
			} else if (click_y < Recty * 2 && click_y > Recty) {
				canvas.drawRect(Rectx*3, Recty, Rectx*4, Recty * 2, pt);
			} else if (click_y < Recty * 3 && click_y > Recty * 2) {
				canvas.drawRect(Rectx*3, Recty*2, Rectx*4, Recty * 3, pt);
			} else if (click_y < Recty * 4 && click_y > Recty * 3) {
				canvas.drawRect(Rectx*3, Recty*3, Rectx*4, Recty * 4, pt);
			} else if (click_y < Recty * 5 && click_y > Recty * 4) {
				canvas.drawRect(Rectx*3, Recty*4, Rectx*4, Recty * 5, pt);
			}
		} else if (click_x < Rectx * 5 && screen_X > Rectx * 4) {
			if (click_y < Recty && click_y > 0) {
				canvas.drawRect(Rectx*4, 0, Rectx*5, Recty, pt);
			} else if (click_y < Recty * 2 && click_y > Recty) {
				canvas.drawRect(Rectx*4, Recty, Rectx*5, Recty * 2, pt);
			} else if (click_y < Recty * 3 && click_y > Recty * 2) {
				canvas.drawRect(Rectx*4, Recty*2, Rectx*5, Recty * 3, pt);
			} else if (click_y < Recty * 4 && click_y > Recty * 3) {
				canvas.drawRect(Rectx*4, Recty*3, Rectx*5, Recty * 4, pt);
			} else if (click_y < Recty * 5 && click_y > Recty * 4) {
				canvas.drawRect(Rectx*4, Recty*4, Rectx*5, Recty * 5, pt);
			}
		}

		/*
		 * if (click_x < Rectx && click_x > 0) { if (click_y < Recty && click_y
		 * > 0) { canvas.drawRect(0, 0, Rectx, Recty, pt); } else if (click_y <
		 * Recty * 2 && click_y > Recty) { canvas.drawRect(0, Recty, Rectx,
		 * Recty * 2, pt); } else if (click_y < Recty * 3 && click_y > Recty *
		 * 2) { canvas.drawRect(0, Recty * 2, Rectx, screen_Y, pt); } } else if
		 * (click_x < Rectx * 2 && click_x > Rectx) { if (click_y < Recty &&
		 * click_y > 0) { canvas.drawRect(Rectx, 0, Rectx * 2, Recty, pt); }
		 * else if (click_y < Recty * 2 && click_y > Recty) {
		 * canvas.drawRect(Rectx, Recty, Rectx * 2, Recty * 2, pt); } else if
		 * (click_y < Recty * 3 && click_y > Recty * 2) { canvas.drawRect(Rectx,
		 * Recty * 2, Rectx * 2, screen_Y, pt); } } else if (click_x < Rectx * 3
		 * && screen_X > Rectx * 2) { if (click_y < Recty && click_y > 0) {
		 * canvas.drawRect(Rectx * 2, 0, Rectx * 3, Recty, pt); } else if
		 * (click_y < Recty * 2 && click_y > Recty) { canvas.drawRect(Rectx * 2,
		 * Recty, Rectx * 3, Recty * 2, pt); } else if (click_y < Recty * 3 &&
		 * click_y > Recty * 2) { canvas.drawRect(Rectx * 2, Recty * 2, Rectx *
		 * 3, screen_Y, pt); } }
		 */
	}

	void DrawSquare(Canvas c) {
		Paint mPaint = new Paint();
		mPaint.setColor(Color.BLACK);
		for (int h = 0; h <= screen_Y; h = h + Recty) {
			c.drawLine(0, h, screen_X, h, mPaint);
		}
		for (int w = 0; w <= screen_X; w = w + Rectx) {
			c.drawLine(w, 0, w, screen_Y, mPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		click_x = event.getX();
		click_y = event.getY();
		if (click_x > 0 && click_y > 0)
			ShowArea(mCanvas);
		invalidate();
		return super.onTouchEvent(event);
	}
}
