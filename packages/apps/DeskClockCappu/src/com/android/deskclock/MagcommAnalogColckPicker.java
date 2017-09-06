package com.android.deskclock;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;

import java.util.Calendar;

import com.android.deskclock.R;

public class MagcommAnalogColckPicker extends View{
	
	private Calendar mCalendar;
	private Point mCenter;
	private int mCircle;
	private Drawable mClockDrawable;
	private int mClockTextColor;
	private int mDialHeight;
	private int mDialWidth;
	private int mDistanceHour;
	private int mDistanceMinute;
	private int mHour = 0;
	private double mHourDegree = 0.0D;
	private int mHourDialHeight;
	private int mHourDialWidth;
	private Drawable mHourDrawable;
	private int mHourMarginbottomOffset = 0;
	private Drawable mHourShadowDrawable;
	private int mHourTextColor;
	private int mHourTextSize = 0;
	private int mHourTextSizeLarge = 0;
	private int mMinuThumbHeight;
	private int mMinuThumbWidth;
	private int mMinute = 0;
	private double mMinuteDegree = 0.0D;
	private Drawable mMinuteDrawable;
	private int mMinuteTextColor;
	private int mMinuteTextSize = 0;
	private int mMinuteTextSizeLarge = 0;
	private double mOldHourDegree;
	private double mOldMinuteDegree;
	private Paint mPaint;
	private int mPickerShadowOffset = 0;
	private Paint mTextPaint;
	private Paint mTextPaintSmall;
	private float m_hour_length;
	private float m_minute_length;
	private boolean movehour = false;
	private boolean moveminute = false;
	private Typeface mRobotoNormal;
  
	public MagcommAnalogColckPicker(Context context){
		this(context, null);
	}

	public MagcommAnalogColckPicker(Context context, AttributeSet attrs){
		
		super(context, attrs);
		Resources res = context.getResources();
		mClockDrawable = res.getDrawable(R.drawable.magcomm_tyd_clock_bg);
		mHourDrawable = res.getDrawable(R.drawable.magcomm_tyd_hour_thumb_picker);
		mMinuteDrawable = res.getDrawable(R.drawable.magcomm_tyd_minute_thumb_picker);
		mMinuteTextSize = res.getDimensionPixelSize(R.dimen.magcomm_clock_min_text_size);
		mHourTextSizeLarge = res.getDimensionPixelSize(R.dimen.magcomm_clock_hour_text_size);
		mMinuteTextSizeLarge = res.getDimensionPixelSize(R.dimen.magcomm_clock_min_text_size_large);
		mDistanceHour = res.getDimensionPixelSize(R.dimen.magcomm_clock_distance_hour);
		mDistanceMinute = res.getDimensionPixelSize(R.dimen.magcomm_clock_distance_min);
		mHourMarginbottomOffset = res.getDimensionPixelSize(R.dimen.magcomm_clock_hour_bottom_offset);
		mPickerShadowOffset = res.getDimensionPixelSize(R.dimen.magcomm_clock_picker_offset);
		mClockTextColor = res.getColor(R.color.magcomm_alarm_time_text_color);
		mHourTextColor = res.getColor(R.color.magcomm_alarm_time_text_color);
		mMinuteTextColor = res.getColor(R.color.white);
		initDrawable();
		initTimeBySystem();
		if (mHour >= 12)
			mCircle = 1;
		else 
			mCircle = 0;
    
		mRobotoNormal = Typeface.create("sans-serif-thin", Typeface.NORMAL);
		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(mMinuteTextSize);
		mPaint.setColor(mMinuteTextColor);
		mPaint.setTypeface(mRobotoNormal);
		mPaint.setTextAlign(Paint.Align.CENTER);

		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(mHourTextSizeLarge);
		mTextPaint.setColor(mClockTextColor);
		mTextPaint.setTypeface(mRobotoNormal);
		m_hour_length = mTextPaint.measureText("00");

		mTextPaintSmall = new Paint();
		mTextPaintSmall.setAntiAlias(true);
		mTextPaintSmall.setTextSize(mMinuteTextSizeLarge);
		mTextPaintSmall.setColor(mClockTextColor);
		mTextPaintSmall.setTypeface(mRobotoNormal);
		m_minute_length = mTextPaintSmall.measureText("00");
	}

	private double calcDegree(int pointX, int pointY){
		return fixDegree(calcMathDegree(pointX, pointY));
	}

	private void calcDegreeByTime(){
		calcDegreeByTime(mHour, mMinute);
	}

	private void calcDegreeByTime(int hour, int minute){
		mMinuteDegree = (minute * 6);
		mOldMinuteDegree = mMinuteDegree;
		mHourDegree = (30 * (hour % 12) + mMinuteDegree / 12.0D);
		mOldHourDegree = mHourDegree;
	}

  	private double calcMathDegree(int pointX, int pointY){
  		int mX = pointX - getCenter().x;
  		return Math.toDegrees(Math.atan2(getCenter().y - pointY, mX));
  	}

  	private void calcTimeByDegree(){
	  
  		mMinute = ((int)(mMinuteDegree / 6.0D));
  		if(moveminute){
  			if (deasil(mMinuteDegree, mOldMinuteDegree) == 2){
  				if (mMinuteDegree < mOldMinuteDegree){
  					mHour = (1 + mHour);
  					mHour %= 24;
  				}else{
  					mHour = (-1 + mHour);
  					if (mHour == -1)
  						mHour = 23;
  					mHour %= 24;
  				}
  			}
  			mOldMinuteDegree = mMinuteDegree;
  			mHourDegree = (30 * (mHour % 12) + mMinuteDegree / 12.0D);
  		}else if (movehour){ 
  			mHour = ((int)(mHourDegree / 30.0D));
  			if (deasil(mHourDegree, mOldHourDegree) == 2){
  				if (mCircle != 0)
  					mCircle = 0;
  				else
  					mCircle = 1; 
  			}

  			if (mCircle == 1)
  				mHour = 12 + mHour;
  			mOldHourDegree = mHourDegree;  
  			mHourDegree = (30 * (mHour % 12) + mMinuteDegree / 12.0D);
  		}
    
  		if (mHour >= 12)
  			mCircle = 1;
  		else
  			mCircle = 0;
  	}

  	private void darawTimeText(Canvas canvas){
	  
  		String hour = String.valueOf(mHour);  
  		if (mHour < 10)
  			hour = "0" + hour;
	  
  		String minute = String.valueOf(mMinute);
  		if (mMinute < 10)
  			minute = "0" + minute;
  		canvas.save();
  		canvas.translate(getCenter().x, getCenter().y);
  		Paint.FontMetrics mFontMetrics = mTextPaint.getFontMetrics();
  		float pointY = -(mFontMetrics.bottom + mFontMetrics.top) / 2.0F - mHourMarginbottomOffset;
  		float pointX = -(m_hour_length + m_minute_length) / 2.0F;
  		canvas.drawText(hour, pointX, pointY, mTextPaint);
  		canvas.drawText(minute, pointX + m_hour_length, pointY, mTextPaintSmall);
  		canvas.restore();
  	}

  	private int deasil(double Degree, double oldDegree){
  		int i = 1;
  		if (Degree == oldDegree)
  			i = 0;
  		if ((Degree - oldDegree > 180.0D) || (oldDegree - Degree > 180.0D))
  			i = 2;
  		return i;
  	}

  	private void drawClock(Canvas canvas){
  		if (mClockDrawable == null)
  			return;
   
  		canvas.save();
  		canvas.translate(getCenter().x, getCenter().y);
  		mClockDrawable.setBounds(-mDialWidth / 2, -mDialHeight / 2, mDialWidth / 2, mDialHeight / 2);
  		mClockDrawable.draw(canvas);
  		canvas.restore();
  	}

  	private void drawHour(Canvas canvas){
  		if (mHourDrawable == null)
  			return;
      
  		canvas.save();
  		canvas.translate(getCenter().x, getCenter().y);
  		mHourDrawable.setBounds(-mHourDialWidth / 2, -mHourDialHeight / 2, mHourDialWidth / 2, mHourDialHeight / 2);
  		canvas.rotate((float)mHourDegree);
  		mHourDrawable.draw(canvas);
  		canvas.restore();
  	}

  	private void drawMinute(Canvas canvas){
  		if (mMinuteDrawable == null)
  			return;
    
  		double d = Math.toRadians(fixDegree(mMinuteDegree));
  		canvas.save();
  		canvas.translate((float)(getCenter().x + mDialWidth*0.45 * Math.cos(d)), (float)(getCenter().y - mDialWidth*0.45 * Math.sin(d)));
  		mMinuteDrawable.setBounds(-mMinuThumbWidth / 2, -mMinuThumbHeight / 2, mMinuThumbWidth / 2, mMinuThumbHeight / 2);
  		canvas.rotate((float)mMinuteDegree);
  		mMinuteDrawable.draw(canvas);
  		String minute = String.valueOf(mMinute);
  		Paint.FontMetrics mFontMetrics = mPaint.getFontMetrics();
  		canvas.drawText(minute, 0.0F, (mFontMetrics.bottom - mFontMetrics.top) / 4.0F, mPaint);
  		canvas.restore();
  	}

  	private double fixDegree(double degree){
  		return (450.0D - (degree + 360.0D) % 360.0D) % 360.0D;
  	}

 	private Point getCenter(){
 		if (mCenter == null)
 			mCenter = new Point(getWidth() / 2, getHeight() / 2);

 		mCenter.x = (getWidth() / 2);
 		mCenter.y = (getHeight() / 2);
    
 		return mCenter;
	}

 	private int getDistanceFromCenter(MotionEvent event){
 		return (int)Math.sqrt((getCenter().y - event.getY()) * (getCenter().y - event.getY()) + (getCenter().x - event.getX()) * (getCenter().x - event.getX()));
 	}

 	private void initDrawable(){
 		mDialWidth = mClockDrawable.getIntrinsicWidth();
 		mDialHeight = mClockDrawable.getIntrinsicHeight();
 		mHourDialWidth = mHourDrawable.getIntrinsicWidth();
 		mHourDialHeight = mHourDrawable.getIntrinsicHeight();
 		mMinuThumbWidth = mMinuteDrawable.getIntrinsicWidth();
 		mMinuThumbHeight = mMinuteDrawable.getIntrinsicHeight();
 	}

 	private void initTimeBySystem(){
 		long now = System.currentTimeMillis();
 		mCalendar = Calendar.getInstance();
 		mCalendar.setTimeInMillis(now);
 		mHour = mCalendar.get(Calendar.HOUR_OF_DAY);
 		mMinute = mCalendar.get(Calendar.MINUTE);
 		calcDegreeByTime();
 	}

 	private int whichMove(MotionEvent event){
 		int i = getDistanceFromCenter(event);
 		int mWidth = getWidth();
 		int mHeight = getHeight();
 		float f = 1.0F;
 		if ((mWidth < mDialWidth) || (mHeight < mDialHeight))
 			f = Math.min(mWidth / mDialWidth, mHeight / mDialHeight);
 		
 		if (i < f * mDialWidth*0.45)
 			return 0;
 		else 
 			return 1;
  }

  	public int getCurrentHour(){
  		return mHour;
  	}

  	public int getCurrentMinute(){
  		return mMinute;
  	}

  	protected void onDraw(Canvas canvas){
  		super.onDraw(canvas);
  		
  		int mWidth = getWidth();
  		int mHeight = getHeight();
 
    	drawClock(canvas);
    	drawHour(canvas);
    	drawMinute(canvas);
    	darawTimeText(canvas);
    	
    	if (mWidth < mDialWidth)
    		canvas.restore();
  
    	float f = Math.min(mWidth/mDialWidth, mHeight/mDialHeight);
    	canvas.save();
    	canvas.scale(f, f, mWidth/2, mHeight/2);
  	}

  	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
  		int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
  		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
  		int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
  		int heightMode = MeasureSpec.getMode(heightMeasureSpec);

  		float f1 = 1.2F;
  		float f2 = 1.2F;
  		if ((widthMode != 0) && (measuredWidth < mDialWidth))
  			f1 = measuredWidth / mDialWidth;
  		if ((heightMode != 0) && (measuredHeight < mDialHeight))
  			f2 = measuredHeight / mDialHeight;
  		float f3 = Math.min(f1, f2);
  		setMeasuredDimension(resolveSizeAndState((int)(f3 * mDialWidth), widthMeasureSpec, 0), resolveSizeAndState((int)(f3 * mDialHeight), heightMeasureSpec, 0));
  	}

  	public boolean onTouchEvent(MotionEvent event){
  		
  		switch (event.getAction()){
  			case MotionEvent.ACTION_DOWN:
    			int i = whichMove(event);
    			if (i == 0){
    				movehour = true;
          			moveminute = false;
          			mHourDegree = calcDegree((int)event.getX(), (int)event.getY());
        		}else if (i == 1){
    				movehour = false;
    				moveminute = true;
    				mMinuteDegree = calcDegree((int)event.getX(), (int)event.getY());
        		}
    			calcTimeByDegree();
        		postInvalidate();
        		return true;
        
    		case MotionEvent.ACTION_MOVE:
    			if (movehour)
            		mHourDegree = calcDegree((int)event.getX(), (int)event.getY());
          		if (moveminute)
          			mMinuteDegree = calcDegree((int)event.getX(), (int)event.getY());       
          		calcTimeByDegree();
          		postInvalidate();      
          		return true;
          		
    		case MotionEvent.ACTION_UP:
    			moveminute = false;
        		movehour = false;
        		postInvalidate();
        		return true;
    		default:
    			break;
    	}
    	return false;

  	}

  	public void setCurrentTime(int hour, int minute){
  		mHour = hour;
  		mMinute = minute;
  		if (mHour >= 12)
  			mCircle = 1;
  		else
  			mCircle = 0;
  		calcDegreeByTime(mHour, mMinute);
  		postInvalidate();
  	}
}