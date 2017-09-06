package com.cappu.halllockscreen;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.cappu.halllockscreen.util.HallTimeLog;

public class RollingRing
  extends View
{
  private static final float ANGLE_START = 0.0F;
  private static final float ANGLE_STOP = 360.0F;
  private static final String TAG_PREFIX = "rolling_ring";
  private int inc = 5;
  float mAngleLeft = 0.0F;
  float mAngleRight = 0.0F;
  boolean mChanged = true;
  Drawable mLeftRing;
  Paint mPaint = new Paint();
  private boolean mPause = false;
  private boolean mRequestRepeat = false;
  Drawable mRightRing;
  int mRingHeight;
  int mRingWidth;
  
  public RollingRing(Context paramContext)
  {
    this(paramContext, null);
  }
  
  public RollingRing(Context paramContext, AttributeSet paramAttributeSet)
  {
    super(paramContext, paramAttributeSet);
    HallTimeLog.d("rolling_ring", " start!!");
    this.mPaint.setColor(-65536);
    this.mPaint.setAntiAlias(true);
    this.mPaint.setStrokeWidth(1.0F);
    this.mPaint.setStyle(Paint.Style.STROKE);
    this.mLeftRing = getResources().getDrawable(R.drawable.hall_window_half_ring_left);
    this.mRightRing = getResources().getDrawable(R.drawable.hall_window_half_ring_right);
    this.mRingHeight = this.mLeftRing.getIntrinsicHeight();
    this.mRingWidth = this.mLeftRing.getIntrinsicWidth();
  }
  
  private void drawRing(Canvas paramCanvas, Drawable paramDrawable, int paramInt1, int paramInt2, float paramFloat, boolean paramBoolean1, boolean paramBoolean2)
  {
    paramCanvas.save();
    int i = getWidth();
    int j = getHeight();
    if (paramBoolean1) {
      paramCanvas.clipRect(paramInt1, paramInt2 - j / 2, paramInt1 + i / 2, paramInt2 + j / 2);
    }
      paramCanvas.rotate(paramFloat, paramInt1, paramInt2);
      if (paramBoolean2) {
        paramDrawable.setBounds(paramInt1 - i / 2, paramInt2 - j / 2, paramInt1 + i / 2, paramInt2 + j / 2);
      }
      paramDrawable.draw(paramCanvas);
      paramCanvas.restore();
      paramCanvas.clipRect(paramInt1 - i / 2, paramInt2 - j / 2, paramInt1, paramInt2 + j / 2);
  }
  
  protected void onDraw(Canvas paramCanvas)
  {
    super.onDraw(paramCanvas);
    boolean bool = this.mChanged;
    if (bool) {
      this.mChanged = false;
    }
    int i = getWidth();
    int j = getHeight();
    int k = i / 2;
    int m = j / 2;
    drawRing(paramCanvas, this.mLeftRing, k, m, this.mAngleLeft, true, bool);
    drawRing(paramCanvas, this.mRightRing, k, m, this.mAngleRight, false, bool);
    if (this.mAngleLeft < 360.0F) {
      if (!this.mPause)
      {
        this.mAngleLeft += this.inc;
        this.mAngleRight -= this.inc;
        invalidate();
      }
    }
    while (!this.mRequestRepeat) {
      return;
    }
    startRolling();
  }
  
  public void onResume()
  {
    startRolling();
  }
  
  protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
    this.mChanged = true;
  }
  
  public void repeatRolling()
  {
    this.mRequestRepeat = true;
    startRolling();
  }
  
  public void rollOnce()
  {
    this.mRequestRepeat = false;
    startRolling();
  }
  
  public void startRolling()
  {
    this.mPause = false;
    this.mAngleLeft = 0.0F;
    this.mAngleRight = 0.0F;
    postInvalidateDelayed(100L);
  }
  
  public void stopRolling()
  {
    this.mPause = false;
    this.mRequestRepeat = false;
    this.mAngleLeft = 360.0F;
    this.mAngleRight = 360.0F;
    invalidate();
  }
  
  public void test()
  {
    if (!this.mPause) {}
    for (boolean bool = true;; bool = false)
    {
      this.mPause = bool;
      setVisibility(0);
      if (this.mAngleLeft >= 360.0F)
      {
        this.mAngleLeft = 0.0F;
        this.mAngleRight = 0.0F;
      }
      invalidate();
      return;
    }
  }
}

