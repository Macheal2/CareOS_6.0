package com.magcomm.keyguard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.ImageView;

public class MagcommUnreadView extends ImageView {

	private int mNumber;

	public MagcommUnreadView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MagcommUnreadView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public MagcommUnreadView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public void setNumber(int number) {
		mNumber = number;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		Paint paint = new Paint();
		paint.setTextSize(22);
		paint.setColor(Color.WHITE);
		paint.setTypeface(Typeface.DEFAULT_BOLD);
		
		int x = this.getWidth() / 2 - 6;
		int y = this.getHeight() / 2 + 8;
        if(mNumber > 9){
    		canvas.drawText(9 + "+", x - 7, y, paint);
        }else{
		    canvas.drawText(mNumber + "", x - 1, y, paint);
        }
        invalidate();
	}
}
